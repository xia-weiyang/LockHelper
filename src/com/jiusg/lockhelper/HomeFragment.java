package com.jiusg.lockhelper;

import java.util.ArrayList;
import java.util.HashMap;

import com.jiusg.lockhelper.R;

import android.app.ListFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class HomeFragment extends ListFragment {

	private ArrayList<HashMap<String, String>> HomeList;
	private SimpleAdapter sap;
	private Handler hd;
	public static boolean IsChooseApp = false;
	private ProgressBar pb;
	private TextView tv;
	private static boolean IsSS = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.activity_home, container,
				false);

		HomeList = new ArrayList<HashMap<String, String>>();
		AddHomeListData();
		sap = new SimpleAdapter(getActivity(), HomeList, R.layout.homelist,
				new String[] { "Location", "Application" }, new int[] {
						R.id.locationId, R.id.applicationId });

		pb = (ProgressBar) fragmentView.findViewById(R.id.progressBar1);
		tv = (TextView) fragmentView.findViewById(R.id.Load);

		GetLock_info_name();
		sap.notifyDataSetChanged();
		hd = new HomeHandler();
		// 启动锁屏帮手服务
		if (!IsSS) {
			Message msg = hd.obtainMessage();
			msg.obj = "SS";
			hd.sendMessageDelayed(msg, 500);
		} else {
			pb.setVisibility(View.GONE);
			tv.setVisibility(View.GONE);
			setListAdapter(sap);
		}
		return fragmentView;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		Intent it = new Intent();
		it.setClassName(getActivity(), "com.jiusg.lockhelper.MyApplication");
		Bundle b = new Bundle();
		b.putInt("lock_position", position);
		it.putExtras(b);
		startActivity(it);
	}

	public void AddHomeListData() {

		HashMap<String, String> hm1 = new HashMap<String, String>();
		hm1.put("Location", "位置一");
		hm1.put("Application", "无");
		HomeList.add(hm1);
		HashMap<String, String> hm2 = new HashMap<String, String>();
		hm2.put("Location", "位置二");
		hm2.put("Application", "无");
		HomeList.add(hm2);
		HashMap<String, String> hm3 = new HashMap<String, String>();
		hm3.put("Location", "位置三");
		hm3.put("Application", "无");
		HomeList.add(hm3);
		HashMap<String, String> hm4 = new HashMap<String, String>();
		hm4.put("Location", "位置四");
		hm4.put("Application", "无");
		HomeList.add(hm4);
		HashMap<String, String> hm5 = new HashMap<String, String>();
		hm5.put("Location", "位置五");
		hm5.put("Application", "无");
		HomeList.add(hm5);
	}

	// 加载界面已选择锁屏应用信息
	public void GetLock_info_name() {

		SharedPreferences Lock_info = getActivity().getSharedPreferences(
				"Lock_info", 0);
		String Lock_info_name0 = Lock_info.getString("Name0", "");
		String Lock_info_name1 = Lock_info.getString("Name1", "");
		String Lock_info_name2 = Lock_info.getString("Name2", "");
		String Lock_info_name3 = Lock_info.getString("Name3", "");
		String Lock_info_name4 = Lock_info.getString("Name4", "");
		if (!(Lock_info_name0.equals(""))) {

			HomeList.get(0).put("Application", Lock_info_name0);
		}
		if (!(Lock_info_name1.equals(""))) {

			HomeList.get(1).put("Application", Lock_info_name1);
		}
		if (!(Lock_info_name2.equals(""))) {

			HomeList.get(2).put("Application", Lock_info_name2);
		}
		if (!(Lock_info_name3.equals(""))) {

			HomeList.get(3).put("Application", Lock_info_name3);
		}
		if (!(Lock_info_name4.equals(""))) {

			HomeList.get(4).put("Application", Lock_info_name4);
		}
	}

	@Override
	public void onResume() {

		super.onResume();

		Message msg = hd.obtainMessage();
		msg.obj = "onResume";
		hd.sendMessageDelayed(msg, 1000);
	}

	class HomeHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {

			if (msg.obj.toString().equals("SS")) {

				Intent it = new Intent();
				it.setClass(getActivity(), LockHelperService.class);
				getActivity().startService(it);
				pb.setVisibility(View.GONE);
				tv.setVisibility(View.GONE);
				IsSS = true;
				setListAdapter(sap);
			}

			if (msg.obj.toString().equals("onResume")) {
				GetLock_info_name();
				sap.notifyDataSetChanged();
				// 用户选择后应立即重新加载锁屏时应用图标新，以便达到刷新的目的。
				try {
					if (IsChooseApp) {
						LockHelperService.Ai.GetApplictionImage(getActivity());
						IsChooseApp = false;
					}
				} catch (Exception e) {
					// Toast.makeText(getActivity(), "服务正在启动...",
					// Toast.LENGTH_SHORT).show();
				}
			}
		}

	}
}
