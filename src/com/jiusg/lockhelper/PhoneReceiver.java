package com.jiusg.lockhelper;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class PhoneReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			
			// 拨出电话
		} else {
			// 非去电即来电
			LockHelperService.isPhone = true;
			TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);     
            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);  

		}
	}
	
	PhoneStateListener listener=new PhoneStateListener(){  
		   
        @Override  
        public void onCallStateChanged(int state, String incomingNumber) {  
                // TODO Auto-generated method stub  
                //state 当前状态 incomingNumber,貌似没有去电的API  
                super.onCallStateChanged(state, incomingNumber);  
                switch(state){  
                case TelephonyManager.CALL_STATE_IDLE:  
                        LockHelperService.isPhone = false;
                        break;  
                /*case TelephonyManager.CALL_STATE_OFFHOOK:  
                        System.out.println("接听");  
                        break;  
                case TelephonyManager.CALL_STATE_RINGING:  
                        System.out.println("响铃:来电号码"+incomingNumber);  
                        //输出来电号码  
                        break; */ 
                }  
        }   
};  

}
