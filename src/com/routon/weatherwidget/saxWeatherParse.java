package com.routon.weatherwidget;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;


public class saxWeatherParse implements weatherParse {
	private CityWeatherInfo weather_info;
//	@Override
	public void parse(InputStream is) throws Exception {
		// TODO Auto-generated method stub
		SAXParserFactory factory = SAXParserFactory.newInstance();	//取得SAXParserFactory实例
		SAXParser parser = factory.newSAXParser();					//从factory获取SAXParser实例
		MyHandler handler = new MyHandler();						//实例化自定义Handler
	
		parser.parse(is, handler);									//根据自定义Handler规则解析输入流
		weather_info = handler.getWeather();
	}

	public CityWeatherInfo getWeatherInfo()
	{
		return weather_info;
	}
	
//	@Override
	public String serialize(CityWeatherInfo weather) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	//需要重写DefaultHandler的方法
	private class MyHandler extends DefaultHandler {
		private CityWeatherInfo weather_info;
		private WeatherDetail	weather_detail;
		private WeatherIndex weatherindex;
		private String type;
		private StringBuilder builder;
		
		//返回解析后得到的Book对象集合
		public CityWeatherInfo getWeather() {
			return weather_info;
		}
		
		@Override
		public void startDocument() throws SAXException {
			super.startDocument();
			weather_info = new CityWeatherInfo();
			builder = new StringBuilder();
			System.out.println("**********文档解析开始了************");
		}
		
		 public void endDocument() throws SAXException {
			
			System.out.println("city_id is: "+weather_info.getCid());
			ArrayList<WeatherDetail> w_info = weather_info.getCityWeather();
			for(int i = 0; i < w_info.size(); i++)
			{
				WeatherDetail w_detail = w_info.get(i);
				System.out.println("    id is: "+w_detail.getId());
				System.out.println(" icon1 is: "+w_detail.getIcon1());
				System.out.println(" icon2 is: "+w_detail.getIcon2());
				System.out.println("detail is: "+w_detail.getDetail());
				System.out.println("   low is: "+w_detail.getLow());
				System.out.println("  high is: "+w_detail.getHigh());
				System.out.println("  wind is: "+w_detail.getWind());
				System.out.println("\n*********************************");
				
			}
			
			ArrayList<WeatherIndex> w_index = weather_info.getWeatherIndex();
			for(int i = 0; i < w_index.size(); i++)
			{
				WeatherIndex index = w_index.get(i);
				System.out.println(index.toString());
			}
			
			System.out.println("**********文档解析结束了************");
		
		}


		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);
		   
			if (localName.equals("index")) 
			{
				weatherindex = new WeatherIndex();
				  for (int i = 0; i < attributes.getLength(); i++) 
				    { 
					  if(attributes.getQName(i).equals("color"))
					  {
						  weatherindex.setColor(attributes.getValue(attributes.getQName(i)).toString());
					  }
					  
					  if(attributes.getQName(i).equals("name"))
					  {
						  weatherindex.setName(attributes.getValue(attributes.getQName(i)).toString());
					  }
				    } 
			}
			
			if (localName.equals("city")) {
				for (int i = 0; i < attributes.getLength(); i++) 
			    { 
					if(attributes.getQName(i).equals("id"))
					{
						weather_info.setCid(Integer.valueOf(attributes.getValue(attributes.getQName(i))));
					}
			    }
			}
			
			if (localName.equals("day")) {
				weather_detail = new WeatherDetail();
				 for (int i = 0; i < attributes.getLength(); i++) 
				 { 
					 if(attributes.getQName(i).equals("id"))
					 {
						 weather_detail.setId(Integer.valueOf(attributes.getValue(attributes.getQName(i))));
					 }
				 }
			}
			
			if (localName.equals("weather")) 
			{
				for (int i = 0; i < attributes.getLength(); i++) 
			    { 
					if(attributes.getQName(i).equals("icon1"))
					{
						if (attributes.getValue(attributes.getQName(i)).equals(""))
							 weather_detail.setIcon1(-100);
						 else
							 weather_detail.setIcon1(Integer.valueOf(attributes.getValue(attributes.getQName(i))));
					}
				 
					if(attributes.getQName(i).equals("icon2"))
					{
						if (attributes.getValue(attributes.getQName(i)).equals(""))
							weather_detail.setIcon2(-100);
						else
							weather_detail.setIcon2(Integer.valueOf(attributes.getValue(attributes.getQName(i))));
					}
			    }
			}
			
			if (localName.equals("temperature")) 
			{
				for (int i = 0; i < attributes.getLength(); i++) 
				{ 
					if(attributes.getQName(i).equals("type"))
					{
						this.type = ((attributes.getValue(attributes.getQName(i))).toString());
					}
			    }
			}
				
			
			builder.setLength(0);	//将字符长度设置为0 以便重新开始读取元素内的字符节点
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			super.characters(ch, start, length);
			builder.append(ch, start, length);	//将读取的字符数组追加到builder中
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			super.endElement(uri, localName, qName);
			if (localName.equals("index")) 
			{
				weatherindex.setDetail(builder.toString());
				ArrayList<WeatherIndex> wi = weather_info.getWeatherIndex();
				wi.add(weatherindex);
			}
			
			if (localName.equals("weather"))
			{
				weather_detail.setDetail(builder.toString());
			}
			
			if (localName.equals("wind"))
			{
				if (!builder.toString().equals("") && (builder.toString().length() != 0)) 
				{
					weather_detail.setWind(builder.toString());
				}
			}
			
			if (localName.equals("temperature"))
			{
				if (this.type.equals("low"))
				{
					if (builder.toString().equals("") || builder.toString().length() == 0) 
					{
						weather_detail.setLow(-100);
					}
					else
					{
						weather_detail.setLow(Integer.valueOf(builder.toString()));
					}
				}
				else
				{
					if (builder.toString().equals("") || builder.toString().length() == 0) 
					{
						weather_detail.setHigh(-100);
					}
					else
					{
						weather_detail.setHigh(Integer.valueOf(builder.toString()));
					}
				}
			}
			
			if (localName.equals("day"))
			{
				ArrayList<WeatherDetail> w_info = weather_info.getCityWeather();
				w_info.add(weather_detail);
			}
		}
	}
}

