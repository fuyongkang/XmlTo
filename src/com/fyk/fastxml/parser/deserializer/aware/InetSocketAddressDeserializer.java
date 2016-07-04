package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.fyk.fastxml.XMLException;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;

public class InetSocketAddressDeserializer implements ObjectDeserializer {

    public final static InetSocketAddressDeserializer instance = new InetSocketAddressDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type clazz, Object fieldName) {
        XMLLexer lexer = parser.getLexer();

        if (lexer.token() == XMLToken.NULL) {
            lexer.nextToken();
            return null;
        }

        parser.accept(XMLToken.LBRACE);

        InetAddress address = null;
        int port = 0;
        for (;;) {
            String key = lexer.stringVal();
            lexer.nextToken(XMLToken.COMMA);
           

            if (key.equals("address")) {
                parser.accept(XMLToken.COLON);
                address = parser.parseObject(InetAddress.class);
            } else if (key.equals("port")) {
                parser.accept(XMLToken.COLON);
                if (lexer.token() != XMLToken.LITERAL_INT) {
                    throw new XMLException("port is not int");
                }
                port = lexer.intValue();
                lexer.nextToken();
            } else {
                parser.accept(XMLToken.COLON);
                parser.parse();
            }

            if (lexer.token() == XMLToken.COMMA) {
                lexer.nextToken();
                continue;
            }

            break;
        }

        parser.accept(XMLToken.RBRACE);

        return (T) new InetSocketAddress(address, port);
    }

    public int getFastMatchToken() {
        return XMLToken.LBRACE;
    }
}
