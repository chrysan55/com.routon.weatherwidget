package com.routon.weatherwidget;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.routon.widgets.CustomDialog;

//@SuppressLint({ "NewApi", "NewApi" })
public class MainActivity extends Activity {

	public static final int ID_WEATHER_ICON = 10001;
	private static final int ADD_NEW_CITY = 1;
	private static final int UPDATE_SEARCH_CITYLIST = 2;
	private static final int UPDATE_SHIELD = 3;
	private static final int DIRECTION_LEFT = 1;
	private static final int DIRECTION_RIGHT = 2;
	private static final int ANIMATION_FOCUS_OUT = 1;
	private static final int ANIMATION_FOCUS_IN = 2;
	private static final int ANIMATION_FOCUS_MOVE = 4;
	private static final int ANIMATION_DURATION_CITY_TO_HEAD = 300;
	private static final int ANIMATION_DURATION_FOCUS_MOVE = 300;

	private PictureLayout weather_main = null;
	private FrameLayout cities_panel = null;
	private View search = null;
	FrameLayout searchLayout = null;
	private TextView searchTmpView = null;
	CityChoosePanel provinceListView = null;
	CityChoosePanel cityListView = null;
	ImageView searchFocus = null;

	private ArrayList<CityInfo> name_list = null;
	private ArrayList<CityInfo> provinceList = null;
	ArrayList<CityInfo> cityOfProvinceList;
	private int currentProvinceIndex = -1;
	private HashMap<CityInfo, ArrayList<CityInfo>> cityHashMap = null;
	private List<CityData> city_list = null;
	WeatherDataPro weather_data = null;

	private ArrayList<HashMap<String, String>> hash_list = null;
	private ArrayList<HashMap<String, String>> provinceHashList = null;
	private ArrayList<HashMap<String, Object>> cityHashList = null;

	private Handler handler = null;
	private CityData updateCD = null;
	private ImageView weather_icon = null;

	private float button_gap;
	private float board_start_x;
	private int focus_index;
	private float button_start_x;
	private float button_start_y;
	private boolean animation_completed = true;
	private int animation_search = 0;
	private boolean flag = false;
	private boolean cityListChanged = false;
	private boolean repick = false;
	private boolean picked = false;
	private boolean needBoardViewAnimation = true;
	private String pickedCityName = null;
	private AnimatorSet weather_btn_anim_set = new AnimatorSet();

	private boolean stopUpdate = false;

	receiver sys_receiver;
	ImageView panel_pic;
	private String tempCity = null;
	private boolean openAnimation = true;

	WindowManager windowManager = null;

	private ExecutorService executorService = Executors.newFixedThreadPool(1);

	// private float search_scale_x;
	// private float search_scale_y;

	static private String pre_path = "/hdisk/";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		// .detectDiskReads().detectDiskWrites().detectNetwork() //
		// 这里可以替换为detectAll()
		// // 就包括了磁盘读写和网络I/O
		// .penaltyLog() // 打印logcat，当然也可以定位到dropbox，通过文件保存相应的log
		// .build());
		// StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		// .detectLeakedSqlLiteObjects().penaltyLog() // 打印logcat
		// .penaltyDeath().build());

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		//得到WindowManager
		windowManager = (WindowManager) getWindowManager();

		// 接收系统关机广播，若此时处在输入法界面则返回主界面
		sys_receiver = new receiver();
		IntentFilter filter = new IntentFilter();
		// 只接收发送到action为"Intent.ACTION_CLOSE_SYSTEM_DIALOGS"的intent
		filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		filter.addAction("tmpCity");
		filter.addAction("tellweatherwidget");
		MainActivity.this.registerReceiver(sys_receiver, filter);

		// 接收Intent传入的临时城市
		Bundle bundle = this.getIntent().getExtras();
		// Bundle bundle = new Bundle();
		// bundle.putString("city", "北  京");
		if (bundle != null) {
			if (bundle.containsKey("city")) {
				tempCity = bundle.getString("city");
				if (tempCity.length() == 2 || tempCity.length() == 3) {
					// 处理空格
					String spaceString = "  ";
					StringBuffer tmp = new StringBuffer();
					for (int i = 0; i < tempCity.length() - 1; i++) {
						tmp.append(tempCity.charAt(i)).append(spaceString);
					}
					tmp.append(tempCity.charAt(tempCity.length() - 1));
					tempCity = tmp.toString();
					Log.i("tag", "Bundle: tempCity = " + tempCity);
				}
			}
			if (bundle.containsKey("cmd_args"))
				if (bundle.getString("cmd_args").equals("NoAnimation"))
					openAnimation = false;
		}

		cities_panel = (FrameLayout) findViewById(R.id.cities_panel);
		if (openAnimation == true) {
			weather_main = (PictureLayout) findViewById(R.id.weathermain);
			animation_completed = false;
			weather_main.startTransition(false);
		} else {
			weather_main_init();
			weather_add_city_button();
			weather_panel_animation(true);
			weather_parse_city_xml();
		}
		// weather_main_init();
		// weather_add_city_button();

		// weather_main_init();
		// weather_add_city_button();
		// weather_panel_animation(true);
		// weather_parse_city_xml();

	}

	public void onPause() {
		Log.i("onPause", "onPause");
		actions_at_exit();
		super.onPause();
	}

	public void onResume() {
		Log.i("TAG", "######################" + search);
		if (search != null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(search, 0);
		}
		super.onResume();
	}

	//
	// protected void onNewIntent(Intent intent) {
	// super.onNewIntent(intent);
	// setIntent(intent);
	// }

	public void onDestroy() {
		super.onDestroy();
	}

	public void actions_at_exit() {
		int i = 0;
		CityData c_data = null;
		Log.i("tag", "ACTIONS AT EXIT!");

		weather_exit_animation();

		if (cityListChanged == true) {
			for (i = 0; i < city_list.size(); i++) {
				c_data = city_list.get(i);
				if (c_data.is_default == true && i > 0) {
					city_list.set(i, city_list.get(0));
					city_list.set(0, c_data);
					break;
				}
			}

			// write mntn.ini
			c_data = null;
			String index = null;
			int cityNo = 0;
			IniReader reader = new IniReader(pre_path + "/etc/mntn/weather.ini");
			for (i = 0; i < city_list.size(); i++) {
				c_data = null;
				c_data = city_list.get(i);

				if (c_data.is_temporary == false) {
					index = null;
					index = new String("FavoriteCityNo[" + cityNo + "]");
					reader.SetValue("Weather", index, c_data.c_id);

					index = null;
					index = new String("FavoriteCityName[" + cityNo + "]");
					reader.SetValue("Weather", index, c_data.c_name);
					System.out.printf("write 城市：%s， %s, %d\n", city_list.get(i).c_name, city_list.get(i).c_id, i);
					cityNo++;
				}
				if (cityNo == 5)
					break;
			}

			for (int j = cityNo; j < 5; j++) {
				index = null;
				index = new String("FavoriteCityNo[" + j + "]");
				reader.SetValue("Weather", index, "");

				index = null;
				index = new String("FavoriteCityName[" + j + "]");
				reader.SetValue("Weather", index, "");
				System.out.printf("write empty city at [%d]", j);
			}

			reader.write();
		}
		// File iniFile = new File(pre_path +"/etc/mntn/mntn.ini");
		// iniFile.
		try {
			MainActivity.this.unregisterReceiver(sys_receiver);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	void weather_write_default_city(CityData c_data) {
		if (c_data == null)
			return;
	}

	public void weather_main_init() {
		panel_pic = (ImageView) findViewById(R.id.panelpic);
		// panel_pic.setImageURI(Uri.fromFile(new File(pre_path
		// +"/rc/pics/weather/widget/widget_panel.png")));
		// panel_pic.setImageResource(R.drawable.widget_panel);
		weather_data = new WeatherDataPro(this.getResources());
		weather_main = (PictureLayout) findViewById(R.id.weathermain);
		cities_panel = (FrameLayout) findViewById(R.id.cities_panel);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case 1:
						if (tempCity != null)
							if (!weather_city_exist(tempCity)) {
								CityInfo c_info = null;
								CityData c_data = null;
								for (int i = 0; i < name_list.size(); i++) {
									c_info = null;
									c_info = name_list.get(i);
									if (c_info.name.equals(tempCity)) {
										c_data = new CityData();
										c_data.c_id = c_info.c_id;
										c_data.c_name = c_info.name;
										c_data.is_temporary = true;
										city_list.add(c_data);
										break;
									}
								}
								if (c_data == null) {
									Toast toast = Toast.makeText(getApplicationContext(), "找不到该城市", Toast.LENGTH_LONG);
									toast.setGravity(Gravity.CENTER, 0, 0);
									toast.show();
									Log.i("tag", "handler : toast!!!!!!!!!!");
								} else {
									weather_add_city_button();
									View btn = city_list.get(city_list.size() - 1).button_view;
									btn.setY(button_start_y);
									focus_index = city_list.size() - 1;
									btn.requestFocus();
								}
							}

						break;

					case UPDATE_SEARCH_CITYLIST:
						HashMap<String, Object> item;
						String currentCityName = null;
						int currentCityPosition = -1;
						if (focus_index < city_list.size()) {
							TextView cityNameTextView = (TextView) city_list.get(focus_index).button_view.findViewById(R.id.cityname);
							currentCityName = (String) cityNameTextView.getText();
						}
						if (!cityHashList.isEmpty()) {
							cityHashList.clear();
						}
						for (int i = 0; i < cityOfProvinceList.size(); i++) {
							item = new HashMap<String, Object>();
							item.put("NAME", cityOfProvinceList.get(i).name);
							if (cityOfProvinceList.get(i).name.equals(currentCityName)) {
								item.put("IMG", R.drawable.point);
							}
							cityHashList.add(item);
						}
						cityListView.setSelectionFromTop(1, 135);
						cityListView.invalidateViews();

						break;
					default:
						break;
				}
			}
		};
		weather_icon = new ImageView(this);
		weather_icon.setId(ID_WEATHER_ICON);

		button_gap = 119.0f + 15.0f;
		board_start_x = 276.0f;
		focus_index = 0;
		button_start_x = 60.0f;
		button_start_y = 16.0f;

		weather_main.setFocusable(true);
		weather_main.setFocusableInTouchMode(true);
		weather_main.requestFocus();

		weather_create_city_list();
		//		weather_add_btn_init();
	}

	void weather_parse_city_xml() {
		name_list = new ArrayList<CityInfo>();//name_list里面存储所有城市，可以用provinceList+cityHashMap来代替，但是要改很多地方，以后修改。
		provinceList = new ArrayList<CityInfo>();
		cityHashMap = new HashMap<CityInfo, ArrayList<CityInfo>>();
		Runnable run = new ParseThread(name_list, provinceList, cityHashMap, city_list, weather_data, handler);
		Thread th = new Thread(run);
		th.start();
	}

	void weather_add_btn_init() {
		/*final ImageButton add_btn = (ImageButton) findViewById(R.id.btn_add);
		// add_btn.setImageURI(Uri.fromFile(new File(pre_path
		// +"/rc/pics/weather/widget/btn_add_white.png")));
		add_btn.setImageResource(R.drawable.btn_add_white);
		add_btn.setVisibility(View.VISIBLE);
		add_btn.setFocusable(true);
		add_btn.setFocusableInTouchMode(true);
		add_btn.setX(button_start_x + city_list.size() * button_gap);
		add_btn.setY(-150);// 开始时隐藏

		// add btn focus
		add_btn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			// @Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (hasFocus) {
					Log.i("button_focus_change_listener", "button focus in ");
					// add_btn.setImageURI(Uri.fromFile(new File(pre_path
					// +"/rc/pics/weather/widget/btn_add_black.png")));
					add_btn.setImageResource(R.drawable.btn_add_black);
					city_list.get(focus_index).board_view.setVisibility(View.INVISIBLE);
					weather_create_search_dialog();
				} else {
					Log.i("button_focus_change_listener", "button focus out");
					// add_btn.setImageURI(Uri.fromFile(new File(pre_path
					// +"/rc/pics/weather/widget/btn_add_white.png")));
					add_btn.setImageResource(R.drawable.btn_add_white);
				}

			}
		});*/

		for (int i = 0; i < 5; i++) {
			final View button_view = LayoutInflater.from(this.getApplication()).inflate(R.layout.city_button, null);
			cities_panel.addView(button_view);
			TextView buttonText = (TextView) button_view.findViewById(R.id.cityname);
			buttonText.setText("城市" + (i + 1));
			ImageView buttonPic = (ImageView) button_view.findViewById(R.id.buttonpic);
			buttonPic.setAlpha(0.7f);
		}
	}

	void weather_create_city_list() {
		Log.i("weather_create_city_list", "weather_create_city_list");
		IniReader reader = null;
		try {
			reader = new IniReader(pre_path + "/etc/mntn/weather.ini");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		city_list = new ArrayList<CityData>();
		CityData c_data = null;
		String index = null;
		String c_name = null;
		String c_id = null;

		for (int i = 0; i < 5; i++) {
			c_data = null;
			index = null;
			c_name = null;
			c_id = null;
			try {
				index = new String("FavoriteCityNo[" + i + "]");
				c_id = reader.getValue("Weather", index);
				index = null;
				index = new String("FavoriteCityName[" + i + "]");
				c_name = reader.getValue("Weather", index);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.i("weather_create_city_list", "exception!");
				e.printStackTrace();
			}

			/* 适应平台数据变化。如果读到的是旧数据（两个字城市中间一个空格），则转为新的数据形式-两个字城市中间两个空格 */
			if (c_name != null)
				if (c_name.length() == 3) {
					StringBuilder sb = new StringBuilder();
					sb.append(c_name.charAt(0)).append("  ").append(c_name.charAt(2));
					c_name = sb.toString();
					cityListChanged = true;
				} else {
					cityListChanged = true;
				}

			if (i == 0) {
				if (c_id == null || c_name == null) {
					c_id = new String("420100");
					c_name = new String("武  汉");
					cityListChanged = true;
				} else {
					if ((c_id != null && c_id.length() < 1) || (c_name != null && c_name.length() < 1)) {
						c_id = new String("420100");
						c_name = new String("武  汉");
						cityListChanged = true;
					}
				}

				c_data = new CityData();
				c_data.c_id = c_id;
				c_data.c_name = c_name;
				c_data.is_default = true;
				city_list.add(c_data);

				System.out.println("城市名：　" + c_data.c_name + ";  城市Id：" + c_data.c_id);
			} else {
				if ((c_id != null && c_id.length() > 1) && (c_name != null && c_name.length() > 1)) {
					c_data = new CityData();
					c_data.c_id = c_id;
					c_data.c_name = c_name;
					c_data.is_default = false;
					city_list.add(c_data);

					System.out.println("城市名：　" + c_data.c_name + ";  城市Id：" + c_data.c_id);
				}
			}

		}

		for (int i = 0; i < city_list.size(); i++) {
			c_data = city_list.get(i);
			if (tempCity != null && c_data != null)
				if (tempCity.replaceAll(" ", "").equals(c_data.c_name.replaceAll(" ", ""))) {
					focus_index = i;
					tempCity = null;
				}
		}

		Log.i("weather_create_city_list", "ok!");
	}

	void weather_add_city_board(CityData c_data, int index) {
		Log.i("tag", "weather_add_city_board : index = " + index + "c_data = " + c_data.c_name);
		float start_y = 70.0f;
		float city_board_gap = 1280.0f;
		View board_view = LayoutInflater.from(this.getApplication()).inflate(R.layout.city_widget, null);
		weather_main.addView(board_view);
		c_data.board_view = board_view;

		FrameLayout city_board = (FrameLayout) board_view.findViewById(R.id.cityboard);
		city_board.setX(board_start_x + (index - focus_index + 1) * city_board_gap);
		city_board.setY(start_y);

		// ImageView def_pic =
		// (ImageView)c_data.board_view.findViewById(R.id.defpic);
		// def_pic.setImageURI(Uri.fromFile(new File(pre_path
		// +"/rc/pics/weather/widget/default.png")));

		Log.i("add_city_board", c_data.c_name + ":" + city_board.getX() + " " + city_board.getY());
	}

	void weather_add_city_button() {
		// create city button
		int i = 0;
		int maxCityNumber = (tempCity == null) ? 5 : 6;
		//		int maxCityNumber = 5;

		List<CityData> list = city_list;
		List<CityData> updateList = new ArrayList<CityData>();
		CityData c_data = null;

		for (i = 0; i < list.size(); i++) {
			c_data = list.get(i);
			Log.i("add_city_button", c_data.c_name + i);

			if (c_data.button_view == null) {
				final View button_view;
				updateList.add(c_data);

				if (cities_panel.getChildCount() < maxCityNumber + 1) {
					button_view = LayoutInflater.from(this.getApplication()).inflate(R.layout.city_button, null);
					cities_panel.addView(button_view);
				} else {
					button_view = cities_panel.getChildAt(i + 1);//city_list的順序一定要和顯示button的順序一致
					button_view.findViewById(R.id.buttonpic).setAlpha(1f);
				}
				c_data.button_view = button_view;

				RelativeLayout button = (RelativeLayout) button_view.findViewById(R.id.city_button);
				button.setX(button_start_x + i * button_gap);
				button.setY(-150);// 开始时隐藏状态

				button_view.setFocusable(true);
				button_view.setFocusableInTouchMode(true);

				TextView city_name = (TextView) button_view.findViewById(R.id.cityname);

				int len = c_data.c_name.length();
				System.out.println("***********add new city, cnt:" + len);
				if (c_data.c_name.indexOf("  ") != -1 && len == 7) {
					c_data.c_name.replaceAll("  ", " ");
					len = 3;
				}
				if (len >= 5) {
					city_name.setTextScaleX((float) (105.0 / (len * 25)));
				}

				city_name.setText(c_data.c_name);
				// city_name.setScaleX(0.8f);

				ImageView btn = (ImageView) button_view.findViewById(R.id.buttonpic);
				// btn.setImageURI(Uri.fromFile(new File(pre_path
				// +"/rc/pics/weather/widget/btn_white.png")));
				btn.setImageResource(R.drawable.btn_white);

				// add city_board, request xml file
				boolean add_bitmap = false;
				weather_add_city_board(c_data, i);
				try {
					// if (focus_index == city_list.size() - 1) {
					// add_bitmap = true;
					// System.out
					// .println("***********add new city, add bitmap!***********");
					// }
					// weather_data.weather_request_xml_data(c_data,
					// add_bitmap);
					weather_data.weather_get_city_image(c_data, name_list);

				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// button on_key_event
				button_view.setOnKeyListener(new View.OnKeyListener() {
					// @Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						// TODO Auto-generated method stub
						int i = 0;
						CityData c_data = null;
						Log.i("tag", "focus_child = " + weather_main.getFocusedChild());
						if (event.getAction() == KeyEvent.ACTION_DOWN) {
							System.out.println("key value is: " + keyCode + " , animation_completed = " + animation_completed);
							switch (keyCode) {
								case KeyEvent.KEYCODE_DPAD_LEFT: // left
								case KeyEvent.KEYCODE_DPAD_UP:
									if ((focus_index > 0) && (animation_completed == true)) {
										animation_completed = false;
										// Log.v("left key:", "focus_index = "+
										// focus_index);
										focus_index--;
										c_data = city_list.get(focus_index);
										c_data.button_view.requestFocus();
										// weather_move_all_city_board(); // move
										// all
										// city
										// board
										// left

										// 影响按键响应速度
										// weather_data.weather_update_animationdrawable_bitmap(city_list.get(focus_index));

										// updateCD =
										// city_list.get(focus_index);
										// new Thread(){
										// public void run(){
										// handler.post(runUpdateBitmap);
										// }
										// }.start();

									}
									return true;
								case KeyEvent.KEYCODE_DPAD_RIGHT: // right
								case KeyEvent.KEYCODE_DPAD_DOWN:
									if (focus_index == cities_panel.getChildCount() - 2)
										return true;

									if (focus_index < cities_panel.getChildCount() - 2 && animation_completed == true) {
										animation_completed = false;
										focus_index++;
										View buttonView = cities_panel.getChildAt(focus_index + 1);
										buttonView.requestFocus();
									}
									/*if ((focus_index < city_list.size() - 1) && (animation_completed == true)) // move
																												// focus
																												// to
																												// city_button
									{
										animation_completed = false;
										focus_index++;
										c_data = city_list.get(focus_index);
										c_data.button_view.requestFocus();

									} else // move focus on add button
									{
										if ((provinceList.size() > 20) && (animation_completed == true)) // city.xml
																											// 还没解析完成不能添加城市
										{
											System.out.printf("************right*********************\n");
											focus_index++;
											View emptyButton = cities_panel.getChildAt(focus_index + 1);
											if (emptyButton.getVisibility() == View.VISIBLE) {
												animation_completed = false;
												emptyButton.requestFocus();
											}
										}
									}*/

									return true;
								case KeyEvent.KEYCODE_D://删除城市，保留测试用，正式版本去掉
									if (city_list.size() == 1)
										return false;
									Log.i("Tag", "==================  animation_completed = " + animation_completed);
									if (animation_completed == true)
										weather_delete_focus_city();
									break;
								case KeyEvent.KEYCODE_MENU:
									for (i = 0; i < city_list.size(); i++) {
										c_data = city_list.get(i);
										if (c_data.is_default == true) {
											c_data.is_default = false;
											break;
										}
									}

									c_data = city_list.get(focus_index);
									c_data.is_default = true;
									c_data.is_temporary = false;
									cityListChanged = true;
									weather_city_to_head(focus_index);

									/*ImageView flag = (ImageView) findViewById(R.id.flag);
									flag.setX(105 + focus_index * button_gap);*/

									// 改变默认城市，发广播消息通知
									Intent intent = new Intent("WEATHER");
									intent.putExtra("msg", c_data.c_id + ":" + c_data.c_name);
									sendBroadcast(intent);

									return true;

								case KeyEvent.KEYCODE_ENTER:
									city_list.get(focus_index).board_view.setVisibility(View.INVISIBLE);
									weather_create_search_dialog();

									return true;

								case KeyEvent.KEYCODE_S:
									stopUpdate = !stopUpdate;

								default:
									break;
							}
						}
						return false;
					}
				});

				// city button focus
				button_view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
					// @Override
					public void onFocusChange(View v, boolean hasFocus) {
						// TODO Auto-generated method stub
						if (hasFocus) {
							Log.i("onFocusChange", "button focus in : needAnim = " + needBoardViewAnimation);
							ImageView button_pic = (ImageView) button_view.findViewById(R.id.buttonpic);
							button_pic.setImageResource(R.drawable.btn_blue);

							TextView city_name = (TextView) button_view.findViewById(R.id.cityname);
							city_name.setTextColor(Color.WHITE);
							if (needBoardViewAnimation)
								weather_move_all_city_board();
							else
								needBoardViewAnimation = true;
						} else {
							Log.i("onFocusChange", "button focus out");
							ImageView button_pic = (ImageView) button_view.findViewById(R.id.buttonpic);
							button_pic.setImageResource(R.drawable.btn_white);

							TextView city_name = (TextView) button_view.findViewById(R.id.cityname);
							city_name.setTextColor(Color.BLACK);
						}

					}
				});
			}

		}

		try {
			// if (focus_index == city_list.size() - 1) {
			// add_bitmap = true;
			// System.out
			// .println("***********add new city, add bitmap!***********");
			// }
			weather_data.weather_request_xml_data(updateList, name_list);
			// weather_data.weather_get_city_image(updateList, name_list);

		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (cities_panel.getChildCount() < (maxCityNumber + 1) && i < maxCityNumber) {/*cities_panel里面应该有maxCityNumber个城市按钮和1个背景ImageView共6个孩子*/
			while (i < maxCityNumber) {
				final View button_view = LayoutInflater.from(this.getApplication()).inflate(R.layout.city_button, null);
				cities_panel.addView(button_view);
				TextView buttonText = (TextView) button_view.findViewById(R.id.cityname);
				buttonText.setText("城市" + (i + 1));
				ImageView buttonPic = (ImageView) button_view.findViewById(R.id.buttonpic);
				buttonPic.setImageResource(R.drawable.btn_white);
				buttonPic.setAlpha(0.7f);

				button_view.setX(button_start_x + i * button_gap);
				button_view.setY(-150);// 开始时隐藏状态
				button_view.setFocusable(true);
				button_view.setFocusableInTouchMode(true);

				button_view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
					// @Override
					public void onFocusChange(View v, boolean hasFocus) {
						// TODO Auto-generated method stub
						if (hasFocus) {
							Log.i("button_focus_change_listener", "button focus in ");
							// add_btn.setImageURI(Uri.fromFile(new File(pre_path
							// +"/rc/pics/weather/widget/btn_add_black.png")));
							city_list.get(focus_index - 1).board_view.setVisibility(View.INVISIBLE);
							weather_create_search_dialog();
						}
					}
				});
				i++;
			}
		}
	}

	void weather_create_search_dialog() {

		if (search == null) {
			searchLayout = new FrameLayout(getApplicationContext());
			search = LayoutInflater.from(this.getApplication()).inflate(R.layout.sreach_panel, null);
			provinceListView = (CityChoosePanel) search.findViewById(R.id.search_province_list);
			cityListView = (CityChoosePanel) search.findViewById(R.id.search_city_list);
			searchFocus = (ImageView) search.findViewById(R.id.search_button);
			TextView provinceShield = (TextView) search.findViewById(R.id.provinceShield);
			TextView cityShield = (TextView) search.findViewById(R.id.cityShield);
			final TextView searchTitle = (TextView) search.findViewById(R.id.search_title);

			provinceListView.setShieldView(provinceShield);
			cityListView.setShieldView(cityShield);
			provinceListView.setSoundEffectsEnabled(false);
			cityListView.setSoundEffectsEnabled(false);

			//			weather_main.addView(search);
			//加入窗口
			WindowManager.LayoutParams wmLayoutParams = new WindowManager.LayoutParams();
			wmLayoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
			wmLayoutParams.dimAmount = 0f;
			wmLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
			wmLayoutParams.format = PixelFormat.RGBA_8888;
			wmLayoutParams.width = 713;
			wmLayoutParams.height = 474;
			wmLayoutParams.gravity = Gravity.CENTER;
			searchLayout.addView(search);
			windowManager.addView(searchLayout, wmLayoutParams);

			//如果无网络数据，不显示列表界面
			if (provinceList.size() == 0) {
				provinceListView.setVisibility(View.INVISIBLE);
				cityListView.setVisibility(View.INVISIBLE);
				searchFocus.setVisibility(View.INVISIBLE);
				searchTitle.setText("请确定网络连接，并重试");

				return;
			}
			/*FrameLayout layout_search = (FrameLayout) search.findViewById(R.id.search1);

			layout_search.setY(50 + 38);
			layout_search.setX(add_btn.getX() - 124);*/

			// search.setPivotX(add_btn.getX() + 22.0f);
			search.setPivotX(146.0f);
			search.setPivotY(0.0f);

			search.setScaleX(0.1f);
			search.setScaleY(0.1f);
			search.setAlpha(0.0f);

			search.setFocusable(true);
			search.setFocusableInTouchMode(true);

			/*final AutoCompleteTextView autocomplete = (AutoCompleteTextView) search.findViewById(R.id.autoComplete);

			autocomplete.setFocusable(true);
			autocomplete.setFocusableInTouchMode(true);
			autocomplete.setNextFocusLeftId(R.id.autoComplete);

			if (hash_list == null) {
				hash_list = new ArrayList<HashMap<String, String>>();
				HashMap<String, String> item;
				for (int i = 0; i < name_list.size(); i++) {
					item = new HashMap<String, String>();
					item.put("brandSearchText", name_list.get(i).c_id + "  " + name_list.get(i).spell);
					item.put("brandName", name_list.get(i).name.replace(" ", ""));
					hash_list.add(item);
					// System.out.printf("%s, key:%s \n", item.get("brandName")
					// +":  ", name_list.get(i).name.replace(" ",
					// "")+name_list.get(i).name.replace(" ", "").length());
				}
			}*/

			if (provinceHashList == null) {
				provinceHashList = new ArrayList<HashMap<String, String>>();
				HashMap<String, String> item;
				for (int i = 0; i < provinceList.size(); i++) {
					item = new HashMap<String, String>();
					item.put("NAME", provinceList.get(i).name);
					provinceHashList.add(item);
				}
			}

			if (cityHashList == null) {
				cityHashList = new ArrayList<HashMap<String, Object>>();
				cityListView.setScaleY(0);
			}

			/*SimpleAdapter adapter = new SimpleAdapter(this, hash_list, R.layout.search, new String[] { "brandSearchText", "brandName" },
					new int[] { R.id.searchText, R.id.brandName });
			autocomplete.setAdapter(adapter);
			*/
			SimpleAdapter provinceAdapter = new SimpleAdapter(this, provinceHashList, R.layout.search_list, new String[] { "NAME" },
					new int[] { R.id.searchText });
			SimpleAdapter cityAdapter = new SimpleAdapter(this, cityHashList, R.layout.search_list, new String[] { "NAME", "IMG" },
					new int[] { R.id.searchText, R.id.point });
			provinceListView.setAdapter(provinceAdapter);
			cityListView.setAdapter(cityAdapter);
			provinceListView.requestFocus();

			OnFocusChangeListener provinceListViewFocusChangeListener = new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					// TODO Auto-generated method stub
					ObjectAnimator animator = null;
					CityChoosePanel view = (CityChoosePanel) v;
					if (hasFocus) {
						view.getShieldView().setVisibility(View.INVISIBLE);
						animator = ObjectAnimator.ofFloat(v, "ScaleY", 1);
						animator.setDuration(ANIMATION_DURATION_FOCUS_MOVE);
						animation_search |= ANIMATION_FOCUS_IN;
						animator.start();
						animator.addListener(new AnimatorListener() {

							@Override
							public void onAnimationStart(Animator animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationRepeat(Animator animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationEnd(Animator animation) {
								// TODO Auto-generated method stub
								animation_search &= ~ANIMATION_FOCUS_IN;
								Log.i("has", "animation_search &= ~ANIMATION_FOCUS_IN = " + animation_search);
							}

							@Override
							public void onAnimationCancel(Animator animation) {
								// TODO Auto-generated method stub

							}
						});
					} else {
						HashMap<String, String> selected = (HashMap<String, String>) view.getItemAtPosition(view.getSelectedItemPosition());
						Log.i("tag", "hasNoFocus : selectedId = " + view.getSelectedItemPosition() + " , selected = " + selected);
						if (selected != null) {
							String shieldText = selected.get("NAME");
							view.getShieldView().setText(shieldText);
						}

						animator = ObjectAnimator.ofFloat(v, "ScaleY", 0);
						animator.setDuration(ANIMATION_DURATION_FOCUS_MOVE);
						animation_search |= ANIMATION_FOCUS_OUT;
						animator.start();
						animator.addListener(new AnimatorListener() {

							@Override
							public void onAnimationStart(Animator animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationRepeat(Animator animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationEnd(Animator animation) {
								// TODO Auto-generated method stub
								CityChoosePanel view = (CityChoosePanel) ((ObjectAnimator) animation).getTarget();
								view.getShieldView().setVisibility(View.VISIBLE);
								animation_search &= ~ANIMATION_FOCUS_OUT;
								Log.i("has", "animation_search &= ~ANIMATION_FOCUS_OUT = " + animation_search);
							}

							@Override
							public void onAnimationCancel(Animator animation) {
								// TODO Auto-generated method stub

							}
						});
					}
				}
			};

			OnFocusChangeListener cityListViewFocusChangeListener = new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					// TODO Auto-generated method stub
					ObjectAnimator animator = null;
					CityChoosePanel view = (CityChoosePanel) v;
					if (hasFocus) {
						view.getShieldView().setVisibility(View.INVISIBLE);
						animator = ObjectAnimator.ofFloat(v, "ScaleY", 1);
						animator.setDuration(ANIMATION_DURATION_FOCUS_MOVE);
						animation_search |= ANIMATION_FOCUS_IN;
						animator.start();
						animator.addListener(new AnimatorListener() {

							@Override
							public void onAnimationStart(Animator animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationRepeat(Animator animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationEnd(Animator animation) {
								// TODO Auto-generated method stub
								animation_search &= ~ANIMATION_FOCUS_IN;
								Log.i("has", "animation_search &= ~ANIMATION_FOCUS_IN = " + animation_search);
							}

							@Override
							public void onAnimationCancel(Animator animation) {
								// TODO Auto-generated method stub

							}
						});
					} else {
						HashMap<String, String> selected = (HashMap<String, String>) view.getItemAtPosition(view.getSelectedItemPosition());
						Log.i("tag", "hasNoFocus : selectedId = " + view.getSelectedItemPosition() + " , selected = " + selected);
						if (selected != null) {
							// by hxb
							//String shieldText = selected.get("NAME");
							//view.getShieldView().setText(shieldText);
						}

						animator = ObjectAnimator.ofFloat(v, "ScaleY", 0);
						animator.setDuration(ANIMATION_DURATION_FOCUS_MOVE);
						animation_search |= ANIMATION_FOCUS_OUT;
						animator.start();
						animator.addListener(new AnimatorListener() {

							@Override
							public void onAnimationStart(Animator animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationRepeat(Animator animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationEnd(Animator animation) {
								// TODO Auto-generated method stub
								CityChoosePanel view = (CityChoosePanel) ((ObjectAnimator) animation).getTarget();
								view.getShieldView().setVisibility(View.VISIBLE);
								animation_search &= ~ANIMATION_FOCUS_OUT;
								Log.i("has", "animation_search &= ~ANIMATION_FOCUS_OUT = " + animation_search);
							}

							@Override
							public void onAnimationCancel(Animator animation) {
								// TODO Auto-generated method stub

							}
						});
					}
				}
			};

			provinceListView.setOnFocusChangeListener(provinceListViewFocusChangeListener);
			cityListView.setOnFocusChangeListener(cityListViewFocusChangeListener);

			provinceListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> v, View arg1, int arg2, long arg3) {
					// TODO Auto-generated method stub
					if (animation_search == 0) {
						int selectedIndex = provinceListView.getSelectedItemPosition();
						if (currentProvinceIndex != selectedIndex) {
							currentProvinceIndex = selectedIndex;
							LinearLayout itemLayout = (LinearLayout) provinceListView.getSelectedView();
							TextView textView = (TextView) itemLayout.findViewById(R.id.searchText);
							String provinceName = (String) textView.getText();

							//找到对应省份的CityInfo
							CityInfo province = null;
							for (int i = 0; i < provinceList.size(); i++) {
								if (provinceList.get(i).name.equals(provinceName)) {
									province = provinceList.get(i);
									break;
								}
							}

							//通过省份CityInfo找到城市list
							cityOfProvinceList = cityHashMap.get(province);

							//更新Adapter
							handler.sendEmptyMessage(UPDATE_SEARCH_CITYLIST);
						}
						weather_search_focus_move(DIRECTION_RIGHT);
						cityListView.requestFocus();
					}
				}
			});

			provinceListView.setOnKeyListener(new View.OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					Log.i("tag", "provinceList.onKey : keyCode = " + keyCode);
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						if (animation_search == 0 && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
							provinceListView.getOnItemClickListener().onItemClick(provinceListView, provinceListView.getSelectedView(),
									provinceListView.getSelectedItemPosition(), provinceListView.getSelectedItemId());
							return true;
						}

						if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) {
							picked = false;
							weather_search_dialog_focusout_anmi();
							return true;
						}
					}
					return false;
				}
			});

			cityListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> v, View arg1, int arg2, long arg3) {
					// TODO Auto-generated method stub
					if (animation_search == 0) {
						LinearLayout itemLayout = (LinearLayout) cityListView.getSelectedView();
						TextView tv = (TextView) itemLayout.findViewById(R.id.searchText);
						pickedCityName = (String) tv.getText();

						// 添加城市到city_list
						if (!weather_city_exist(pickedCityName)) {
							picked = true;
							if (focus_index != 0) {
								/* --废弃，有CustomDialog替代 ---by hxb 
								AlertDialog.Builder alertDefaultCity = new Builder(MainActivity.this);
								alertDefaultCity.setMessage("是否设置为默认城市？？？？？");
								alertDefaultCity.setPositiveButton("是", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										dialog.dismiss();
										addCity(pickedCityName, true);
										weather_search_dialog_focusout_anmi();
									}
								});
								alertDefaultCity.setNegativeButton("否", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										dialog.dismiss();
										addCity(pickedCityName, false);
										weather_search_dialog_focusout_anmi();
									}
								});
								alertDefaultCity.create().show();
								*/
								try {
									CustomDialog.Builder alertBuilder = new CustomDialog.Builder(MainActivity.this);
									alertBuilder.setMessage("是否设置为默认城市？");
									alertBuilder.create();
									alertBuilder.setPositiveButton("是", new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											// TODO Auto-generated method stub
											dialog.dismiss();
											addCity(pickedCityName, true);
											weather_search_dialog_focusout_anmi();
										}
									});
									alertBuilder.setNegativeButton("否", new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											// TODO Auto-generated method stub
											dialog.dismiss();
											addCity(pickedCityName, false);
											weather_search_dialog_focusout_anmi();
										}
									});
									CustomDialog alertDialog = alertBuilder.create();
									alertDialog.show();
								} catch (Exception ex) {
									Log.d("AlertDialog", "AlertDialog error: " + ex.getLocalizedMessage());
								}
							} else {
								addCity(pickedCityName, true);
								weather_search_dialog_focusout_anmi();
							}

						} else {
							searchTitle.setText("该城市已经选择，请重新选择");
							repick = true;
						}
					}
				}
			});

			cityListView.setOnKeyListener(new View.OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					if (animation_search == 0 && event.getAction() == KeyEvent.ACTION_DOWN) {
						if (repick == true) {
							repick = false;
							searchTitle.setText("请选择城市");
						}
						if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK 
								|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
							weather_search_focus_move(DIRECTION_LEFT);
							provinceListView.requestFocus();
							return true;
						}
					}
					return false;
				}
			});

			ObjectAnimator scale_x = ObjectAnimator.ofFloat(search, "scaleX", 1.0f);
			scale_x.setDuration(500);
			ObjectAnimator scale_y = ObjectAnimator.ofFloat(search, "scaleY", 1.0f);
			scale_y.setDuration(500);
			ObjectAnimator alpha = ObjectAnimator.ofFloat(search, "alpha", 1.0f);
			alpha.setDuration(500);

			AnimatorSet animator_set = new AnimatorSet();
			animator_set.play(scale_x).with(scale_y).with(alpha);
			animator_set.setInterpolator(new AccelerateInterpolator(1.0f));
			animator_set.start();

			//			animator_set.addListener(new AnimatorListenerAdapter() {
			//				@Override
			//				public void onAnimationEnd(Animator animation) {
			//					// TODO Auto-generated method stub
			//					autocomplete.requestFocus();
			//					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			//					imm.showSoftInput(autocomplete, 0);
			//				}
			//			});
			//
			//			autocomplete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			//
			//				public void onFocusChange(View v, boolean hasFocus) {
			//					// TODO Auto-generated method stub
			//					Log.i("tag", "======================" + hasFocus);
			//					if (hasFocus) {
			//						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			//						Log.i("tag", "======================" + imm.isActive());
			//						if (!imm.isActive())
			//							imm.showSoftInput(autocomplete, 0);
			//					} else {
			//						InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			//						imm.hideSoftInputFromWindow(autocomplete.getWindowToken(), 0);
			//					}
			//				}
			//			});
			//
			//			autocomplete.setOnKeyListener(new View.OnKeyListener() {
			//				// @Override
			//				public boolean onKey(View v, int keyCode, KeyEvent event) {
			//					// TODO Auto-generated method stub
			//					Editable text = null;
			//
			//					if (event.getAction() == KeyEvent.ACTION_DOWN) {
			//						Log.i("TAG", "**********ACTION_DOWN:" + autocomplete.isInEditMode() + "||" + autocomplete.isInputMethodTarget()
			//								+ "||" + autocomplete.isInTouchMode());
			//
			//						System.out.println("key value is: " + keyCode);
			//						switch (keyCode) {
			//							case KeyEvent.KEYCODE_META_RIGHT: // 输入法切换 118
			//
			//								break;
			//							case KeyEvent.KEYCODE_DPAD_UP:
			//
			//								// 当焦点在文本框上且文本框无内容时，再按向上键，autocompletetextview控件默认将UI焦点转移至其他控件，在此屏蔽按键向上转移
			//								if (autocomplete.getListSelection() == -1)
			//									return true;
			//
			//								break;
			//
			//							case KeyEvent.KEYCODE_ESCAPE:
			//								// 当焦点处于文本框时esc退出，防止只有输入法退出，无法再次调出输入法
			//								// if (autocomplete.getListSelection() == -1)
			//								weahter_search_dialog_focusout_anmi();
			//								break;
			//
			//							default:
			//								break;
			//						}
			//					}
			//
			//					return false;
			//				}
			//
			//			});
			//
			//			autocomplete.setOnItemClickListener(new OnItemClickListener() {
			//				// @Override
			//				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			//					TextView tv = (TextView) arg1.findViewById(R.id.brandName);
			//					autocomplete.setText(tv.getText().toString());
			//					autocomplete.setSelection((autocomplete.getText().toString()).length());
			//
			//					TextView text = (TextView) arg1.findViewById(R.id.searchText);
			//					String a[] = ((String) text.getText()).split(" ");
			//
			//					// 添加城市到city_list
			//					if (!weather_city_exist(a[0])) {
			//						CityInfo c_info = null;
			//						for (int i = 0; i < name_list.size(); i++) {
			//							c_info = null;
			//							c_info = name_list.get(i);
			//							if (c_info.c_id.equals(a[0])) {
			//								CityData c_data = new CityData();
			//								c_data.c_id = c_info.c_id;
			//								c_data.c_name = c_info.name;
			//								city_list.add(c_data);
			//								cityListChanged = true;
			//								break;
			//							}
			//						}
			//					}
			//
			//					weahter_search_dialog_focusout_anmi();
			//
			//				}
			//			});

		}
	}

	/*
	 * 在city_list中加入新的城市
	 *
	 * @
	 */
	void addCity(String a, boolean isDefault) {
		CityInfo c_info = null;
		CityData c_data = null;

		//如果设置了默认城市，把原先的默认城市取消
		if (isDefault) {
			for (int i = 0; i < city_list.size(); i++) {
				CityData tmp_c_data = city_list.get(i);
				if (tmp_c_data.is_default == true) {
					tmp_c_data.is_default = false;
					break;
				}
			}
		}

		for (int i = 0; i < cityOfProvinceList.size(); i++) {
			c_info = cityOfProvinceList.get(i);
			Log.i("tag", "c_info = " + c_info.name + " , a = " + a);
			if (c_info.name.equals(a)) {
				c_data = new CityData();
				c_data.c_id = c_info.c_id;
				c_data.c_name = c_info.name;
				if (isDefault || focus_index == 0) {
					c_data.is_default = true;
					c_data.is_temporary = false;
				}
				if (focus_index < city_list.size()) {
					Log.i("tag", "line 1284 : focus_id = " + focus_index);
					CityData delCity = city_list.get(focus_index);
					weather_delete_city(delCity);
					city_list.add(focus_index, c_data);
				} else {
					city_list.add(c_data);
				}
				cityListChanged = true;
				break;
			}
		}

		if (isDefault) {
			// 改变默认城市，发广播消息通知
			Intent intent = new Intent("WEATHER");
			intent.putExtra("msg", c_data.c_id + ":" + c_data.c_name);
			sendBroadcast(intent);
		}

	}

	@Override
	public void onBackPressed() {
		// android.os.Process.killProcess(android.os.Process.myPid());
		// finish();
		/*if (search != null)
			weather_search_dialog_focusout_anmi();
		else*/
		Log.i("tag", "onBackPressed : search = " + search);
		if (search == null && animation_completed == true && weather_btn_anim_set.isRunning() == false
				&& weather_main.isAnimating() == false) {
			actions_at_exit();
		}
		// else
		// super.onBackPressed();
	}

	void weather_search_focus_move(int direction) {
		switch (direction) {
			case DIRECTION_LEFT:
				ObjectAnimator animatorR2L = ObjectAnimator.ofFloat(searchFocus, "x", 104f);//  104来源与sreach_panel.xml
				animatorR2L.setDuration(ANIMATION_DURATION_FOCUS_MOVE);
				animation_search |= ANIMATION_FOCUS_MOVE;
				animatorR2L.start();
				animatorR2L.addListener(new AnimatorListener() {

					@Override
					public void onAnimationStart(Animator animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationRepeat(Animator animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationEnd(Animator animation) {
						// TODO Auto-generated method stub
						animation_search &= ~ANIMATION_FOCUS_MOVE;
						Log.i("has", "animation_search &= ~ANIMATION_FOCUS_MOVE = " + animation_search);
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						// TODO Auto-generated method stub

					}
				});
				break;

			case DIRECTION_RIGHT:
				ObjectAnimator animatorL2R = ObjectAnimator.ofFloat(searchFocus, "x", 422f);
				animatorL2R.setDuration(ANIMATION_DURATION_FOCUS_MOVE);
				animation_search |= ANIMATION_FOCUS_MOVE;
				animatorL2R.start();
				animatorL2R.addListener(new AnimatorListener() {

					@Override
					public void onAnimationStart(Animator animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationRepeat(Animator animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationEnd(Animator animation) {
						// TODO Auto-generated method stub
						animation_search &= ~ANIMATION_FOCUS_MOVE;
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						// TODO Auto-generated method stub

					}
				});
				break;

			default:
				break;
		}
	}

	void weather_search_dialog_focusout_anmi() {
		// 缩小,淡出
		Log.i("tag", "anim : search  =" + search);
		ObjectAnimator scale_x = ObjectAnimator.ofFloat(search, "scaleX", 0.0f);
		scale_x.setDuration(500);
		ObjectAnimator scale_y = ObjectAnimator.ofFloat(search, "scaleY", 0.0f);
		scale_y.setDuration(500);
		ObjectAnimator alpha = ObjectAnimator.ofFloat(search, "alpha", 0.0f);
		alpha.setDuration(500);

		AnimatorSet animator_set = new AnimatorSet();
		animator_set.play(scale_x).with(scale_y).with(alpha);
		animator_set.setInterpolator(new AccelerateInterpolator(1.0f));
		animation_search = 0;
		animator_set.start();

		animator_set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				animation_completed = true;
				//先把隐藏的界面显示出来
				Log.i("tag", "focus_index = " + focus_index + " , citylist.size = " + city_list.size());
				if (!picked && focus_index < city_list.size())
					city_list.get(focus_index).board_view.setVisibility(View.VISIBLE);
				else if (!picked && focus_index >= city_list.size() - 1)
					city_list.get(city_list.size() - 1).board_view.setVisibility(View.VISIBLE);
				else if (picked && focus_index >= city_list.size() - 1) {
					city_list.get(city_list.size() - 2).board_view.setVisibility(View.VISIBLE);
				}

				weather_add_city_button();
				if (!picked && focus_index >= city_list.size()) {
					focus_index = city_list.size() - 1;
				}
				//如果第一个城市不是默认城市，将后面的默认城市提前
				if (city_list.get(0).is_default == false) {
					for (int i = 1; i < city_list.size(); i++) {
						if (city_list.get(i).is_default == true)
							weather_city_to_head(i);
					}
				}

				Log.i("tag", "focus_id = " + focus_index + " , is_default = " + city_list.get(0).is_default);
				for (int i = 0; i < city_list.size(); i++) {
					Log.i("tag", " i = " + i + " , city_name = " + city_list.get(i).c_name);
				}
				View btn = city_list.get(focus_index).button_view;
				btn.setY(button_start_y);

				//				weather_main.removeView(search);
				windowManager.removeView(searchLayout);
				cities_panel.findViewById(R.id.panelpic).requestFocus();
				btn.requestFocus();
				Log.i("tag", "focus_child = " + weather_main.getFocusedChild() + " , btn = " + btn + " , cities_panel = " + cities_panel);
				search = null;
				searchLayout = null;
				cityOfProvinceList = null;
				provinceHashList = null;
				cityHashList = null;
				currentProvinceIndex = -1;
			}
		});
	}

	/*
	 * 将默认城市提到最前 
	 * 
	 *  @position : 默认城市原来的位置
	 **/

	void weather_city_to_head(int position) {
		if (position < 1 || position >= city_list.size())
			return;

		View buttonView = city_list.get(position).button_view;
		View boardView;
		AnimatorSet animatorSet = new AnimatorSet();
		ObjectAnimator[] animators = new ObjectAnimator[position + 1];

		//改变在city_list中的位置
		CityData defaultCity = city_list.get(position);
		city_list.remove(position);
		city_list.add(0, defaultCity);

		//改变位置，动画
		cities_panel.getChildAt(0).requestFocus();
		focus_index = 0;
		cities_panel.removeView(buttonView);
		cities_panel.addView(buttonView, 1);
		buttonView.setY(button_start_y);
		buttonView.setX(-button_gap);

		for (int i = 0; i <= position; i++) {
			buttonView = city_list.get(i).button_view;
			boardView = city_list.get(i).board_view;

			animators[i] = ObjectAnimator.ofFloat(buttonView, "x", button_start_x + i * button_gap);
			animators[i].setDuration(ANIMATION_DURATION_CITY_TO_HEAD);
			if (i > 0)
				animatorSet.play(animators[i - 1]).with(animators[i]);

			FrameLayout city_board = (FrameLayout) boardView.findViewById(R.id.cityboard);
			FrameLayout weatherIconFrameLayout = (FrameLayout) city_board.findViewById(R.id.weathericon);
			if (weatherIconFrameLayout.getChildCount() != 0)
				weatherIconFrameLayout.removeAllViews();
			city_board.setX(board_start_x + i * 1280);
		}
		animatorSet.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				animation_completed = true;
				updateCD = city_list.get(focus_index);
				updateCD.button_view.requestFocus();
				executorService.submit(new Runnable() {

					public void run() {
						// TODO Auto-generated method stub
						handler.post(runUpdateBitmap);
					}
				});
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub

			}
		});
		animation_completed = false;
		needBoardViewAnimation = false;
		animatorSet.start();
	}

	/*
	 * 检测已有城市中是否存在指定城市。
	 * 
	 * #Return: 当存在时返回true，否则返回false #Parameters:
	 * 
	 * @value 可以是城市编号@c_no，也可以是城市名称@c_name
	 */
	boolean weather_city_exist(String value) {
		if (value == null)
			return false;

		if (city_list != null)
			for (int i = 0; i < city_list.size(); i++) {
				if (city_list.get(i).c_id.equals(value))
					return true;
				if (city_list.get(i).c_name.equals(value))
					return true;
			}

		return false;
	}

	void weather_panel_animation(boolean down) {
		float pos_y = 0;

		if (down == true)
			pos_y = 0f;
		else
			pos_y = -108.0f;
		//
		// ObjectAnimator an = ObjectAnimator.ofFloat(cities_panel, "Y", pos_y);
		// an.setDuration(2000);
		// an.start();

		if (down)
			weather_button_drop_animation();
	}

	void weather_button_drop_animation() {
		Log.i("weather_button_drop_animation", "drop one by one");
		int i = 0;

		CityData c_data = null;
		panel_pic.setVisibility(View.VISIBLE);
		weather_main.findViewById(R.id.panelHint).setVisibility(View.VISIBLE);

		// weather_btn_anim_set = new AnimatorSet();
		if (weather_btn_anim_set.isRunning()) {
			weather_btn_anim_set.cancel();
		}
		ObjectAnimator[] button_an = new ObjectAnimator[cities_panel.getChildCount() - 1];
		for (i = 0; i < cities_panel.getChildCount() - 1; i++) {
			View button = cities_panel.getChildAt(i + 1);
			Log.i("tag", " cities_panel.getChildCount = " + cities_panel.getChildCount() + " , button = " + button);
			button_an[i] = ObjectAnimator.ofFloat(button, "Y", button_start_y);
			button_an[i].setDuration(200);

			if (i > 0)
				weather_btn_anim_set.play(button_an[i - 1]).before(button_an[i]);
		}

		weather_btn_anim_set.start();

		Log.i("button_animation", "try");
		/*try {
			ImageButton add_btn = (ImageButton) findViewById(R.id.btn_add);
			button_an[i] = ObjectAnimator.ofFloat(add_btn, "Y", button_start_y);
			button_an[i].setDuration(300);

			weather_btn_anim_set.play(button_an[i - 1]).before(button_an[i]);
			weather_btn_anim_set.start();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		Log.i("tag", "button_an[" + i + "]  =" + button_an[i - 1]);
		// the last button_drop_animation finish
		button_an[i - 1].addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator an) {
				int i = 0;
				CityData c_data = null;
				for (i = 0; i < city_list.size(); i++) {
					c_data = city_list.get(i);
					/*if (c_data.is_default) {
						ImageView flag = (ImageView) findViewById(R.id.flag);
						flag.setX(105 + i * button_gap);
						flag.setVisibility(View.VISIBLE);
					}*/

					if (i == focus_index) {
						// Log.v("focus", c_data.c_name + " set focus!");
						// if(c_data.button_view.isFocusable())
						// Log.v("focus", c_data.c_name + " is focusable!");

						c_data.button_view.requestFocus();
						// c_data.button_view.requestFocusFromTouch();

						// if(c_data.button_view.isFocused())
						// Log.v("focus", c_data.c_name + " has focus!");
					}
				}

			}
		});

	}

	public void weather_move_all_city_board() {
		int i = 0;
		CityData c_data = null;

		animation_completed = false;
		for (i = 0; i < city_list.size(); i++) {
			c_data = city_list.get(i);
			System.out.println("*********board_animation:" + i + "focus_index:" + focus_index + "************");
			weather_city_board_animation(c_data, i);

		}

	}

	void weather_city_board_animation(CityData c_data, int board_index) {
		float pos_x = 0;
		// WeatherDetail w_detail = null;
		pos_x = board_start_x + (board_index - focus_index) * 1280;

		FrameLayout city_board = (FrameLayout) c_data.board_view.findViewById(R.id.cityboard);
		FrameLayout weatherIconFrameLayout = (FrameLayout) city_board.findViewById(R.id.weathericon);
		if (weatherIconFrameLayout.getChildCount() != 0)
			weatherIconFrameLayout.removeAllViews();

		ObjectAnimator an = ObjectAnimator.ofFloat(city_board, "X", pos_x);
		an.setDuration(600);

		an.start();
		if (board_index == focus_index)
			an.addListener(new AnimatorListener() {

				public void onAnimationStart(Animator animation) {
					// TODO Auto-generated method stub
				}

				public void onAnimationRepeat(Animator animation) {
					// TODO Auto-generated method stub
				}

				public void onAnimationEnd(Animator animation) {
					// TODO Auto-generated method stub

					updateCD = city_list.get(focus_index);

					if (stopUpdate == true) {
						float alpha = (float) 0.5;
						weather_icon.setAlpha(alpha);
						FrameLayout weatherIconFrameLayout = (FrameLayout) updateCD.board_view.findViewById(R.id.weathericon);
						weatherIconFrameLayout.addView(weather_icon, weatherIconFrameLayout.getWidth(), weatherIconFrameLayout.getHeight());
						weather_icon.setBackgroundResource(R.drawable.widget_bkgrd);
						animation_completed = true;
						return;
					}

					executorService.submit(new Runnable() {

						public void run() {
							// TODO Auto-generated method stub
							handler.post(runUpdateBitmap);
						}
					});
				}

				public void onAnimationCancel(Animator animation) {
					// TODO Auto-generated method stub

				}
			});
	}

	void weather_exit_animation() {
		CityData c_data = city_list.get(focus_index);
		FrameLayout city_board = (FrameLayout) c_data.board_view.findViewById(R.id.cityboard);
		float pos_x = 0, pos_y = 0;
		pos_x = board_start_x + 1280;
		pos_y = -108;

		weather_main.findViewById(R.id.panelHint).setVisibility(View.INVISIBLE);

		AnimatorSet animator_set = new AnimatorSet();

		ObjectAnimator an = ObjectAnimator.ofFloat(city_board, "X", pos_x);
		an.setDuration(600);
		ObjectAnimator dropAn = ObjectAnimator.ofFloat(cities_panel, "Y", pos_y);
		// an.setDuration(2000);

		animator_set.play(dropAn).with(an);
		animator_set.addListener(new Animator.AnimatorListener() {
			// @Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub

				if (openAnimation == true)
					weather_main.startTransition(true);
				else {
					onDestroy();
					android.os.Process.killProcess(android.os.Process.myPid());
				}
			}

			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub

			}

			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub

			}

			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub

			}
		});
		animator_set.start();

	}

	void weather_delete_city(CityData del_data) {
		CityData c_data = null;
		View button = null;

		if (city_list.contains(del_data)) {
			FrameLayout weatherIconFrameLayout = (FrameLayout) del_data.board_view.findViewById(R.id.weathericon);
			weatherIconFrameLayout.removeAllViews();
			del_data.board_view.setVisibility(View.INVISIBLE);
			weather_main.removeView(del_data.board_view);
			del_data.board_view = null;
			city_list.remove(del_data);
			cityListChanged = true;
		}
	}

	void weather_delete_focus_city() {
		animation_completed = false;
		final CityData del_data = city_list.get(focus_index);
		CityData c_data = null;

		if (city_list.size() > 1) {
			View button = null;

			del_data.board_view.setPivotX(0.0f);
			del_data.board_view.setPivotY(478.0f);
			ObjectAnimator scale_x = ObjectAnimator.ofFloat(del_data.board_view, "scaleX", 0.0f);
			scale_x.setDuration(700);
			ObjectAnimator scale_y = ObjectAnimator.ofFloat(del_data.board_view, "scaleY", 0.0f);
			scale_y.setDuration(700);
			ObjectAnimator alpha = ObjectAnimator.ofFloat(del_data.board_view, "alpha", 0.0f);
			alpha.setDuration(700);

			/*if (city_list.size() == 5) {
				ImageButton add_btn = (ImageButton) findViewById(R.id.btn_add);
				add_btn.setFocusable(true);
				// add_btn.setAlpha(1.0f);
				add_btn.setVisibility(View.VISIBLE);
			}*/

			AnimatorSet animator_set = new AnimatorSet();
			animator_set.play(scale_x).with(scale_y).with(alpha);
			animator_set.setInterpolator(new AccelerateInterpolator(1.0f));
			animator_set.start();

			animator_set.addListener(new AnimatorListener() {

				// @Override
				public void onAnimationCancel(Animator animation) {
					// TODO Auto-generated method stub
				}

				// @Override
				public void onAnimationEnd(Animator animation) {
					// TODO Auto-generated method stub
					// CityData c_data =null;
					// if(focus_index + 1 == )
					Log.i("Tag", "*******del_ani_end*********  animation_completed  = " + animation_completed);
					FrameLayout weatherIconFrameLayout = (FrameLayout) del_data.board_view.findViewById(R.id.weathericon);
					weatherIconFrameLayout.removeAllViews();
					del_data.board_view.setVisibility(View.INVISIBLE);
					weather_main.removeView(del_data.board_view);
					del_data.board_view = null;
					city_list.remove(del_data);
					cityListChanged = true;
					Log.v("del", "onAnimationEnd" + city_list.size());

					CityData c_data = city_list.get(focus_index);
					c_data.button_view.requestFocus();

					animation_completed = true;
					for (int i = 0; i < city_list.size(); i++) {
						System.out.printf("城市：%s， %s, %d\n", city_list.get(i).c_name, city_list.get(i).c_id, i);
					}
				}

				// @Override
				public void onAnimationRepeat(Animator animation) {
					// TODO Auto-generated method stub

				}

				// @Override
				public void onAnimationStart(Animator animation) {
					// TODO Auto-generated method stub
					Log.v("del", "onAnimationStart");

				}

			});

			del_data.button_view.setVisibility(View.INVISIBLE);
			weather_main.removeView(del_data.button_view);
			del_data.button_view = null;

			// 找下一个有焦点的城市

			if (focus_index == city_list.size() - 1) // 删除最后一个城市
			{
				focus_index = focus_index - 1;
				c_data = city_list.get(focus_index);
			} else {
				for (int i = focus_index + 1; i < city_list.size(); i++) {
					c_data = city_list.get(i);
					button = c_data.button_view;
					button.setX(button.getX() - button_gap);
					// 如果默认城市在删除城市之后，那么连同默认flag图标一同移动
					/*if (c_data.is_default == true) {
						ImageView flag = (ImageView) findViewById(R.id.flag);
						flag.setX(105 + (i - 1) * button_gap);
					}*/
				}

				c_data = city_list.get(focus_index + 1);
				Log.i("tag", "------------------------------" + c_data.button_view);
			}
			// c_data.button_view.requestFocus();

			// 如果删除了默认城市，则修改默认城市
			if (del_data.is_default == true) {
				c_data.is_default = true;
				/*ImageView flag = (ImageView) findViewById(R.id.flag);
				flag.setX(105 + focus_index * button_gap);*/

				Intent intent = new Intent("WEATHER");
				intent.putExtra("msg", c_data.c_id + ":" + c_data.c_name);
				sendBroadcast(intent);

			}

			/*View btn_add = (View) findViewById(R.id.btn_add);
			btn_add.setX(btn_add.getX() - button_gap);*/
			/*
			 * weather_data.weather_update_animationdrawable_bitmap(city_list
			 * .get(focus_index));
			 */
		}
	}

	Runnable runUpdateBitmap = new Runnable() {

		// @Override
		public void run() {
			// TODO Auto-generated method stub
			if (updateCD != null)
				weather_data.weather_update_animationdrawable_bitmap(updateCD, weather_icon);

			animation_completed = true;
		}
	};

	//
	// Runnable runRecycleBitmap = new Runnable(){
	//
	// public void run(){
	// weather_data.weather_update_recycle_bitmap();
	// }
	// };
	//
	public class receiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS) && search != null)
				weather_search_dialog_focusout_anmi();
			if (intent.getAction().equals("tellweatherwidget")) {
				if (search != null)
					weather_search_dialog_focusout_anmi();
				/* 解析Intent中附加的字符串，将其格式转为和平台xml中城市格式一致 */
				if (intent != null) {
					Bundle bundle = intent.getExtras();
					String newCity = null;
					CityData c_data = null;
					if (bundle != null)
						if (bundle.containsKey("city")) {
							newCity = bundle.getString("city");
							if (newCity.length() == 2 || newCity.length() == 3) {
								// 处理空格
								String spaceString = "  ";
								StringBuffer tmp = new StringBuffer();
								for (int i = 0; i < newCity.length() - 1; i++) {
									tmp.append(newCity.charAt(i)).append(spaceString);
								}
								tmp.append(newCity.charAt(newCity.length() - 1));
								newCity = tmp.toString();
								Log.i("tag", "Bundle: newCity = " + newCity);
							}
						}

					/* 如果查询了新的城市，而程序中已存在的城市超过5个，那么去掉以前的可能存在的临时城市 */
					if ((newCity != null) && !weather_city_exist(newCity) && city_list != null) {
						if (city_list.size() >= 5)
							for (int i = 0; i < city_list.size(); i++) {
								c_data = city_list.get(i);
								if (c_data.is_temporary == true) {
									FrameLayout weatherIconFrameLayout = (FrameLayout) c_data.board_view.findViewById(R.id.weathericon);
									weatherIconFrameLayout.removeAllViews();
									Log.i("tag", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
									c_data.board_view.setVisibility(View.INVISIBLE);
									c_data.button_view.setVisibility(View.INVISIBLE);
									weather_main.removeView(c_data.board_view);
									c_data.board_view = null;
									c_data.button_view = null;
									city_list.remove(c_data);
								}
							}

						/* 如果不存在被查询的城市，加入新的城市 */
						tempCity = newCity;
						CityInfo c_info = null;
						for (int i = 0; i < name_list.size(); i++) {
							c_info = null;
							c_info = name_list.get(i);
							if (c_info.name.equals(tempCity)) {
								c_data = new CityData();
								c_data.c_id = c_info.c_id;
								c_data.c_name = c_info.name;
								c_data.is_temporary = true;
								city_list.add(c_data);
								break;
							}
						}
						if (c_data == null) {
							Toast toast = Toast.makeText(getApplicationContext(), "找不到该城市", Toast.LENGTH_LONG);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
							Log.i("tag", "handler : toast!!!!!!!!!!");

							/*if (city_list.size() < 5) {
								ImageButton add_btn = (ImageButton) findViewById(R.id.btn_add);
								add_btn.setFocusable(true);
								// add_btn.setAlpha(1.0f);
								add_btn.setVisibility(View.VISIBLE);
							}*/
						} else {
							weather_add_city_button();
							View btn = city_list.get(city_list.size() - 1).button_view;
							btn.setY(button_start_y);
							focus_index = city_list.size() - 1;
							btn.requestFocus();
						}
					}

					else /* 否则，如果查询的城市在已有的城市当中的话，转移焦点 */
					if ((newCity != null) && weather_city_exist(newCity) && city_list != null) {
						for (int i = 0; i < city_list.size(); i++) {
							c_data = city_list.get(i);
							if (c_data.c_name.equals(newCity)) {
								focus_index = i;
								c_data = city_list.get(focus_index);
								c_data.button_view.requestFocus();
							}
						}
					}
				}
			}
		}
	}
}

// 使用线程解析city.xml文件，加快程序启动的速度
class ParseThread implements Runnable {
	private ArrayList<CityInfo> provinceList, n_list;
	private HashMap<CityInfo, ArrayList<CityInfo>> cityHashMap;
	private List<CityData> c_list;
	private WeatherDataPro weather_data;
	private String tempCity;
	private Handler handler;

	// @Override
	public void run() {
		// TODO Auto-generated method stub
		String pre_path = "/hdisk/";
		saxCityParse parse = new saxCityParse();
		Log.i("parse", "parse file /hdisk/backup/cache/XMLs/city.xml");
		File city_file = new File(pre_path + "/backup/cache/XMLs/city.xml");
		File default_file = new File(pre_path + "/etc/mntn/city.xml");

		File out_dir = new File(pre_path + "/backup/cache/XMLs/");
		if (!city_file.exists()) {
			if (!out_dir.exists())
				out_dir.mkdirs();

			try {
				copyFile(default_file, city_file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			CityInfo cityInfo;
			ArrayList<CityInfo> tmpList;

			parse.parse(new FileInputStream(city_file));
			provinceList.addAll(parse.getProvinceList());
			cityHashMap.putAll(parse.getCityHashMap());

			for (int i = 0; i < provinceList.size(); i++) {
				cityInfo = provinceList.get(i);
				if (cityInfo != null && (tmpList = cityHashMap.get(cityInfo)) != null) {
					for (int j = 0; j < tmpList.size(); j++) {
						n_list.add(tmpList.get(j));
						//						System.out.printf("解析结果：城市：|%s|， %s, %d\n", tmpList.get(j).name, tmpList.get(j).c_id, j);
					}
				}
				// System.out.printf("城市：|%s|， %s, %d\n", n_list.get(i).name,
				// n_list.get(i).c_id, i);
			}

			//c_list保存已选城市，根据n_list的信息添加城市图片
			for (int i = 0; i < c_list.size(); i++) {
				CityData c_data = c_list.get(i);
				String pic_path = pre_path + "/rc/pics/weather/city/" + c_data.c_id + ".jpg";
				File picFile = new File(pic_path);
				if (!picFile.exists()) {
					weather_data.weather_get_city_image(c_data, n_list);
				}
			}

			Message msg = new Message();
			msg.what = 1;
			handler.sendMessage(msg);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void copyFile(File sourceFile, File targetFile) throws IOException {
		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		try {
			// 新建文件输入流并对它进行缓冲
			inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

			// 新建文件输出流并对它进行缓冲
			outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

			// 缓冲数组
			byte[] b = new byte[1024 * 5];
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}
			// 刷新此缓冲的输出流
			outBuff.flush();
		} finally {
			// 关闭流
			if (inBuff != null)
				inBuff.close();
			if (outBuff != null)
				outBuff.close();
		}
	}

	public ParseThread(ArrayList<CityInfo> name_list, ArrayList<CityInfo> provinceList, HashMap<CityInfo, ArrayList<CityInfo>> cityHashMap,
			List<CityData> city_list, WeatherDataPro w_data, Handler handler) {
		this.provinceList = provinceList;
		this.cityHashMap = cityHashMap;
		n_list = name_list;
		c_list = city_list;
		weather_data = w_data;
		this.handler = handler;
	}

}
