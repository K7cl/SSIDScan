package com.k7cl.ssidscan;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;

public class SecInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sec_info);

        Bundle bundle = getIntent().getExtras();
        String[] arraySSID = new String[10];
        System.arraycopy(bundle.getStringArray("arraySSID"), 0, arraySSID, 0, 10);

        TextView SSID2 = (TextView) findViewById(R.id.SSID2);
        SSID2.setText(arraySSID[0]);

        TextView MAC2 = (TextView) findViewById(R.id.MAC2);
        MAC2.setText(arraySSID[1]);

        TextView encrypt2 = (TextView) findViewById(R.id.encrypt2);
        encrypt2.setText(arraySSID[2]);

        TextView vuls2 = (TextView) findViewById(R.id.vuls2);
        vuls2.setText(arraySSID[3]);


        int location = Integer.parseInt(arraySSID[4]);
        Log.d("location", arraySSID[4]);
        if ( location != 0 ){
            ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.parseColor("red"));
            SpannableStringBuilder builder = new SpannableStringBuilder(SSID2.getText().toString());
            builder.setSpan(redSpan, location-1, location, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            SSID2.setText(builder);
        }


    }
}