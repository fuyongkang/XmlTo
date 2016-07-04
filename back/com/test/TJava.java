package com.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.fyk.fastxml.parser.DefaultXMLParser;
import com.fyk.fastxml.parser.ParserConfig;
import com.fyk.fastxml.parser.XMLScanner;
import com.fyk.fastxml.parser.deserializer.JavaXmlDeserializer;

/** 
 * 
 */
public class TJava {

	public static Object jsonParsObj(String text, Class<?> clazz) {
		return JSON.parseObject(text, clazz);
	}

	public static void main(String[] args) {
		String text="{\"rspCode\":2,\"rspDesc\":\"操作成功\",\"result\":{\"storeId\":294,\"storeType\":2,\"storeName\":\"me铺\",\"storeNameEn\":\"meenglishname\",\"regionId\":24,\"storeAddress\":\"中国北京市朝阳区竞园23竞报\",\"storeLon\":116.505692,\"storeLat\":39.893453,\"storeLogo\":\"http://server.daimanman.cn:8888/IDaiShuPic/dmm_stroe/294/store_logo.jpg\",\"storeHours\":\"09:30-22:00\",\"storeDesc\":\"哈哈哈哈哈哈哈\",\"storeTel\":\"876543216\",\"storeWifi\":null,\"storePayment\":\"[ 1, 2]\",\"storeIsthird\":null,\"companyType\":\"[0,1]\",\"storeDescCn\":null,\"storeDescEn\":null,\"storeDescJp\":null,\"storeDescKo\":null,\"isOrder\":\"1\",\"descStatus\":0,\"saleCoupon\":null}}";
		
		long length=text.length();
		for (int i = 0; i < length; i++) {
			System.out.println(text.charAt(i));
		}
		
		
//		TaskResult tr=(TaskResult) jsonParsObj(text, TaskResult.class);
//		System.out.println(tr.toString());
		
		
		DefaultXMLParser parser=new DefaultXMLParser(text, new XMLScanner(text, 0), ParserConfig.getGlobalInstance());
		JavaXmlDeserializer jxd=new JavaXmlDeserializer();
		jxd.deserialze(parser, null, null, null);
		
		
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
