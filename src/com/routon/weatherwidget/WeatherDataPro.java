package com.routon.weatherwidget;

import java.io.BufferedInputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import javax.security.auth.PrivateCredentialPermission;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.routon.weatherwidget.R.color;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

class CityData implements Serializable {
	String c_id;
	String c_name;
	boolean is_temporary = false;
	boolean is_default;
	View button_view;
	View board_view;

	CityWeatherInfo weather_info;
}

public class WeatherDataPro {

	// private List name_list;
	private AnimationDrawable animDrawable;
	private Resources res;
	private ArrayList<Bitmap> bitmapList;
	private FrameLayout parentLayout = null;

	private Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			CityData c_data = (CityData) msg.getData()
					.getSerializable("c_data");
			switch (msg.what) {
			case 1:

				weather_update_weather_info(c_data, true);
				break;
			case 2:
				try {
					ImageView city_pic = (ImageView) c_data.board_view
							.findViewById(R.id.citypic);
					String pic_path = msg.getData().getString("pic_path");
					city_pic.setImageURI(Uri.fromFile(new File(pic_path)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			super.handleMessage(msg);
		}
	};

	static private String pre_path = "/hdisk/";
	static private String[] week_index = { "周日", "周一", "周二", "周三", "周四", "周五",
			"周六",

	};

	static public String[] icon_name = { "qing", "duoyun", "yin", "zhenyu",
			"leizhenyu", "leizhenyu_binbao", "yujiaxue", "xiaoyu", "zhongyu",
			"dayu", "baoyu", "dabaoyu", "tedabaoyu", "zhenxue", "xiaoxue",
			"zhongxue", "daxue", "baoxue", "wu", "dongyu", "shachenbao",
			"xiaodaozhongyu", "zhongdaodayu", "dadaobaoyu", "baoyudaodabaoyu",
			"dabaoyudaotedabaoyu", "xiaodaozhongxue", "zhongdaodaxue",
			"dadaobaoxue", "fuchen", "yangsha", "qiangshachenbao", "qing", };

	public WeatherDataPro(Resources resource) {
		res = resource;
		bitmapList = new ArrayList<Bitmap>();
	}

	public boolean weather_request_xml_data(List<CityData> updateList,
			ArrayList<CityInfo> name_list) throws IllegalStateException,
			Exception {

		requestXmlDataThread requestXmlData = new requestXmlDataThread(
				updateList, name_list);
		requestXmlData.start();

		// requestXmlDataThread requestXmlData = new requestXmlDataThread();
		// requestXmlData.execute(c_data);

		return false;
	}

	void weather_update_animationdrawable_bitmap(CityData c_data,
			ImageView weather_icon) {
		WeatherDetail w_detail = null;
		// ImageView weather_icon = null;
		if (c_data.weather_info == null)
			return;
		CityWeatherInfo weather_info = c_data.weather_info;
		w_detail = weather_info.getCityWeather().get(0);
		// weather_icon = (ImageView) c_data.board_view
		// .findViewById(R.id.weather0);
		weather_icon.setVisibility(ImageView.INVISIBLE);
		FrameLayout weatherIconFrameLayout = (FrameLayout) c_data.board_view
				.findViewById(R.id.weathericon);
		if(parentLayout != null)
			if(parentLayout.getChildCount() != 0)
				parentLayout.removeAllViews();
		weatherIconFrameLayout.addView(weather_icon,
				weatherIconFrameLayout.getWidth(),
				weatherIconFrameLayout.getHeight());
		parentLayout = weatherIconFrameLayout;

		int res_id = 0;
		int id2 = -1;
		String icon1_path = null;
		String icon2_path = null;

		try {
			if (w_detail.getIcon1() >= 0 && w_detail.getIcon1() < 33)
				res_id = (Integer) R.drawable.class.getField(
						icon_name[w_detail.getIcon1()]).getInt(0);
			else
				res_id = (Integer) R.drawable.class.getField(icon_name[0])
						.getInt(0);

			if (w_detail.getIcon2() >= 0 && w_detail.getIcon2() < 33)
				id2 = (Integer) R.drawable.class.getField(
						icon_name[w_detail.getIcon2()]).getInt(0);
			else
				id2 = -1;

			if (animDrawable != null) {
				animDrawable.stop();
				animDrawable = null;
				System.out.println("**********animDrawable.stop***********");
			}
			
/*			if (w_detail.getIcon1() >= 0 && w_detail.getIcon1() < 33)
				icon1_path = "/hdisk/rc/pics/weather/"+icon_name[w_detail.getIcon1()]+".png";
			else
				icon1_path = "/hdisk/rc/pics/weather/"+icon_name[0]+".png";
			
			if (w_detail.getIcon2() >= 0 && w_detail.getIcon2() < 33)
				icon2_path = "/hdisk/rc/pics/weather/"+icon_name[w_detail.getIcon2()]+".png";
*/
			int i = 0;
			while (bitmapList.size() > 0) {
				if (bitmapList.get(0) != null
						&& !bitmapList.get(0).isRecycled()) {
					bitmapList.get(0).recycle();
					// bitmapList.get(0) = null;
					bitmapList.set(0, null);
					System.out.println("**********bitmap recycle***********"
							+ i++);
				}
				bitmapList.remove(0);
			}

			Log.v("LOG", "============ icon1_path =" + icon1_path + " , icon2_path = " + icon2_path);
			animDrawable = getAnimationDrawable(res_id, id2, 9, 3, 2700);
//			animDrawable = getAnimationDrawable(icon1_path, icon2_path, 10, 10, 3000);
			weather_icon.setImageDrawable(animDrawable);
			if (animDrawable.isVisible() == false)
				animDrawable.setVisible(true, true);
			animDrawable.start();

			weather_icon.setVisibility(ImageView.VISIBLE);

		} catch (Exception e) {
			Log.e("Exception", "AAAAAAAAAAAAAA");
			e.printStackTrace();
		}

	}

	void weather_update_recycle_bitmap() {
		if (animDrawable != null) {
			animDrawable.stop();
			animDrawable = null;
			System.out.println("**********animDrawable.stop***********");
		}

		int i = 0;
		while (bitmapList.size() > 0) {
			if (bitmapList.get(0) != null && !bitmapList.get(0).isRecycled()) {
				bitmapList.get(0).recycle();
				// bitmapList.get(0) = null;
				bitmapList.set(0, null);
				System.out.println("**********bitmap recycle***********" + i++);
			}
			bitmapList.remove(0);
		}
	}

	void weather_update_hide_bitmap() {
		Log.v("hide", "************hide bitmap****************** "
				+ animDrawable);
		if (animDrawable != null)
			animDrawable.setVisible(false, false);
	}

	void weather_update_weather_info(CityData c_data, boolean add_bitmap) {

		if (c_data != null) {
			WeatherDetail w_detail = null;
			CityWeatherInfo weather_info = c_data.weather_info;
			String degree = "℃";

			ImageView weather_icon = null;
			String icon_path = null;

			TextView text = (TextView) c_data.board_view
					.findViewById(R.id.cityname);
			text.setText(c_data.c_name);

			// day 0
			c_data.board_view.findViewById(R.id.day0).setVisibility(
					View.VISIBLE);

			w_detail = weather_info.getCityWeather().get(0);
			text = (TextView) c_data.board_view.findViewById(R.id.temp0);
			text.setText(w_detail.getLow() + degree + "~" + w_detail.getHigh()
					+ degree);

			text = (TextView) c_data.board_view.findViewById(R.id.detail0);
			// text.setText(w_detail.getDetail() + "  " + w_detail.getWind());
			// //没有滚动字幕控件，先不添加风向信息
			text.setText(w_detail.getDetail());

			text = (TextView) c_data.board_view.findViewById(R.id.current0);
			if (w_detail.getLow() < 12)
				text.setText(w_detail.getLow() + degree);
			else
				text.setText(w_detail.getHigh() + degree);

			int res_id = 0;

			// if (add_bitmap == true)
			// weather_update_animationdrawable_bitmap(c_data);

			// int id2 = -1;
			// try{
			// if(w_detail.getIcon1() >= 0 && w_detail.getIcon1() < 33)
			// res_id = (Integer)
			// R.drawable.class.getField(icon_name[w_detail.getIcon1()]).getInt(0);
			// else
			// res_id = (Integer)
			// R.drawable.class.getField(icon_name[0]).getInt(0);
			//
			// if(w_detail.getIcon2() >= 0 && w_detail.getIcon2() < 33)
			// id2 = (Integer)
			// R.drawable.class.getField(icon_name[w_detail.getIcon2()]).getInt(0);
			// else
			// id2 = -1;
			//
			//
			// AnimationDrawable animationDrawable =
			// getAnimationDrawable(res_id,id2, 9, 3, 2700);
			// weather_icon = (ImageView)
			// c_data.board_view.findViewById(R.id.weather0);
			// weather_icon.setImageDrawable(animationDrawable);
			// animationDrawable.start();
			//
			// }
			// catch(Exception e)
			// {
			// Log.e("Exception", "AAAAAAAAAAAAAA");
			// e.printStackTrace();
			// }

			Calendar rightNow = Calendar.getInstance();
			int today = rightNow.get(Calendar.DAY_OF_WEEK) - 1;

			System.out.println("****************" + today);

			// day1~day5
			for (int i = 1; i < weather_info.getCityWeather().size() - 1; i++) // weather_info.getCityWeather().size()最多为7,只用显示六天的数据，所以减1
			{
				w_detail = weather_info.getCityWeather().get(i);

				try {
					res_id = (Integer) R.id.class.getField("day" + i).getInt(0);
					text = (TextView) c_data.board_view.findViewById(res_id);
					text.setText(week_index[(today + i) % 7]);
					System.out.println("****************"
							+ week_index[(today + i) % 6]);

					res_id = (Integer) R.id.class.getField("temp" + i)
							.getInt(0);
					text = (TextView) c_data.board_view.findViewById(res_id);
					text.setText((w_detail.getLow() == -100 ? " " : w_detail
							.getLow())
							+ degree
							+ "~"
							+ (w_detail.getHigh() == -100 ? " " : w_detail
									.getHigh()) + degree);

					res_id = (Integer) R.id.class.getField("detail" + i)
							.getInt(0);
					text = (TextView) c_data.board_view.findViewById(res_id);
					text.setText(w_detail.getDetail());

					res_id = (Integer) R.id.class.getField("daypic" + i)
							.getInt(0);
					weather_icon = (ImageView) c_data.board_view
							.findViewById(res_id);

					if (w_detail.getIcon1() >= 0 && w_detail.getIcon1() < 33)
						icon_path = "s_" + icon_name[w_detail.getIcon1()];
					else
						icon_path = "s_" + icon_name[0];

					Log.v("weather icon:", icon_path);
					res_id = (Integer) R.drawable.class.getField(icon_path)
							.getInt(0);
					weather_icon.setImageResource(res_id);

				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (weather_info.getWeatherIndex() != null
					&& weather_info.getWeatherIndex().size() == 4) {

				View mask = (View) c_data.board_view.findViewById(R.id.maskpic);
				mask.setVisibility(View.VISIBLE);

				for (int i = 1; i <= 4; i++) {
					try {
						res_id = (Integer) R.id.class.getField("ind" + i)
								.getInt(0);
						text = (TextView) c_data.board_view
								.findViewById(res_id);
						text.setText(weather_info.getWeatherIndex().get(i - 1)
								.getName());

						res_id = (Integer) R.id.class.getField("index" + i)
								.getInt(0);
						text = (TextView) c_data.board_view
								.findViewById(res_id);
						text.setText(weather_info.getWeatherIndex().get(i - 1)
								.getDetail());
						if (weather_info.getWeatherIndex().get(i - 1)
								.getColor().equals("red"))
							text.setTextColor(Color.RED);
						else
							text.setTextColor(Color.GREEN);

					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchFieldException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}
	}

	void weather_get_city_image(CityData c_data, List<CityInfo> name_list)

	{
		CityInfo c_info = null;
		if (name_list != null) {
			for (int i = 0; i < name_list.size(); i++) {
				c_info = name_list.get(i);
				if (c_info.c_id.equals(c_data.c_id))
					break;
				c_info = null;
			}
		}

		String pic_path = pre_path + "/rc/pics/weather/city/" + c_data.c_id
				+ ".jpg";
		File pic_dir = new File(pre_path + "/rc/pics/weather/city/");

		File storeFile = new File(pic_path);
		if (!storeFile.exists() && c_info != null) {
			if (!pic_dir.exists())
				pic_dir.mkdirs();

			String url = "http://www.grandes.com.cn" + c_info.pic_path;
			Log.v("get city image", url + "   " + pic_path);

			HttpGet http_request = new HttpGet(url);
			// 取得HttpClient 对象
			HttpClient httpc_lient = new DefaultHttpClient();
			try {
				// 请求httpClient ，取得HttpRestponse
				HttpResponse http_response = httpc_lient.execute(http_request);
				if (http_response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					Log.v("get city image", " HttpStatus.SC_OK");
					// 取得相关信息 取得HttpEntiy
					HttpEntity http_entity = http_response.getEntity();
					if (http_entity != null) {
						Log.v("get city image", "http_entity OK!");
						// 获得一个输入流
						InputStream input = http_entity.getContent();
						BufferedInputStream bis = new BufferedInputStream(input);
						Log.v("get city image", "buffer input stream!");

						FileOutputStream output = new FileOutputStream(
								storeFile);
						Log.v("get city image", "file output stream!");

						// 得到网络资源并写入文件
						byte b[] = new byte[1024];
						int j = 0;
						while ((j = bis.read(b)) != -1)
							output.write(b, 0, j);

						Log.v("get city image", "write OK!");

						input.close();
						output.close();
					}
				}

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				Log.v("exception", "22222222222222");
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.v("exception", "3333333333333");
				e.printStackTrace();
			}
		} else
			System.out.println("**********file is exist!");

		System.out.println("............. 结束");

		// try {
		// ImageView city_pic = (ImageView) c_data.board_view
		// .findViewById(R.id.citypic);
		// city_pic.setImageURI(Uri.fromFile(new File(pic_path)));
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		// 发送消息通知更新UI
		Message msg = new Message();
		Bundle data = new Bundle();
		data.putSerializable("c_data", c_data);
		data.putString("pic_path", pic_path);
		msg.what = 2;
		msg.setData(data);
		WeatherDataPro.this.myHandler.sendMessage(msg);
	}

	@SuppressWarnings("deprecation")
	public AnimationDrawable getAnimationDrawable(int id1, int id2,
			int frameNum, int columnNum, int time) {
		AnimationDrawable animationDrawable = null;
		int width, height;
		Bitmap bitmapSrc = null;

		try {
			// bitmapSrc = BitmapFactory.decodeStream(new FileInputStream(new
			// File(fileName)));

			bitmapSrc = BitmapFactory.decodeResource(res, id1);

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (bitmapSrc != null) {
			System.out.println("width:" + bitmapSrc.getWidth());
			System.out.println("height:" + bitmapSrc.getHeight());
			width = bitmapSrc.getWidth() / 3;
			height = bitmapSrc.getHeight() / 3;

			animationDrawable = new AnimationDrawable();

			// Bitmap bitmap = Bitmap.createBitmap(bitmapSrc,0, 0,
			// bitmapSrc.getWidth(), bitmapSrc.getHeight());
			// animationDrawable.addFrame(new BitmapDrawable(bitmap),time /
			// frameNum);
			int lay = 0;
			for (int frame = 0; frame < frameNum; frame++) {
				lay = frame / columnNum;
				bitmapList.add(Bitmap.createBitmap(bitmapSrc,
						(frame % columnNum) * width, lay * height, width,
						height));
				animationDrawable.addFrame(
						new BitmapDrawable(
								bitmapList.get(bitmapList.size() - 1)), time
								/ frameNum);
			}
			bitmapList.add(bitmapSrc);
			bitmapSrc = null;

			if (id2 > 0) {
				bitmapSrc = BitmapFactory.decodeResource(res, id2);
				for (int frame = 0; frame < frameNum; frame++) {
					lay = frame / columnNum;
					bitmapList.add(Bitmap.createBitmap(bitmapSrc,
							(frame % columnNum) * width, lay * height, width,
							height));
					animationDrawable.addFrame(
							new BitmapDrawable(
									bitmapList.get(bitmapList.size() - 1)),
							time / frameNum);
				}
				bitmapList.add(bitmapSrc);
				bitmapSrc = null;
			}

			animationDrawable.setOneShot(false);
		}
		return animationDrawable;
	}
	
	public AnimationDrawable getAnimationDrawable(String icon1_path, String icon2_path,
			int frameNum, int columnNum, int time) {
		AnimationDrawable animationDrawable = null;
		int width, height;
		Bitmap bitmapSrc = null;

		try {
			// bitmapSrc = BitmapFactory.decodeStream(new FileInputStream(new
			// File(fileName)));

			bitmapSrc = BitmapFactory.decodeFile(icon1_path);

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (bitmapSrc != null) {
			System.out.println("width:" + bitmapSrc.getWidth());
			System.out.println("height:" + bitmapSrc.getHeight());
			width = bitmapSrc.getWidth() / 10;
			height = bitmapSrc.getHeight();

			animationDrawable = new AnimationDrawable();

			// Bitmap bitmap = Bitmap.createBitmap(bitmapSrc,0, 0,
			// bitmapSrc.getWidth(), bitmapSrc.getHeight());
			// animationDrawable.addFrame(new BitmapDrawable(bitmap),time /
			// frameNum);
			int lay = 0;
			for (int frame = 0; frame < frameNum; frame++) {
				lay = frame / columnNum;
				bitmapList.add(Bitmap.createBitmap(bitmapSrc,
						(frame % columnNum) * width, lay * height, width,
						height));
				animationDrawable.addFrame(
						new BitmapDrawable(
								bitmapList.get(bitmapList.size() - 1)), time
								/ frameNum);
			}
			bitmapList.add(bitmapSrc);
			bitmapSrc = null;

			if (icon2_path != null) {
				bitmapSrc = BitmapFactory.decodeFile(icon2_path);
				for (int frame = 0; frame < frameNum; frame++) {
					lay = frame / columnNum;
					bitmapList.add(Bitmap.createBitmap(bitmapSrc,
							(frame % columnNum) * width, lay * height, width,
							height));
					animationDrawable.addFrame(
							new BitmapDrawable(
									bitmapList.get(bitmapList.size() - 1)),
							time / frameNum);
				}
				bitmapList.add(bitmapSrc);
				bitmapSrc = null;
			}

			animationDrawable.setOneShot(false);
		}
		return animationDrawable;
	}

	public class requestXmlDataThread extends Thread {

		private List<CityData> updateList;
		private ArrayList<CityInfo> name_list;
		private CityData c_data;
		private boolean add_bitmap;

		public requestXmlDataThread(List<CityData> updateList,
				ArrayList<CityInfo> name_list) {
			this.updateList = updateList;
			this.name_list = name_list;
		}

		public void run() {
			for (int i = 0; i < updateList.size(); i++) {
				c_data = updateList.get(i);
				add_bitmap = false;

				weather_request_xml_data(c_data, add_bitmap);
				weather_get_city_image(c_data, name_list);
			}
		}

		public void weather_request_xml_data(CityData c_data, boolean add_bitmap) {

			String url = "http://www.grandes.com.cn/weather/getweather.action?days=7&cityCode="
					+ c_data.c_id;
			Log.v("req:", url);

			try {
				HttpClient http_client = new DefaultHttpClient();
				HttpGet http_request = new HttpGet(url);
				HttpResponse response = http_client.execute(http_request);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					Log.v("weather_request_xml_data:", "OK!");

					// parse
					saxWeatherParse parse = new saxWeatherParse();
					parse.parse(response.getEntity().getContent());
					c_data.weather_info = parse.getWeatherInfo();
					// 发送消息通知更新UI
					Message msg = new Message();
					Bundle data = new Bundle();
					data.putSerializable("c_data", c_data);
					msg.what = 1;
					msg.setData(data);
					WeatherDataPro.this.myHandler.sendMessage(msg);
					// weather_update_weather_info(c_data, true);

				} else {
					Log.v("weather_request_xml_data:", "error!");
				}

			} catch (ClientProtocolException e) {
				e.printStackTrace();
				Log.e("HttpConnectionUtil", e.getMessage(), e);

			} catch (IOException e) {
				e.printStackTrace();

				Log.e("HttpConnectionUtil", e.getMessage(), e);

			} catch (IllegalStateException e) {
				e.printStackTrace();
				Log.e("IllegalStateException", e.getMessage(), e);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("error", e.getMessage(), e);
			}
		}

		public void weather_get_city_image(CityData c_data,
				List<CityInfo> name_list)

		{
			CityInfo c_info = null;
			if (name_list != null) {
				for (int i = 0; i < name_list.size(); i++) {
					c_info = name_list.get(i);
					if (c_info.c_id.equals(c_data.c_id))
						break;
					c_info = null;
				}
			}

			String pic_path = pre_path + "/rc/pics/weather/city/" + c_data.c_id
					+ ".jpg";
			File pic_dir = new File(pre_path + "/rc/pics/weather/city/");

			File storeFile = new File(pic_path);
			if (!storeFile.exists() && c_info != null) {
				if (!pic_dir.exists())
					pic_dir.mkdirs();

				String url = "http://www.grandes.com.cn" + c_info.pic_path;
				Log.v("get city image", url + "   " + pic_path);

				HttpGet http_request = new HttpGet(url);
				// 取得HttpClient 对象
				HttpClient httpc_lient = new DefaultHttpClient();
				try {
					// 请求httpClient ，取得HttpRestponse
					HttpResponse http_response = httpc_lient
							.execute(http_request);
					if (http_response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						Log.v("get city image", " HttpStatus.SC_OK");
						// 取得相关信息 取得HttpEntiy
						HttpEntity http_entity = http_response.getEntity();
						if (http_entity != null) {
							Log.v("get city image", "http_entity OK!");
							// 获得一个输入流
							InputStream input = http_entity.getContent();
							BufferedInputStream bis = new BufferedInputStream(
									input);
							Log.v("get city image", "buffer input stream!");

							FileOutputStream output = new FileOutputStream(
									storeFile);
							Log.v("get city image", "file output stream!");

							// 得到网络资源并写入文件
							byte b[] = new byte[1024];
							int j = 0;
							while ((j = bis.read(b)) != -1)
								output.write(b, 0, j);

							Log.v("get city image", "write OK!");

							input.close();
							output.close();
						}
					}

				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					Log.v("exception", "22222222222222");
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.v("exception", "3333333333333");
					e.printStackTrace();
				}
			} else
				System.out.println("**********file is exist!");

			System.out.println("............. 结束");

			// 发送消息通知更新UI
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putSerializable("c_data", c_data);
			data.putString("pic_path", pic_path);
			msg.what = 2;
			msg.setData(data);
			WeatherDataPro.this.myHandler.sendMessage(msg);

		}
	}
}
