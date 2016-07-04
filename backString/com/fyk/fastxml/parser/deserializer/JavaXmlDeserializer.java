package com.fyk.fastxml.parser.deserializer;

import com.alibaba.fastjson.JSONException;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.Feature;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;

public class JavaXmlDeserializer {

	@SuppressWarnings("unchecked")
	public String deserialze(DefaultXMLParser parser) {
		XMLLexer lexer = parser.getLexer(); // xxx

		if (lexer.token() == XMLToken.NULL) {
			lexer.nextToken(XMLToken.COMMA);
			return null;
		}

		try {

			if (lexer.token() == XMLToken.RBRACE) {
				lexer.nextToken(XMLToken.COMMA);
				return "";
			}

			if (lexer.token() != XMLToken.LBRACE
					&& lexer.token() != XMLToken.COMMA) {
				StringBuffer buf = (new StringBuffer()) //
						.append("syntax error, expect {, actual ") //
						.append(lexer.tokenName()) //
						.append(", pos ") //
						.append(lexer.pos()) //
				;
				throw new JSONException(buf.toString());
			}

			for (;;) {

				String key = lexer.scanSymbol(parser.getSymbolTable());
				if(lexer.identifier!=0)lexer.addbuiler(lexer.identifier);
				lexer.addbuilerKey(key);
				if(lexer.identifier!=0)lexer.addbuiler(lexer.identifier);
				lexer.identifier=0;

				if (key == null) {
					if (lexer.token() == XMLToken.RBRACE) {
						lexer.nextToken(XMLToken.COMMA);
						break;
					}
					if (lexer.token() == XMLToken.COMMA) {
						 if (parser.isEnabled(Feature.AllowArbitraryCommas)) {
							 continue;
						 }
					}
				}

				if ("$ref" == key) {
					lexer.nextTokenWithColon(XMLToken.LITERAL_STRING);
					if (lexer.token() == XMLToken.LITERAL_STRING) {
						String ref = lexer.stringVal();
					} else {
						throw new JSONException("illegal ref, "
								+ XMLToken.name(lexer.token()));
					}

					lexer.nextToken(XMLToken.RBRACE);
					if (lexer.token() != XMLToken.RBRACE) {
						throw new JSONException("illegal ref");
					}
					lexer.nextToken(XMLToken.COMMA);

					return "";
				}

				boolean match = parseField(parser);
				 if (!match) {
					 if (lexer.token() == XMLToken.RBRACE) {
						 lexer.nextToken();
						 break;
					 }
				
					 continue;
				 }

				if (lexer.token() == XMLToken.COMMA) {
					continue;
				}

				if (lexer.token() == XMLToken.RBRACE) {
//					lexer.nextToken(XMLToken.COMMA);
					break;
				}

				if (lexer.token() == XMLToken.IDENTIFIER
						|| lexer.token() == XMLToken.ERROR) {
					throw new JSONException("syntax error, unexpect token "
							+ XMLToken.name(lexer.token()));
				}
			}

			return "";
		} finally {
		}
	}

	public boolean parseField(DefaultXMLParser parser) {
		XMLLexer lexer = parser.getLexer(); // xxx

		
		ValueFieldDeserializer fieldDeserializer=new ValueFieldDeserializer(this);
		
//		if (fieldDeserializer == null) {
//			for (Map.Entry<String, FieldDeserializer> entry : feildDeserializerMap
//					.entrySet()) {
//				if (entry.getKey().equalsIgnoreCase(key)) {
//					fieldDeserializer = entry.getValue();
//					break;
//				}
//			}
//		}


		lexer.nextTokenWithColon(XMLToken.LITERAL_STRING);

		fieldDeserializer.parseField(parser);

		return true;
	}

	public int getFastMatchToken() {
		return XMLToken.LBRACE;
	}

}
