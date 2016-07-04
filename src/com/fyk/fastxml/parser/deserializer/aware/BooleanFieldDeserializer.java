package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import java.util.Map;

import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.ParserConfig;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;
import com.fyk.fastxml.util.FieldInfo;
import com.fyk.fastxml.util.TypeUtils;

public class BooleanFieldDeserializer extends FieldDeserializer {

    public BooleanFieldDeserializer(ParserConfig mapping, Class<?> clazz, FieldInfo fieldInfo){
        super(clazz, fieldInfo);
    }

    @Override
    public void parseField(DefaultXMLParser parser, Object object, Type objectType, Map<String, Object> fieldValues) {
        Boolean value;

        final XMLLexer lexer = parser.getLexer();
        if (lexer.token() == XMLToken.TRUE) {
            lexer.nextToken(XMLToken.COMMA);
            if (object == null) {
                fieldValues.put(fieldInfo.getName(), Boolean.TRUE);
            } else {
                setValue(object, true);
            }
            return;
        }

        if (lexer.token() == XMLToken.LITERAL_INT) {
            int val = lexer.intValue();
            lexer.nextToken(XMLToken.COMMA);
            boolean booleanValue = val == 1;
            if (object == null) {
                fieldValues.put(fieldInfo.getName(), booleanValue);
            } else {
                setValue(object, booleanValue);
            }
            return;
        }

        if (lexer.token() == XMLToken.NULL) {
            value = null;
            lexer.nextToken(XMLToken.COMMA);

            if (getFieldClass() == boolean.class) {
                // skip
                return;
            }

            if (object != null) {
                setValue(object, null);
            }
            return;
        }

        if (lexer.token() == XMLToken.FALSE) {
            lexer.nextToken(XMLToken.COMMA);
            if (object == null) {
                fieldValues.put(fieldInfo.getName(), Boolean.FALSE);
            } else {
                setValue(object, false);
            }
            return;
        }

        Object obj = parser.parse();

        value = TypeUtils.castToBoolean(obj);

        if (value == null && getFieldClass() == boolean.class) {
            // skip
            return;
        }

        if (object == null) {
            fieldValues.put(fieldInfo.getName(), value);
        } else {
            setValue(object, value);
        }
    }

    public int getFastMatchToken() {
        return XMLToken.TRUE;
    }
}
