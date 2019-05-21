package com.example.smartsoundcontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {

    SeekBar volumeBar;
    TextView currentdB;
    Button settingBtn, exitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        volumeBar = (SeekBar)findViewById(R.id.volumeBar);
        currentdB = (TextView)findViewById(R.id.currentdB);
        settingBtn = (Button)findViewById(R.id.settingBtn);
        exitBtn = (Button)findViewById(R.id.exitBtn);

        settingBtn.setOnClickListener((View.OnClickListener)this);
        exitBtn.setOnClickListener((View.OnClickListener)this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.settingBtn) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.exitBtn) {
            finish();
        }
        else {
            // empty
        }
    }
}
