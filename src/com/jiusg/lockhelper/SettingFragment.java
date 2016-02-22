package com.jiusg.lockhelper;

import com.jiusg.Tools.Mydialog;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SettingFragment extends PreferenceFragment {

	private SharedPreferences sp;
	private PreferenceScreen pfs_lesson;
	private PreferenceScreen pfs_version;
	private PreferenceScreen pfs_exit;
	private PreferenceScreen pfs_size;
	private PreferenceScreen pfs_positionY;
	private PreferenceScreen pfs_alpha;
	private Handler hd;
	private SharedPreferences sp_ver;
	private ListPreference lpf_positionX;
	private ListPreference lpf_style;
	private ListPreference lpf_cancel;
	public ProgressDialog mydialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.setting);

		sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sp_ver = getActivity().getSharedPreferences("Screen", 0);
		pfs_lesson = (PreferenceScreen) findPreference("Lesson");
		pfs_version = (PreferenceScreen) findPreference("Version");
		pfs_exit = (PreferenceScreen) findPreference("Exit");
		pfs_size = (PreferenceScreen) findPreference("Size");
		pfs_alpha = (PreferenceScreen) findPreference("Alpha");
		pfs_positionY = (PreferenceScreen) findPreference("LockpositionY");
		lpf_positionX = (ListPreference) findPreference("LockpositionX");
		lpf_style = (ListPreference) findPreference("LockStyle");
		lpf_cancel = (ListPreference) findPreference("TouchCancel");

		hd = new SettingHandle();
		Message msg = hd.obtainMessage();
		msg.obj = "Version";
		hd.sendMessageDelayed(msg, 2000);

		if (sp.getString("LockStyle", "").equals("Show"))
			lpf_style.setSummary("激活锁屏助手的方式,当前:直接显示");
		else
			lpf_style.setSummary("激活锁屏助手的方式,当前:触摸显示");

		pfs_lesson
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						final Mydialog dl = new Mydialog(getActivity(),
								R.style.dialog);
						LayoutInflater inflater = LayoutInflater
								.from(getActivity());
						View layout = inflater.inflate(R.layout.lesson, null,
								false);
						dl.setContentView(layout);
						dl.setCancelable(true);
						dl.show();
						return false;
					}
				});
		pfs_version
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						String appIdentify = "360f07008a874b078626604f4ba99a7f";
						Uri appUri = Uri
								.parse("mstore:http://app.meizu.com/phone/apps/"
										+ appIdentify);
						Intent intent = new Intent(Intent.ACTION_VIEW, appUri);
						startActivity(intent);
						/*
						 * Uri uri = Uri.parse("http://app.meizu.com/guowang");
						 * startActivity(new Intent(Intent.ACTION_VIEW,uri));
						 */
						return true;
					}
				});
		pfs_exit.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {

				Intent it = new Intent();
				it.setClass(getActivity(), LockHelperService.class);
				getActivity().stopService(it);
				getActivity().finish();
				System.exit(0);
				return false;
			}
		});
		pfs_positionY
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					SharedPreferences Lock_setting = PreferenceManager
							.getDefaultSharedPreferences(getActivity());
					SharedPreferences Screen = getActivity()
							.getSharedPreferences("Screen", 0);
					int Height = Screen.getInt("ScreenHeight", 0);

					@Override
					public boolean onPreferenceClick(Preference preference) {

						final SeekBar sb = new SeekBar(getActivity());
						sb.setMax(30);
						sb.setProgress((Lock_setting
								.getInt("LockpositionY", 10) * 30 / Height));
						new AlertDialog.Builder(getActivity()).setTitle("调节高度")
								.setIcon(android.R.drawable.ic_dialog_info)
								.setView(sb).setPositiveButton("确定", null)
								.show();
						sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

							@Override
							public void onStopTrackingTouch(SeekBar seekBar) {
								// TODO Auto-generated method stub

								Intent it = new Intent();
								it.setAction("com.jiusg.lockhelper");
								it.putExtra("msg", "HeightStop");
								getActivity().sendBroadcast(it);
							}

							@Override
							public void onStartTrackingTouch(SeekBar seekBar) {

								Intent it = new Intent();
								it.setAction("com.jiusg.lockhelper");
								it.putExtra("msg", "HeightStart");
								getActivity().sendBroadcast(it);

							}

							@Override
							public void onProgressChanged(SeekBar seekBar,
									int progress, boolean fromUser) {

								int value = Height / 30 * progress;
								Lock_setting.edit()
										.putInt("LockpositionY", value)
										.commit();
								Intent it = new Intent();
								it.setAction("com.jiusg.lockhelper");
								it.putExtra("msg", "Height");
								getActivity().sendBroadcast(it);
							}
						});
						return false;
					}
				});
		lpf_positionX
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {

						mydialog = ProgressDialog.show(getActivity(), null,
								"正在应用设置...", true);
						Intent it = new Intent();
						it.setClass(getActivity(), LockHelperService.class);
						getActivity().startService(it);
						Message msg = hd.obtainMessage();
						msg.obj = "LockpositionX";
						hd.sendMessageDelayed(msg, 3000);
						return true;
					}
				});
		lpf_style
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {

						Message msg = hd.obtainMessage();
						msg.obj = "LockStyle";
						hd.sendMessage(msg);
						return true;
					}
				});
		lpf_cancel
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {

						Message msg = hd.obtainMessage();
						msg.obj = "TouchCancel";
						hd.sendMessage(msg);
						return true;
					}
				});
		pfs_size.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			SharedPreferences Lock_setting = PreferenceManager
					.getDefaultSharedPreferences(getActivity());

			@Override
			public boolean onPreferenceClick(Preference preference) {

				final SeekBar sb = new SeekBar(getActivity());
				sb.setMax(20);
				sb.setProgress((Lock_setting.getInt("Size", 0) - 300) / 10);
				new AlertDialog.Builder(getActivity()).setTitle("调节大小")
						.setIcon(android.R.drawable.ic_dialog_info).setView(sb)
						.setPositiveButton("确定", null).show();
				sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub

						Intent it = new Intent();
						it.setAction("com.jiusg.lockhelper");
						it.putExtra("msg", "SizeStop");
						getActivity().sendBroadcast(it);
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

						Intent it = new Intent();
						it.setAction("com.jiusg.lockhelper");
						it.putExtra("msg", "SizeStart");
						getActivity().sendBroadcast(it);

					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {

						int value = 300 + 10 * progress;
						Lock_setting.edit().putInt("Size", value).commit();
						Intent it = new Intent();
						it.setAction("com.jiusg.lockhelper");
						it.putExtra("msg", "Size");
						getActivity().sendBroadcast(it);
					}
				});
				return false;
			}
		});
		pfs_alpha.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			SharedPreferences Lock_setting = PreferenceManager
					.getDefaultSharedPreferences(getActivity());

			@Override
			public boolean onPreferenceClick(Preference preference) {

				final SeekBar sb = new SeekBar(getActivity());
				sb.setMax(20);
				sb.setProgress(Lock_setting.getInt("Alpha", 20));
				new AlertDialog.Builder(getActivity())
						.setTitle("调节透明度(由透明到不透明)")
						.setIcon(android.R.drawable.ic_dialog_info).setView(sb)
						.setPositiveButton("确定", null).show();
				sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub

						Intent it = new Intent();
						it.setAction("com.jiusg.lockhelper");
						it.putExtra("msg", "AlphaStop");
						getActivity().sendBroadcast(it);
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

						Intent it = new Intent();
						it.setAction("com.jiusg.lockhelper");
						it.putExtra("msg", "AlphaStart");
						getActivity().sendBroadcast(it);

					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {

						Lock_setting.edit().putInt("Alpha", progress).commit();
						Intent it = new Intent();
						it.setAction("com.jiusg.lockhelper");
						it.putExtra("msg", "Alpha");
						getActivity().sendBroadcast(it);
					}
				});
				return false;
			}
		});
	}

	class SettingHandle extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.obj.equals("Version")) {

				if (sp_ver.getString("UserVersionInfo", "").equals(
						"OfficialVersionISTRUE")) {
					pfs_version.setSummary("正式版 1.1.5");
				} else if (sp_ver.getString("UserVersionInfo", "").equals(
						"TrialVersionOver")
						| sp_ver.getString("UserVersionInfo", "").equals(
								"TrialVersion"))
					pfs_version.setSummary("试用版 1.1.5");
			} else if (msg.obj.equals("LockStyle")) {

				if (sp.getString("LockStyle", "").equals("Show"))
					lpf_style.setSummary("激活锁屏助手的方式,当前:直接显示");
				else
					lpf_style.setSummary("激活锁屏助手的方式,当前:触摸显示");
			} else if (msg.obj.equals("LockpositionX")) {

				mydialog.dismiss();
			} else if (msg.obj.equals("TouchCancel")) {

				new AlertDialog.Builder(getActivity()).setTitle("提示")
						.setMessage("当次数为2时，可能会影响在锁屏的其他操作体验，如通知栏，但这样可以防止误触。")
						.setPositiveButton("确定", null).show();
			}
		}

	}
}
