package com.me.android.scanNedit;

public class Picture {
	private String picture;
	private int id;
	private String title;
	private String desc;
	private String date;
	private String url;
	public int getId() {
		return id;
	}
	public String getPicture() {
		return picture;
	}
	public void setPicture(String picture) {
		picture = picture;
	}
	public void setId(int count) {
		this.id = count;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

}
