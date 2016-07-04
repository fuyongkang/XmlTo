package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;

import com.fyk.fastxml.XMLArray;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;
import com.fyk.fastxml.util.TypeUtils;

public class ArrayDeserializer implements ObjectDeserializer {

    public final static ArrayDeserializer instance = new ArrayDeserializer();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T deserialze(DefaultXMLParser parser, Type type, Object fieldName) {
        final XMLLexer lexer = parser.getLexer();
        if (lexer.token() == XMLToken.NULL) {
            lexer.nextToken(XMLToken.COMMA);
            return null;
        }
        
        if (type == AtomicIntegerArray.class) {
            XMLArray array = new XMLArray();
            String xml;
//            parser.parseArray(array);

            AtomicIntegerArray atomicArray = new AtomicIntegerArray(array.size());
            for (int i = 0; i < array.size(); ++i) {
                atomicArray.set(i, array.getInteger(i));
            }

            return (T) atomicArray;
        }
        
        if (type == AtomicLongArray.class) {
            XMLArray array = new XMLArray();
            
//            parser.parseArray(array);

            AtomicLongArray atomicArray = new AtomicLongArray(array.size());
            for (int i = 0; i < array.size(); ++i) {
                atomicArray.set(i, array.getLong(i));
            }

            return (T) atomicArray;
        }

        if (lexer.token() == XMLToken.LITERAL_STRING) {
            byte[] bytes = lexer.bytesValue();
            lexer.nextToken(XMLToken.COMMA);
            return (T) bytes;
        }

        Class componentClass;
        Type componentType;
        if (type instanceof GenericArrayType) {
            GenericArrayType clazz = (GenericArrayType) type;
            componentType = clazz.getGenericComponentType();
            if (componentType instanceof TypeVariable) {
                TypeVariable typeVar = (TypeVariable) componentType;
                Type objType = parser.getContext().getType();
                if (objType instanceof ParameterizedType) {
                    ParameterizedType objParamType = (ParameterizedType) objType;
                    Type objRawType = objParamType.getRawType();
                    Type actualType = null;
                    if (objRawType instanceof Class) {
                        TypeVariable[] objTypeParams = ((Class) objRawType).getTypeParameters();
                        for (int i = 0; i < objTypeParams.length; ++i) {
                            if (objTypeParams[i].getName().equals(typeVar.getName())) {
                                actualType = objParamType.getActualTypeArguments()[i];
                            }
                        }
                    }
                    if (actualType instanceof Class) {
                        componentClass = (Class) actualType;
                    } else {
                        componentClass = Object.class;
                    }
                } else {
                    componentClass = Object.class;
                }
            } else {
                componentClass = (Class) componentType;
            }
        } else {
            Class clazz = (Class) type;
            componentType = componentClass = clazz.getComponentType();
        }
        XMLArray array = new XMLArray();
        parser.parseArray(componentClass, array, fieldName);

        return (T) toObjectArray(parser, componentClass, array);
    }

    @SuppressWarnings("unchecked")
    private <T> T toObjectArray(DefaultXMLParser parser, Class<?> componentType, XMLArray array) {
        if (array == null) {
            return null;
        }

        int size = array.size();

        Object objArray = Array.newInstance(componentType, size);
        for (int i = 0; i < size; ++i) {
            Object value = array.get(i);

            if (value == array) {
                Array.set(objArray, i, objArray);
                continue;
            }

            if (componentType.isArray()) {
                Object element;
                if (componentType.isInstance(value)) {
                    element = value;
                } else {
                    element = toObjectArray(parser, componentType, (XMLArray) value);
                }

                Array.set(objArray, i, element);
            } else {
                Object element = null;
                if (value instanceof XMLArray) {
                    boolean contains = false;
                    XMLArray valueArray = (XMLArray) value;
                    int valueArraySize = valueArray.size();
                    for (int y = 0; y < valueArraySize; ++y) {
                        Object valueItem = valueArray.get(y);
                        if (valueItem == array) {
                            valueArray.set(i, objArray);
                            contains = true;
                        }
                    }
                    if (contains) {
                        element = valueArray.toArray();
                    }
                }

                if (element == null) {
                    element = TypeUtils.cast(value, componentType, parser.getConfig());
                }
                Array.set(objArray, i, element);

            }
        }

        array.setRelatedArray(objArray);
        array.setComponentType(componentType);
        return (T) objArray; // TODO
    }

    public int getFastMatchToken() {
        return XMLToken.LBRACKET;
    }
}
