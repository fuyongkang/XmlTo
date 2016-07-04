package com.fyk.fastxml.parser;

import java.lang.reflect.Type;

/** 
 * 
 * @ClassName DefaultXMLParser 
 * @author fuyongkang
 * @date 2016-5-6 下午3:57:03 
 */
public class DefaultXMLParser {

    protected final Object             input;
    protected final XMLLexer          lexer;

    protected ParserConfig             config;
    protected final SymbolTable        symbolTable;
    
    public DefaultXMLParser(final String input, final XMLLexer lexer, final ParserConfig config){
        this.lexer = lexer;
        this.input = input;
        this.config = config;
        this.symbolTable = config.getSymbolTable();

        lexer.nextToken(XMLToken.LBRACE); // prime the pump
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
    
    public String parseObject(Type type) {
        if (lexer.token() == XMLToken.NULL) {
            lexer.nextToken();
            return null;
        }


        try {
            return "";
        } catch (Exception e) {
            throw e;
        } 
    }
}
