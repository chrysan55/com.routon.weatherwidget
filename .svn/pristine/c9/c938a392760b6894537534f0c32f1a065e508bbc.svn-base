package com.routon.weatherwidget;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.routon.weatherwidget.WeatherIndex;
import com.routon.weatherwidget.weatherParse;

import android.R.bool;
import android.util.Log;


class CityInfo
{
	String c_id;
	String pic_path;
	String name;
	String cht_name;
	String spell;
}


public class saxCityParse implements weatherParse {
	private ArrayList<CityInfo> city_list;
	
	
//	@Override
	public void parse(InputStream is) throws Exception {
		// TODO Auto-generated method stub
		SAXParserFactory factory = SAXParserFactory.newInstance();	//取得SAXParserFactory实例
		SAXParser parser = factory.newSAXParser();					//从factory获取SAXParser实例
		MyHandler handler = new MyHandler();						//实例化自定义Handler
	
		parser.parse(is, handler);									//根据自定义Handler规则解析输入流
		city_list = handler.getCityList();
	}

	public ArrayList<CityInfo> getCityList()
	{
		return city_list;
	}
	
//	@Override
	public String serialize(CityWeatherInfo weather) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	//需要重写DefaultHandler的方法
	private class MyHandler extends DefaultHandler {
		private ArrayList<CityInfo> city_list;
		CityInfo c_info;
		private StringBuilder builder;
		
		//返回解析后得到的Book对象集合
		public ArrayList<CityInfo> getCityList() {
			return city_list;
		}
		
		@Override
		public void startDocument() throws SAXException {
			super.startDocument();
			city_list = new ArrayList();
			builder = new StringBuilder();
			System.out.println("**********文档解析city.xml开始了************");
		}
		
		 public void endDocument() throws SAXException {
			
			CityInfo c_info = city_list.get(city_list.size() -1);
			System.out.println("the last city is: "+c_info.c_id+" "+c_info.name +" "+c_info.cht_name+" "+c_info.spell+" "+c_info.pic_path );
			
			System.out.println("**********文档解析结束了************");
		}


		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);
		  
			if (localName.equals("city")) 
			{
				c_info= new CityInfo();
				for (int i = 0; i < attributes.getLength(); i++) 
			    { 
				  if(attributes.getQName(i).equals("cid"))
				  {
					  c_info.c_id =attributes.getValue(attributes.getQName(i));
				  }
				  
				  if(attributes.getQName(i).equals("name"))
				  {
					  c_info.name = attributes.getValue(attributes.getQName(i)).toString();
				  }
				  
				  if(attributes.getQName(i).equals("cht-name"))
				  {
					  c_info.cht_name = attributes.getValue(attributes.getQName(i)).toString();
				  }
				  
				  if(attributes.getQName(i).equals("spell"))
				  {
					  c_info.spell = attributes.getValue(attributes.getQName(i)).toString();
				  }
				  
				  if(attributes.getQName(i).equals("pic"))
				  {
					  c_info.pic_path = attributes.getValue(attributes.getQName(i)).toString();
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
					
			if (localName.equals("city"))
			{
				if(city_list.size()==0)
					city_list.add(c_info);
				else if( city_list.get(city_list.size()-1 ) != c_info)
					city_list.add(c_info);
			}
		}
	}
}

