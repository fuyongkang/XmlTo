package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;
import com.fyk.fastxml.util.TypeUtils;

public class BooleanDeserializer implements ObjectDeserializer {

    public final static BooleanDeserializer instance = new BooleanDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type clazz, Object fieldName) {
        final XMLLexer lexer = parser.getLexer();

        Boolean boolObj;
        if (lexer.token() == XMLToken.TRUE) {
            lexer.nextToken(XMLToken.COMMA);
            boolObj = Boolean.TRUE;
        } else if (lexer.token() == XMLToken.FALSE) {
            lexer.nextToken(XMLToken.COMMA);
            boolObj = Boolean.FALSE;
        } else if (lexer.token() == XMLToken.LITERAL_INT) {
            int intValue = lexer.intValue();
            lexer.nextToken(XMLToken.COMMA);

            if (intValue == 1) {
                boolObj = Boolean.TRUE;
            } else {
                boolObj = Boolean.FALSE;
            }
        } else {
            Object value = parser.parse();

            if (value == null) {
                return null;
            }

            boolObj = TypeUtils.castToBoolean(value);
        }

        if (clazz == AtomicBoolean.class) {
            return (T) new AtomicBoolean(boolObj.booleanValue());
        }

        return (T) boolObj;
    }

    public int getFastMatchToken() {
        return XMLToken.TRUE;
    }
}
