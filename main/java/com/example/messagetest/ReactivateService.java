package com.example.messagetest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;


// 서비스가 삭제될때 마다  onDestory 메소드가 호출되며 실제로 제거되기 전에 해당 메소드를 사용하여 브로드캐스트 수신기를 호출함
public class ReactivateService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Check: ","Receiver Started");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, SensorService.class));
        } else {
            context.startService(new Intent(context, SensorService.class));
        }
    }
}