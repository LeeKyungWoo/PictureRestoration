package com.lkw.picturerestoration.common;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BRcvr extends BroadcastReceiver {
	private static final String TAG = "BRcvr";
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			PrjUtils.debug(TAG, "received");

			ComponentName cn = new ComponentName(context.getPackageName(),
					AppService.class.getName());
			ComponentName svcName = context.startService(new Intent()
					.setComponent(cn));
			if (svcName == null){
				PrjUtils.debug(TAG, "Could not start service " + cn.toString());
			}
		}
	}

}
