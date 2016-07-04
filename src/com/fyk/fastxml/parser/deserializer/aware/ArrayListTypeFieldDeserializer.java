package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.fyk.fastxml.XMLException;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.Feature;
import com.fyk.fastxml.parser.ParseContext;
import com.fyk.fastxml.parser.ParserConfig;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;
import com.fyk.fastxml.util.FieldInfo;

public class ArrayListTypeFieldDeserializer extends FieldDeserializer {

    private final Type         itemType;
    private int                itemFastMatchToken;
    private ObjectDeserializer deserializer;

    public ArrayListTypeFieldDeserializer(ParserConfig mapping, Class<?> clazz, FieldInfo fieldInfo){
        super(clazz, fieldInfo);

        Type fieldType = getFieldType();
        if (fieldType instanceof ParameterizedType) {
            this.itemType = ((ParameterizedType) getFieldType()).getActualTypeArguments()[0];
        } else {
            this.itemType = Object.class;
        }
    }

    public int getFastMatchToken() {
        return XMLToken.LBRACE_XML;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void parseField(DefaultXMLParser parser, Object object, Type objectType, Map<String, Object> fieldValues) {
        if (parser.getLexer().token() == XMLToken.NULL) {
            setValue(object, null);
            return;
        }

        ArrayList list = new ArrayList();

        ParseContext context = parser.getContext();

        parser.setContext(context, object, fieldInfo.getName());
        parseArray(parser, objectType, list);
        parser.setContext(context);

        if (object == null) {
            fieldValues.put(fieldInfo.getName(), list);
        } else {
            setValue(object, list);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final void parseArray(DefaultXMLParser parser, Type objectType, Collection array) {
        Type itemType = this.itemType;
        ObjectDeserializer itemTypeDeser = this.deserializer;
        
        if (itemType instanceof TypeVariable //
            && objectType instanceof ParameterizedType) {
            TypeVariable typeVar = (TypeVariable) itemType;
            ParameterizedType paramType = (ParameterizedType) objectType;

            Class<?> objectClass = null;
            if (paramType.getRawType() instanceof Class) {
                objectClass = (Class<?>) paramType.getRawType();
            }

            int paramIndex = -1;
            if (objectClass != null) {
                for (int i = 0, size = objectClass.getTypeParameters().length; i < size; ++i) {
                    TypeVariable item = objectClass.getTypeParameters()[i];
                    if (item.getName().equals(typeVar.getName())) {
                        paramIndex = i;
                        break;
                    }
                }
            }

            if (paramIndex != -1) {
                itemType = paramType.getActualTypeArguments()[paramIndex];
                if (!itemType.equals(this.itemType)) {
                    itemTypeDeser = parser.getConfig().getDeserializer(itemType);
                }
            }
        }

        final XMLLexer lexer = parser.getLexer();

        if (lexer.token() != XMLToken.LBRACE_XML) {
            String errorMessage = "exepct '[', but " + XMLToken.name(lexer.token());
            if (objectType != null) {
                errorMessage += ", type : " + objectType;
            }
            throw new XMLException(errorMessage);
        }

        if (itemTypeDeser == null) {
            itemTypeDeser = deserializer = parser.getConfig().getDeserializer(itemType);
            itemFastMatchToken = deserializer.getFastMatchToken();
        }

        for (int i = 0;; ++i) {
        	
        	if(lexer.getCurrent() == '<'){
        		lexer.nextToken(XMLToken.LBRACE_XML);
        		
        		if(lexer.getCurrent() == '/'){
        			lexer.nextToken(XMLToken.COMMA);
        			break;
        		}
        		
        		if( ! lexer.equalsSymbol(fieldInfo.getName()) ){
        			break;
        		}
        		lexer.scanSymbol(parser.getSymbolTable());
        	}
        	
            if (lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                while (lexer.token() == XMLToken.COMMA) {
                    lexer.nextToken();
                    continue;
                }
            }

            Object val = itemTypeDeser.deserialze(parser, itemType, i);
            array.add(val);

            parser.checkListResolve(array);

            if (lexer.token() == XMLToken.COMMA) {
                lexer.nextToken(itemFastMatchToken);
                continue;
            }
        }

//        lexer.nextToken(XMLToken.COMMA);
    }

}
