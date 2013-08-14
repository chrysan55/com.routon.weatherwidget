package com.routon.weatherwidget;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;


class CityInfo
{
	String c_id;
	String pic_path;
	String name;
	String cht_name;
	String spell;
	int level;
}

public class saxCityParse implements weatherParse {
	static final int PROVINCE_LEVEL = 1;
	static final int CITY_LEVEL = 2;
	static final int IGNORE_LEVEL = 0;
	static final String[] FOREIGN_CONTINENTS = {"asia","africa", "north_america","south_america","europe","oceania"};
	
	private HashMap<CityInfo, ArrayList<CityInfo>> cityHashMap;
	private ArrayList<CityInfo> province_list;
	private int depth = 0;
	private boolean isMunicipality = false;
	private boolean isForeign = false;
	
	
//	@Override
	public void parse(InputStream is) throws Exception {
		// TODO Auto-generated method stub
		SAXParserFactory factory = SAXParserFactory.newInstance();	//取得SAXParserFactory实例
		SAXParser parser = factory.newSAXParser();					//从factory获取SAXParser实例
		MyHandler handler = new MyHandler();						//实例化自定义Handler
	
		parser.parse(is, handler);									//根据自定义Handler规则解析输入流
		province_list = handler.getProvinceList();
		cityHashMap = handler.getCityHashMap();
	}

	public HashMap<CityInfo, ArrayList<CityInfo>> getCityHashMap()
	{
		return cityHashMap;
	}
	
	public ArrayList<CityInfo> getProvinceList(){
		return province_list;
	}
	
//	@Override
	public String serialize(CityWeatherInfo weather) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	//需要重写DefaultHandler的方法
	private class MyHandler extends DefaultHandler {
		private ArrayList<CityInfo> city_list,province_list;
		private HashMap<CityInfo, ArrayList<CityInfo>> cityHashMap;
		CityInfo c_info, foreign_c_info;
		private StringBuilder builder;
		List<String> foreignContinents;
		
		//返回解析后得到的省份和城市对应的HashMap
		public HashMap<CityInfo, ArrayList<CityInfo>> getCityHashMap() {
			return cityHashMap;
		}
		
		//返回解析后得到的省份列表
		public ArrayList<CityInfo> getProvinceList(){
			return province_list;
		}
		
		@Override
		public void startDocument() throws SAXException {
			super.startDocument();
			cityHashMap = new HashMap<CityInfo, ArrayList<CityInfo>>();
			province_list = new ArrayList<CityInfo>();
			foreign_c_info = new CityInfo();
			builder = new StringBuilder();
			foreignContinents = Arrays.asList(FOREIGN_CONTINENTS);
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
		  
			if (localName.equals("city") && depth >= 0) 
			{
				c_info= new CityInfo();
				depth ++;
				for (int i = 0; i < attributes.getLength(); i++) 
			    { 
//					Log.i("Tag", "depth = "+depth+" , localName = "+localName+" , qName = "+qName+" , attributes.getQName("+i+") = "+attributes.getValue(attributes.getQName(i)));
				  if(attributes.getQName(i).equals("cid"))
				  {
					  c_info.c_id =attributes.getValue(attributes.getQName(i));

					  switch (depth) {
						case 1:
							c_info.level = IGNORE_LEVEL;
							break;

						case 2:
							if(isForeign)	//标记了isForeign，第二层是国家名，无需记录
								c_info.level = IGNORE_LEVEL;
							else
								c_info.level = PROVINCE_LEVEL;
							break;
							
						case 3:
							if(isMunicipality)	//标记了isMunicipality，第三层是直辖市名，记录为PROVINCE_LEVEL
								c_info.level = PROVINCE_LEVEL;
							else
								c_info.level = CITY_LEVEL;
						default:
							break;
					  }
					  
					  //如果是“直辖市”或“特别行政区”,element忽略，设置isMunicipality
					  if(c_info.c_id.equals("municipality")||c_info.c_id.equals("special_region")){
						  c_info.level = IGNORE_LEVEL;
						  isMunicipality = true;
					  }
					  //如果是国外"x洲",element忽略，标记isForeign
					  else if(foreignContinents.contains(c_info.c_id)){
						  isForeign = true;
					  }
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
				
				if(c_info.level == IGNORE_LEVEL && isForeign && !cityHashMap.containsKey(foreign_c_info)){
					//第一次解析到国外
					foreign_c_info.c_id = "foreign";
					foreign_c_info.level = PROVINCE_LEVEL;
					foreign_c_info.name = "国外";
					foreign_c_info.cht_name = "國外";
					foreign_c_info.spell = "Guowai";
					
					city_list = new ArrayList<CityInfo>();
					
					province_list.add(foreign_c_info);
					cityHashMap.put(foreign_c_info, city_list);
				}else
					if(c_info.level == PROVINCE_LEVEL){
						//解析到PROVINCE_LEVEL，创建这个省对应的新的城市列表，并加入到HashMap中
						city_list = new ArrayList<CityInfo>();
						province_list.add(c_info);
						cityHashMap.put(c_info, city_list);
						
//						Log.i("tag", "解析结果： 省！ name = "+c_info.name);
						
						//处理直辖市的情况
						if(isMunicipality)
							city_list.add(c_info);
					}
					else
						if(c_info.level == CITY_LEVEL){
							city_list.add(c_info);
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
				if(depth > 0)
					depth--;
				else
					Log.e("tag", "ERROR");
				
				//直辖市/特别行政区读取完毕，设置标记
				if(depth == 1 && isMunicipality)
					isMunicipality = false;
				//一个洲的城市读取完毕，设置标记
				else 
					if(depth == 0 && isForeign)
						isForeign = false;
			}
		}
	}
}

