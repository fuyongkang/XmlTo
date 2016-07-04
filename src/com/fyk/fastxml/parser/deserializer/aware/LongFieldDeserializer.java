package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import java.util.Map;

import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.ParserConfig;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;
import com.fyk.fastxml.util.FieldInfo;
import com.fyk.fastxml.util.TypeUtils;

public class LongFieldDeserializer extends FieldDeserializer {

    private final ObjectDeserializer fieldValueDeserilizer;

    public LongFieldDeserializer(ParserConfig mapping, Class<?> clazz, FieldInfo fieldInfo){
        super(clazz, fieldInfo);

        fieldValueDeserilizer = mapping.getDeserializer(fieldInfo);
    }

    @Override
    public void parseField(DefaultXMLParser parser, Object object, Type objectType, Map<String, Object> fieldValues) {
        Long value;
        
        final XMLLexer lexer = parser.getLexer();
        if (lexer.token() == XMLToken.LITERAL_INT) {
            long val = lexer.longValue();
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

            value = TypeUtils.castToLong(obj);
        }
        
        if (value == null && getFieldClass() == long.class) {
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
        return fieldValueDeserilizer.getFastMatchToken();
    }
}
