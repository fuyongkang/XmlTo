package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import java.util.TimeZone;

import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLToken;

public class TimeZoneDeserializer implements ObjectDeserializer {
    public final static TimeZoneDeserializer instance = new TimeZoneDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type clazz, Object fieldName) {
        
        String id = (String) parser.parse();
        
        if (id == null) {
            return null;
        }
        
        return (T) TimeZone.getTimeZone(id);
    }

    public int getFastMatchToken() {
        return XMLToken.LITERAL_STRING;
    }
}
