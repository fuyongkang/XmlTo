package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.fyk.fastxml.XMLException;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;

@SuppressWarnings("rawtypes")
public class EnumDeserializer implements ObjectDeserializer {

    private final Class<?>           enumClass;

    private final Map<Integer, Enum> ordinalMap = new HashMap<Integer, Enum>();
    private final Map<String, Enum>  nameMap    = new HashMap<String, Enum>();

    public EnumDeserializer(Class<?> enumClass){
        this.enumClass = enumClass;

        try {
            Method valueMethod = enumClass.getMethod("values");
            Object[] values = (Object[]) valueMethod.invoke(null);
            for (Object value : values) {
                Enum e = (Enum) value;
                ordinalMap.put(e.ordinal(), e);
                nameMap.put(e.name(), e);
            }
        } catch (Exception ex) {
            throw new XMLException("init enum values error, " + enumClass.getName());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type type, Object fieldName) {
        try {
            Object value;
            final XMLLexer lexer = parser.getLexer();
            if (lexer.token() == XMLToken.LITERAL_INT) {
                value = lexer.intValue();
                lexer.nextToken(XMLToken.COMMA);

                T e = (T) ordinalMap.get(value);
                if (e == null) {
                    throw new XMLException("parse enum " + enumClass.getName() + " error, value : " + value);
                }
                return e;
            } else if (lexer.token() == XMLToken.LITERAL_STRING) {
                String strVal = lexer.stringVal();
                lexer.nextToken(XMLToken.COMMA);

                if (strVal.length() == 0) {
                    return (T) null;
                }

                value = nameMap.get(strVal);

                return (T) Enum.valueOf((Class<Enum>) enumClass, strVal);
            } else if (lexer.token() == XMLToken.NULL) {
                value = null;
                lexer.nextToken(XMLToken.COMMA);

                return null;
            } else {
                value = parser.parse();
            }

            throw new XMLException("parse enum " + enumClass.getName() + " error, value : " + value);
        } catch (XMLException e) {
            throw e;
        } catch (Throwable e) {
            throw new XMLException(e.getMessage(), e);
        }
    }

    public int getFastMatchToken() {
        return XMLToken.LITERAL_INT;
    }
}
