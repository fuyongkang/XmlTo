package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;

import com.fyk.fastxml.XMLException;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;
import com.fyk.fastxml.util.TypeUtils;

public class ClassDerializer implements ObjectDeserializer {

    public final static ClassDerializer instance = new ClassDerializer();

    public ClassDerializer(){
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type type, Object fieldName) {
        XMLLexer lexer = parser.getLexer();
        
        if (lexer.token() == XMLToken.NULL) {
            lexer.nextToken();
            return null;
        }
        
        if (lexer.token() != XMLToken.LITERAL_STRING) {
            throw new XMLException("expect className");
        }
        String className = lexer.stringVal();
        lexer.nextToken(XMLToken.COMMA);

        return (T) TypeUtils.loadClass(className);
    }

    public int getFastMatchToken() {
        return XMLToken.LITERAL_STRING;
    }

}
