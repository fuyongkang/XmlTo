package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;

import com.alibaba.fastjson.parser.JSONToken;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;

public class CharArrayDeserializer implements ObjectDeserializer {

    public final static CharArrayDeserializer instance = new CharArrayDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type clazz, Object fieldName) {
        return (T) deserialze(parser);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T deserialze(DefaultXMLParser parser) {
        final XMLLexer lexer = parser.getLexer();
        if (lexer.token() == XMLToken.LITERAL_STRING) {
            String val = lexer.stringVal();
            lexer.nextToken(JSONToken.COMMA);
            return (T) val.toCharArray();
        }
        
        if (lexer.token() == XMLToken.LITERAL_INT) {
            Number val = lexer.integerValue();
            lexer.nextToken(JSONToken.COMMA);
            return (T) val.toString().toCharArray();
        }

        Object value = parser.parse();

        if (value == null) {
            return null;
        }

        return (T) "".toCharArray();
    }

    public int getFastMatchToken() {
        return XMLToken.LITERAL_STRING;
    }

}
