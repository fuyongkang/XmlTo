package com.test;


public class TaskResult {
	public String rspCode;
	public String rspDesc;
	public Store result;
	@Override
	public String toString() {
		return "TaskResult ["  + " rspCode=" + rspCode
				+ ", rspDesc=" + rspDesc + ", result=" + result.toString() + "]";
	}
	
	
}
