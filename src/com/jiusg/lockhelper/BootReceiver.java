package com.jiusg.lockhelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 接收系统广播，开机自启服务
 * 
 * @author Administrator
 * 
 */
public class BootReceiver extends BroadcastReceiver {

	private SharedPreferences Lock_setting;
	private SharedPreferences sp_ver;

	@Override
	public void onReceive(Context context, Intent intent) {

		Lock_setting = PreferenceManager.getDefaultSharedPreferences(context);
		sp_ver = context.getSharedPreferences("Screen", 0);

		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

			// 当用户设置了开机自启并且是正式版，或者未到期的试用版才能进行开机自启
			if (Lock_setting.getBoolean("PowerBoot", false)
					& (sp_ver.getString("UserVersionInfo", "").equals(
							"OfficialVersionISTRUE") || sp_ver.getString(
							"UserVersionInfo", "").equals("TrialVersion")))
				context.startService(new Intent(context,
						LockHelperService.class));

		}

	}

}
