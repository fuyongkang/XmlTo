package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.Type;
import java.util.Map;

import com.fyk.fastxml.parser.DefaultXMLParser;

@SuppressWarnings("rawtypes")
public final class MapResolveFieldDeserializer extends FieldDeserializer {

    private final String              key;
    private final Map map;

    public MapResolveFieldDeserializer(Map map, String index){
        super(null, null);
        this.key = index;
        this.map = map;
    }

    @SuppressWarnings("unchecked")
    public void setValue(Object object, Object value) {
        map.put(key, value);
    }


    @Override
    public void parseField(DefaultXMLParser parser, Object object, Type objectType, Map<String, Object> fieldValues) {

    }
}
