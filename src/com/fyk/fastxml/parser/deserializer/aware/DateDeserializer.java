package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;

import com.fyk.fastxml.XMLException;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLScanner;
import com.fyk.fastxml.parser.XMLToken;

public class DateDeserializer extends AbstractDateDeserializer implements ObjectDeserializer {

    public final static DateDeserializer instance = new DateDeserializer();

    @SuppressWarnings("unchecked")
    protected <T> T cast(DefaultXMLParser parser, Type clazz, Object fieldName, Object val) {

        if (val == null) {
            return null;
        }

        if (val instanceof java.util.Date) {
            return (T) val;
        } else if (val instanceof Number) {
            return (T) new java.util.Date(((Number) val).longValue());
        } else if (val instanceof String) {
            String strVal = (String) val;
            if (strVal.length() == 0) {
                return null;
            }

            XMLScanner dateLexer = new XMLScanner(strVal);
            try {
                if (dateLexer.scanISO8601DateIfMatch(false)) {
                    return (T) dateLexer.getCalendar().getTime();
                }
            } finally {
                dateLexer.close();
            }

            DateFormat dateFormat = parser.getDateFormat();
            try {
                return (T) dateFormat.parse(strVal);
            } catch (ParseException e) {
                // skip
            }

            long longVal = Long.parseLong(strVal);
            return (T) new java.util.Date(longVal);
        }

        throw new XMLException("parse error");
    }

    public int getFastMatchToken() {
        return XMLToken.LITERAL_INT;
    }
}
