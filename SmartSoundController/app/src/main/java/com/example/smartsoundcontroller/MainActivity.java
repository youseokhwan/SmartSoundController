package com.example.smartsoundcontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends Activity implements View.OnClickListener {
    private BluetoothSPP bt;

    SeekBar volumeBar;
    TextView currentdB;
    Button settingBtn, exitBtn;
    AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 블루투스!! ㅁㄴㅇㄹㄴㅇㄹㄴ
        // 여기다가 해보자
        // ㄷㄹㅁㄴㅇㄹㄴㅇㄹ

        volumeBar = (SeekBar)findViewById(R.id.volumeBar);
        currentdB = (TextView)findViewById(R.id.currentdB);
        settingBtn = (Button)findViewById(R.id.settingBtn);
        exitBtn = (Button)findViewById(R.id.exitBtn);

        settingBtn.setOnClickListener((View.OnClickListener)this);
        exitBtn.setOnClickListener((View.OnClickListener)this);

        // 기기의 현재 미디어 볼륨 정보를 가져와서 volumeBar에 적용
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
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

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }
        });
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

    // 기기 볼륨 Up, Down키 조절할 때 volumeBar에 실시간 반영
    public boolean onKeyDown(int keycode, KeyEvent event) {
        switch(keycode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                break;
        }

        // true면 볼륨 조절 시스템 창 뜨지 않게 설정
        return false;
    }
}
