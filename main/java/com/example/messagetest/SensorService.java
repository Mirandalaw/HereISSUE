package com.example.messagetest;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.example.messagetest.ContactModel;
import com.example.messagetest.DbHelper;
import com.example.messagetest.R;

import java.util.List;

public class SensorService extends Service {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    public SensorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public void onCreate() {

        super.onCreate();

        // 핸드폰을 Hold를 한경우에도 실행
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());

        // ShakeDetector(충격감지) 초기화
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @SuppressLint("MissingPermission")
            @Override
            public void onShake(int count) {
                // 사용자가 흔들었는지 확인
                // count수 만큼 흔들린 경우를 예로
                if (count == 3) {

                    // 진동이 울리기
                    vibrate();

                    // 사용자 위치를 얻기 위해 FusedLocationProviderClient 생성
                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

                    /* PRIORITY_BALANCED_POWER_ACCURACY 를 사용하여
                    서비스가 GPS를 통해 불필요한 전력을 사용하지 않도록
                    지금 이 순간에만 GPS를 사용합니다. */
                    fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, new CancellationToken() {
                        @Override
                        public boolean isCancellationRequested() {
                            return false;
                        }

                        @NonNull
                        @Override
                        public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                            return null;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            /* 위지 정보가 NULL인지 확인
                               두가지 경우에 대비해 두가지의 메시지를 만들기*/
                            if (location != null) {

                                // SMSManager 받기
                                SmsManager smsManager = SmsManager.getDefault();

                                // DB에 있는 모든 연락처 목록을 가져오기
                                DbHelper db = new DbHelper(SensorService.this);
                                List<ContactModel> list = db.getAllContacts();

                                // 각 연락처에 SMS 보내기
                                for (ContactModel c : list) {
                                    String message = "지금 , " + c.getName() +" 님, " + "저는 위험에 처했습니다.도움이 필요합니다. 긴급한 상황입니다. 현재 저의 위치입니다.\n ";
//                                    "\https://www.google.com/maps/\"+ location.getLatitude() + "," + location.getLongitude()
                                    smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                                }
                            } else {
                                String message = "나는 위험에 처했습니다. 도움이 필요합니다. 긴급한 연락을 요합니다.\n" + "GPS가 꺼져 있어 현재 위치를 찾을 수 없습니다. 근처 경찰서에 연락해주세요.";
                                SmsManager smsManager = SmsManager.getDefault();
                                DbHelper db = new DbHelper(SensorService.this);
                                List<ContactModel> list = db.getAllContacts();
                                for (ContactModel c : list) {
                                    smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Check: ", "OnFailure");
                            String message = "저는 위험한 상황입니다. 도움이 필요합니다. 긴급한 연락을 요합니다.\n" + "GPS가 꺼져 있어 현재 위치를 찾을 수 없습니다. 근처 경찰서에 연락해주세요.";
                            SmsManager smsManager = SmsManager.getDefault();
                            DbHelper db = new DbHelper(SensorService.this);
                            List<ContactModel> list = db.getAllContacts();
                            for (ContactModel c : list) {
                                smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                            }
                        }
                    });

                }
            }
        });

        // 받는 사람 등록
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    // 진동시키는 메소드
    public void vibrate() {

        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect vibEff;

        // Android Q and above have some predefined vibrating patterns
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibEff = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK);
            vibrator.cancel();
            vibrator.vibrate(vibEff);
        } else {
            vibrator.vibrate(500);
        }


    }

    // For Build versions higher than Android Oreo, we launch
    // a foreground service in a different way. This is due to the newly
    // implemented strict notification rules, which require us to identify
    // our own notification channel in order to view them correctly.
    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_MIN);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("당신을 지켜주는 중입니다.")
                .setContentText("우리는 당신 곁에 있습니다.")

                // this is important, otherwise the notification will show the way
                // you want i.e. it will show some default notification
                .setSmallIcon(R.drawable.ic_launcher_foreground)

                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public void onDestroy() {
        // create an Intent to call the Broadcast receiver
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, ReactivateService.class);
        this.sendBroadcast(broadcastIntent);
        super.onDestroy();
    }

}