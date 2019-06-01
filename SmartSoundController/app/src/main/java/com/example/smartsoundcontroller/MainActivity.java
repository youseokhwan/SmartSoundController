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
    private BluetoothSPP bluetoothSPP;
    public Vibrator vibrator;

    SeekBar currentVolumeSeekBar;
    TextView currentDecibelTextView;
    Button settingButton, exitButton;
    AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appData = getSharedPreferences("appData", MODE_PRIVATE);
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        bluetoothSPP = new BluetoothSPP(this);
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
                                int distancedB = Integer.parseInt(currentDecibelTextView.getText().toString())
                                        - appData.getInt("STD_DB", 20);

                                if(distancedB < 0)
                                    distancedB = 0;

                                int divideUnit = distancedB / appData.getInt("STD_UNIT", 5);

                                // 변경할 값이랑 현재 값이랑 다르면 실행
                                if(currentVolumeSeekBar.getProgress() != appData.getInt("STD_VOLUME", 3) + divideUnit) {
                                    currentVolumeSeekBar.setProgress(appData.getInt("STD_VOLUME", 3) + divideUnit);

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

        currentVolumeSeekBar = (SeekBar)findViewById(R.id.currentVolumeSeekBar);
        currentDecibelTextView = (TextView)findViewById(R.id.currentDecibelTextView);
        settingButton = (Button)findViewById(R.id.settingButton);
        exitButton = (Button)findViewById(R.id.exitButton);

        // 블루투스 사용 불가할 때 메시지 출력
        if(!bluetoothSPP.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext(), "블루투스 사용 불가", Toast.LENGTH_SHORT).show();
        }

        // 블루투스 모듈 선택 화면
        Intent intent = new Intent(getApplicationContext(), DeviceList.class);
        startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);

        // 데이터 수신
        bluetoothSPP.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {
                // Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                int temp = (int)(20 * Math.log10(Integer.parseInt(message)));

                currentDecibelTextView.setText(Integer.toString(temp));
            }
        });

        // 설정, 종료 버튼 리스너
        settingButton.setOnClickListener((View.OnClickListener)this);
        exitButton.setOnClickListener((View.OnClickListener)this);

        // 기기의 현재 미디어 볼륨 정보를 가져와서 volumeBar에 적용
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        currentDecibelTextView.setText("0");
        currentVolumeSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        currentVolumeSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        // volumeBar의 progress값 변경 이벤트
        currentVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        if(!bluetoothSPP.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        }
        else {
            if(!bluetoothSPP.isServiceAvailable()) {
                bluetoothSPP.setupService();
                bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
                bluetoothSPP.send("Text", true);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK) {
                bluetoothSPP.connect(data);
            }
        }
        else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bluetoothSPP.setupService();
                bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
                bluetoothSPP.send("Text", true);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        bluetoothSPP.stopService(); // 앱 종료될 때 블루투스 중지
    }


    // 버튼 클릭 이벤트
    @Override
    public void onClick(View v) {
        // "설정" 버튼 클릭
        if(v.getId() == R.id.settingButton) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        }
        // "종료" 버튼 클릭
        else if(v.getId() == R.id.exitButton) {
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
                if(currentVolumeSeekBar.getProgress() > 0)
                    currentVolumeSeekBar.setProgress(currentVolumeSeekBar.getProgress() - 1);
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(currentVolumeSeekBar.getProgress() < currentVolumeSeekBar.getMax())
                    currentVolumeSeekBar.setProgress(currentVolumeSeekBar.getProgress() + 1);
                break;
        }

        return false; // true면 볼륨 조절 시스템 창 뜨지 않게 설정
    }

}
