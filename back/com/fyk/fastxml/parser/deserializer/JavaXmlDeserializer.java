package com.fyk.fastxml.parser.deserializer;

import java.lang.reflect.Type;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.DefaultJSONParser.ResolveTask;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.ParseContext;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLLexer;

public class JavaXmlDeserializer {

    @SuppressWarnings("unchecked")
    public String deserialze(DefaultXMLParser parser, Type type, Object fieldName, Object object) {
        XMLLexer lexer = parser.getLexer(); // xxx

        if (lexer.token() == JSONToken.NULL) {
            lexer.nextToken(JSONToken.COMMA);
            return null;
        }

        ParseContext childContext = null;

        try {
            Map<String, Object> fieldValues = null;

            if (lexer.token() == JSONToken.RBRACE) {
                lexer.nextToken(JSONToken.COMMA);
                return "";
            }

            if (lexer.token() != JSONToken.LBRACE && lexer.token() != JSONToken.COMMA) {
                StringBuffer buf = (new StringBuffer()) //
                .append("syntax error, expect {, actual ") //
                .append(lexer.tokenName()) //
                .append(", pos ") //
                .append(lexer.pos()) //
                ;
                if (fieldName instanceof String) {
                    buf //
                    .append(", fieldName ") //
                    .append(fieldName);
                }
                throw new JSONException(buf.toString());
            }


            for (;;) {

                String key = lexer.scanSymbol(parser.getSymbolTable());

                if (key == null) {
                    if (lexer.token() == JSONToken.RBRACE) {
                        lexer.nextToken(JSONToken.COMMA);
                        break;
                    }
                    if (lexer.token() == JSONToken.COMMA) {
//                        if (parser.isEnabled(Feature.AllowArbitraryCommas)) {
//                            continue;
//                        }
                    }
                }

                if ("$ref" == key) {
                    lexer.nextTokenWithColon(JSONToken.LITERAL_STRING);
                    if (lexer.token() == JSONToken.LITERAL_STRING) {
                        String ref = lexer.stringVal();
                    } else {
                        throw new JSONException("illegal ref, " + JSONToken.name(lexer.token()));
                    }

                    lexer.nextToken(JSONToken.RBRACE);
                    if (lexer.token() != JSONToken.RBRACE) {
                        throw new JSONException("illegal ref");
                    }
                    lexer.nextToken(JSONToken.COMMA);


                    return "";
                }

                if (JSON.DEFAULT_TYPE_KEY == key) {
                    lexer.nextTokenWithColon(JSONToken.LITERAL_STRING);
                    if (lexer.token() == JSONToken.LITERAL_STRING) {
                        String typeName = lexer.stringVal();
                        lexer.nextToken(JSONToken.COMMA);

                        if (type instanceof Class && typeName.equals(((Class<?>) type).getName())) {
                            if (lexer.token() == JSONToken.RBRACE) {
                                lexer.nextToken();
                                break;
                            }
                            continue;
                        }

//                        Class<?> userType = TypeUtils.loadClass(typeName);
//                        ObjectDeserializer deserizer = parser.getConfig().getDeserializer(userType);
//                        return (T) deserizer.deserialze(parser, userType, fieldName);
                        return "";
                    } else {
                        throw new JSONException("syntax error");
                    }
                }

//                if (object == null && fieldValues == null) {
//                    object = createInstance(parser, type);
//                    if (object == null) {
//                        fieldValues = new HashMap<String, Object>(this.fieldDeserializers.size());
//                    }
//                    childContext = parser.setContext(context, object, fieldName);
//                }

//                boolean match = parseField(parser, key, object, type, fieldValues);
//                if (!match) {
//                    if (lexer.token() == JSONToken.RBRACE) {
//                        lexer.nextToken();
//                        break;
//                    }
//
//                    continue;
//                }

                if (lexer.token() == JSONToken.COMMA) {
                    continue;
                }

                if (lexer.token() == JSONToken.RBRACE) {
                    lexer.nextToken(JSONToken.COMMA);
                    break;
                }

                if (lexer.token() == JSONToken.IDENTIFIER || lexer.token() == JSONToken.ERROR) {
                    throw new JSONException("syntax error, unexpect token " + JSONToken.name(lexer.token()));
                }
            }

//            return (T) object;
            return "";
        } finally {
            if (childContext != null) {
                childContext.setObject(object);
            }
        }
    }

    public int getFastMatchToken() {
        return JSONToken.LBRACE;
    }

}
