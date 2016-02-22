package com.jiusg.lockhelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.jiusg.Tools.ApplicationImage;
import com.jiusg.lockhelper.R;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * 锁屏帮手服务类
 * 
 * @author Administrator 过往
 * 
 */
@SuppressWarnings("deprecation")
public class LockHelperService extends Service {

	private WindowManager Wm;
	private WindowManager.LayoutParams wmParams;
	private View win;
	private ImageView ib;
	private ImageView ib_1_1;
	private ImageView ib_2_2;
	private ImageView ib_3_3;
	private ImageView ib_4_4;
	private ImageView ib_5_5;
	private ImageView ib_1;
	private ImageView ib_2;
	private ImageView ib_3;
	private ImageView ib_4;
	private ImageView ib_5;
	private KeyguardLock kl;
	private boolean IsView;
	private List<String> homeList; // 桌面应用程序包名
	private SharedPreferences Lock_info;
	private SharedPreferences Lock_setting;
	private SharedPreferences Screen;
	public static boolean isPhone = false; // 是否来电
	private int TouchCancelCount;
	public static ApplicationImage Ai;
	private ActivityManager mActivityManager;
	private Handler mHandler;
	private LockHelpReceivce lhr;
	private Timer timer;
	private boolean IsLockPassword; // 是否是密码锁屏
	// 此值代表了选则的应用，0表示没有选择
	static int flag = 0;
	private Vibrator vibrator;

	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}

	@Override
	public void onCreate() {

		super.onCreate();

		Screen = getSharedPreferences("Screen", 0);
		Lock_info = getSharedPreferences("Lock_info", 0);
		Lock_setting = PreferenceManager
				.getDefaultSharedPreferences(getApplication());
		Wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		wmParams = new WindowManager.LayoutParams();
		wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

		KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		IsLockPassword = km.isKeyguardSecure();
		// 得到键盘锁管理器对象
		kl = km.newKeyguardLock("unLock");

		ScreenActionReceiver sar = new ScreenActionReceiver();
		sar.registerScreenActionReceiver(this);

		// 注册广播接收器
		IntentFilter inf = new IntentFilter();
		inf.addAction("com.jiusg.lockhelper");
		lhr = new LockHelpReceivce();
		registerReceiver(lhr, inf);

		IsView = false;
		TouchCancelCount = 0;
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		wmParams.format = PixelFormat.RGBA_8888;
		wmParams.width = wmParams.height = Lock_setting.getInt("Size", 420);
		wmParams.alpha = Lock_setting.getInt("Alpha", 20) * 0.05f;
		wmParams.gravity = Gravity.TOP;
		wmParams.y = Lock_setting.getInt("LockpositionY",
				Screen.getInt("ScreenHeight", 0) / 2);
		if (Lock_setting.getString("LockpositionX", "").equals("1")) {

			wmParams.gravity = Gravity.RIGHT | Gravity.TOP;
			wmParams.x = 0;
			wmParams.width = wmParams.width / 3 * 2;
		} else if (Lock_setting.getString("LockpositionX", "").equals("-1")) {

			wmParams.gravity = Gravity.LEFT | Gravity.TOP;
			wmParams.x = 0;
			wmParams.width = wmParams.width / 3 * 2;
		}

		mHandler = new LockHandler();
		homeList = getHomes();
		Ai = new ApplicationImage();
		Ai.GetApplictionImage(this);
		LoadImageView(); // 加载布局实例View

		// 对点击View以外取消View的操作
		win.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {

					if (Lock_setting.getString("TouchCancel", "").equals("1")) {
						if (IsView)
							RemoveView();
					} else if (IsView & TouchCancelCount == 1)
						RemoveView();
					if (TouchCancelCount >= 1)
						TouchCancelCount = 0;
					else
						TouchCancelCount++;
				}
				return false;
			}
		});

		// 锁屏帮手的监听器
		ib.setOnTouchListener(new OnTouchListener() {

			int lastX, lastY;

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					lastX = (int) event.getRawX();// 获取触摸事件触摸位置的原始X坐标
					lastY = (int) event.getRawY();
					ShowLockView(); // 显示应用图标
					ib.setImageResource(R.drawable.lock_image); // 设置ib的图片可见，对于关闭了锁屏提示的
					break;
				case MotionEvent.ACTION_MOVE:

					int dx = (int) event.getRawX() - lastX;
					int dy = (int) event.getRawY() - lastY;
					int l = v.getLeft() + dx;
					int b = v.getBottom() + dy;
					int r = v.getRight() + dx;
					int t = v.getTop() + dy;

					// 对移动到了某些位置进行的操作

					// 移动到了整个View边缘,此时ib应不再移动
					if (v.getLeft() < 0) {
						l = 0;
						r = 0 + v.getWidth();
					}
					if (v.getRight() > win.getWidth()) {
						r = win.getWidth();
						l = win.getWidth() - v.getWidth();
					}
					if (v.getTop() < 0) {
						t = 0;
						b = 0 + v.getHeight();
					}
					if (v.getBottom() > win.getHeight()) {
						b = win.getHeight();
						t = win.getHeight() - v.getHeight();
					}

					// 移动到某个应用上的操作 对于/2是为了不让选择应用过于灵敏
					if (v.getTop() - ib_1.getTop() < ib_1.getHeight() / 2
							& v.getTop() - ib_1.getTop() > -ib_1.getHeight() / 2
							& v.getLeft() - ib_1.getLeft() < ib_1.getWidth() / 2
							& v.getLeft() - ib_1.getLeft() > -ib_1.getWidth() / 2) {
						if (flag != 1) {
							flag = 1;
							UpdataView(flag);
						}
					} else if (v.getTop() - ib_2.getTop() < ib_2.getHeight() / 2
							& v.getTop() - ib_2.getTop() > -ib_2.getHeight() / 2
							& v.getLeft() - ib_2.getLeft() < ib_2.getWidth() / 2
							& v.getLeft() - ib_2.getLeft() > -ib_2.getWidth() / 2) {
						if (flag != 2) {
							flag = 2;
							UpdataView(flag);
						}
					} else if (v.getTop() - ib_3.getTop() < ib_3.getHeight() / 2
							& v.getTop() - ib_3.getTop() > -ib_3.getHeight() / 2
							& v.getLeft() - ib_3.getLeft() < ib_3.getWidth() / 2
							& v.getLeft() - ib_3.getLeft() > -ib_3.getWidth() / 2) {
						if (flag != 3) {
							flag = 3;
							UpdataView(flag);
						}
					} else if (v.getTop() - ib_4.getTop() < ib_4.getHeight() / 2
							& v.getTop() - ib_4.getTop() > -ib_4.getHeight() / 2
							& v.getLeft() - ib_4.getLeft() < ib_4.getWidth() / 2
							& v.getLeft() - ib_4.getLeft() > -ib_4.getWidth() / 2) {
						if (flag != 4) {
							flag = 4;
							UpdataView(flag);
						}
					} else if (v.getTop() - ib_5.getTop() < ib_5.getHeight() / 2
							& v.getTop() - ib_5.getTop() > -ib_5.getHeight() / 2
							& v.getLeft() - ib_5.getLeft() < ib_5.getWidth() / 2
							& v.getLeft() - ib_5.getLeft() > -ib_5.getWidth() / 2) {
						if (flag != 5) {
							flag = 5;
							UpdataView(flag);
						}
					} else {
						if (flag != 0) {
							flag = 0;
							UpdataView(flag);
						}
					}

					// 更新ib的操作
					v.layout(l, t, r, b);
					lastY = (int) event.getRawY();
					lastX = (int) event.getRawX();
					v.postInvalidate();
					break;
				case MotionEvent.ACTION_UP:
					// 手势结束后，执行打开应用的方法
					OpenApp(flag);
					if (Lock_setting.getString("LockpositionX", "").equals("0")) {
						v.layout(win.getWidth() / 2 - v.getWidth() / 2,
								win.getHeight() / 2 - v.getHeight() / 2,
								win.getWidth() / 2 + v.getWidth() / 2,
								win.getHeight() / 2 + v.getHeight() / 2);
					} else if (Lock_setting.getString("LockpositionX", "")
							.equals("1")) {

						v.layout(win.getWidth() - v.getWidth(), win.getHeight()
								/ 2 - v.getHeight() / 2, win.getWidth(),
								win.getHeight() / 2 + v.getHeight() / 2);
					} else if (Lock_setting.getString("LockpositionX", "")
							.equals("-1")) {

						v.layout(0, win.getHeight() / 2 - v.getHeight() / 2,
								v.getWidth(),
								win.getHeight() / 2 + v.getHeight() / 2);
					}
					break;
				default:
					break;
				}
				return true;
			}
		});

		return START_STICKY;
	}

	/**
	 * 接受系统广播，来处理锁屏帮手
	 * 
	 * @author Administrator
	 * 
	 */
	class ScreenActionReceiver extends BroadcastReceiver {

		private boolean isRegisterReceiver = false;

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(Intent.ACTION_SCREEN_ON)) { // 亮屏

				flag = 0;
				if (!isScreenLocked(context) & !isPhone) {
					if (!IsView) {

						// 对于用户选择了“直接显示”的操作，或者“触摸显示”
						if (Lock_setting.getString("LockStyle", "").equals(
								"Show")) {

							ShowLockView();
						} else {

							// 触摸显示
						}
						Wm.addView(win, wmParams);
						IsView = true;
						TouchCancelCount = 0;

					}
				}
			} else if (action.equals(Intent.ACTION_SCREEN_OFF)) { // 灭屏

				kl.reenableKeyguard(); // /给手机屏幕上锁
				if (IsView)
					RemoveView();

			} else if (action.equals(Intent.ACTION_USER_PRESENT)) { // 解锁

				if (IsView)
					RemoveView();
			}
		}

		// 注册广播的方法
		public void registerScreenActionReceiver(Context mContext) {
			if (!isRegisterReceiver) {
				isRegisterReceiver = true;

				IntentFilter filter = new IntentFilter();
				filter.addAction(Intent.ACTION_SCREEN_OFF);
				filter.addAction(Intent.ACTION_SCREEN_ON);
				filter.addAction(Intent.ACTION_USER_PRESENT);
				mContext.registerReceiver(ScreenActionReceiver.this, filter);

			}
		}

		public void unRegisterScreenActionReceiver(Context mContext) {
			if (isRegisterReceiver) {
				isRegisterReceiver = false;
				// Logcat.d(TAG, "注销屏幕解锁、加锁广播接收者...");
				mContext.unregisterReceiver(ScreenActionReceiver.this);
			}
		}

	}

	/**
	 * 移除WindowManager的方法
	 */
	private void RemoveView() {

		if (IsView) {

			if (!Lock_setting.getBoolean("LockHint", false))
				ib.setImageResource(R.drawable.lock_ignore);
			else
				ib.setImageResource(R.drawable.lock_image);
			ib_1_1.setVisibility(View.INVISIBLE);
			ib_2_2.setVisibility(View.INVISIBLE);
			ib_3_3.setVisibility(View.INVISIBLE);
			ib_4_4.setVisibility(View.INVISIBLE);
			ib_5_5.setVisibility(View.INVISIBLE);
			ib_1.setVisibility(View.INVISIBLE);
			ib_2.setVisibility(View.INVISIBLE);
			ib_3.setVisibility(View.INVISIBLE);
			ib_4.setVisibility(View.INVISIBLE);
			ib_5.setVisibility(View.INVISIBLE);
			Wm.removeView(win);
			IsView = false;
		}
	}

	/**
	 * 判断当前界面是否为锁屏界面，这些的处理是为了在某些像来电触发屏幕点亮而触发锁屏帮手而必要考虑的
	 * 
	 * @param c
	 * @return
	 */
	public final static boolean isScreenLocked(Context c) {
		android.app.KeyguardManager mKeyguardManager = (KeyguardManager) c
				.getSystemService(c.KEYGUARD_SERVICE);
		return !mKeyguardManager.inKeyguardRestrictedInputMode();
	}

	/**
	 * 显示锁屏应用图标的方法
	 */
	private void ShowLockView() {

		if (!Lock_info.getString("Name0", "").equals("暂无")) {

			ib_1.setVisibility(View.VISIBLE);
			ib_1.setImageDrawable(ApplicationImage.db_1);
		}
		if (!Lock_info.getString("Name1", "").equals("暂无")) {

			ib_2.setVisibility(View.VISIBLE);
			ib_2.setImageDrawable(ApplicationImage.db_2);
		}
		if (!Lock_info.getString("Name2", "").equals("暂无")) {

			ib_3.setVisibility(View.VISIBLE);
			ib_3.setImageDrawable(ApplicationImage.db_3);
		}
		if (!Lock_info.getString("Name3", "").equals("暂无")) {

			ib_4.setVisibility(View.VISIBLE);
			ib_4.setImageDrawable(ApplicationImage.db_4);
		}
		if (!Lock_info.getString("Name4", "").equals("暂无")) {

			ib_5.setVisibility(View.VISIBLE);
			ib_5.setImageDrawable(ApplicationImage.db_5);

		}
	}

	// 有更简单的方法来处理，找到桌面程序的包名，就不用这么麻烦的来获取
	/**
	 * 获得属于桌面的应用的应用包名称
	 * 
	 * @return 返回包含所有包名的字符串列表
	 */
	private List<String> getHomes() {
		List<String> names = new ArrayList<String>();
		PackageManager packageManager = this.getPackageManager();
		// 属性
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(
				intent, PackageManager.MATCH_DEFAULT_ONLY);
		for (ResolveInfo ri : resolveInfo) {
			names.add(ri.activityInfo.packageName);
		}
		// names.add("com.jiusg.flashlight"); // 添加自己软件的包名
		return names;
	}

	/**
	 * 判断当前界面是否是桌面
	 */
	private boolean isHome() {

		if (mActivityManager == null) {
			mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		}
		List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
		return homeList.contains(rti.get(0).topActivity.getPackageName());
	}

	/**
	 * 开始一个计时器，不停的验证当前是否处于桌面，如果处于桌面则向LockHandler发送一条消息
	 */
	private void StartTimer() {

		TimerTask task = new TimerTask() {
			public void run() {

				Message msg1 = mHandler.obtainMessage();
				msg1.obj = ("start");
				mHandler.sendMessage(msg1);
			}
		};
		timer = new Timer(true);
		timer.schedule(task, 5000, 100);
	}

	/**
	 * 处理显示选中图标的方法
	 * 
	 * @param flag
	 *            代表哪个位置的图片
	 */
	private void UpdataView(int flag) {

		switch (flag) {

		case 0:
			ib_1_1.setVisibility(View.INVISIBLE);
			ib_2_2.setVisibility(View.INVISIBLE);
			ib_3_3.setVisibility(View.INVISIBLE);
			ib_4_4.setVisibility(View.INVISIBLE);
			ib_5_5.setVisibility(View.INVISIBLE);
			break;
		case 1:
			ib_1_1.setVisibility(View.VISIBLE);
			break;
		case 2:
			ib_2_2.setVisibility(View.VISIBLE);
			break;
		case 3:
			ib_3_3.setVisibility(View.VISIBLE);
			break;
		case 4:
			ib_4_4.setVisibility(View.VISIBLE);
			break;
		case 5:
			ib_5_5.setVisibility(View.VISIBLE);
			break;
		default:
			break;

		}
		// 震动反馈
		if (Lock_setting.getBoolean("Shake", false) && flag != 0) {

			vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			long[] pattern = { 50, 100 };

			switch (flag) {
			case 1:
				if (!(Lock_info.getString("Name0", "").equals("暂无") | Lock_info
						.getString("Name0", "").equals("")))
					vibrator.vibrate(pattern, -1);
				break;
			case 2:
				if (!(Lock_info.getString("Name1", "").equals("暂无") | Lock_info
						.getString("Name1", "").equals("")))
					vibrator.vibrate(pattern, -1);
				break;
			case 3:
				if (!(Lock_info.getString("Name2", "").equals("暂无") | Lock_info
						.getString("Name2", "").equals("")))
					vibrator.vibrate(pattern, -1);
				break;
			case 4:
				if (!(Lock_info.getString("Name3", "").equals("暂无") | Lock_info
						.getString("Name3", "").equals("")))
					vibrator.vibrate(pattern, -1);
				break;
			case 5:
				if (!(Lock_info.getString("Name4", "").equals("暂无") | Lock_info
						.getString("Name4", "").equals("")))
					vibrator.vibrate(pattern, -1);
				break;
			default:
				break;
			}

		}
	}

	/**
	 * 加载View，用于用户一些设置后，需要再次加载View（对于这里为什么会这样，因为我在布局文件中，命名是一样的！！）
	 */
	private void LoadImageView() {

		if (Lock_setting.getString("LockpositionX", "").equals("-1")) { // 在左边

			win = LayoutInflater.from(this).inflate(R.layout.window_left, null);
			ib = (ImageView) win.findViewById(R.id.lock_Left);
			ib_1_1 = (ImageView) win.findViewById(R.id.lock_left_1_1);
			ib_2_2 = (ImageView) win.findViewById(R.id.lock_left_2_2);
			ib_3_3 = (ImageView) win.findViewById(R.id.lock_left_3_3);
			ib_4_4 = (ImageView) win.findViewById(R.id.lock_left_4_4);
			ib_5_5 = (ImageView) win.findViewById(R.id.lock_left_5_5);
			ib_1 = (ImageView) win.findViewById(R.id.lock_left_1);
			ib_2 = (ImageView) win.findViewById(R.id.lock_left_2);
			ib_3 = (ImageView) win.findViewById(R.id.lock_left_3);
			ib_4 = (ImageView) win.findViewById(R.id.lock_left_4);
			ib_5 = (ImageView) win.findViewById(R.id.lock_left_5);
		} else if (Lock_setting.getString("LockpositionX", "").equals("1")) { // 在右边

			win = LayoutInflater.from(this)
					.inflate(R.layout.window_right, null);
			ib = (ImageView) win.findViewById(R.id.lock_right);
			ib_1_1 = (ImageView) win.findViewById(R.id.lock_right_1_1);
			ib_2_2 = (ImageView) win.findViewById(R.id.lock_right_2_2);
			ib_3_3 = (ImageView) win.findViewById(R.id.lock_right_3_3);
			ib_4_4 = (ImageView) win.findViewById(R.id.lock_right_4_4);
			ib_5_5 = (ImageView) win.findViewById(R.id.lock_right_5_5);
			ib_1 = (ImageView) win.findViewById(R.id.lock_right_1);
			ib_2 = (ImageView) win.findViewById(R.id.lock_right_2);
			ib_3 = (ImageView) win.findViewById(R.id.lock_right_3);
			ib_4 = (ImageView) win.findViewById(R.id.lock_right_4);
			ib_5 = (ImageView) win.findViewById(R.id.lock_right_5);

		} else { // 如果是在中间
			if (Lock_info.getString("Name4", "").equals("暂无")
					| Lock_info.getString("Name4", "").equals(""))

				win = LayoutInflater.from(this)
						.inflate(R.layout.window_0, null);
			else
				win = LayoutInflater.from(this)
						.inflate(R.layout.window_1, null);

			ib = (ImageView) win.findViewById(R.id.loock);
			ib_1_1 = (ImageView) win.findViewById(R.id.loock_1_1);
			ib_2_2 = (ImageView) win.findViewById(R.id.loock_2_2);
			ib_3_3 = (ImageView) win.findViewById(R.id.loock_3_3);
			ib_4_4 = (ImageView) win.findViewById(R.id.loock_4_4);
			ib_5_5 = (ImageView) win.findViewById(R.id.loock_5_5);
			ib_1 = (ImageView) win.findViewById(R.id.loock_1);
			ib_2 = (ImageView) win.findViewById(R.id.loock_2);
			ib_3 = (ImageView) win.findViewById(R.id.loock_3);
			ib_4 = (ImageView) win.findViewById(R.id.loock_4);
			ib_5 = (ImageView) win.findViewById(R.id.loock_5);

		}

		ib_1_1.setVisibility(View.INVISIBLE);
		ib_2_2.setVisibility(View.INVISIBLE);
		ib_3_3.setVisibility(View.INVISIBLE);
		ib_4_4.setVisibility(View.INVISIBLE);
		ib_5_5.setVisibility(View.INVISIBLE);
		ib_1.setVisibility(View.INVISIBLE);
		ib_2.setVisibility(View.INVISIBLE);
		ib_3.setVisibility(View.INVISIBLE);
		ib_4.setVisibility(View.INVISIBLE);
		ib_5.setVisibility(View.INVISIBLE);
		ib_1_1.setImageDrawable(ApplicationImage.db_1);
		ib_2_2.setImageDrawable(ApplicationImage.db_2);
		ib_3_3.setImageDrawable(ApplicationImage.db_3);
		ib_4_4.setImageDrawable(ApplicationImage.db_4);
		ib_5_5.setImageDrawable(ApplicationImage.db_5);
	}

	/**
	 * 打开应用的方法
	 * 
	 * @param flag
	 */
	private void OpenApp(int flag) {

		if (flag != 0
				& !Lock_info.getString("Name" + (flag - 1), "").equals("暂无")) {
			if (Lock_info.getString("Name" + (flag - 1), "").equals("相机")) {

				Intent i = new Intent(Intent.ACTION_CAMERA_BUTTON, null);

				sendBroadcast(i);
			} else {
				Intent intent = new Intent();
				intent.setComponent(new ComponentName(Lock_info.getString(
						"Packagename" + (flag - 1), ""), Lock_info.getString(
						"MainActivity" + (flag - 1), "")));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}

			if (!Lock_info.getString("Packagename" + (flag - 1), "").equals(
					"com.jiusg.flashlight")) {
				kl.disableKeyguard(); // 解锁手机屏幕
				RemoveView();
			}
			if (IsLockPassword) // 解锁后如果是密码锁屏，则在返回某个应用后应锁屏手机
				StartTimer();
		}
	}

	/**
	 * 处理锁屏密码的Handler类，计时器取消并执行锁屏操作
	 * 
	 * @author Administrator
	 * 
	 */
	class LockHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {

			if (isHome()) {

				timer.cancel();
				kl.reenableKeyguard();
			}
		}

	}

	/**
	 * 接收activity发来的广播
	 * 
	 * @author Administrator
	 * 
	 */
	class LockHelpReceivce extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getStringExtra("msg").equals("SizeStart")) {

				wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
				ShowLockView();
				Wm.addView(win, wmParams);
				IsView = true;
			} else if (intent.getStringExtra("msg").equals("Size")) {

				Wm.removeView(win);
				wmParams.width = wmParams.height = Lock_setting.getInt("Size",
						420);
				if (!Lock_setting.getString("LockpositionX", "").equals("0"))
					wmParams.width = wmParams.width / 3 * 2;
				Wm.addView(win, wmParams);
			} else if (intent.getStringExtra("msg").equals("SizeStop")) {

				Wm.removeView(win);
				wmParams.width = wmParams.height = Lock_setting.getInt("Size",
						420);
				if (!Lock_setting.getString("LockpositionX", "").equals("0"))
					wmParams.width = wmParams.width / 3 * 2;
				wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
				IsView = false;
			} else if (intent.getStringExtra("msg").equals("AlphaStart")) {

				wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
				ShowLockView();
				Wm.addView(win, wmParams);
				IsView = true;
			} else if (intent.getStringExtra("msg").equals("Alpha")) {

				Wm.removeView(win);
				wmParams.alpha = Lock_setting.getInt("Alpha", 20) * 0.05f;
				Wm.addView(win, wmParams);
			} else if (intent.getStringExtra("msg").equals("AlphaStop")) {

				Wm.removeView(win);
				wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
				IsView = false;
			} else if (intent.getStringExtra("msg").equals("HeightStart")) {

				wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
				ShowLockView();
				Wm.addView(win, wmParams);
				IsView = true;
			} else if (intent.getStringExtra("msg").equals("Height")) {

				Wm.removeView(win);
				wmParams.y = Lock_setting.getInt("LockpositionY",
						Screen.getInt("ScreenHeight", 0) / 2);
				Wm.addView(win, wmParams);
			} else if (intent.getStringExtra("msg").equals("HeightStop")) {

				Wm.removeView(win);
				wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
				IsView = false;
			}
		}

	}

}
