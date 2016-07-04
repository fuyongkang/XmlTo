package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;

import com.fyk.fastxml.parser.DefaultXMLParser;

public interface ObjectDeserializer {
    <T> T deserialze(DefaultXMLParser parser, Type type, Object fieldName);
    
    int getFastMatchToken();
}
