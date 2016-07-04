package com.fyk.fastxml.parser.deserializer;

import com.fyk.fastxml.parser.DefaultPrXMLParser;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;

public class JavaXmlDeserializer {

	public String deserialze(DefaultPrXMLParser parser) {
		XMLLexer lexer = parser.getLexer(); // xxx

//		if (lexer.token() == XMLToken.NULL) {
//			lexer.nextToken(XMLToken.COMMA);
//			return null;
//		}

		try {
			lexer.nextToken(XMLToken.LBRACE_XML); // prime the pump
			 
			lexer.skipXmlVersion();
            lexer.skipWhitespace();
			
			parser.parseXML(null);
			lexer.addbuiler('}');
			
			
			return "";
		} finally {
		}
	}

	public boolean parseField(DefaultXMLParser parser) {
		XMLLexer lexer = parser.getLexer(); // xxx

		
		ValueFieldDeserializer fieldDeserializer=new ValueFieldDeserializer();
		
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
