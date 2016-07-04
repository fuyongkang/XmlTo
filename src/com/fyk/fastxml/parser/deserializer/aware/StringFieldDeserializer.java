package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import java.util.Map;

import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.ParserConfig;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;
import com.fyk.fastxml.util.FieldInfo;

public class StringFieldDeserializer extends FieldDeserializer {

    private final ObjectDeserializer fieldValueDeserilizer;

    public StringFieldDeserializer(ParserConfig config, Class<?> clazz, FieldInfo fieldInfo){
        super(clazz, fieldInfo);

        fieldValueDeserilizer = config.getDeserializer(fieldInfo);
    }

    @Override
    public void parseField(DefaultXMLParser parser, Object object, Type objectType, Map<String, Object> fieldValues) {
        String value;

        final XMLLexer lexer = parser.getLexer();
        if (lexer.token() == XMLToken.LITERAL_STRING) {
            value = lexer.stringVal();
            lexer.nextToken(XMLToken.COMMA);
        } else {

            Object obj = parser.parse();

            if (obj == null) {
                value = null;
            } else {
                value = obj.toString();
            }
        }

        if (object == null) {
            fieldValues.put(fieldInfo.getName(), value);
        } else {
            setValue(object, value);
        }
    }

    public int getFastMatchToken() {
        return fieldValueDeserilizer.getFastMatchToken();
    }
}
