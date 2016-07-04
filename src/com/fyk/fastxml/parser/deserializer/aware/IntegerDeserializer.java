package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;
import com.fyk.fastxml.util.TypeUtils;

public class IntegerDeserializer implements ObjectDeserializer {

    public final static IntegerDeserializer instance = new IntegerDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type clazz, Object fieldName) {
        final XMLLexer lexer = parser.getLexer();

        if (lexer.token() == XMLToken.NULL) {
            lexer.nextToken(XMLToken.COMMA);
            return null;
        }

        Integer intObj;
        if (lexer.token() == XMLToken.LITERAL_INT) {
            int val = lexer.intValue();
            lexer.nextToken(XMLToken.COMMA);
            intObj = Integer.valueOf(val);
        } else if (lexer.token() == XMLToken.LITERAL_FLOAT) {
            BigDecimal decimalValue = lexer.decimalValue();
            lexer.nextToken(XMLToken.COMMA);
            intObj = Integer.valueOf(decimalValue.intValue());
        } else {
            Object value = parser.parse();

            intObj = TypeUtils.castToInt(value);
        }
        
        if (clazz == AtomicInteger.class) {
            return (T) new AtomicInteger(intObj.intValue());
        }
        
        return (T) intObj;
    }

    public int getFastMatchToken() {
        return XMLToken.LITERAL_INT;
    }
}
