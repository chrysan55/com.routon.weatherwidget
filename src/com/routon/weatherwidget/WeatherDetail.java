package com.routon.weatherwidget;

public class WeatherDetail {
	private int id;		//天气日期20120302
	private int icon1;	//第一天气图标
	private int icon2;	//第二天气图标
	private String detail;//天气"多云转小雨“
	private int low;		//最低温度，如果平台未提供设置为-100
	private int high;		//最高温度，如果平中未提供设置为-100
	private String wind; //风，如“东南风４-5级"

	public int getId()
	{
		return this.id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	public int getIcon1()
	{
		return this.icon1;
	}
	
	public void setIcon1(int icon1)
	{
		this.icon1 = icon1;
	}
	
	public int getIcon2()
	{
		return this.icon2;
	}
	
	public void setIcon2(int icon2)
	{
		this.icon2 = icon2;
	}
	
	public int getLow()
	{
		return this.low;
	}
	
	public void setLow(int low)
	{
		this.low = low;
	}
	
	public int getHigh()
	{
		return this.high;
	}
	
	public void setHigh(int high)
	{
		this.high = high;
	}
	
	public String getWind()
	{
		return this.wind;
	}
	
	public void setWind(String wind)
	{
		this.wind = wind;
	}
	
	public String getDetail()
	{
		return this.detail;
	}
	
	public void setDetail(String detail)
	{
		this.detail = detail;
	}
	
	public String toString()
	{
		return "weather:" + this.id+ " " + this.icon1+ " " +this.icon2+ " " + this.detail+ " " + this.low+ " " + this.high+ " " + this.wind;
	}
}
