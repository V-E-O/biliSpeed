package com.veo.hook.bili.speed;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText et = findViewById(R.id.editTextText);
        final Button button = findViewById(R.id.button);

        try {
            prefs = this.getSharedPreferences("speed", Context.MODE_WORLD_READABLE);
            float speed = prefs.getFloat("speed", 1.5f);
            et.setText(String.valueOf(speed));
        } catch (SecurityException ignored) {
            // The new XSharedPreferences is not enabled or module's not loading
            prefs = null;
            Toast.makeText(getApplicationContext(), "激活模块后，重新打开本应用", Toast.LENGTH_LONG).show();
        }

        button.setOnClickListener(v -> {
            if (prefs == null) {
                Toast.makeText(getApplicationContext(), "未激活模块或不支持XSharedPreferences", Toast.LENGTH_LONG).show();
            } else {
                SharedPreferences.Editor e = prefs.edit();
                try {
                    float speed = Float.valueOf(et.getText().toString());
                    e.putFloat("speed", speed);
                    e.commit();
                    Toast.makeText(getApplicationContext(), "设置成功，重启bili生效", Toast.LENGTH_LONG).show();
                } catch (NumberFormatException ignored) {
                    Toast.makeText(getApplicationContext(), "输浮点数字~~", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}