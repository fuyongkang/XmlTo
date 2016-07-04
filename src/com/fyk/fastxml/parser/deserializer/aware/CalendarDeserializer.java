package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;

import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLToken;


public class CalendarDeserializer implements ObjectDeserializer {
    public static final CalendarDeserializer instance = new CalendarDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type type, Object fieldName) {
        Date date = DateDeserializer.instance.deserialze(parser, type, fieldName);
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        return (T) calendar;
    }

    public int getFastMatchToken() {
        return XMLToken.LITERAL_INT;
    }

}
