package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicLong;

import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;
import com.fyk.fastxml.util.TypeUtils;

public class LongDeserializer implements ObjectDeserializer {

    public final static LongDeserializer instance = new LongDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type clazz, Object fieldName) {
        final XMLLexer lexer = parser.getLexer();

        Long longObject;
        if (lexer.token() == XMLToken.LITERAL_INT) {
            long longValue = lexer.longValue();
            lexer.nextToken(XMLToken.COMMA);
            longObject = Long.valueOf(longValue);
        } else {

            Object value = parser.parse();

            if (value == null) {
                return null;
            }

            longObject = TypeUtils.castToLong(value);
        }
        
        if (clazz == AtomicLong.class) {
            return (T) new AtomicLong(longObject.longValue());
        }
        
        return (T) longObject;
    }

    public int getFastMatchToken() {
        return XMLToken.LITERAL_INT;
    }
}
