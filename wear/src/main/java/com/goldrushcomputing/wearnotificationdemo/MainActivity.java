package com.goldrushcomputing.wearnotificationdemo;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.widget.TextView;

import com.goldrushcomputing.wearnotificationdemo.model.SharedData;
import com.goldrushcomputing.wearnotificationdemo.utilities.Constants;
import com.goldrushcomputing.wearnotificationdemo.utilities.DismissNotificationCommand;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Takamitsu Mizutori on 02/16/2016.
 */
public class MainActivity extends WearableActivity  implements
    DataApi.DataListener,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener  {
    public static final String TAG = "MainActivity";

    private Context context;
    GoogleApiClient mGoogleApiClient;
    public SharedData sharedData;


    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView oxygenRationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        context = this;

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        oxygenRationTextView = (TextView) findViewById(R.id.oxygen_ratio);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        updateView();

        reloadData();

        Intent intent = getIntent();
        String trigger = intent.getStringExtra("trigger");
        if(trigger != null){
            if(trigger.equals("oxygenRatioNotification")){
                dismissOxygenRatioNotification();
            }else if(trigger.equals("reminderNotification")){
                dismissReminderNotification();
            }
        }

        //dismissOxygenRatioNotification();


    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            //mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            //oxygenRationTextView.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            //mContainerView.setBackground(null);
            //oxygenRationTextView.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }
    }


    private void dismissOxygenRatioNotification() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(Constants.OXYGEN_NOTIFICATION_ID);

        new DismissNotificationCommand(this).execute();
    }

    private void dismissReminderNotification() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(Constants.REMINDER_NOTIFICATION_ID);

        new DismissNotificationCommand(this).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    private void reloadData(){

        PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient);
        results.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                for (DataItem dataItem : dataItems) {
                    loadDataItem(dataItem);
                }
                dataItems.release();
            }
        });

    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                loadDataItem(item);
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }
    private void loadDataItem(DataItem dataItem){
        String path = dataItem.getUri().getPath();
        if (path.equals("/data")) {
            DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();

            String action = dataMap.getString("action");
            if(action != null && action.equals("notification")){
                //ignore this case
            }else{
                sharedData = new SharedData();

                sharedData.oxygenRatio = dataMap.getInt("oxygenRatio");

                //TODO:pac notification.
                Intent intent = new Intent(Constants.kNotification_wearDataModelUpdated);
                LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(intent);

                updateView();
            }
        }
    }

    private void updateView(){
        if(sharedData != null){
            oxygenRationTextView.setText("Oxygen:" + sharedData.oxygenRatio + "%");
        }
    }

}
