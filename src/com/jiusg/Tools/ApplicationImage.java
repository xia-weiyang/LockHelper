package com.jiusg.Tools;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

public class ApplicationImage {

	public static Drawable db_1;
	public static Drawable db_2;
	public static Drawable db_3;
	public static Drawable db_4;
	public static Drawable db_5;

	// 加载应用的图标
	public void GetApplictionImage(Context mcontext) {
		
		SharedPreferences Lock_info = mcontext.getSharedPreferences(
				"Lock_info", 0);
		PackageManager pm = mcontext.getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent,
				PackageManager.GET_RESOLVED_FILTER);
		Collections.sort(resolveInfos,
				new ResolveInfo.DisplayNameComparator(pm));

		for (ResolveInfo reInfo : resolveInfos) {
			String activityName = reInfo.activityInfo.name; // 获得该应用程序的启动Activity的name
			String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
			String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
			Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标

			if (Lock_info.getString("Packagename0", "").equals(
					pkgName) & Lock_info.getString("Name0", "").equals(appLabel)) {

				db_1 = icon;
			}
			if (Lock_info.getString("Packagename1", "").equals(
					pkgName) & Lock_info.getString("Name1", "").equals(appLabel)) {

				db_2 = icon;
			}
			if (Lock_info.getString("Packagename2", "").equals(
					pkgName) & Lock_info.getString("Name2", "").equals(appLabel)) {

				db_3 = icon;
			}
			if (Lock_info.getString("Packagename3", "").equals(
					pkgName) & Lock_info.getString("Name3", "").equals(appLabel)) {

				db_4 = icon;
			}
			if (Lock_info.getString("Packagename4", "").equals(
					pkgName) & Lock_info.getString("Name4", "").equals(appLabel)) {

				db_5 = icon;
			}

		System.out.println(appLabel + " activityName---" + activityName
					+ " pkgName---" + pkgName);
		}
	}

}
