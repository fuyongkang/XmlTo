package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;

import com.fyk.fastxml.XMLException;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.Feature;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLScanner;
import com.fyk.fastxml.parser.XMLToken;

public abstract class AbstractDateDeserializer implements ObjectDeserializer {

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type clazz, Object fieldName) {
        XMLLexer lexer = parser.getLexer();

        Object val = null;
        if (lexer.token() == XMLToken.LITERAL_INT) {
            val = lexer.longValue();
            lexer.nextToken(XMLToken.COMMA);
        } else if (lexer.token() == XMLToken.LITERAL_STRING) {
            String strVal = lexer.stringVal();
            val = strVal;
            lexer.nextToken(XMLToken.COMMA);
            
            if (lexer.isEnabled(Feature.AllowISO8601DateFormat)) {
                XMLScanner iso8601Lexer = new XMLScanner(strVal);
                if (iso8601Lexer.scanISO8601DateIfMatch()) {
                    val = iso8601Lexer.getCalendar().getTime();
                }
                iso8601Lexer.close();
            }
        } else if (lexer.token() == XMLToken.NULL) {
            lexer.nextToken();
            val = null;
        } else if (lexer.token() == XMLToken.LBRACE) {
            lexer.nextToken();
            
            if (lexer.token() == XMLToken.LITERAL_STRING) {
                lexer.nextTokenWithColon(XMLToken.LITERAL_INT);
            } else {
                throw new XMLException("syntax error");
            }
            
            long timeMillis;
            if (lexer.token() == XMLToken.LITERAL_INT) {
                timeMillis = lexer.longValue();
                lexer.nextToken();
            } else {
                throw new XMLException("syntax error : " + lexer.tokenName());
            }

            val = timeMillis;
            
            parser.accept(XMLToken.RBRACE);
        } else if (parser.getResolveStatus() == DefaultXMLParser.TypeNameRedirect) {
            parser.setResolveStatus(DefaultXMLParser.NONE);
            parser.accept(XMLToken.COMMA);

            if (lexer.token() == XMLToken.LITERAL_STRING) {
                if (!"val".equals(lexer.stringVal())) {
                    throw new XMLException("syntax error");
                }
                lexer.nextToken();
            } else {
                throw new XMLException("syntax error");
            }

            parser.accept(XMLToken.COLON);

            val = parser.parse();

            parser.accept(XMLToken.RBRACE);
        } else {
            val = parser.parse();
        }

        return (T) cast(parser, clazz, fieldName, val);
    }

    protected abstract <T> T cast(DefaultXMLParser parser, Type clazz, Object fieldName, Object value);
}
