//说明：在alarm_main.xml文件中，ListView的设置要为：
//android:layout_width="match_parent"
//android:layout_height="match_parent"
//否则发生意想不的问题：如Switch联动，SharedPreferences自动改写等问题
package com.example.alarmclock;

import android.os.Bundle;
import android.app.Activity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.ViewGroup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import com.example.floatwindow.R;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

public class AlarmMainActivity extends Activity {
	public static final String EXTRA_STRING_TARGET_PKG_NAME = "target_pkgname";
	public static final int ADD_ALARM = 0;
	public static final int UPDATE_ALARM = 1;
	private int alarm_number = 0;
	private ListView listview;
	public MyAdapter adapter;
	private Button btn_stop = null;
	private Button btn_alarm = null;
	private TextView titleText = null;
	private AlarmManager alarmManager = null;
	final int DIALOG_TIME = 0;
	boolean hasAlarm = false;
	String packageName;
	String appName;

	// String appName = "weibo";

	@Override
	protected void onResume() {
		super.onResume();
		packageName = getIntent().getStringExtra(EXTRA_STRING_TARGET_PKG_NAME);
		if (packageName == null || packageName == "") {
			packageName = "我的游戏";
		}
		appName = MyProgramPackage.getProgramNameByPackageName(this,
				packageName);
		if (appName == null || appName == "") {
			appName = "我的游戏";
		}
		titleText.setText(appName + "闹钟列表");
		try {
			// 获得SharedPreferences数据
			alarm_number = GetAlarmNumberFromSharedPreferences();
			Log.e("onResume", "alarm_number:" + alarm_number);
			adapter.arr.clear();
			ArrayList<MyData> list = GetAlarmDatasFromSharedPreferences(packageName);
			Log.e("ArrayList<MyData>", "" + list.size());
			for (int i = 0; i < list.size(); i++) {
				MyData t = list.get(i);
				Log.e("uuuuuuuuuuuuuuu", "" + t.arrAlarmNumber + "  " + t.open);
				adapter.arr.add(t);
			}

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			/*
			 * Toast.makeText(AlarmMainActivity.this,
			 * "GetAlarmDatasFromSharedPreferences()异常", Toast.LENGTH_LONG)
			 * .show();
			 */
		}
		/*
		 * Toast.makeText(AlarmMainActivity.this, "onResume()",
		 * Toast.LENGTH_LONG) .show();
		 */

		// 更新界面
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_main);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		
		//标题栏
		titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText("闹钟列表");
		View titleClose = findViewById(R.id.title_close_btn);
		titleClose.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		listview = (ListView) findViewById(R.id.listView1);
		adapter = new MyAdapter(this);
		listview.setAdapter(adapter);		
		
		listview.setOnItemClickListener(new OnItemClickListener() {  
        @Override  
        public void onItemClick(AdapterView<?> adapterView, View view, int position,  
            long id) {  
        	//Toast.makeText(AlarmMainActivity.this, "onItemClick()",
        	//		  Toast.LENGTH_LONG) .show();
        	
        	Intent intent = new Intent(AlarmMainActivity.this,
					SetAlarmActivity.class);
			intent.putExtra(SetAlarmActivity.EXTRA_STRING_TARGET_PKG_NAME,
					getIntent()
							.getStringExtra(EXTRA_STRING_TARGET_PKG_NAME));
			MyData data=adapter.arr.get(position);
			Bundle bundle = new Bundle();
			bundle.putBoolean("set_vibrator", data.vibrator);
			bundle.putBoolean("set_ring", data.ring);
			bundle.putInt("hour", data.hour);
			bundle.putInt("minute", data.minute);
			bundle.putInt("alarmID", data.arrAlarmNumber);			
			intent.putExtras(bundle);			
			startActivityForResult(intent, UPDATE_ALARM);
        }  
    });


		// 更新界面：OnCreate会调用OnResume，这里不再获取数据
		// alarm_number=GetAlarmNumberFromSharedPreferences();
		// adapter.notifyDataSetChanged();
		btn_alarm = (Button) findViewById(R.id.btn_alarm);
		btn_alarm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(AlarmMainActivity.this,
						SetAlarmActivity.class);
				intent.putExtra(SetAlarmActivity.EXTRA_STRING_TARGET_PKG_NAME,
						getIntent()
								.getStringExtra(EXTRA_STRING_TARGET_PKG_NAME));
				Bundle bundle = new Bundle();
				bundle.putBoolean("set_vibrator", true);
				bundle.putBoolean("set_ring", true);
				bundle.putInt("hour", 0);
				bundle.putInt("minute", 0);	
				bundle.putInt("alarmID", alarm_number);					
				intent.putExtras(bundle);	
				
				startActivityForResult(intent, ADD_ALARM);
			}
		});

		// 如果原来没有闹钟数据，直接跳到闹钟设置页面
		try {
			ArrayList<MyData> list = GetAlarmDatasFromSharedPreferences(getIntent()
					.getStringExtra(EXTRA_STRING_TARGET_PKG_NAME));
			alarm_number=GetAlarmNumberFromSharedPreferences();
			if (list.size() == 0) {				
				Intent intent = new Intent(AlarmMainActivity.this,
						SetAlarmActivity.class);
				intent.putExtra(SetAlarmActivity.EXTRA_STRING_TARGET_PKG_NAME,
						getIntent()
								.getStringExtra(EXTRA_STRING_TARGET_PKG_NAME));
				Bundle bundle = new Bundle();
				bundle.putBoolean("set_vibrator", true);
				bundle.putBoolean("set_ring", true);
				bundle.putInt("hour", 0);
				bundle.putInt("minute", 0);	
				bundle.putInt("alarmID", alarm_number);					
				intent.putExtras(bundle);	
				
				startActivityForResult(intent, ADD_ALARM);
			}
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void UpdateAlarm(Intent intentData) {
		// 获得bundle数据
		Bundle bundle = intentData.getExtras();		
		int currentAlarmNumber=bundle.getInt("alarmID");
		int hour = bundle.getInt("hour");
		int minute = bundle.getInt("minute");
		boolean set_vibrator = bundle.getBoolean("set_vibrator");
		boolean set_ring = bundle.getBoolean("set_ring");
		long timemillis = 60 * 1000 * (hour * 60 + minute);
		
		//bundle.putInt("alarmID", currentAlarmNumber);

		Intent intent = new Intent(AlarmMainActivity.this, AlarmActivity.class);
		intent.putExtras(bundle);
		intent.putExtra(AlarmActivity.EXTRA_STRING_TARGET_PKG_NAME, getIntent()
				.getStringExtra(EXTRA_STRING_TARGET_PKG_NAME));
		intent.setAction("com.alarm.action_alarm_on");

		PendingIntent pi = PendingIntent.getActivity(AlarmMainActivity.this,
				alarm_number, intent, 0);
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
				+ timemillis, pi);

		// 更新数据
		for (int i = 0; i < adapter.arr.size(); i++) {
			if (adapter.arr.get(i).arrAlarmNumber == currentAlarmNumber) {
				// 更新adapter.arr
				adapter.arr.get(i).hour = hour;
				adapter.arr.get(i).minute = minute;
				adapter.arr.get(i).vibrator = set_vibrator;
				adapter.arr.get(i).ring = set_ring;
				adapter.arr.get(i).open = true;
				// 更新SharedPreferences
				try {
					PutAlarmDatasToSharedPreferences(packageName, adapter.arr);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
	}

	public void AddNewAlarm(Intent intentData) {
		// 设置闹钟
		Bundle bundle = intentData.getExtras();
		int hour = bundle.getInt("hour");
		int minute = bundle.getInt("minute");
		boolean set_vibrator = bundle.getBoolean("set_vibrator");
		boolean set_ring = bundle.getBoolean("set_ring");
		long timemillis = 60 * 1000 * (hour * 60 + minute);
		//bundle.putInt("alarmID", alarm_number);
		Intent intent = new Intent(AlarmMainActivity.this, AlarmActivity.class);
		intent.putExtras(bundle);
		intent.putExtra(AlarmActivity.EXTRA_STRING_TARGET_PKG_NAME, getIntent()
				.getStringExtra(EXTRA_STRING_TARGET_PKG_NAME));
		intent.setAction("com.alarm.action_alarm_on");

		PendingIntent pi = PendingIntent.getActivity(AlarmMainActivity.this,
				alarm_number, intent, 0);
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
				+ timemillis, pi);

		// 更新数据
		MyData data = new MyData();
		data.hour = hour;
		data.minute = minute;
		data.vibrator = set_vibrator;
		data.ring = set_ring;
		data.open = true;
		data.arrAlarmNumber = alarm_number;
		adapter.arr.add(data);
		try {
			PutAlarmDatasToSharedPreferences(packageName, adapter.arr);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		alarm_number++;
		PutAlarmNumberToSharedPreferences(alarm_number);

		// onActivityResult()之后调用OnResume(),会重新从SharedPreferences读取
		// 数据并刷新ListView
		// adapter.notifyDataSetChanged();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intentData) {
		if (resultCode == RESULT_OK) {
			if (requestCode == ADD_ALARM) {
				AddNewAlarm(intentData);
			} else if (requestCode == UPDATE_ALARM) {
				UpdateAlarm(intentData);
			}

		} else if (resultCode == RESULT_CANCELED) {
			// 闹钟设置中如果没有设置闹钟，判断原来有没有闹钟数据，没有的话推出
			try {
				ArrayList<MyData> list = GetAlarmDatasFromSharedPreferences(getIntent()
						.getStringExtra(EXTRA_STRING_TARGET_PKG_NAME));
				if (list.size() == 0) {
					finish();
				}
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class MyAdapter extends BaseAdapter {
		private Context context;
		private LayoutInflater inflater;
		public ArrayList<MyData> arr;

		public MyAdapter(Context context) {
			super();
			this.context = context;
			inflater = LayoutInflater.from(context);
			arr = new ArrayList<MyData>();
			/*
			 * try { arr = GetAlarmDatasFromSharedPreferences(appName); } catch
			 * (Throwable e) { // TODO Auto-generated catch block //
			 * e.printStackTrace(); Toast.makeText(AlarmMainActivity.this,
			 * "GetAlarmDatasFromSharedPreferences()异常",
			 * 
			 * Toast.LENGTH_LONG).show(); }
			 */

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return arr.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(final int position, View view, ViewGroup arg2) {
			// TODO Auto-generated method stub

			Log.e("getView()", "" + position);

			if (view == null) {
				view = inflater.inflate(R.layout.alarm_list, null);
			}

			Switch switch_open = null;
			TextView timeMsg = null;
			TextView openMsg = null;
			Button button = null;

			switch_open = (Switch) view.findViewById(R.id.array_switch);
			timeMsg = (TextView) view.findViewById(R.id.array_time);
			openMsg = (TextView) view.findViewById(R.id.array_open);
			button = (Button) view.findViewById(R.id.array_button);
			MyData data = arr.get(position);
			String msg = "定时";
			if (data.hour != 0) {
				msg = msg + data.hour + "时";
			}
			if (data.minute != 0) {
				msg = msg + data.minute + "分";
			}
			timeMsg.setText(msg);
			if (data.open) {
				openMsg.setText("已开启");

			} else {
				openMsg.setText("已关闭");
			}
			switch_open.setChecked(data.open);
			switch_open
					.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							// TODO Auto-generated method stub

							for (int i = 0; i < arr.size(); i++) {
								MyData t = arr.get(i);
								Log.e("Switch更改前数据", "" + t.arrAlarmNumber
										+ "  " + t.open);
								// adapter.arr.add(t);
							}

							arr.get(position).open = isChecked;

							System.out.println(arr.get(position));
							try {
								PutAlarmDatasToSharedPreferences(packageName,
										arr);
								ArrayList<MyData> l = GetAlarmDatasFromSharedPreferences(packageName);
								for (int i = 0; i < l.size(); i++) {
									MyData t = l.get(i);
									Log.e("SharedPreferences", ""
											+ t.arrAlarmNumber + "  " + t.open);
									// adapter.arr.add(t);
								}

							} catch (Throwable e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								/*
								 * Toast.makeText(AlarmMainActivity.this,
								 * "PutAlarmDatasToSharedPreferences()异常",
								 * Toast.LENGTH_LONG).show();
								 */
							}

							// 加上下面这句话会发生联动效果，要想不联动需要改动布局文件的属性
							adapter.notifyDataSetChanged();

							if (isChecked) {
								OpenAlarm(arr.get(position));
							} else {
								CloseAlarm(arr.get(position));
							}

							for (int i = 0; i < arr.size(); i++) {
								MyData t = arr.get(i);
								Log.e("Switch更改后数据", "" + t.arrAlarmNumber
										+ "  " + t.open);
								// adapter.arr.add(t);
							}
						}
					});

			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					//
					System.out.println(position);
					System.out.println(arr);
					CloseAlarm(arr.get(position));
					arr.remove(position);
					try {
						PutAlarmDatasToSharedPreferences(packageName, arr);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						/*
						 * Toast.makeText(AlarmMainActivity.this,
						 * "PutAlarmDatasToSharedPreferences()异常",
						 * 
						 * Toast.LENGTH_LONG).show();
						 */
					}

					System.out.println(arr);
					adapter.notifyDataSetChanged();
					// 如果没有了闹钟，则退出界面
//					if (arr.size() == 0) {
//						AlarmMainActivity.this.finish();
//					}

				}
			});

			// final EditText edit = (EditText) view.findViewById(R.id.edit);

			return view;
		}
	}

	//
	public void CloseAlarm(MyData data) {
		Bundle bundle = new Bundle();
		bundle.putBoolean("set_vibrator", data.vibrator);
		bundle.putBoolean("set_ring", data.ring);
		bundle.putInt("hour", data.hour);
		bundle.putInt("minute", data.minute);
		bundle.putInt("alarmID", data.arrAlarmNumber);
		Intent intent = new Intent(AlarmMainActivity.this, AlarmActivity.class);
		intent.setAction("com.alarm.action_alarm_on");
		intent.putExtras(bundle);
		intent.putExtra(AlarmActivity.EXTRA_STRING_TARGET_PKG_NAME, getIntent()
				.getStringExtra(EXTRA_STRING_TARGET_PKG_NAME));

		PendingIntent pi = PendingIntent.getActivity(AlarmMainActivity.this,
				data.arrAlarmNumber, intent, 0);
		alarmManager.cancel(pi);
		/*
		 * Toast.makeText(AlarmMainActivity.this, "闹钟停止！", Toast.LENGTH_SHORT)
		 * .show();
		 */
	}

	//
	public void OpenAlarm(MyData data) {

		Bundle bundle = new Bundle();
		bundle.putBoolean("set_vibrator", data.vibrator);
		bundle.putBoolean("set_ring", data.ring);
		bundle.putInt("hour", data.hour);
		bundle.putInt("minute", data.minute);
		bundle.putInt("alarmID", data.arrAlarmNumber);
		Intent intent = new Intent(AlarmMainActivity.this, AlarmActivity.class);
		intent.setAction("com.alarm.action_alarm_on");
		intent.putExtras(bundle);
		intent.putExtra(AlarmActivity.EXTRA_STRING_TARGET_PKG_NAME, getIntent()
				.getStringExtra(EXTRA_STRING_TARGET_PKG_NAME));
		long timemillis = 60 * 1000 * (data.hour * 60 + data.minute);
		PendingIntent pi = PendingIntent.getActivity(AlarmMainActivity.this,
				data.arrAlarmNumber, intent, 0);

		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
				+ timemillis, pi);

		/*
		 * Toast.makeText( AlarmMainActivity.this, "alarm_number=" + alarm_number
		 * + ",vibrator=" + data.vibrator + ",ring=" + data.ring,
		 * Toast.LENGTH_SHORT).show();
		 */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<MyData> GetAlarmDatasFromSharedPreferences(
			String packageName) throws Throwable {
		ArrayList<MyData> list = new ArrayList<MyData>();
		SharedPreferences sharedPreferences = getSharedPreferences(
				"AlarmInfos", Activity.MODE_PRIVATE);
		String info = sharedPreferences.getString(packageName, "");
		if (info != "") {
			byte[] infoBytes = Base64.decode(info.getBytes(), Base64.DEFAULT);
			// byte[] infoBytes = info.getBytes();
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
					infoBytes);
			ObjectInputStream objectInputStream = new ObjectInputStream(
					byteArrayInputStream);
			list = (ArrayList<MyData>) objectInputStream.readObject();
			objectInputStream.close();

		}
		return list;

	}

	public void PutAlarmDatasToSharedPreferences(String packageName,
			ArrayList<MyData> list) throws Throwable {

		/*
		 * for (int i = 0; i < list.size(); i++) {
		 * Log.i("PutAlarmDatasToSharedPreferences()","hour:"+list.get(i).hour);
		 * Log
		 * .i("PutAlarmDatasToSharedPreferences()","minute"+list.get(i).minute);
		 * }
		 */

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				byteArrayOutputStream);
		objectOutputStream.writeObject(list);
		objectOutputStream.flush();

		SharedPreferences sharedPreferences = getSharedPreferences(
				"AlarmInfos", Activity.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		String info = new String(Base64.encode(
				byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
		editor.putString(packageName, info);
		editor.commit();
		objectOutputStream.close();

	}

	public int GetAlarmNumberFromSharedPreferences() {
		SharedPreferences sharedPreferences = getSharedPreferences(
				"AlarmInfos", Activity.MODE_PRIVATE);
		return sharedPreferences.getInt("AlarmNumber", 0);
	}

	public void PutAlarmNumberToSharedPreferences(int id) {
		SharedPreferences sharedPreferences = getSharedPreferences(
				"AlarmInfos", Activity.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putInt("AlarmNumber", id);
		editor.commit();
	}
}
