package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;

import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;

public class StringDeserializer implements ObjectDeserializer {

    public final static StringDeserializer instance = new StringDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type clazz, Object fieldName) {
        return (T) deserialze(parser);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T deserialze(DefaultXMLParser parser) {
        final XMLLexer lexer = parser.getLexer();
        if (lexer.token() == XMLToken.LITERAL_STRING) {
            String val = lexer.stringVal();
            lexer.nextToken(XMLToken.COMMA);
            return (T) val;
        }
        
        if (lexer.token() == XMLToken.LITERAL_INT) {
            String val = lexer.numberString();
            lexer.nextToken(XMLToken.COMMA);
            return (T) val;
        }

        Object value = parser.parse();

        if (value == null) {
            return null;
        }

        return (T) value.toString();
    }

    public int getFastMatchToken() {
        return XMLToken.LITERAL_STRING;
    }

}
