package com.fyk.fastxml.parser.deserializer;

import com.alibaba.fastjson.util.TypeUtils;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;

public class ValueFieldDeserializer {
	
    public void parseField(DefaultXMLParser parser) {
    	final XMLLexer lexer = parser.getLexer();
        
        if (lexer.token() == XMLToken.LITERAL_STRING) {
        	String val = lexer.stringVal();
        	if(lexer.identifier!=0)lexer.addbuiler(lexer.identifier);
        	lexer.addbuilerValue(val);
        	if(lexer.identifier!=0)lexer.addbuiler(lexer.identifier);
        	lexer.identifier=0;
            lexer.nextToken(XMLToken.COMMA);
            
        }else if (lexer.token() == XMLToken.LITERAL_INT) {
//        	 int val = lexer.intValue();
        	 String val = lexer.numberString();
//        	 Number val = lexer.integerValue();
        	 lexer.addbuilerValue(val);
        	 lexer.nextToken(XMLToken.COMMA);
        	 
        }else if (lexer.token() == XMLToken.LITERAL_FLOAT) {
            float val = lexer.floatValue();
       	 	lexer.addbuilerValue(val+"");
            lexer.nextToken(XMLToken.COMMA);
            
        }else if (lexer.token() == XMLToken.LBRACE) {//类
        	  parser.parse();
        	 
        }else if (lexer.token() == XMLToken.LBRACKET) {//集合
        	 parser.parse();

        }else if (lexer.token() == XMLToken.NULL) {
        	lexer.addbuilerValue("null");
        	lexer.nextToken(XMLToken.COMMA);
        	
        }else{
        	 parser.parse();
        	 
        }
        
        
    }

    public int getFastMatchToken() {
        return XMLToken.LITERAL_STRING;
    }
}
