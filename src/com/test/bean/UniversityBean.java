package com.test.bean;

import java.util.List;

/** 
 * 
 * @ClassName UniversityBean 
 * @author fuyongkang
 * @date 2016-6-29 上午10:15:17 
 */
public class UniversityBean {

	public String name;
	public List<CollegeBean> college;
	
	
	@Override
	public String toString() {
		return "UniversityBean [name=" + name + ", college=" + college + "]";
	}
	
	
}
