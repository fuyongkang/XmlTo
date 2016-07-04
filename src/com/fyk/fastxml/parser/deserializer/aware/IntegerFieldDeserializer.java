package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import java.util.Map;

import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.ParserConfig;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;
import com.fyk.fastxml.util.FieldInfo;
import com.fyk.fastxml.util.TypeUtils;

public class IntegerFieldDeserializer extends FieldDeserializer {

    public IntegerFieldDeserializer(ParserConfig mapping, Class<?> clazz, FieldInfo fieldInfo){
        super(clazz, fieldInfo);
    }

    @Override
    public void parseField(DefaultXMLParser parser, Object object, Type objectType, Map<String, Object> fieldValues) {
        Integer value;

        final XMLLexer lexer = parser.getLexer();
        if (lexer.token() == XMLToken.LITERAL_INT) {
            int val = lexer.intValue();
            lexer.nextToken(XMLToken.COMMA);
            if (object == null) {
                fieldValues.put(fieldInfo.getName(), val);
            } else {
                setValue(object, val);
            }
            return;
        } else if (lexer.token() == XMLToken.NULL) {
            value = null;
            lexer.nextToken(XMLToken.COMMA);
        } else {
            Object obj = parser.parse();

            value = TypeUtils.castToInt(obj);
        }

        if (value == null && getFieldClass() == int.class) {
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
        return XMLToken.LITERAL_INT;
    }
}
