package com.fyk.fastxml.parser.deserializer.aware;

import static com.fyk.fastxml.parser.XMLLexer.EOI;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.fyk.fastxml.XMLException;
import com.fyk.fastxml.XMLObject;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.Feature;
import com.fyk.fastxml.parser.ParseContext;
import com.fyk.fastxml.parser.ParserConfig;
import com.fyk.fastxml.parser.XMLLexer;
import com.fyk.fastxml.parser.XMLToken;
import com.fyk.fastxml.util.DeserializeBeanInfo;
import com.fyk.fastxml.util.FieldInfo;

public class JavaBeanDeserializer implements ObjectDeserializer {
	private final String LABLE_VALUE="item";

	private final Map<String, FieldDeserializer> feildDeserializerMap = new IdentityHashMap<String, FieldDeserializer>();

	private final List<FieldDeserializer> fieldDeserializers = new ArrayList<FieldDeserializer>();

	private final Class<?> clazz;

	private DeserializeBeanInfo beanInfo;

	public JavaBeanDeserializer(ParserConfig config, Class<?> clazz) {
		this(config, clazz, clazz);
	}

	public JavaBeanDeserializer(ParserConfig config, Class<?> clazz, Type type) {
		this.clazz = clazz;

		beanInfo = DeserializeBeanInfo.computeSetters(clazz, type);

		for (FieldInfo fieldInfo : beanInfo.getFieldList()) {
			addFieldDeserializer(config, clazz, fieldInfo);
		}
	}

	public Map<String, FieldDeserializer> getFieldDeserializerMap() {
		return feildDeserializerMap;
	}

	private void addFieldDeserializer(ParserConfig mapping, Class<?> clazz,
			FieldInfo fieldInfo) {
		FieldDeserializer fieldDeserializer = createFieldDeserializer(mapping,
				clazz, fieldInfo);

		feildDeserializerMap.put(fieldInfo.getName().intern(),
				fieldDeserializer);
		fieldDeserializers.add(fieldDeserializer);
	}

	public FieldDeserializer createFieldDeserializer(ParserConfig mapping,
			Class<?> clazz, FieldInfo fieldInfo) {
		return mapping.createFieldDeserializer(mapping, clazz, fieldInfo);
	}

	public Object createInstance(DefaultXMLParser parser, Type type) {
		if (type instanceof Class) {
			if (clazz.isInterface()) {
				Class<?> clazz = (Class<?>) type;
				ClassLoader loader = Thread.currentThread()
						.getContextClassLoader();
				final XMLObject obj = new XMLObject();
				Object proxy = Proxy.newProxyInstance(loader,
						new Class<?>[] { clazz }, obj);
				return proxy;
			}
		}

		if (beanInfo.getDefaultConstructor() == null) {
			return null;
		}

		Object object;
		try {
			Constructor<?> constructor = beanInfo.getDefaultConstructor();
			if (constructor.getParameterTypes().length == 0) {
				object = constructor.newInstance();
			} else {
				object = constructor.newInstance(parser.getContext()
						.getObject());
			}
		} catch (Exception e) {
			throw new XMLException("create instance error, class "
					+ clazz.getName(), e);
		}

		if (parser.isEnabled(Feature.InitStringFieldAsEmpty)) {
			for (FieldInfo fieldInfo : beanInfo.getFieldList()) {
				if (fieldInfo.getFieldClass() == String.class) {
					try {
						fieldInfo.set(object, "");
					} catch (Exception e) {
						throw new XMLException("create instance error, class "
								+ clazz.getName(), e);
					}
				}
			}
		}

		return object;
	}

	public <T> T deserialze(DefaultXMLParser parser, Type type, Object fieldName) {
		return deserialze(parser, type, fieldName, null);
	}


    /**
     * 取当前标签的所有属性
     */
    public final void parseLabel(DefaultXMLParser parser, Type type, Object fieldName, Object object, Map<String, Object> fieldValues){
    	
    	final XMLLexer lexer = parser.getLexer();;
    	try {
            for (;;) {
                lexer.skipWhitespace();
                
                String key=null;
                
                char ch = lexer.getCurrent();
                if (ch == '"') {
                	lexer.addbuiler(ch);
                    key = lexer.scanSymbol(parser.getSymbolTable(), '"');
                    lexer.addbuilerKey(String.valueOf(key));
                	lexer.addbuiler(ch);
                    lexer.skipWhitespace();
                    ch = lexer.getCurrent();
                    if (ch != '=') {
                        throw new XMLException("expect ':' at " + lexer.pos() + ", name " + key);
                    }
                } else if (ch == EOI) {
                    throw new XMLException("syntax error");
                }else {
              	  key = lexer.scanSymbolUnQuoted(parser.getSymbolTable());
                    lexer.addbuilerKey(String.valueOf(key));
                    lexer.skipWhitespace();
                    ch = lexer.getCurrent();
                    if (ch != '=') {
                        throw new XMLException("expect ':' at " + lexer.pos() + ", actual " + ch);
                    }
                }
                
                
//                lexer.addbuilerCH(":");
//                lexer.next();
//                lexer.skipWhitespace();
                
                if (key == null) {
					if (lexer.token() == XMLToken.RENDING_XML) {
						break;
					}
				}


				boolean match = parseFieldXml(parser, key, object, type, fieldValues);
				if (!match) {
					if (lexer.token() == XMLToken.RENDING_XML) {
						lexer.nextToken();
						break;
					}

					continue;
				}
                
				
				if (lexer.token() == XMLToken.COMMA) {
					lexer.addbuilerCH(",");
					continue;
				}else if (lexer.token() == XMLToken.RBRACE_XML) {
//                	lexer.token=XMLToken.RBRACE_XML;
                	lexer.identifier=',';
					break;
				}else if (lexer.token() == XMLToken.RENDING_XML) {
                	lexer.nextWindup();
//                	lexer.token=XMLToken.RENDING_XML;
					break;
				}

            }
        } catch(Exception e){
        	e.printStackTrace();
        }
        return ;
    }
    
    /**
     * 取标签值
     */
    public final void parseValue(DefaultXMLParser parser, Type type, Object fieldName, Object object, Map<String, Object> fieldValues){
    	final XMLLexer lexer = parser.getLexer();;
    	try {
            for (;;) {
            	if(lexer.getCurrent() == '<'){
            		lexer.nextToken(XMLToken.LBRACE_XML);
            	}

            	String key = lexer.scanSymbol(parser.getSymbolTable());
            	if (key == null) {
                    if (lexer.token() == XMLToken.RENDING_XML) {
                        break;
                    }
                    if (lexer.token() == XMLToken.COMMA) {
                        if (parser.isEnabled(Feature.AllowArbitraryCommas)) {
                            continue;
                        }
                    }
                }
            	
            	boolean match = parseFieldXml(parser, key, object, type, fieldValues);
            	if (!match) {
					if (lexer.token() == XMLToken.RENDING_XML) {
						lexer.nextToken();
						break;
					}
					continue;
				}
            	
            	
            	if(lexer.token() == XMLToken.IDENTIFIER){
            		 lexer.addbuiler(',');
 	      			 continue;
            	}
            	 /*
  	      	   * 标签结束
  	      	   */
  	            if(lexer.token()==XMLToken.RENDING_XML){//标签结束
  	          	  lexer.addbuiler('}');
  	      		  lexer.skipWhitespace();
  	      		  
      			  break;
  	      	  }
            	
            }
        } catch(Exception e){
        	e.printStackTrace();
        }
        return ;
    }
    
	public <T> T deserialze(DefaultXMLParser parser, Type type,
			Object fieldName, Object object) {
		XMLLexer lexer = parser.getLexer(); // xxx

		ParseContext context = parser.getContext();
		if (object != null) {
			context = context.getParentContext();
		}
		ParseContext childContext = null;

		try {
			Map<String, Object> fieldValues = null;

			if (lexer.token() == XMLToken.RBRACE) {
				if (object == null) {
					object = createInstance(parser, type);
				}
				return (T) object;
			}

			if (lexer.token() != XMLToken.LBRACE_XML && lexer.token() != XMLToken.IDENTIFIER
					&& lexer.token() != XMLToken.COMMA) {
				StringBuffer buf = (new StringBuffer()) //
						.append("syntax error, expect <, actual ") //
						.append(lexer.tokenName()) //
						.append(", pos ") //
						.append(lexer.pos()) //
				;
				if (fieldName instanceof String) {
					buf //
					.append(", fieldName ") //
							.append(fieldName);
				}
				throw new XMLException(buf.toString());
			}

			if (parser.getResolveStatus() == DefaultXMLParser.TypeNameRedirect) {
				parser.setResolveStatus(DefaultXMLParser.NONE);
			}

			//跳过标签名称
			
			for (;;) {
				if (object == null && fieldValues == null) {
					object = createInstance(parser, type);
					if (object == null) {
						fieldValues = new HashMap<String, Object>(
								this.fieldDeserializers.size());
					}
					childContext = parser
							.setContext(context, object, fieldName);
				}

				/*
                 * 取标签属性
                 */
				parseLabel(parser, type, fieldName, object, fieldValues);
				
				 /*
                 * 取标签内容
                 */
				
			  boolean match=true;
          	  if(lexer.token() == XMLToken.RBRACE_XML){
        		  lexer.skipWhitespace();
        		  
        		  if(lexer.getCurrent() == '<'){
        			  if(lexer.getNext() != '/'){//标签里包含的标签 ( 标签嵌套 )
        				  
        				  if(lexer.identifier!=0){
            				  lexer.addbuiler(',');
            			  }

        				 /*
    	                 * 取标签值
    	                 */
    					  parseValue(parser, type, fieldName, object, fieldValues);
        			  }else{
        				  lexer.nextToken(XMLToken.RENDING_XML);
        			  }
        		  }else{//标签下的值

        			  if(lexer.identifier!=0){
        				  lexer.addbuiler(',');
        			  }
        			  lexer.addbuilerCH(LABLE_VALUE);
        			  lexer.addbuiler(':');

					  match = parseFieldXml(parser, LABLE_VALUE, object, type, fieldValues);
        		  }
          	  }
          	  
	          	if (!match) {
					if (lexer.token() == XMLToken.RENDING_XML) {
						lexer.nextToken();
						break;
					}
					continue;
				}
					
	           /*
	      	   * 标签结束
	      	   */
	            if(lexer.token()==XMLToken.RENDING_XML){//标签结束
	          	  lexer.addbuiler('}');
	      		  lexer.skipWhitespace();
	      		  
	      		  break;
	      	  }
            
				if (lexer.token() == XMLToken.IDENTIFIER
						|| lexer.token() == XMLToken.ERROR) {
					throw new XMLException("syntax error, unexpect token "
							+ XMLToken.name(lexer.token()));
				}
			}

			if (object == null) {
				if (fieldValues == null) {
					object = createInstance(parser, type);
					return (T) object;
				}

				List<FieldInfo> fieldInfoList = beanInfo.getFieldList();
				int size = fieldInfoList.size();
				Object[] params = new Object[size];
				for (int i = 0; i < size; ++i) {
					FieldInfo fieldInfo = fieldInfoList.get(i);
					params[i] = fieldValues.get(fieldInfo.getName());
				}

				if (beanInfo.getCreatorConstructor() != null) {
					try {
						object = beanInfo.getCreatorConstructor().newInstance(
								params);
					} catch (Exception e) {
						throw new XMLException("create instance error, "
								+ beanInfo.getCreatorConstructor()
										.toGenericString(), e);
					}
				} else if (beanInfo.getFactoryMethod() != null) {
					try {
						object = beanInfo.getFactoryMethod().invoke(null,
								params);
					} catch (Exception e) {
						throw new XMLException("create factory method error, "
								+ beanInfo.getFactoryMethod().toString(), e);
					}
				}
			}

			return (T) object;
		} finally {
			if (childContext != null) {
				childContext.setObject(object);
			}
			parser.setContext(context);
		}
	}

	public boolean parseFieldXml(DefaultXMLParser parser, String key, Object object, Type objectType, Map<String, Object> fieldValues) {
		XMLLexer lexer = parser.getLexer(); // xxx

		FieldDeserializer fieldDeserializer = feildDeserializerMap.get(key);

		if (fieldDeserializer == null) {
			for (Map.Entry<String, FieldDeserializer> entry : feildDeserializerMap.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(key)) {
					fieldDeserializer = entry.getValue();
					break;
				}
			}
		}

		if (fieldDeserializer == null) {
			if (!parser.isEnabled(Feature.IgnoreNotMatch)) {
				throw new XMLException("setter not found, class "
						+ clazz.getName() + ", property " + key);
			}

			lexer.nextTokenWithColon();
			parser.parse(); // skip

			return false;
		}

		lexer.nextTokenWithColon(fieldDeserializer.getFastMatchToken());

		fieldDeserializer.parseField(parser, object, objectType, fieldValues);

		return true;
	}

	public int getFastMatchToken() {
		return XMLToken.LBRACE;
	}

}
