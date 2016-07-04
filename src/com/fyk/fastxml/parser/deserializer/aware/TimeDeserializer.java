package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import com.fyk.fastxml.XMLException;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLScanner;
import com.fyk.fastxml.parser.XMLToken;

public class TimeDeserializer implements ObjectDeserializer {

    public final static TimeDeserializer instance = new TimeDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type clazz, Object fieldName) {
        XMLLexer lexer = parser.getLexer();
        
        if (lexer.token() == XMLToken.COMMA) {
            lexer.nextToken(XMLToken.LITERAL_STRING);
            
            if (lexer.token() != XMLToken.LITERAL_STRING) {
                throw new XMLException("syntax error");
            }
            
            lexer.nextTokenWithColon(XMLToken.LITERAL_INT);
            
            if (lexer.token() != XMLToken.LITERAL_INT) {
                throw new XMLException("syntax error");
            }
            
            long time = lexer.longValue();
            lexer.nextToken(XMLToken.RBRACE);
            if (lexer.token() != XMLToken.RBRACE) {
                throw new XMLException("syntax error");
            }
            lexer.nextToken(XMLToken.COMMA);
            
            return (T) new java.sql.Time(time);
        }
        
        Object val = parser.parse();

        if (val == null) {
            return null;
        }

        if (val instanceof java.sql.Time) {
            return (T) val;
        } else if (val instanceof Number) {
            return (T) new java.sql.Time(((Number) val).longValue());
        } else if (val instanceof String) {
            String strVal = (String) val;
            if (strVal.length() == 0) {
                return null;
            }
            
            long longVal;
            XMLScanner dateLexer = new XMLScanner(strVal);
            if (dateLexer.scanISO8601DateIfMatch()) {
                longVal = dateLexer.getCalendar().getTimeInMillis();
            } else {
                longVal = Long.parseLong(strVal);
            }
            dateLexer.close();
            return (T) new java.sql.Time(longVal);
        }
        
        throw new XMLException("parse error");
    }

    public int getFastMatchToken() {
        return XMLToken.LITERAL_INT;
    }
}
