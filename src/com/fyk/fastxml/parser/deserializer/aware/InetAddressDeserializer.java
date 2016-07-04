package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.fyk.fastxml.XMLException;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLToken;

public class InetAddressDeserializer implements ObjectDeserializer {

    public final static InetAddressDeserializer instance = new InetAddressDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type clazz, Object fieldName) {

        String host = (String) parser.parse();

        if (host == null) {
            return null;
        }
        
        if (host.length() == 0) {
            return null;
        }

        try {
            return (T) InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new XMLException("deserialize error", e);
        }
    }
    
    public int getFastMatchToken() {
        return XMLToken.LITERAL_STRING;
    }

}
