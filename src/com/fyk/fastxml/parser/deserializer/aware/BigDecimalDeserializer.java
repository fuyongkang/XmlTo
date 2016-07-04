package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;
import com.fyk.fastxml.util.TypeUtils;

public class BigDecimalDeserializer implements ObjectDeserializer {

    public final static BigDecimalDeserializer instance = new BigDecimalDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type clazz, Object fieldName) {
        return (T) deserialze(parser);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialze(DefaultXMLParser parser) {
        final XMLLexer lexer = parser.getLexer();
        if (lexer.token() == XMLToken.LITERAL_INT) {
            long val = lexer.longValue();
            lexer.nextToken(XMLToken.COMMA);
            return (T) new BigDecimal(val);
        }

        if (lexer.token() == XMLToken.LITERAL_FLOAT) {
            BigDecimal val = lexer.decimalValue();
            lexer.nextToken(XMLToken.COMMA);
            return (T) val;
        }

        Object value = parser.parse();

        if (value == null) {
            return null;
        }

        return (T) TypeUtils.castToBigDecimal(value);
    }

    public int getFastMatchToken() {
        return XMLToken.LITERAL_INT;
    }
}
