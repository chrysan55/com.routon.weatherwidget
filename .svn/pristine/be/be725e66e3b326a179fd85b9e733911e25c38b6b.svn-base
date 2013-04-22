package com.routon.weatherwidget;

import java.io.InputStream;

public interface weatherParse {
	/**
	 * 解析输入流 得到Book对象集合
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public void parse(InputStream is) throws Exception;
	
	/**
	 * 序列化Book对象集合 得到XML形式的字符串
	 * @param books
	 * @return
	 * @throws Exception
	 */
	public String serialize(CityWeatherInfo weather_info) throws Exception;
}
