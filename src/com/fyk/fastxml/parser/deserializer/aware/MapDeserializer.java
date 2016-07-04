package com.fyk.fastxml.parser.deserializer.aware;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fyk.fastxml.XML;
import com.fyk.fastxml.XMLException;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.DefaultXMLParser.ResolveTask;
import com.fyk.fastxml.parser.Feature;
import com.fyk.fastxml.parser.ParseContext;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;
import com.fyk.fastxml.util.TypeUtils;

public class MapDeserializer implements ObjectDeserializer {

    public final static MapDeserializer instance = new MapDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultXMLParser parser, Type type, Object fieldName) {
        final XMLLexer lexer = parser.getLexer();
        if (lexer.token() == XMLToken.NULL) {
            lexer.nextToken(XMLToken.COMMA);
            return null;
        }

        Map<Object, Object> map = createMap(type);

        ParseContext context = parser.getContext();

        try {
            parser.setContext(context, map, fieldName);
            return (T) deserialze(parser, type, fieldName, map);
        } finally {
            parser.setContext(context);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Object deserialze(DefaultXMLParser parser, Type type, Object fieldName, Map map) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type keyType = parameterizedType.getActualTypeArguments()[0];
            Type valueType = parameterizedType.getActualTypeArguments()[1];

            if (String.class == keyType) {
                return parseMap(parser, (Map<String, Object>) map, valueType, fieldName);
            } else {
                return parseMap(parser, map, keyType, valueType, fieldName);
            }
        } else {
//            return parser.parseObject(map, fieldName);
        	return null;
        }
    }
    
    @SuppressWarnings("rawtypes")
    public static Map parseMap(DefaultXMLParser parser, Map<String, Object> map, Type valueType, Object fieldName) {
        XMLLexer lexer = parser.getLexer();

        if (lexer.token() != XMLToken.LBRACE) {
            throw new XMLException("syntax error, expect {, actual " + lexer.token());
        }

        ParseContext context = parser.getContext();
        try {
            for (;;) {
                lexer.skipWhitespace();
                char ch = lexer.getCurrent();
                if (parser.isEnabled(Feature.AllowArbitraryCommas)) {
                    while (ch == ',') {
                        lexer.next();
                        lexer.skipWhitespace();
                        ch = lexer.getCurrent();
                    }
                }

                String key;
                if (ch == '"') {
                    key = lexer.scanSymbol(parser.getSymbolTable(), '"');
                    lexer.skipWhitespace();
                    ch = lexer.getCurrent();
                    if (ch != ':') {
                        throw new XMLException("expect ':' at " + lexer.pos());
                    }
                } else if (ch == '}') {
                    lexer.next();
                    lexer.resetStringPosition();
                    lexer.nextToken(XMLToken.COMMA);
                    return map;
                } else if (ch == '\'') {
                    if (!parser.isEnabled(Feature.AllowSingleQuotes)) {
                        throw new XMLException("syntax error");
                    }

                    key = lexer.scanSymbol(parser.getSymbolTable(), '\'');
                    lexer.skipWhitespace();
                    ch = lexer.getCurrent();
                    if (ch != ':') {
                        throw new XMLException("expect ':' at " + lexer.pos());
                    }
                } else {
                    if (!parser.isEnabled(Feature.AllowUnQuotedFieldNames)) {
                        throw new XMLException("syntax error");
                    }

                    key = lexer.scanSymbolUnQuoted(parser.getSymbolTable());
                    lexer.skipWhitespace();
                    ch = lexer.getCurrent();
                    if (ch != ':') {
                        throw new XMLException("expect ':' at " + lexer.pos() + ", actual " + ch);
                    }
                }

                lexer.next();
                lexer.skipWhitespace();
                ch = lexer.getCurrent();

                lexer.resetStringPosition();

                if (key == XML.DEFAULT_TYPE_KEY) {
                    String typeName = lexer.scanSymbol(parser.getSymbolTable(), '"');
                    Class<?> clazz = TypeUtils.loadClass(typeName);

                    if (clazz == map.getClass()) {
                        lexer.nextToken(XMLToken.COMMA);
                        if (lexer.token() == XMLToken.RBRACE) {
                            lexer.nextToken(XMLToken.COMMA);
                            return map;
                        }
                        continue;
                    }

                    ObjectDeserializer deserializer = parser.getConfig().getDeserializer(clazz);

                    lexer.nextToken(XMLToken.COMMA);

                    parser.setResolveStatus(DefaultXMLParser.TypeNameRedirect);

                    if (context != null && !(fieldName instanceof Integer)) {
                        parser.popContext();
                    }

                    return (Map) deserializer.deserialze(parser, clazz, fieldName);
                }

                Object value;
                lexer.nextToken();

                if (lexer.token() == XMLToken.NULL) {
                    value = null;
                    lexer.nextToken();
                } else {
                    value = parser.parseObject(valueType);
                }

                map.put(key, value);
//                parser.checkMapResolve(map, key);

                parser.setContext(context, value, key);

                final int tok = lexer.token();
                if (tok == XMLToken.EOF || tok == XMLToken.RBRACKET) {
                    return map;
                }

                if (tok == XMLToken.RBRACE) {
                    lexer.nextToken();
                    return map;
                }
            }
        } finally {
            parser.setContext(context);
        }

    }
    
    public static Object parseMap(DefaultXMLParser parser, Map<Object, Object> map, Type keyType, Type valueType,
                                  Object fieldName) {
    	XMLLexer lexer = parser.getLexer();

        if (lexer.token() != XMLToken.LBRACE && lexer.token() != XMLToken.COMMA) {
            throw new XMLException("syntax error, expect {, actual " + lexer.tokenName());
        }

        ObjectDeserializer keyDeserializer = parser.getConfig().getDeserializer(keyType);
        ObjectDeserializer valueDeserializer = parser.getConfig().getDeserializer(valueType);
        lexer.nextToken(keyDeserializer.getFastMatchToken());

        ParseContext context = parser.getContext();
        try {
            for (;;) {
                if (lexer.token() == XMLToken.RBRACE) {
                    lexer.nextToken(XMLToken.COMMA);
                    break;
                }

                if (lexer.token() == XMLToken.LITERAL_STRING && lexer.isRef()) {
                    Object object = null;

                    lexer.nextTokenWithColon(XMLToken.LITERAL_STRING);
                    if (lexer.token() == XMLToken.LITERAL_STRING) {
                        String ref = lexer.stringVal();
                        if ("..".equals(ref)) {
                            ParseContext parentContext = context.getParentContext();
                            object = parentContext.getObject();
                        } else if ("$".equals(ref)) {
                            ParseContext rootContext = context;
                            while (rootContext.getParentContext() != null) {
                                rootContext = rootContext.getParentContext();
                            }

                            object = rootContext.getObject();
                        } else {
                            parser.addResolveTask(new ResolveTask(context, ref));
                            parser.setResolveStatus(DefaultXMLParser.NeedToResolve);
                        }
                    } else {
                        throw new XMLException("illegal ref, " + XMLToken.name(lexer.token()));
                    }

                    lexer.nextToken(XMLToken.RBRACE);
                    if (lexer.token() != XMLToken.RBRACE) {
                        throw new XMLException("illegal ref");
                    }
                    lexer.nextToken(XMLToken.COMMA);

                    // parser.setContext(context, map, fieldName);
                    // parser.setContext(context);

                    return object;
                }

                if (map.size() == 0 //
                    && lexer.token() == XMLToken.LITERAL_STRING //
                    && XML.DEFAULT_TYPE_KEY.equals(lexer.stringVal())) {
                    lexer.nextTokenWithColon(XMLToken.LITERAL_STRING);
                    lexer.nextToken(XMLToken.COMMA);
                    if (lexer.token() == XMLToken.RBRACE) {
                        lexer.nextToken();
                        return map;
                    }
                    lexer.nextToken(keyDeserializer.getFastMatchToken());
                }

                Object key = keyDeserializer.deserialze(parser, keyType, null);

                if (lexer.token() != XMLToken.COLON) {
                    throw new XMLException("syntax error, expect :, actual " + lexer.token());
                }

                lexer.nextToken(valueDeserializer.getFastMatchToken());

                Object value = valueDeserializer.deserialze(parser, valueType, key);

                map.put(key, value);

                if (lexer.token() == XMLToken.COMMA) {
                    lexer.nextToken(keyDeserializer.getFastMatchToken());
                }
            }
        } finally {
            parser.setContext(context);
        }

        return map;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Map<Object, Object> createMap(Type type) {
        if (type == Properties.class) {
            return new Properties();
        }

        if (type == Hashtable.class) {
            return new Hashtable();
        }

        if (type == IdentityHashMap.class) {
            return new IdentityHashMap();
        }

        if (type == SortedMap.class || type == TreeMap.class) {
            return new TreeMap();
        }

        if (type == ConcurrentMap.class || type == ConcurrentHashMap.class) {
            return new ConcurrentHashMap();
        }
        
        if (type == Map.class || type == HashMap.class) {
            return new HashMap();
        }
        
        if (type == LinkedHashMap.class) {
            return new LinkedHashMap();
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            return createMap(parameterizedType.getRawType());
        }

        Class<?> clazz = (Class<?>) type;
        if (clazz.isInterface()) {
            throw new XMLException("unsupport type " + type);
        }
        
        try {
            return (Map<Object, Object>) clazz.newInstance();
        } catch (Exception e) {
            throw new XMLException("unsupport type " + type, e);
        }
    }

    public int getFastMatchToken() {
        return XMLToken.LBRACE;
    }
}
