/*
 * 2019년 1학기 모바일프로그래밍 프로젝트
 * https://github.com/youseokhwan/SmartSoundController
 *
 * SmartSoundController
 * - 아두이노 사운드센서로 주변 소음을 측정하여 미디어 볼륨을 자동으로 조절해주는 어플
 */

package com.example.smartsoundcontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

/**
 * SmartSoundController
 *
 * 아두이노 보드와 사운드 센서(SZH-EK033)와 블루투스 모듈(HC-06)을 이용하며,
 * 센서에서 측정한 전압 값(아마도?)을 블루투스로 기기에 전달합니다. (아두이노 스케치 사용)
 * 전달받은 값과 사용자가 설정한 값으로 수치를 계산하여 자동으로 미디어 볼륨을 조절합니다.
 */

public class MainActivity extends Activity implements View.OnClickListener {

    public SharedPreferences appData;
    private Thread soundManager;
    private BluetoothSPP bt;
    public Vibrator vibrator;

    SeekBar volumeBar;
    TextView currentdB;
    Button settingBtn, exitBtn;
    AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appData = getSharedPreferences("appData", MODE_PRIVATE);
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        bt = new BluetoothSPP(this);
        soundManager = new Thread(new Runnable() {
            final Handler threadHandler = new Handler();

            /**
             * 변경할 볼륨 = 설정한 최소 볼륨 + (주변 소음 - 설정한 기준 소음) / (설정한 단위)
             */
            @Override
            public void run() {
                while(true) {
                    try {
                        soundManager.sleep(1000);

                        threadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                int distancedB = Integer.parseInt(currentdB.getText().toString())
                                        - appData.getInt("STD_DB", 20);

                                if(distancedB < 0)
                                    distancedB = 0;

                                int divideUnit = distancedB / appData.getInt("STD_UNIT", 5);

                                // 변경할 값이랑 현재 값이랑 다르면 실행
                                if(volumeBar.getProgress() != appData.getInt("STD_VOLUME", 3) + divideUnit) {
                                    volumeBar.setProgress(appData.getInt("STD_VOLUME", 3) + divideUnit);

                                    if(appData.getBoolean("VIB_CHECK", false)) {
                                        vibrator.vibrate(500);
                                    }
                                }
                            }
                        });
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        soundManager.start();

        volumeBar = (SeekBar)findViewById(R.id.volumeBar);
        currentdB = (TextView)findViewById(R.id.currentdB);
        settingBtn = (Button)findViewById(R.id.settingBtn);
        exitBtn = (Button)findViewById(R.id.exitBtn);

        // 블루투스 사용 불가할 때 메시지 출력
        if(!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext(), "블루투스 사용 불가", Toast.LENGTH_SHORT).show();
        }

        // 블루투스 모듈 선택 화면
        Intent intent = new Intent(getApplicationContext(), DeviceList.class);
        startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);

        // 데이터 수신
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {
                // Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                int temp = (int)(20 * Math.log10(Integer.parseInt(message)));

                currentdB.setText(Integer.toString(temp));
            }
        });

        // 설정, 종료 버튼 리스너
        settingBtn.setOnClickListener((View.OnClickListener)this);
        exitBtn.setOnClickListener((View.OnClickListener)this);

        // 기기의 현재 미디어 볼륨 정보를 가져와서 volumeBar에 적용
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        currentdB.setText("0");
        volumeBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        // volumeBar의 progress값 변경 이벤트
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
                // empty
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                // empty
            }

            // Progress값 변경 시
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        // 블루투스 disabled면 실행
        if(!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        }
        else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                bt.send("Text", true);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK) {
                bt.connect(data);
            }
        }
        else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                bt.send("Text", true);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        bt.stopService(); // 앱 종료될 때 블루투스 중지
    }


    // 버튼 클릭 이벤트
    @Override
    public void onClick(View v) {
        // "설정" 버튼 클릭
        if(v.getId() == R.id.settingBtn) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        }
        // "종료" 버튼 클릭
        else if(v.getId() == R.id.exitBtn) {
            finish();
        }
        else {
            // empty
        }
    }

    // 기기 볼륨 버튼 누를 때 volumeBar에 반영
    public boolean onKeyDown(int keycode, KeyEvent event) {
        switch(keycode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(volumeBar.getProgress() > 0)
                    volumeBar.setProgress(volumeBar.getProgress() - 1);
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(volumeBar.getProgress() < volumeBar.getMax())
                    volumeBar.setProgress(volumeBar.getProgress() + 1);
                break;
        }

        return false; // true면 볼륨 조절 시스템 창 뜨지 않게 설정
    }

}
