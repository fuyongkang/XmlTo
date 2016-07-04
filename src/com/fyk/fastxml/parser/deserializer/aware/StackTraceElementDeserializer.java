package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;

import com.fyk.fastxml.XML;
import com.fyk.fastxml.XMLException;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.Feature;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;

public class StackTraceElementDeserializer implements ObjectDeserializer {

    public final static StackTraceElementDeserializer instance = new StackTraceElementDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type type, Object fieldName) {
        XMLLexer lexer = parser.getLexer();
        if (lexer.token() == XMLToken.NULL) {
            lexer.nextToken();
            return null;
        }

        if (lexer.token() != XMLToken.LBRACE && lexer.token() != XMLToken.COMMA) {
            throw new XMLException("syntax error: " + XMLToken.name(lexer.token()));
        }

        String declaringClass = null;
        String methodName = null;
        String fileName = null;
        int lineNumber = 0;

        for (;;) {
            // lexer.scanSymbol
            String key = lexer.scanSymbol(parser.getSymbolTable());

            if (key == null) {
                if (lexer.token() == XMLToken.RBRACE) {
                    lexer.nextToken(XMLToken.COMMA);
                    break;
                }
                if (lexer.token() == XMLToken.COMMA) {
                    if (lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                        continue;
                    }
                }
            }

            lexer.nextTokenWithColon(XMLToken.LITERAL_STRING);
            if (key == "className") {
                if (lexer.token() == XMLToken.NULL) {
                    declaringClass = null;
                } else if (lexer.token() == XMLToken.LITERAL_STRING) {
                    declaringClass = lexer.stringVal();
                } else {
                    throw new XMLException("syntax error");
                }
            } else if (key == "methodName") {
                if (lexer.token() == XMLToken.NULL) {
                    methodName = null;
                } else if (lexer.token() == XMLToken.LITERAL_STRING) {
                    methodName = lexer.stringVal();
                } else {
                    throw new XMLException("syntax error");
                }
            } else if (key == "fileName") {
                if (lexer.token() == XMLToken.NULL) {
                    fileName = null;
                } else if (lexer.token() == XMLToken.LITERAL_STRING) {
                    fileName = lexer.stringVal();
                } else {
                    throw new XMLException("syntax error");
                }
            } else if (key == "lineNumber") {
                if (lexer.token() == XMLToken.NULL) {
                    lineNumber = 0;
                } else if (lexer.token() == XMLToken.LITERAL_INT) {
                    lineNumber = lexer.intValue();
                } else {
                    throw new XMLException("syntax error");
                }
            } else if (key == "nativeMethod") {
                if (lexer.token() == XMLToken.NULL) {
                    lexer.nextToken(XMLToken.COMMA);
                } else if (lexer.token() == XMLToken.TRUE) {
                    lexer.nextToken(XMLToken.COMMA);
                } else if (lexer.token() == XMLToken.FALSE) {
                    lexer.nextToken(XMLToken.COMMA);
                } else {
                    throw new XMLException("syntax error");
                }
            } else if (key == XML.DEFAULT_TYPE_KEY) {
               if (lexer.token() == XMLToken.LITERAL_STRING) {
                    String elementType = lexer.stringVal();
                    if (!elementType.equals("java.lang.StackTraceElement")) {
                        throw new XMLException("syntax error : " + elementType);    
                    }
                } else {
                    if (lexer.token() != XMLToken.NULL) {
                        throw new XMLException("syntax error");
                    }
                }
            } else {
                throw new XMLException("syntax error : " + key);
            }

            if (lexer.token() == XMLToken.RBRACE) {
                lexer.nextToken(XMLToken.COMMA);
                break;
            }
        }
        return (T) new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
    }

    public int getFastMatchToken() {
        return XMLToken.LBRACE;
    }
}
