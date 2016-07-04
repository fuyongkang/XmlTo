package com.test;

public class TaskResult {
	public int rspCode;
	public String rspDesc;
	public Store result;
	@Override
	public String toString() {
		return "TaskResult ["  + " rspCode=" + rspCode
				+ ", rspDesc=" + rspDesc + ", result=" + result.toString() + "]";
	}
	
	
}
