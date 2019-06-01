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

    EditText standardDecibelEditText;
    SeekBar standardVolumeSeekBar;
    EditText standardUnitEditText;
    CheckBox vibratorCheckBox;
    Button applyButton;

    AudioManager audioManager;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        standardDecibelEditText = (EditText)findViewById(R.id.standardDecibelEditText);
        standardVolumeSeekBar = (SeekBar)findViewById(R.id.standardVolumeSeekBar);
        standardUnitEditText = (EditText)findViewById(R.id.standardUnitEditText);
        vibratorCheckBox = (CheckBox)findViewById(R.id.vibratorCheckBox);
        applyButton = (Button)findViewById(R.id.applyButton);

        // 기기의 볼륨 상태 가져오고 standardVolumeSeekBar의 Max값 지정
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        standardVolumeSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        // 진동 관련
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        // 이벤트 리스너 등록
        applyButton.setOnClickListener((View.OnClickListener)this);
        vibratorCheckBox.setOnClickListener((View.OnClickListener)this);

        // 저장된 설정 값 불러오기
        appData = getSharedPreferences("appData", MODE_PRIVATE);
        standardDecibelEditText.setText(Integer.toString(appData.getInt("STD_DB", 20)));
        standardVolumeSeekBar.setProgress(appData.getInt("STD_VOLUME", 3));
        standardUnitEditText.setText(Integer.toString(appData.getInt("STD_UNIT", 5)));
        vibratorCheckBox.setChecked(appData.getBoolean("VIB_CHECK", false));
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.applyButton) {
            // 적용 버튼 누르면 기준 dB, 기준 볼륨, 기준 단위, 진동 여부 4가지 값 저장하고 해당 레이아웃 종료
            SharedPreferences.Editor editor = appData.edit();
            editor.putInt("STD_DB", Integer.parseInt(standardDecibelEditText.getText().toString().trim()));
            editor.putInt("STD_VOLUME", standardVolumeSeekBar.getProgress());
            editor.putInt("STD_UNIT", Integer.parseInt(standardUnitEditText.getText().toString().trim()));
            editor.putBoolean("VIB_CHECK", vibratorCheckBox.isChecked());
            editor.apply();

            finish();
        }
        // 진동 체크박스 클릭
        else if(v.getId() == R.id.vibratorCheckBox) {
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
