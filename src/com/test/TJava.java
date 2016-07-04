package com.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.fyk.fastxml.XML;
import com.test.bean.UniversityBean;

/** 
 * 
 */
public class TJava {

	public static void main(String[] args) {
		
//		String text=readFileByLines("src/com/test/test.xml");
		String text=readFileByLines("src/com/test/1test.xml");
		System.out.println(text);
		System.out.println("===================");
		
		
//		DefaultPrXMLParser parser=new DefaultPrXMLParser(text, new XMLScanner(text, 0), ParserConfig.getGlobalInstance());
//		JavaXmlDeserializer jxd=new JavaXmlDeserializer();
// 		jxd.deserialze(parser);
//		System.out.println(parser.getLexer().getBuilerString());
 		
		
		UniversityBean ub = XML.parseObject(text, UniversityBean.class);
		System.out.println(ub.toString());
		
		
//		System.out.println(readFileByLines("src/com/test/test.xml"));
		
	}
	
	  /** 
     * 以行为单位读取文件，常用于读面向行的格式化文件 
     */  
    public static String readFileByLines(String fileName) {  
        File file = new File(fileName);  
        BufferedReader reader = null;  
        StringBuilder stringb = new StringBuilder();
        try {  
            reader = new BufferedReader(new FileReader(file));  
            String tempString = null;  

            while ((tempString = reader.readLine()) != null) {  
                stringb.append(tempString);
            }  
            reader.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            if (reader != null) {  
                try {  
                    reader.close();  
                } catch (IOException e1) {  
                }  
            }  
        }  
        return stringb.toString();
    }  

}
