package com.fyk.fastxml.parser;

import static com.alibaba.fastjson.parser.JSONLexer.EOI;
import static com.fyk.fastxml.parser.XMLToken.EOF;
import static com.fyk.fastxml.parser.XMLToken.ERROR;
import static com.fyk.fastxml.parser.XMLToken.FALSE;
import static com.fyk.fastxml.parser.XMLToken.LBRACE;
import static com.fyk.fastxml.parser.XMLToken.LBRACKET;
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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.TreeSet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;


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
    
    public DefaultXMLParser(final String input, final XMLLexer lexer, final ParserConfig config){
        this.lexer = lexer;
        this.input = input;
        this.config = config;
        this.symbolTable = config.getSymbolTable();

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

    public final void parseArray() {
        final XMLLexer lexer = getLexer();

        if (lexer.token() == XMLToken.SET || lexer.token() == XMLToken.TREE_SET) {
            lexer.nextToken();
        }

        if (lexer.token() != XMLToken.LBRACKET) {
            throw new JSONException("syntax error, expect [, actual " + XMLToken.name(lexer.token()) + ", pos "
                                    + lexer.pos());
        }

        lexer.nextToken(XMLToken.LITERAL_STRING);

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
                    case LBRACE:
                        JSONObject object = new JSONObject();
                        parseObject();
                        break;
                    case LBRACKET:
                        Collection items = new JSONArray();
                        parseArray();
                        value = items;
                        break;
                    case NULL:
                        value = null;
                        lexer.addbuilerValue("null");
                        lexer.nextToken(XMLToken.LITERAL_STRING);
                        break;
                    case RBRACKET:
                        lexer.nextToken(XMLToken.COMMA);
                        return;
                    case EOF:
                        throw new JSONException("unclosed jsonArray");
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
    
    public final void parseObject() {
        final XMLLexer lexer = this.lexer;

        if (lexer.token() != XMLToken.LBRACE && lexer.token() != XMLToken.COMMA) {
            throw new JSONException("syntax error, expect {, actual " + lexer.tokenName());
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
                        throw new JSONException("expect ':' at " + lexer.pos() + ", name " + key);
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
                        throw new JSONException("syntax error");
                    }

                    key = lexer.scanSymbol(symbolTable, '\'');
                    lexer.addbuilerKey(String.valueOf(key));
                	lexer.addbuiler(ch);
                    lexer.skipWhitespace();
                    ch = lexer.getCurrent();
                    if (ch != ':') {
                        throw new JSONException("expect ':' at " + lexer.pos());
                    }
                } else if (ch == EOI) {
                    throw new JSONException("syntax error");
                } else if (ch == ',') {
                    throw new JSONException("syntax error");
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
                        throw new JSONException("expect ':' at " + lexer.pos() + ", name " + key);
                    }
                } else if (ch == '{' || ch == '[') {
                	lexer.addbuiler(ch);
                    lexer.nextToken();
                    parse();
                    isObjectKey = true;
                } else {
                    if (!isEnabled(Feature.AllowUnQuotedFieldNames)) {
                        throw new JSONException("syntax error");
                    }

                    key = lexer.scanSymbolUnQuoted(symbolTable);
                    lexer.addbuilerKey(String.valueOf(key));
                    lexer.skipWhitespace();
                    ch = lexer.getCurrent();
                    if (ch != ':') {
                        throw new JSONException("expect ':' at " + lexer.pos() + ", actual " + ch);
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
                    JSONArray list = new JSONArray();
                    this.parseArray();
                    value = list;
                    
                    if (lexer.token() == XMLToken.RBRACE) {
                        lexer.nextToken();
                        return ;
                    } else if (lexer.token() == XMLToken.COMMA) {
                        continue;
                    } else {
                        throw new JSONException("syntax error");
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
                        throw new JSONException("syntax error, " + lexer.tokenName());
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
                        throw new JSONException("syntax error, position at " + lexer.pos() + ", name " + String.valueOf(key));
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
                    throw new JSONException("syntax error, position at " + lexer.pos() + ", name " + String.valueOf(key));
                }

            }
        } catch(Exception e){
        	e.printStackTrace();
        }
        return ;
    }

    public void parse() {
         parse(null);
    }

    public void parse(Object fieldName) {
        final XMLLexer lexer = getLexer();
        switch (lexer.token()) {
            case SET:
                lexer.nextToken();
                HashSet<Object> set = new HashSet<Object>();
                parseArray();
                return ;
            case TREE_SET:
                lexer.nextToken();
                TreeSet<Object> treeSet = new TreeSet<Object>();
                parseArray();
                return ;
            case LBRACKET:
                JSONArray array = new JSONArray();
                parseArray();
                return ;
            case LBRACE:
                JSONObject object = new JSONObject();
                 parseObject();
                return;
            case LITERAL_INT:
                Number intValue = lexer.integerValue();
                lexer.addbuilerValue(intValue.longValue()+"");
                lexer.nextToken();
                return ;
            case LITERAL_FLOAT:
            	lexer.addbuilerValue(lexer.numberString());
//                Object value = lexer.decimalValue(isEnabled(Feature.UseBigDecimal));
                lexer.nextToken();
                return ;
            case LITERAL_STRING:
                String value = lexer.stringVal();
                lexer.nextToken(XMLToken.COMMA);

                if (lexer.isEnabled(Feature.AllowISO8601DateFormat)) {
                    XMLScanner iso8601Lexer = new XMLScanner(value);
                    try {
                        if (iso8601Lexer.scanISO8601DateIfMatch()) {
                        	lexer.addbuilerValue(iso8601Lexer.getCalendar().getTime().toString());
                            return ;
                        }
                    } finally {
                        iso8601Lexer.close();
                    }
                }
                lexer.addbuilerValue(value);
                return ;
            case NULL:
                lexer.addbuilerValue("null");
                lexer.nextToken();
                return ;
            case TRUE:
            	lexer.addbuilerValue("true");
                lexer.nextToken();
                return ;
            case FALSE:
            	lexer.addbuilerValue("false");
                lexer.nextToken();
                return ;
            case NEW:
                lexer.nextToken(XMLToken.IDENTIFIER);

                if (lexer.token() != XMLToken.IDENTIFIER) {
                    throw new JSONException("syntax error");
                }
                lexer.nextToken(XMLToken.LPAREN);

                accept(XMLToken.LPAREN);
                long time = ((Number) lexer.integerValue()).longValue();
                lexer.addbuilerValue(new Date(time).toString());
                accept(XMLToken.LITERAL_INT);

                accept(XMLToken.RPAREN);

                return ;
            case EOF:
                if (lexer.isBlankInput()) {
                    return ;
                }
                throw new JSONException("unterminated json string, pos " + lexer.getBufferPosition());
            case ERROR:
            default:
                throw new JSONException("syntax error, pos " + lexer.getBufferPosition());
        }
    }

    public final void accept(final int token) {
        final XMLLexer lexer = getLexer();
        if (lexer.token() == token) {
            lexer.nextToken();
        } else {
            throw new JSONException("syntax error, expect " + XMLToken.name(token) + ", actual "
                                    + XMLToken.name(lexer.token()));
        }
    }
    

    public void close() {
        final XMLLexer lexer = getLexer();

        try {
            if (isEnabled(Feature.AutoCloseSource)) {
                if (lexer.token() != XMLToken.EOF) {
                    throw new JSONException("not close json text, token : " + XMLToken.name(lexer.token()));
                }
            }
        } finally {
            lexer.close();
        }
    }
}
