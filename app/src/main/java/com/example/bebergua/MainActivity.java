package com.example.bebergua;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_NOTIFY = "key_notify";
    private static final String KEY_INTERVAL = "key_interval";
    private static final String KEY_HOUR = "key_hour";
    private static final String KEY_MINUTE = "key_minute";

    private TimePicker timePicker;
    private Button btnNotify;
    private EditText editMinutes;
    private int interval;
    private int hour;
    private int minute;
    private boolean activated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timePicker = findViewById(R.id.Time_picker);
        btnNotify = findViewById(R.id.button_notify);
        editMinutes = findViewById(R.id.edit_number_interval);

        final SharedPreferences storage = getSharedPreferences("storage", Context.MODE_PRIVATE);
        activated = storage.getBoolean(KEY_NOTIFY, false);

        if (activated) {
            btnNotify.setText(R.string.pause);
            btnNotify.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.black));
            editMinutes.setText(String.valueOf(storage.getInt(KEY_INTERVAL, 0)));
            timePicker.setCurrentHour(storage.getInt(KEY_MINUTE, timePicker.getCurrentHour()));
            timePicker.setCurrentMinute(storage.getInt(KEY_MINUTE, timePicker.getCurrentMinute()));
        } else {
            btnNotify.setText(R.string.notify);
            btnNotify.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorAccent));
        }

        timePicker.setIs24HourView(true);

        btnNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!activated){
                    String sInterval = editMinutes.getText().toString();

                    if(sInterval.isEmpty()) {
                        Toast.makeText(MainActivity.this, R.string.validation, Toast.LENGTH_LONG).show();
                        return;
                    }

                    interval = Integer.parseInt(sInterval);
                    hour = timePicker.getCurrentHour();
                    minute = timePicker.getCurrentMinute();

                    Log.d("testes", String.format("%d, %d, %d", interval, hour, minute));

                    btnNotify.setText(R.string.pause);
                    btnNotify.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.black));

                    SharedPreferences.Editor edit = storage.edit();
                    edit.putBoolean(KEY_NOTIFY, true);
                    edit.putInt(KEY_INTERVAL, interval);
                    edit.putInt(KEY_HOUR, hour);
                    edit.putInt(KEY_MINUTE, minute);
                    edit.apply();

                    Intent notificationIntent = new Intent(MainActivity.this, NotificationPublisher.class);
                    notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION_ID, 1);
                    notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION, getString(R.string.alert_message));


                    PendingIntent broadcast = PendingIntent.getBroadcast(MainActivity.this, 0,
                            notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                    //building the calendar and setting it's hour
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);


                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                            interval * 60 * 1000, broadcast);

                    activated = true;
                } else {
                    btnNotify.setText(R.string.notify);
                    btnNotify.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorAccent));
                    SharedPreferences.Editor edit = storage.edit();
                    edit.putBoolean(KEY_NOTIFY, false);
                    edit.remove(KEY_INTERVAL);
                    edit.remove(KEY_HOUR);
                    edit.remove(KEY_MINUTE);
                    edit.apply();

                    Intent notificationIntent = new Intent(MainActivity.this, NotificationPublisher.class);
                    PendingIntent broadcast = PendingIntent.getBroadcast(MainActivity.this, 0,
                            notificationIntent, 0);

                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.cancel(broadcast);

                    activated = false;
                }

            }
        });

    }
}
