package com.zune.nettimeclockauto;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this, "已为你开启自动网络时间", Toast.LENGTH_SHORT).show();
        finish();
    }
}
