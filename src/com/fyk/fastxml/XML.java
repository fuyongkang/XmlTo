/*
 * Copyright 1999-2101 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fyk.fastxml;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.DefaultXMLParser.ResolveTask;
import com.fyk.fastxml.parser.Feature;
import com.fyk.fastxml.parser.ParserConfig;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;
import com.fyk.fastxml.parser.deserializer.aware.FieldDeserializer;
import com.fyk.fastxml.util.IOUtils;
import com.fyk.fastxml.util.ThreadLocalCache;
import com.fyk.fastxml.util.TypeUtils;


/**
 * @author wenshao<szujobs@hotmail.com>
 */
public abstract class XML implements XMLStreamAware, XMLAware {
    public static String DEFAULT_TYPE_KEY     = "@type";

    public static int    DEFAULT_PARSER_FEATURE;
    static {
        int features = 0;
        features |= Feature.AutoCloseSource.getMask();
        features |= Feature.InternFieldNames.getMask();
        features |= Feature.UseBigDecimal.getMask();
        features |= Feature.AllowUnQuotedFieldNames.getMask();
        features |= Feature.AllowSingleQuotes.getMask();
        features |= Feature.AllowArbitraryCommas.getMask();
        features |= Feature.SortFeidFastMatch.getMask();
        features |= Feature.IgnoreNotMatch.getMask();
        DEFAULT_PARSER_FEATURE = features;
    }
    public static String DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final Object parse(String text) {
        return parse(text, DEFAULT_PARSER_FEATURE);
    }

    public static final Object parse(String text, int features) {
        if (text == null) {
            return null;
        }

        DefaultXMLParser parser = new DefaultXMLParser(text, ParserConfig.getGlobalInstance(), features);
        Object value = parser.parse();

        handleResovleTask(parser, value);

        parser.close();

        return value;
    }
    
    public static final Object parse(String text, Feature... features) {
        int featureValues = DEFAULT_PARSER_FEATURE;
        for (Feature featrue : features) {
            featureValues = Feature.config(featureValues, featrue, true);
        }

        return parse(text, featureValues);
    }

    public static final XMLObject parseObject(String text, Feature... features) {
        return (XMLObject) parse(text, features);
    }


    @SuppressWarnings("unchecked")
    public static final <T> T parseObject(String text, TypeReference<T> type, Feature... features) {
        return (T) parseObject(text, type.getType(), ParserConfig.getGlobalInstance(), DEFAULT_PARSER_FEATURE, features);
    }

    @SuppressWarnings("unchecked")
    public static final <T> T parseObject(String text, Class<T> clazz, Feature... features) {
        return (T) parseObject(text, (Type) clazz, ParserConfig.getGlobalInstance(), DEFAULT_PARSER_FEATURE, features);
    }

    @SuppressWarnings("unchecked")
    public static final <T> T parseObject(String input, Type clazz, Feature... features) {
        return (T) parseObject(input, clazz, ParserConfig.getGlobalInstance(), DEFAULT_PARSER_FEATURE, features);
    }

    @SuppressWarnings("unchecked")
    public static final <T> T parseObject(String input, Type clazz, int featureValues, Feature... features) {
        if (input == null) {
            return null;
        }

        for (Feature featrue : features) {
            featureValues = Feature.config(featureValues, featrue, true);
        }

        DefaultXMLParser parser = new DefaultXMLParser(input, ParserConfig.getGlobalInstance(), featureValues);
        T value = (T) parser.parseObject(clazz);

        handleResovleTask(parser, value);

        parser.close();

        return (T) value;
    }

    @SuppressWarnings("unchecked")
    public static final <T> T parseObject(String input, Type clazz, ParserConfig config, int featureValues,
                                          Feature... features) {
        if (input == null) {
            return null;
        }

        for (Feature featrue : features) {
            featureValues = Feature.config(featureValues, featrue, true);
        }

        DefaultXMLParser parser = new DefaultXMLParser(input, config, featureValues);
        T value = (T) parser.parseObject(clazz);

        handleResovleTask(parser, value);

        parser.close();

        return (T) value;
    }

    public static <T> int handleResovleTask(DefaultXMLParser parser, T value) {
        int size = parser.getResolveTaskList().size();
        for (int i = 0; i < size; ++i) {
            ResolveTask task = parser.getResolveTaskList().get(i);
            FieldDeserializer fieldDeser = task.getFieldDeserializer();

            Object object = null;
            if (task.getOwnerContext() != null) {
                object = task.getOwnerContext().getObject();
            }

            String ref = task.getReferenceValue();
            Object refValue;
            if (ref.startsWith("$")) {
                refValue = parser.getObject(ref);
            } else {
                refValue = task.getContext().getObject();
            }
            fieldDeser.setValue(object, refValue);
        }

        return size;
    }

    @SuppressWarnings("unchecked")
    public static final <T> T parseObject(byte[] input, Type clazz, Feature... features) {
        return (T) parseObject(input, 0, input.length, Charset.forName("UTF-8").newDecoder(), clazz, features);
    }

    @SuppressWarnings("unchecked")
    public static final <T> T parseObject(byte[] input, int off, int len, CharsetDecoder charsetDecoder, Type clazz,
                                          Feature... features) {
        charsetDecoder.reset();

        int scaleLength = (int) (len * (double) charsetDecoder.maxCharsPerByte());
        char[] chars = ThreadLocalCache.getChars(scaleLength);

        ByteBuffer byteBuf = ByteBuffer.wrap(input, off, len);
        CharBuffer charByte = CharBuffer.wrap(chars);
        IOUtils.decode(charsetDecoder, byteBuf, charByte);

        int position = charByte.position();

        return (T) parseObject(chars, position, clazz, features);
    }

    @SuppressWarnings("unchecked")
    public static final <T> T parseObject(char[] input, int length, Type clazz, Feature... features) {
        if (input == null || input.length == 0) {
            return null;
        }

        int featureValues = DEFAULT_PARSER_FEATURE;
        for (Feature featrue : features) {
            featureValues = Feature.config(featureValues, featrue, true);
        }

        DefaultXMLParser parser = new DefaultXMLParser(input, length, ParserConfig.getGlobalInstance(), featureValues);
        T value = (T) parser.parseObject(clazz);

        handleResovleTask(parser, value);

        parser.close();

        return (T) value;
    }

    public static final <T> T parseObject(String text, Class<T> clazz) {
        return parseObject(text, clazz, new Feature[0]);
    }

    public static final XMLArray parseArray(String text) {
        if (text == null) {
            return null;
        }

        DefaultXMLParser parser = new DefaultXMLParser(text, ParserConfig.getGlobalInstance());

        XMLArray array;

        XMLLexer lexer = parser.getLexer();
        if (lexer.token() == XMLToken.NULL) {
            lexer.nextToken();
            array = null;
        } else if (lexer.token() == XMLToken.EOF) {
            array = null;
        } else {
            array = new XMLArray();
            parser.parseArray(array);

            handleResovleTask(parser, array);
        }

        parser.close();

        return array;
    }

    public static final <T> List<T> parseArray(String text, Class<T> clazz) {
        if (text == null) {
            return null;
        }

        List<T> list;

        DefaultXMLParser parser = new DefaultXMLParser(text, ParserConfig.getGlobalInstance());
        XMLLexer lexer = parser.getLexer();
        if (lexer.token() == XMLToken.NULL) {
            lexer.nextToken();
            list = null;
        } else {
            list = new ArrayList<T>();
            parser.parseArray(clazz, list);

            handleResovleTask(parser, list);
        }

        parser.close();

        return list;
    }

    public static final List<Object> parseArray(String text, Type[] types) {
        if (text == null) {
            return null;
        }

        List<Object> list;

        DefaultXMLParser parser = new DefaultXMLParser(text, ParserConfig.getGlobalInstance());
        Object[] objectArray = parser.parseArray(types);
        if (objectArray == null) {
            list = null;
        } else {
            list = Arrays.asList(objectArray);
        }

        handleResovleTask(parser, list);

        parser.close();

        return list;
    }

    // ======================

    // ======================================

    @Override
    public String toString() {
        return toJSONString();
    }

    public String toJSONString() {
//        SerializeWriter out = new SerializeWriter();
//        try {
//            new JSONSerializer(out).write(this);
//            return out.toString();
//        } finally {
//            out.close();
//        }
    	return "";
    }

    public void writeJSONString(Appendable appendable) {
    }


    public static final <T> T toJavaObject(XML json, Class<T> clazz) {
        return TypeUtils.cast(json, clazz, ParserConfig.getGlobalInstance());
    }

    public final static String VERSION = "1.1.34-android";
}
