package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLToken;

public class CharsetDeserializer implements ObjectDeserializer {
    public final static CharsetDeserializer instance = new CharsetDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type clazz, Object fieldName) {
        Object value = parser.parse();

        if (value == null) {
            return null;
        }
        
        String charset = (String) value;
        
        return (T) Charset.forName(charset);
    }

    public int getFastMatchToken() {
        return XMLToken.LITERAL_STRING;
    }
}
