package com.example.smartsoundcontroller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;

public class SettingActivity extends Activity implements View.OnClickListener {

    public SharedPreferences appData;

    EditText stdDbEdt;
    SeekBar stdVolumeBar;
    EditText stdUnitEdt;
    CheckBox vibCheckBox;
    Button applyBtn;

    AudioManager audioManager;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        stdDbEdt = (EditText)findViewById(R.id.stdDbEdt);
        stdVolumeBar = (SeekBar)findViewById(R.id.stdVolumeBar);
        stdUnitEdt = (EditText)findViewById(R.id.stdUnitEdt);
        vibCheckBox = (CheckBox)findViewById(R.id.vibCheckBox);
        applyBtn = (Button)findViewById(R.id.applyBtn);

        // AudioManager (기기의 볼륨 상태 가져오는 것)
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        stdVolumeBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        // Vibrator: 진동 관련
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        // 이벤트 리스너 등록
        applyBtn.setOnClickListener((View.OnClickListener)this);
        vibCheckBox.setOnClickListener((View.OnClickListener)this);

        // 저장된 설정 값 불러오기
        appData = getSharedPreferences("appData", MODE_PRIVATE);
        stdDbEdt.setText(Integer.toString(appData.getInt("STD_DB", 20)));
        stdVolumeBar.setProgress(appData.getInt("STD_VOLUME", 3));
        stdUnitEdt.setText(Integer.toString(appData.getInt("STD_UNIT", 5)));
        vibCheckBox.setChecked(appData.getBoolean("VIB_CHECK", false));
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.applyBtn) {
            // 적용 버튼 누르면 기준 dB, 기준 볼륨, 기준 단위, 진동 여부 4가지 값 저장하고 레이아웃 종료(부모 레이아웃으로 복귀)
            SharedPreferences.Editor editor = appData.edit();
            editor.putInt("STD_DB", Integer.parseInt(stdDbEdt.getText().toString().trim()));
            editor.putInt("STD_VOLUME", stdVolumeBar.getProgress());
            editor.putInt("STD_UNIT", Integer.parseInt(stdUnitEdt.getText().toString().trim()));
            editor.putBoolean("VIB_CHECK", vibCheckBox.isChecked());
            editor.apply();

            finish();
        }
        // 진동 체크박스 클릭
        else if(v.getId() == R.id.vibCheckBox) {
            // 체크될 때 잠깐 진동
            if(((CheckBox)v).isChecked()) {
                vibrator.vibrate(200);
            }
        }
        else {
            // empty
        }
    }
}
