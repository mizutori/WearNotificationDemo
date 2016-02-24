package com.goldrushcomputing.wearnotificationdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.goldrushcomputing.wearnotificationdemo.utilities.WearService;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    //Android Wear
    public WearService wearService;
    Timer zoomBlockingTimer;
    Handler handlerOnUIThread;
    int oxygenRatio = 0;
    TextView oxygenRatioTextView;

    private ServiceConnection wearServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            String name = className.getClassName();

            if(name.endsWith("WearService")){
                wearService = ((WearService.WearServiceBinder)service).getService();
            }



        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            if(className.getClassName().equals("WearService")){
                wearService = null;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton oxygenButton = (FloatingActionButton) findViewById(R.id.oxygen_notification_button);
        oxygenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Sending Oxygen Ratio Notification...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();



                /* Update Android Wear to transit from running view to plan view */
                Intent intentWear = new Intent(Constants.kNotification_WearOxygenNotification);
                intentWear.putExtra("oxygenRatio", oxygenRatio);
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intentWear);
            }
        });


        FloatingActionButton reminderButton = (FloatingActionButton) findViewById(R.id.reminder_notification_button);
        reminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Sending Reminder Notification...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();



                /* Update Android Wear to transit from running view to plan view */
                Intent intentWear = new Intent(Constants.kNotification_WearReminderNotification);
                intentWear.putExtra("reminderMessage", "Leave Basecamp");
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intentWear);
            }
        });


        /* Start WearService */
        Log.d(TAG, "Start WearService");
        final Intent wearServiceStart = new Intent(this.getApplication(), WearService.class);
        this.getApplication().startService(wearServiceStart);
        this.getApplication().bindService(wearServiceStart, wearServiceConnection, Context.BIND_AUTO_CREATE);



        oxygenRatioTextView = (TextView)findViewById(R.id.main_oxygen_value);


        handlerOnUIThread = new Handler();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handlerOnUIThread.post(new Runnable() {
                    @Override
                    public void run() {
                        int max = 30;
                        int min = 1;

                        Random rand = new Random();
                        // nextInt is normally exclusive of the top value,
                        // so add 1 to make it inclusive
                        oxygenRatio = rand.nextInt((max - min) + 1) + min;
                        oxygenRatioTextView.setText(oxygenRatio + "%");


                    }
                });
            }
        };
        zoomBlockingTimer = new Timer();
        zoomBlockingTimer.scheduleAtFixedRate(task, 0, 2 * 1000);
        Log.d(TAG, "start blocking auto zoom");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onDestroy() {
        if (zoomBlockingTimer != null) {
            zoomBlockingTimer.cancel();
        }

        super.onDestroy();

    }
}
