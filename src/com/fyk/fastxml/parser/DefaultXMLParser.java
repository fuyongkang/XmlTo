package com.fyk.fastxml.parser;

import static com.fyk.fastxml.parser.XMLLexer.EOI;
import static com.fyk.fastxml.parser.XMLToken.EOF;
import static com.fyk.fastxml.parser.XMLToken.ERROR;
import static com.fyk.fastxml.parser.XMLToken.FALSE;
import static com.fyk.fastxml.parser.XMLToken.LITERAL_FLOAT;
import static com.fyk.fastxml.parser.XMLToken.LITERAL_INT;
import static com.fyk.fastxml.parser.XMLToken.LITERAL_STRING;
import static com.fyk.fastxml.parser.XMLToken.NEW;
import static com.fyk.fastxml.parser.XMLToken.NULL;
import static com.fyk.fastxml.parser.XMLToken.RBRACKET;
import static com.fyk.fastxml.parser.XMLToken.SET;
import static com.fyk.fastxml.parser.XMLToken.TREE_SET;
import static com.fyk.fastxml.parser.XMLToken.TRUE;

import java.io.Closeable;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import com.fyk.fastxml.XML;
import com.fyk.fastxml.XMLException;
import com.fyk.fastxml.parser.deserializer.aware.FieldDeserializer;
import com.fyk.fastxml.parser.deserializer.aware.IntegerDeserializer;
import com.fyk.fastxml.parser.deserializer.aware.ListResolveFieldDeserializer;
import com.fyk.fastxml.parser.deserializer.aware.ObjectDeserializer;
import com.fyk.fastxml.parser.deserializer.aware.StringDeserializer;
import com.fyk.fastxml.util.TypeUtils;



/** 
 * 
 * @ClassName DefaultXMLParser 
 * @author fuyongkang
 * @date 2016-5-6 下午3:57:03 
 */
public class DefaultXMLParser implements Closeable{

    protected final Object             input;
    protected final XMLLexer          lexer;

    protected ParserConfig             config;
    protected final SymbolTable        symbolTable;

    protected ParseContext             context;
    private ParseContext[]             contextArray      = new ParseContext[8];
    private int                        contextArrayIndex = 0;

    private String                     dateFormatPattern = XML.DEFFAULT_DATE_FORMAT;
    private DateFormat                 dateFormat;

    private final List<ResolveTask>    resolveTaskList   = new ArrayList<ResolveTask>();
    
    public final static int            NONE              = 0;
    public final static int            NeedToResolve     = 1;
    public final static int            TypeNameRedirect  = 2;
    private int                        resolveStatus     = NONE;
    

    public DefaultXMLParser(String input){
        this(input, ParserConfig.getGlobalInstance(), XML.DEFAULT_PARSER_FEATURE);
    }

    public DefaultXMLParser(final String input, final ParserConfig config){
        this(input, new XMLScanner(input, XML.DEFAULT_PARSER_FEATURE), config);
    }

    public DefaultXMLParser(final String input, final ParserConfig config, int features){
        this(input, new XMLScanner(input, features), config);
    }

    public DefaultXMLParser(final char[] input, int length, final ParserConfig config, int features){
        this(new String(input), new XMLScanner(input, length, features), config);
    }

    public DefaultXMLParser(final XMLLexer lexer){
        this(lexer, ParserConfig.getGlobalInstance());
    }

    public DefaultXMLParser(final XMLLexer lexer, final ParserConfig config){
        this(null, lexer, config);
    }
    
    public DefaultXMLParser(final String input, final XMLLexer lexer, final ParserConfig config){
        this.lexer = lexer;
        this.input = input;
        this.config = config;
        this.symbolTable = config.getSymbolTable();


        if(lexer.ch=='<'){
        	lexer.next();
        }
        lexer.skipXmlVersion();
        lexer.skipWhitespace();
        lexer.nextToken(XMLToken.LBRACE_XML); // prime the pump

		lexer.scanSymbol(getSymbolTable());//跳过标签名称
		lexer.nextToken(XMLToken.COMMA);
    }

    public DateFormat getDateFormat() {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(dateFormatPattern);
        }
        return dateFormat;
    }

    public int getResolveStatus() {
        return resolveStatus;
    }

    public void setResolveStatus(int resolveStatus) {
        this.resolveStatus = resolveStatus;
    }
    
    public boolean isEnabled(Feature feature) {
        return getLexer().isEnabled(feature);
    }

    public XMLLexer getLexer() {
        return lexer;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public ParserConfig getConfig() {
        return config;
    }

    public ParseContext getContext() {
        return context;
    }

    public ParseContext setContext(Object object, Object fieldName) {
        if (isEnabled(Feature.DisableCircularReferenceDetect)) {
            return null;
        }

        return setContext(this.context, object, fieldName);
    }

    public ParseContext setContext(ParseContext parent, Object object, Object fieldName) {
        if (isEnabled(Feature.DisableCircularReferenceDetect)) {
            return null;
        }

        this.context = new ParseContext(parent, object, fieldName);
        addContext(this.context);

        return this.context;
    }

    private void addContext(ParseContext context) {
        int i = contextArrayIndex++;
        if (i >= contextArray.length) {
            int newLen = (contextArray.length * 3) / 2;
            ParseContext[] newArray = new ParseContext[newLen];
            System.arraycopy(contextArray, 0, newArray, 0, contextArray.length);
            contextArray = newArray;
        }
        contextArray[i] = context;
    }

    public void setContext(ParseContext context) {
        if (isEnabled(Feature.DisableCircularReferenceDetect)) {
            return;
        }
        this.context = context;
    }

    public void popContext() {
        if (isEnabled(Feature.DisableCircularReferenceDetect)) {
            return;
        }

        this.context = this.context.getParentContext();
        contextArray[contextArrayIndex - 1] = null;
        contextArrayIndex--;
    }

    public Object getObject(String path) {
        for (int i = 0; i < contextArrayIndex; ++i) {
            if (path.equals(contextArray[i].getPath())) {
                return contextArray[i].getObject();
            }
        }

        return null;
    }

    public void checkListResolve(Collection array) {
        if (resolveStatus == NeedToResolve) {
            final int index = array.size() - 1;
            final List list = (List) array;
            ResolveTask task = getLastResolveTask();
            task.setFieldDeserializer(new ListResolveFieldDeserializer(this, list, index));
            task.setOwnerContext(context);
            setResolveStatus(DefaultXMLParser.NONE);
        }
    }
    
    public List<ResolveTask> getResolveTaskList() {
        return resolveTaskList;
    }

    public void addResolveTask(ResolveTask task) {
        resolveTaskList.add(task);
    }

    public ResolveTask getLastResolveTask() {
        return resolveTaskList.get(resolveTaskList.size() - 1);
    }


    @SuppressWarnings("rawtypes")
    public final void parseArray(final Collection array) {
        parseArray(array, null);
    }
    
    public void parseArray(Class<?> clazz, @SuppressWarnings("rawtypes") Collection array) {
        parseArray((Type) clazz, array);
    }

    @SuppressWarnings("rawtypes")
    public void parseArray(Type type, Collection array) {
        parseArray(type, array, null);
    }
    
    public void parseArray(Type type, Collection array, Object fieldName) {
        if (lexer.token() == XMLToken.SET || lexer.token() == XMLToken.TREE_SET) {
            lexer.nextToken();
        }

        if (lexer.token() != XMLToken.LBRACKET) {
            throw new XMLException("exepct '[', but " + XMLToken.name(lexer.token()));
        }

        ObjectDeserializer deserializer = null;
        if (int.class == type) {
            deserializer = IntegerDeserializer.instance;
            lexer.nextToken(XMLToken.LITERAL_INT);
        } else if (String.class == type) {
            deserializer = StringDeserializer.instance;
            lexer.nextToken(XMLToken.LITERAL_STRING);
        } else {
            deserializer = config.getDeserializer(type);
            lexer.nextToken(deserializer.getFastMatchToken());
        }

        ParseContext context = this.getContext();
        this.setContext(array, fieldName);
        try {
            for (int i = 0;; ++i) {
                if (isEnabled(Feature.AllowArbitraryCommas)) {
                    while (lexer.token() == XMLToken.COMMA) {
                        lexer.nextToken();
                        continue;
                    }
                }

                if (lexer.token() == XMLToken.RBRACKET) {
                    break;
                }

                if (int.class == type) {
                    Object val = IntegerDeserializer.instance.deserialze(this, null, null);
                    array.add(val);
                } else if (String.class == type) {
                    String value;
                    if (lexer.token() == XMLToken.LITERAL_STRING) {
                        value = lexer.stringVal();
                        lexer.nextToken(XMLToken.COMMA);
                    } else {
                        Object obj = this.parse();
                        if (obj == null) {
                            value = null;
                        } else {
                            value = obj.toString();
                        }
                    }

                    array.add(value);
                } else {
                    Object val;
                    if (lexer.token() == XMLToken.NULL) {
                        lexer.nextToken();
                        val = null;
                    } else {
                        val = deserializer.deserialze(this, type, i);
                    }
                    array.add(val);
                    checkListResolve(array);
                }

                if (lexer.token() == XMLToken.COMMA) {
                    lexer.nextToken(deserializer.getFastMatchToken());
                    continue;
                }
            }
        } finally {
            this.setContext(context);
        }

        lexer.nextToken(XMLToken.COMMA);
    }
    
    public final void parseArray(final Collection array, Object fieldName) {
        final XMLLexer lexer = getLexer();

        if (lexer.token() == XMLToken.SET || lexer.token() == XMLToken.TREE_SET) {
            lexer.nextToken();
        }

        if (lexer.token() != XMLToken.LBRACKET) {
            throw new XMLException("syntax error, expect [, actual " + XMLToken.name(lexer.token()) + ", pos "
                                    + lexer.pos());
        }

        lexer.nextToken(XMLToken.LITERAL_STRING);

        ParseContext context = this.getContext();
        this.setContext(array, fieldName);
        try {
            for (;;) {
                if (isEnabled(Feature.AllowArbitraryCommas)) {
                    while (lexer.token() == XMLToken.COMMA) {
                        lexer.nextToken();
                        continue;
                    }
                }

                Object value;
                switch (lexer.token()) {
                    case LITERAL_INT:
                        value = lexer.integerValue();
                        lexer.addbuilerValue(String.valueOf(value));
                        lexer.nextToken(XMLToken.COMMA);
                        break;
                    case LITERAL_FLOAT:
                        if (lexer.isEnabled(Feature.UseBigDecimal)) {
                            value = lexer.decimalValue(true);
                        } else {
                            value = lexer.decimalValue(false);
                        }
                        lexer.addbuilerValue(String.valueOf(value));
                        lexer.nextToken(XMLToken.COMMA);
                        break;
                    case LITERAL_STRING:
                        String stringLiteral = lexer.stringVal();

                        if (lexer.isEnabled(Feature.AllowISO8601DateFormat)) {
                            XMLScanner iso8601Lexer = new XMLScanner(stringLiteral);
                            if (iso8601Lexer.scanISO8601DateIfMatch()) {
                                value = iso8601Lexer.getCalendar().getTime();
                            } else {
                                value = stringLiteral;
                            }
                            iso8601Lexer.close();
                        } else {
                            value = stringLiteral;
                        }

                        lexer.addbuilerValue(String.valueOf(value));
                        lexer.nextToken(XMLToken.COMMA);
                        break;
                    case TRUE:
                        value = Boolean.TRUE;
                        lexer.addbuilerValue("true");
                        lexer.nextToken(XMLToken.COMMA);
                        break;
                    case FALSE:
                        value = Boolean.FALSE;
                        lexer.addbuilerValue("false");
                        lexer.nextToken(XMLToken.COMMA);
                        break;
//                    case LBRACE:
//                        JSONObject object = new JSONObject();
//                        parseObject();
//                        break;
//                    case LBRACKET:
//                        Collection items = new JSONArray();
//                        parseArray(items, fieldName);
//                        value = items;
//                        break;
                    case NULL:
                        value = null;
                        lexer.addbuilerValue("null");
                        lexer.nextToken(XMLToken.LITERAL_STRING);
                        break;
                    case RBRACKET:
                        lexer.nextToken(XMLToken.COMMA);
                        return;
                    case EOF:
                        throw new XMLException("unclosed jsonArray");
                    default:
                        parse();
                        break;
                }

                if (lexer.token() == XMLToken.COMMA) {
                    lexer.nextToken(XMLToken.LBRACE);
                    continue;
                }
            }
        } catch (Exception e){
        	e.printStackTrace();
        }
    }

    public Object[] parseArray(Type[] types) {
        if (lexer.token() == XMLToken.NULL) {
            lexer.nextToken(XMLToken.COMMA);
            return null;
        }

        if (lexer.token() != XMLToken.LBRACKET) {
            throw new XMLException("syntax error : " + lexer.tokenName());
        }

        Object[] list = new Object[types.length];
        if (types.length == 0) {
            lexer.nextToken(XMLToken.RBRACKET);

            if (lexer.token() != XMLToken.RBRACKET) {
                throw new XMLException("syntax error");
            }

            lexer.nextToken(XMLToken.COMMA);
            return new Object[0];
        }

        lexer.nextToken(XMLToken.LITERAL_INT);

        for (int i = 0; i < types.length; ++i) {
            Object value;

            if (lexer.token() == XMLToken.NULL) {
                value = null;
                lexer.nextToken(XMLToken.COMMA);
            } else {
                Type type = types[i];
                if (type == int.class || type == Integer.class) {
                    if (lexer.token() == XMLToken.LITERAL_INT) {
                        value = Integer.valueOf(lexer.intValue());
                        lexer.nextToken(XMLToken.COMMA);
                    } else {
                        value = this.parse();
                        value = TypeUtils.cast(value, type, config);
                    }
                } else if (type == String.class) {
                    if (lexer.token() == XMLToken.LITERAL_STRING) {
                        value = lexer.stringVal();
                        lexer.nextToken(XMLToken.COMMA);
                    } else {
                        value = this.parse();
                        value = TypeUtils.cast(value, type, config);
                    }
                } else {
                    boolean isArray = false;
                    Class<?> componentType = null;
                    if (i == types.length - 1) {
                        if (type instanceof Class) {
                            Class<?> clazz = (Class<?>) type;
                            isArray = clazz.isArray();
                            componentType = clazz.getComponentType();
                        }
                    }

                    // support varArgs
                    if (isArray && lexer.token() != XMLToken.LBRACKET) {
                        List<Object> varList = new ArrayList<Object>();

                        ObjectDeserializer derializer = config.getDeserializer(componentType);
                        int fastMatch = derializer.getFastMatchToken();

                        if (lexer.token() != XMLToken.RBRACKET) {
                            for (;;) {
                                Object item = derializer.deserialze(this, type, null);
                                varList.add(item);

                                if (lexer.token() == XMLToken.COMMA) {
                                    lexer.nextToken(fastMatch);
                                } else if (lexer.token() == XMLToken.RBRACKET) {
                                    break;
                                } else {
                                    throw new XMLException("syntax error :" + XMLToken.name(lexer.token()));
                                }
                            }
                        }

                        value = TypeUtils.cast(varList, type, config);
                    } else {
                        ObjectDeserializer derializer = config.getDeserializer(type);
                        value = derializer.deserialze(this, type, null);
                    }
                }
            }
            list[i] = value;

            if (lexer.token() == XMLToken.RBRACKET) {
                break;
            }

            if (lexer.token() != XMLToken.COMMA) {
                throw new XMLException("syntax error :" + XMLToken.name(lexer.token()));
            }

            if (i == types.length - 1) {
                lexer.nextToken(XMLToken.RBRACKET);
            } else {
                lexer.nextToken(XMLToken.LITERAL_INT);
            }
        }

        if (lexer.token() != XMLToken.RBRACKET) {
            throw new XMLException("syntax error");
        }

        lexer.nextToken(XMLToken.COMMA);

        return list;
    }

    public <T> T parseObject(Type type) {
        if (lexer.token() == XMLToken.NULL) {
            lexer.nextToken();
            return null;
        }

        ObjectDeserializer derializer = config.getDeserializer(type);

        try {
            return (T) derializer.deserialze(this, type, null);
        } catch (XMLException e) {
            throw e;
        } catch (Throwable e) {
            throw new XMLException(e.getMessage(), e);
        }
    }
    
    public final void parseObject() {
        final XMLLexer lexer = this.lexer;

        if (lexer.token() != XMLToken.LBRACE && lexer.token() != XMLToken.COMMA) {
            throw new XMLException("syntax error, expect {, actual " + lexer.tokenName());
        }

        try {
            for (;;) {
                lexer.skipWhitespace();
                char ch = lexer.getCurrent();
                if (isEnabled(Feature.AllowArbitraryCommas)) {
                	
                	if(ch == ',')lexer.addbuiler(ch);
                    while (ch == ',') {
                        lexer.next();
                        lexer.skipWhitespace();
                        ch = lexer.getCurrent();
                        if(ch == ',')lexer.addbuiler(ch);
                    }
                }

                boolean isObjectKey = false;
                Object key = null;
                if (ch == '"') {
                	lexer.addbuiler(ch);
                    key = lexer.scanSymbol(symbolTable, '"');
                    lexer.addbuilerKey(String.valueOf(key));
                	lexer.addbuiler(ch);
                    lexer.skipWhitespace();
                    ch = lexer.getCurrent();
                    if (ch != ':') {
                        throw new XMLException("expect ':' at " + lexer.pos() + ", name " + key);
                    }
                } else if (ch == '}') {
                	lexer.addbuiler(ch);
                    lexer.next();
                    lexer.resetStringPosition();
                    lexer.nextToken();
                    return ;
                } else if (ch == '\'') {
                	lexer.addbuiler(ch);
                    if (!isEnabled(Feature.AllowSingleQuotes)) {
                        throw new XMLException("syntax error");
                    }

                    key = lexer.scanSymbol(symbolTable, '\'');
                    lexer.addbuilerKey(String.valueOf(key));
                	lexer.addbuiler(ch);
                    lexer.skipWhitespace();
                    ch = lexer.getCurrent();
                    if (ch != ':') {
                        throw new XMLException("expect ':' at " + lexer.pos());
                    }
                } else if (ch == EOI) {
                    throw new XMLException("syntax error");
                } else if (ch == ',') {
                    throw new XMLException("syntax error");
                } else if ((ch >= '0' && ch <= '9') || ch == '-') {
                    lexer.resetStringPosition();
                    lexer.scanNumber();
                    if (lexer.token() == XMLToken.LITERAL_INT) {
                        key = lexer.integerValue();
                    } else {
                        key = lexer.decimalValue(true);
                    }
                    lexer.addbuilerKey(String.valueOf(key));
                    ch = lexer.getCurrent();
                    if (ch != ':') {
                        throw new XMLException("expect ':' at " + lexer.pos() + ", name " + key);
                    }
                } else if (ch == '{' || ch == '[') {
                	lexer.addbuiler(ch);
                    lexer.nextToken();
                    parse();
                    isObjectKey = true;
                } else {
                    if (!isEnabled(Feature.AllowUnQuotedFieldNames)) {
                        throw new XMLException("syntax error");
                    }

                    key = lexer.scanSymbolUnQuoted(symbolTable);
                    lexer.addbuilerKey(String.valueOf(key));
                    lexer.skipWhitespace();
                    ch = lexer.getCurrent();
                    if (ch != ':') {
                        throw new XMLException("expect ':' at " + lexer.pos() + ", actual " + ch);
                    }
                }

                lexer.addbuilerCH(":");
                if (!isObjectKey) {
                    lexer.next();
                    lexer.skipWhitespace();
                }

                ch = lexer.getCurrent();

                lexer.resetStringPosition();

                Object value;
                if (ch == '"') {
                	lexer.addbuiler(ch);
                    lexer.scanString();
                    String strValue = lexer.stringVal();
                    value = strValue;

                    if (lexer.isEnabled(Feature.AllowISO8601DateFormat)) {
                        XMLScanner iso8601Lexer = new XMLScanner(strValue);
                        if (iso8601Lexer.scanISO8601DateIfMatch()) {
                            value = iso8601Lexer.getCalendar().getTime();
                        }
                        iso8601Lexer.close();
                    }

                    lexer.addbuilerValue(String.valueOf(value));
                	lexer.addbuiler(ch);
                } else if (ch >= '0' && ch <= '9' || ch == '-') {
                    lexer.scanNumber();
                    if (lexer.token() == XMLToken.LITERAL_INT) {
                        value = lexer.integerValue();
                    } else {
                        value = lexer.numberValue();
                    }

                    lexer.addbuilerValue(String.valueOf(value));
                } else if (ch == '[') { // 减少嵌套，兼容android
                	lexer.addbuiler(ch);
                    lexer.nextToken();
//                    JSONArray list = new JSONArray();
//                    this.parseArray();
//                    value = list;
                    
                    if (lexer.token() == XMLToken.RBRACE) {
                        lexer.nextToken();
                        return ;
                    } else if (lexer.token() == XMLToken.COMMA) {
                        continue;
                    } else {
                        throw new XMLException("syntax error");
                    }
                } else if (ch == '{') { // 减少嵌套，兼容android
                	lexer.addbuiler(ch);
                    lexer.nextToken();
                    this.parseObject();

                    if (lexer.token() == XMLToken.RBRACE) {
                        lexer.nextToken();
                        return ;
                    } else if (lexer.token() == XMLToken.COMMA) {
                        continue;
                    } else {
                        throw new XMLException("syntax error, " + lexer.tokenName());
                    }
                } else {
                    lexer.nextToken();
                    parse();

                    if (lexer.token() == XMLToken.RBRACE) {
                        lexer.nextToken();
                        return ;
                    } else if (lexer.token() == XMLToken.COMMA) {
                        continue;
                    } else {
                        throw new XMLException("syntax error, position at " + lexer.pos() + ", name " + String.valueOf(key));
                    }
                }

                lexer.skipWhitespace();
                ch = lexer.getCurrent();
                if (ch == ',') {
                	lexer.addbuiler(ch);
                    lexer.next();
                    continue;
                } else if (ch == '}') {
                	lexer.addbuiler(ch);
                    lexer.next();
                    lexer.resetStringPosition();
                    lexer.nextToken();

                    return ;
                } else {
                    throw new XMLException("syntax error, position at " + lexer.pos() + ", name " + String.valueOf(key));
                }

            }
        } catch(Exception e){
        	e.printStackTrace();
        }
        return ;
    }

    public Object parse() {
        return parse(null);
    }

    public Object parse(Object fieldName) {
        final XMLLexer lexer = getLexer();
        switch (lexer.token()) {
            case SET:
                lexer.nextToken();
                HashSet<Object> set = new HashSet<Object>();
                parseArray(set, fieldName);
                return set;
            case TREE_SET:
                lexer.nextToken();
                TreeSet<Object> treeSet = new TreeSet<Object>();
                parseArray(treeSet, fieldName);
                return treeSet;
            case LITERAL_INT:
                Number intValue = lexer.integerValue();
                lexer.addbuilerValue(intValue.longValue()+"");
                lexer.nextToken();
                return intValue;
            case LITERAL_FLOAT:
            	Object value = lexer.numberString();
            	lexer.addbuilerValue(value.toString());
                lexer.nextToken();
                return value;
            case LITERAL_STRING:
                String strValue = lexer.stringVal();
                lexer.nextToken(XMLToken.COMMA);

                if (lexer.isEnabled(Feature.AllowISO8601DateFormat)) {
                    XMLScanner iso8601Lexer = new XMLScanner(strValue);
                    try {
                        if (iso8601Lexer.scanISO8601DateIfMatch()) {
                        	strValue = iso8601Lexer.getCalendar().getTime().toString();
                        	lexer.addbuilerValue(strValue);
                            return strValue;
                        }
                    } finally {
                        iso8601Lexer.close();
                    }
                }
                lexer.addbuilerValue(strValue);
                return strValue;
            case NULL:
                lexer.addbuilerValue("null");
                lexer.nextToken();
                return null;
            case TRUE:
            	lexer.addbuilerValue("true");
                lexer.nextToken();
                return Boolean.TRUE;
            case FALSE:
            	lexer.addbuilerValue("false");
                lexer.nextToken();
                return Boolean.FALSE;
            case NEW:
                lexer.nextToken(XMLToken.IDENTIFIER);

                if (lexer.token() != XMLToken.IDENTIFIER) {
                    throw new XMLException("syntax error");
                }
                lexer.nextToken(XMLToken.LPAREN);

                accept(XMLToken.LPAREN);
                long time = ((Number) lexer.integerValue()).longValue();
                lexer.addbuilerValue(new Date(time).toString());
                accept(XMLToken.LITERAL_INT);

                accept(XMLToken.RPAREN);

                return new Date(time);
            case EOF:
                if (lexer.isBlankInput()) {
                    return null;
                }
                throw new XMLException("unterminated json string, pos " + lexer.getBufferPosition());
            case ERROR:
            default:
                throw new XMLException("syntax error, pos " + lexer.getBufferPosition());
        }
    }

    public final void accept(final int token) {
        final XMLLexer lexer = getLexer();
        if (lexer.token() == token) {
            lexer.nextToken();
        } else {
            throw new XMLException("syntax error, expect " + XMLToken.name(token) + ", actual "
                                    + XMLToken.name(lexer.token()));
        }
    }
    

    public void close() {
        final XMLLexer lexer = getLexer();

        try {
//            if (isEnabled(Feature.AutoCloseSource)) {
//                if (lexer.token() != XMLToken.EOF) {
//                    throw new XMLException("not close json text, token : " + XMLToken.name(lexer.token()));
//                }
//            }
        } finally {
            lexer.close();
        }
    }
    
    public static class ResolveTask {

        private final ParseContext context;
        private final String       referenceValue;
        private FieldDeserializer  fieldDeserializer;
        private ParseContext       ownerContext;

        public ResolveTask(ParseContext context, String referenceValue){
            this.context = context;
            this.referenceValue = referenceValue;
        }

        public ParseContext getContext() {
            return context;
        }

        public String getReferenceValue() {
            return referenceValue;
        }

        public FieldDeserializer getFieldDeserializer() {
            return fieldDeserializer;
        }

        public void setFieldDeserializer(FieldDeserializer fieldDeserializer) {
            this.fieldDeserializer = fieldDeserializer;
        }

        public ParseContext getOwnerContext() {
            return ownerContext;
        }

        public void setOwnerContext(ParseContext ownerContext) {
            this.ownerContext = ownerContext;
        }

    }
}
