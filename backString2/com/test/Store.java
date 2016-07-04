package com.test;

import java.io.Serializable;

/**
 * 商家店铺实体
 * by sz 2015.10.22
 * **/
public class Store implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -218904865252134948L;

	private long storeId;//店铺id
	
	private int storeType;//店铺类型     0 �? 1�? 2�?
	
	private String storeName;//店铺名字(初始�?

	private String storeNameEn;//店铺名字(英文)

	private long regionId;//区域id
	
	private String storeAddress;//店铺地址(初始�?

	private String storeLon;//店铺经度
	
	private String storeLat;//店铺维度
	
	private String storeLogo;//店铺logo (png/jpg)
	
	private String storeHours;//营业时间(初始�?

	private String storeDesc;//店铺�?��(初始�?
	
	private String storeDescCn;

	private String storeDescEn;

	private String storeDescJp;

	private String storeDescKo;

	private String storeTel;//店铺联系电话
	
	private String isOrder;//是否支持预订   0不支�?  1支持
	
	private String storeWifi;//是否有wifi(0没有 1�?
	
	private String storePayment;//支持的支付方�?1 master 2 银联 3 VISA)  :[1,2,3]
	
	private String storeIsthird;//是否支持�?��媒体联盟 0 不开  1�?
	
	private String companyFile;//企业资质文件(路径)
	
	private String companyType;//经营范围

	private String companyStatus;//是否提交认证资料
	
	private String companyName;//企业名字
	
	private String companyNameEn;//企业名字英文
	
	private String companyPostcode;//企业注册地邮编（同营业执照）
	
	private String companyAddr;//企业注册地地�?��同营业执照）
	
	private String companyTel;//企业联系电话
	
	private String companyOwner;//企业法人名字
	
	private String companyId;//营业执照id
	
	private String operatorName;//运营者名�?
	
	private String operatorTel;//运营者电�?
	
	private long crtTm;
	
	private long crtUser;
	
	private long updTm;
	
	private long updUser;
	
	public long getStoreId() {
		return storeId;
	}

	public void setStoreId(long storeId) {
		this.storeId = storeId;
	}

	public int getStoreType() {
		return storeType;
	}

	public void setStoreType(int storeType) {
		this.storeType = storeType;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getStoreNameEn() {
		return storeNameEn;
	}

	public void setStoreNameEn(String storeNameEn) {
		this.storeNameEn = storeNameEn;
	}

	public long getRegionId() {
		return regionId;
	}

	public void setRegionId(long regionId) {
		this.regionId = regionId;
	}

	public String getStoreAddress() {
		return storeAddress;
	}

	public void setStoreAddress(String storeAddress) {
		this.storeAddress = storeAddress;
	}

	public String getStoreLon() {
		return storeLon;
	}

	public void setStoreLon(String storeLon) {
		this.storeLon = storeLon;
	}

	public String getStoreLat() {
		return storeLat;
	}

	public void setStoreLat(String storeLat) {
		this.storeLat = storeLat;
	}

	public String getStoreLogo() {
		return storeLogo;
	}

	public void setStoreLogo(String storeLogo) {
		this.storeLogo = storeLogo;
	}

	public String getStoreHours() {
		return storeHours;
	}

	public void setStoreHours(String storeHours) {
		this.storeHours = storeHours;
	}

	public String getStoreDesc() {
		return storeDesc;
	}

	public void setStoreDesc(String storeDesc) {
		this.storeDesc = storeDesc;
	}

	public String getStoreTel() {
		return storeTel;
	}

	public void setStoreTel(String storeTel) {
		this.storeTel = storeTel;
	}

	public String getStoreWifi() {
		return storeWifi;
	}

	public void setStoreWifi(String storeWifi) {
		this.storeWifi = storeWifi;
	}

	public String getStorePayment() {
		return storePayment;
	}

	public void setStorePayment(String storePayment) {
		this.storePayment = storePayment;
	}

	public String getStoreIsthird() {
		return storeIsthird;
	}

	public void setStoreIsthird(String storeIsthird) {
		this.storeIsthird = storeIsthird;
	}

	public String getCompanyFile() {
		return companyFile;
	}

	public void setCompanyFile(String companyFile) {
		this.companyFile = companyFile;
	}

	public String getCompanyType() {
		return companyType;
	}

	public void setCompanyType(String companyType) {
		this.companyType = companyType;
	}

	public String getCompanyStatus() {
		return companyStatus;
	}

	public void setCompanyStatus(String companyStatus) {
		this.companyStatus = companyStatus;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCompanyNameEn() {
		return companyNameEn;
	}

	public void setCompanyNameEn(String companyNameEn) {
		this.companyNameEn = companyNameEn;
	}

	public String getCompanyPostcode() {
		return companyPostcode;
	}

	public void setCompanyPostcode(String companyPostcode) {
		this.companyPostcode = companyPostcode;
	}

	public String getCompanyAddr() {
		return companyAddr;
	}

	public void setCompanyAddr(String companyAddr) {
		this.companyAddr = companyAddr;
	}

	public String getCompanyTel() {
		return companyTel;
	}

	public void setCompanyTel(String companyTel) {
		this.companyTel = companyTel;
	}

	public String getCompanyOwner() {
		return companyOwner;
	}

	public void setCompanyOwner(String companyOwner) {
		this.companyOwner = companyOwner;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	public String getOperatorTel() {
		return operatorTel;
	}

	public void setOperatorTel(String operatorTel) {
		this.operatorTel = operatorTel;
	}

	public long getCrtTm() {
		return crtTm;
	}

	public void setCrtTm(long crtTm) {
		this.crtTm = crtTm;
	}

	public long getCrtUser() {
		return crtUser;
	}

	public void setCrtUser(long crtUser) {
		this.crtUser = crtUser;
	}

	public long getUpdTm() {
		return updTm;
	}

	public void setUpdTm(long updTm) {
		this.updTm = updTm;
	}

	public long getUpdUser() {
		return updUser;
	}

	public void setUpdUser(long updUser) {
		this.updUser = updUser;
	}

	public String getStoreDescCn() {
		return storeDescCn;
	}

	public void setStoreDescCn(String storeDescCn) {
		this.storeDescCn = storeDescCn;
	}

	public String getStoreDescEn() {
		return storeDescEn;
	}

	public void setStoreDescEn(String storeDescEn) {
		this.storeDescEn = storeDescEn;
	}

	public String getStoreDescJp() {
		return storeDescJp;
	}

	public void setStoreDescJp(String storeDescJp) {
		this.storeDescJp = storeDescJp;
	}

	public String getStoreDescKo() {
		return storeDescKo;
	}

	public void setStoreDescKo(String storeDescKo) {
		this.storeDescKo = storeDescKo;
	}

	public String getIsOrder() {
		return isOrder;
	}

	public void setIsOrder(String isOrder) {
		this.isOrder = isOrder;
	}

	@Override
	public String toString() {
		return "Store [storeId=" + storeId + ", storeType=" + storeType
				+ ", storeName=" + storeName + ", storeNameEn=" + storeNameEn
				+ ", regionId=" + regionId + ", storeAddress=" + storeAddress
				+ ", storeLon=" + storeLon + ", storeLat=" + storeLat
				+ ", storeLogo=" + storeLogo + ", storeHours=" + storeHours
				+ ", storeDesc=" + storeDesc + ", storeDescCn=" + storeDescCn
				+ ", storeDescEn=" + storeDescEn + ", storeDescJp="
				+ storeDescJp + ", storeDescKo=" + storeDescKo + ", storeTel="
				+ storeTel + ", isOrder=" + isOrder + ", storeWifi="
				+ storeWifi + ", storePayment=" + storePayment
				+ ", storeIsthird=" + storeIsthird + ", companyFile="
				+ companyFile + ", companyType=" + companyType
				+ ", companyStatus=" + companyStatus + ", companyName="
				+ companyName + ", companyNameEn=" + companyNameEn
				+ ", companyPostcode=" + companyPostcode + ", companyAddr="
				+ companyAddr + ", companyTel=" + companyTel
				+ ", companyOwner=" + companyOwner + ", companyId=" + companyId
				+ ", operatorName=" + operatorName + ", operatorTel="
				+ operatorTel + ", crtTm=" + crtTm + ", crtUser=" + crtUser
				+ ", updTm=" + updTm + ", updUser=" + updUser + "]";
	}
	
	
}
