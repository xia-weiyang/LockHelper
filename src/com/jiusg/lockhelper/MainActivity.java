package com.jiusg.lockhelper;

import java.util.Calendar;

import com.jiusg.Tools.Mydialog;
import com.jiusg.Tools.SmartBarUtils;
import com.jiusg.lockhelper.R;
import com.meizu.mstore.license.ILicensingService;
import com.meizu.mstore.license.LicenseCheckHelper;
import com.meizu.mstore.license.LicenseResult;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;

public class MainActivity extends Activity {

	private ILicensingService mLicensingService = null;
	private final String APKPublic = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCL1JrmG/y+pHE67dj99Myr+ZVVX7QgRUIuTWcQvdSmM8o57UEA214tzy9IZkDpAk7KWE9s4h2c3a4JwecCXIwbiT4K5X+7YNqPkAh1EIQ3MR7l3+WSqyAISzOf9XUMv7mzZ3QtKiAZmKH7SEs4M4VpFp+g5/DeBvIzjrKM47pYAQIDAQAB";
	private ServiceConnection mLicenseConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

			mLicensingService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			mLicensingService = ILicensingService.Stub.asInterface(service);
		}
	};
	private Handler hd;
	private SharedPreferences sp;
	public static boolean IsChooseApp = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ActionBar bar = getActionBar();
		bar.addTab(bar
				.newTab()
				.setIcon(R.drawable.ic_tab_home)
				.setTabListener(
						new MyTabListener<HomeFragment>(this, "锁屏帮手",
								HomeFragment.class)));
		bar.addTab(bar
				.newTab()
				.setIcon(R.drawable.ic_tab_setting)
				.setTabListener(
						new MyTabListener<SettingFragment>(this, "设置",
								SettingFragment.class)));

		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		SmartBarUtils.setActionBarTabsShowAtBottom(bar, true); // 调用魅族提供的接口，使其显示在SmartBar上

		// 第一次执行程序时，计算出LockHelper的默认位置
		// 程序第一次执行时会调用
		sp = getSharedPreferences("Screen", 0); // Screen,存了好多必要的信息，名字取错了
		SharedPreferences sp_setting = PreferenceManager
				.getDefaultSharedPreferences(getApplication());
		// 一定要记着aaaaaa
		// sp.edit().putString("UserVersionInfo", "OfficialVersionISTRUE")
		// .commit();

		// 判断其版本，版本信息不对，则用户可能进行了更新
		if (!sp.getString("Version", "").equals("1.1.5")) {

			sp.edit().putString("Version", "1.1.5").commit();
			final Mydialog dl = new Mydialog(this, R.style.dialog);
			LayoutInflater inflater = LayoutInflater.from(this);
			View layout = inflater.inflate(R.layout.lesson, null, false);
			dl.setContentView(layout);
			dl.setCancelable(true);
			dl.show();
		}
		// 判断位置编辑是否有值？没有的话用户则是第一次安装，获取设备的信息，设置位置的默认值，否则不要清除了用户的数据
		if (sp_setting.getString("Lockposition", "").equals("")) {
			sp_setting.edit().putString("Lockposition", "0").commit();

			// 存入用户手机的屏幕大小信息
			sp.edit().putInt("ScreenHeight", GetScreenHeight()).commit();
			sp.edit().putInt("ScreenWidth", GetScreenWidth()).commit();

			// 存入一些初始数据
			sp_setting.edit().putInt("Alpha", 15).commit();
			sp_setting.edit().putInt("Size", 300 + 10 * 15).commit();
			sp_setting.edit()
					.putInt("LockpositionY", GetScreenHeight() / 30 * 10)
					.commit();
		}

		// 如果已经验证了用户为正式版，那么每次启动应用就没必要验证了
		if (!sp.getString("UserVersionInfo", "")
				.equals("OfficialVersionISTRUE")) {
			// 绑定服务 这些都是用来验证是否正式版本的
			if (mLicensingService == null) {

				Intent intent = new Intent();
				intent.setAction(ILicensingService.class.getName());
				bindService(intent, mLicenseConnection,
						Context.BIND_AUTO_CREATE);

			}

			hd = new LicenseHandler();
			Message msg1 = hd.obtainMessage();
			msg1.obj = "License";
			hd.sendMessageDelayed(msg1, 3000);
		}
	}

	public static class MyTabListener<T extends Fragment> implements
			ActionBar.TabListener {

		private final Activity mActivity;
		private final String mTag;
		private final Class<T> mClass;
		private final Bundle mArgs;
		private Fragment mFragment;

		public MyTabListener(Activity activity, String tag, Class<T> clz) {
			this(activity, tag, clz, null);
		}

		public MyTabListener(Activity activity, String tag, Class<T> clz,
				Bundle args) {
			mActivity = activity;
			mTag = tag;
			mClass = clz;
			mArgs = args;

			// Check to see if we already have a fragment for this tab, probably
			// from a previously saved state. If so, deactivate it, because our
			// initial state is that a tab isn't shown.
			mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
			if (mFragment != null && !mFragment.isDetached()) {
				FragmentTransaction ft = mActivity.getFragmentManager()
						.beginTransaction();
				ft.detach(mFragment);
				ft.commit();
			}
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {

			// Toast.makeText(mActivity, "Reselected!",
			// Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {

			if (mFragment == null) {
				mFragment = Fragment.instantiate(mActivity, mClass.getName(),
						mArgs);
				ft.add(android.R.id.content, mFragment, mTag);
			} else {
				ft.attach(mFragment);
			}

			mActivity.getActionBar().setTitle(mTag);
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {

			if (mFragment != null) {
				ft.detach(mFragment);
			}
		}

	}

	class LicenseHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {

			// super.handleMessage(msg);

			if (msg.obj.toString().equals("License")) {
				// 调用服务检查License，并返回检查结果
				LicenseResult result = null;
				try {

					result = mLicensingService.checkLicense(getApplication()
							.getPackageName());

				} catch (RemoteException e) {
					e.printStackTrace();
					Toast.makeText(getApplication(), "错误:4 验证License发生严重错误！",
							Toast.LENGTH_SHORT).show();
				}
				// 进行判断
				if (result.getResponseCode() == LicenseResult.RESPONSE_CODE_SUCCESS) {

					boolean bSuccess = LicenseCheckHelper.checkResult(
							APKPublic, result);

					if (bSuccess
							&& result.getPurchaseType() == LicenseResult.PURCHASE_TYPE_NORMAL) {
						// 当前验证为正式版
						sp.edit()
								.putString("UserVersionInfo",
										"OfficialVersionISTRUE").commit();
					} else {

						if (bSuccess
								&& result.getPurchaseType() == LicenseResult.PURCHASE_TYPE_TRIAL) {
							// 当前验证为试用版
							Calendar cal = result.getStartDate();
							cal.add(Calendar.DAY_OF_MONTH, 3);
							Calendar cNow = Calendar.getInstance(); // 获取当前系统时间
							if (cNow.after(cal)) {

								Toast.makeText(getApplication(),
										"试用时间已经结束！锁屏帮手将不再提供服务！",
										Toast.LENGTH_SHORT).show();
								Intent it = new Intent();
								it.setClass(MainActivity.this,
										LockHelperService.class);
								stopService(it);
								sp.edit()
										.putString("UserVersionInfo",
												"TrialVersionOver").commit();

							} else {

								Toast.makeText(getApplication(), "当前为试用版！",
										Toast.LENGTH_SHORT).show();
								sp.edit()
										.putString("UserVersionInfo",
												"TrialVersion").commit();
							}
						} else {
							Intent it = new Intent();
							it.setClass(MainActivity.this,
									LockHelperService.class);
							stopService(it);
							Toast.makeText(getApplication(), "错误:1 验证失败！请重试",
									Toast.LENGTH_SHORT).show();
							sp.edit().putString("UserVersionInfo", "Error")
									.commit();
						}
					}
				} else {
					if (result.getResponseCode() == LicenseResult.RESPONSE_CODE_NO_LICENSE_FILE) {

						Intent it = new Intent();
						it.setClass(MainActivity.this, LockHelperService.class);
						stopService(it);
						Toast.makeText(getApplication(), "错误:2 验证失败！请重试！",
								Toast.LENGTH_SHORT).show();
						sp.edit().putString("UserVersionInfo", "Error")
								.commit();
					} else {

						Intent it = new Intent();
						it.setClass(MainActivity.this, LockHelperService.class);
						stopService(it);
						Toast.makeText(getApplication(), "错误:3 验证失败！请重试",
								Toast.LENGTH_SHORT).show();
						sp.edit().putString("UserVersionInfo", "Error")
								.commit();
					}
				}
				// 解除绑定
				if (mLicensingService != null) {
					unbindService(mLicenseConnection);
				}
			}

		}
	}

	/**
	 * 获取当前屏幕的宽
	 * 
	 * @param activity
	 * @return
	 */
	public int GetScreenWidth() {

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}

	/**
	 * 获取当前屏幕的高度
	 * 
	 * @param activity
	 * @return
	 */
	public int GetScreenHeight() {

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels - getStatusBarHeight();
	}

	/**
	 * 获取状态栏的高度
	 * 
	 * @param activity
	 * @return
	 */
	public int getStatusBarHeight() {
		Class<?> c = null;
		Object obj = null;
		java.lang.reflect.Field field = null;
		int x = 0;
		int statusBarHeight = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = getResources().getDimensionPixelSize(x);
			return statusBarHeight;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusBarHeight;
	}
}
