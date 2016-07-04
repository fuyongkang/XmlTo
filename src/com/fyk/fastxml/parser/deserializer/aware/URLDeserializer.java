package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;

import com.fyk.fastxml.XMLException;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLToken;

public class URLDeserializer implements ObjectDeserializer {
    public final static URLDeserializer instance = new URLDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type clazz, Object fieldName) {
        
        String url = (String) parser.parse();
        
        if (url == null) {
            return null;
        }
        
        try {
            return (T) new URL(url);
        } catch (MalformedURLException e) {
            throw new XMLException("create url error", e);
        }
    }

    public int getFastMatchToken() {
        return XMLToken.LITERAL_STRING;
    }
}
