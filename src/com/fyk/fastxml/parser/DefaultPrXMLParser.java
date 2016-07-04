package com.fyk.fastxml.parser;

import static com.fyk.fastxml.parser.XMLLexer.EOI;

import com.fyk.fastxml.XMLException;



/** 
 * 
 * @ClassName DefaultXMLParser 
 * @author fuyongkang
 * @date 2016-5-6 下午3:57:03 
 */
public class DefaultPrXMLParser extends DefaultXMLParser{

	private final String LABLE_VALUE="item";
	
    public DefaultPrXMLParser(final String input, final XMLLexer lexer, final ParserConfig config){
      super(input, lexer, config);
    }
    
    /**
     * 取当前标签的所有属性
     */
    public final void parseLabel(){
    	final XMLLexer lexer = this.lexer;
    	try {
            for (;;) {
                lexer.skipWhitespace();
                
                String key=null;
                
                char ch = lexer.getCurrent();
                if (ch == '"') {
                	lexer.addbuiler(ch);
                    key = lexer.scanSymbol(symbolTable, '"');
                    lexer.addbuilerKey(String.valueOf(key));
                	lexer.addbuiler(ch);
                    lexer.skipWhitespace();
                    ch = lexer.getCurrent();
                    if (ch != '=') {
                        throw new XMLException("expect ':' at " + lexer.pos() + ", name " + key);
                    }
                } else if (ch == EOI) {
                    throw new XMLException("syntax error");
                } else if ((ch >= '0' && ch <= '9') || ch == '-') {
                    lexer.resetStringPosition();
                    lexer.scanNumber();
                    if (lexer.token() == XMLToken.LITERAL_INT) {
                    	lexer.addbuilerKey(String.valueOf(lexer.integerValue()));
                    } else {
                        lexer.addbuilerKey(String.valueOf(lexer.decimalValue(true)));
                    }
                    lexer.addbuilerKey(String.valueOf(key));
                    ch = lexer.getCurrent();
                    if (ch != '=') {
                        throw new XMLException("expect ':' at " + lexer.pos() + ", name " + key);
                    }
                }else {
              	  key = lexer.scanSymbolUnQuoted(symbolTable);
                    lexer.addbuilerKey(String.valueOf(key));
                    lexer.skipWhitespace();
                    ch = lexer.getCurrent();
                    if (ch != '=') {
                        throw new XMLException("expect ':' at " + lexer.pos() + ", actual " + ch);
                    }
                }
                
                
                lexer.addbuilerCH(":");
                lexer.next();
                lexer.skipWhitespace();
                
                
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
                } else  if (ch == '\'') {
                	lexer.addbuiler('"');
                    lexer.scanStringSingleQuote();
                    String strValue = lexer.stringVal();
                    lexer.addbuilerValue(String.valueOf(strValue));
                	lexer.addbuiler('"');
                } else {
                    lexer.nextToken();
                    parse();
                }
                
                lexer.skipWhitespace();
                ch = lexer.getCurrent();
                if (ch == '>') {
                	lexer.token=XMLToken.RBRACE_XML;
                	lexer.identifier=',';
                    return;
                }else if (ch == '/') {
                	lexer.nextWindup();
                	lexer.token=XMLToken.RENDING_XML;
                	return;
                } else {
                	lexer.addbuilerCH(",");
                	continue;
                } 
                
            }
        } catch(Exception e){
        	e.printStackTrace();
        }
        return ;
    }
    
    public final void parseXML(String key){
    	 final XMLLexer lexer = this.lexer;
    	 
    	 
    	 int len=0;
    	  try {
              for (;;) {

//            	  String key=null;
                  
                /*
                 * 取标签名称
                 */
                  char ch = lexer.getCurrent();
                  if(ch == '<'){
                	  lexer.next();
                	  
                	  if(ch == '/'){
                		  lexer.nextWindup();
                		  
                	  }else{//取到标签名称
                	  
	                      key = lexer.scanSymbolUnQuoted(symbolTable);
	                      lexer.addbuilerName(key);
	                      lexer.addbuiler(':');
	                      
//	                      String temp = lexer.scanSymbolUnQuoted(symbolTable);
//                		  if(key==null || !key.equals(temp) ){
//		                      key = temp;
//		                      lexer.addbuilerName(key);
//		                      lexer.addbuiler(':');
//		                      len=lexer.getBuilerLength();
//                		  }else{
//                			 if(lexer.getBuilerCH(len) != '['){
//                				 lexer.insertBuilerCH(len, '[');
//                			 }
//                		  }
                	  }
                      
                  }

                  
                  /*
                   * 取标签属性
                   */
                  ch = lexer.getCurrent();
                  if (ch == '/') {
                	  
                	  lexer.addbuilerCH("null");
                	  lexer.nextWindup();
                	  lexer.token=XMLToken.RENDING_XML;
                  }else if (ch == '>') {
                	  lexer.token=XMLToken.RBRACE_XML;
                	  lexer.identifier=0;
                  }else {//取便签属性
                	  
                	  lexer.addbuiler('{');
                	  parseLabel();
                  }
                  

                  /*
                   * 取标签内容
                   */
            	  if(lexer.token==XMLToken.RBRACE_XML){
            		  lexer.next();
            		  lexer.skipWhitespace();
            		  
            		  if(lexer.getCurrent() == '<'){
            			  if(lexer.getNext() != '/'){//标签里包含的标签 ( 标签嵌套 )
            				  
            				  if(lexer.identifier!=0){
                				  lexer.addbuiler(',');
                			  }
            				  parseXML(key);
            			  }
            		  }else{//标签下的值
            			  
            			  if(lexer.identifier!=0){
            				  lexer.addbuiler(',');
            			  }
            			  lexer.scanLabelString();
            			  String strValue = lexer.stringVal();
            			  lexer.addbuilerCH(LABLE_VALUE);
            			  lexer.addbuiler(':');
            			  lexer.addbuiler('\"');
            			  lexer.addbuilerCH(strValue);
            			  lexer.addbuiler('\"');
            		  }
            		  
            		  lexer.nextWindup();
    				  lexer.token=XMLToken.RENDING_XML;
    				  lexer.identifier=0;
            	  }
            	  
            	  
            	  /*
            	   * 标签结束
            	   */
                  if(lexer.token==XMLToken.RENDING_XML){//标签结束
                	  lexer.addbuiler('}');
                	  
                	  lexer.next();
            		  lexer.skipWhitespace();
            		  
            		  if(lexer.getCurrent() == '<' && lexer.getNext() != '/'){
            			  lexer.addbuiler(',');
            			  continue;
            		  }else{
            			  return;
            		  }
            	  }
            	  
              }
          } catch(Exception e){
          	e.printStackTrace();
          }
          return ;
    }
    
}
