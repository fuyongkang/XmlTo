package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import java.math.BigInteger;

import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;
import com.fyk.fastxml.util.TypeUtils;

public class BigIntegerDeserializer implements ObjectDeserializer {

    public final static BigIntegerDeserializer instance = new BigIntegerDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type clazz, Object fieldName) {
        return (T) deserialze(parser);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialze(DefaultXMLParser parser) {
        final XMLLexer lexer = parser.getLexer();
        if (lexer.token() == XMLToken.LITERAL_INT) {
            String val = lexer.numberString();
            lexer.nextToken(XMLToken.COMMA);
            return (T) new BigInteger(val);
        }

        Object value = parser.parse();

        if (value == null) {
            return null;
        }

        return (T) TypeUtils.castToBigInteger(value);
    }

    public int getFastMatchToken() {
        return XMLToken.LITERAL_INT;
    }
}
