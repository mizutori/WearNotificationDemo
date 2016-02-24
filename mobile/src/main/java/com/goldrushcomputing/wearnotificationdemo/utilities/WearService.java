package com.goldrushcomputing.wearnotificationdemo.utilities;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.goldrushcomputing.wearnotificationdemo.Constants;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

public class WearService extends Service implements NodeApi.NodeListener{
    GoogleApiClient mGoogleApiClient;
    private final WearServiceBinder binder = new WearServiceBinder();
    public ArrayList<Node> connectedDevices = new ArrayList<Node>();
    public static final String TAG = "WearService";


    final Handler mainHandler = new Handler();

    /*
     * Receivers
     */
    public BroadcastReceiver oxygenNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int oxygenRatio = intent.getIntExtra("oxygenRatio", 0);

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    sendOxygenNotification(oxygenRatio);
                }
            };
            mainHandler.post(myRunnable);



        }
    };

    public BroadcastReceiver reminderNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String reminderMessage = intent.getStringExtra("reminderMessage");

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    sendReminderNotification(reminderMessage);
                }
            };
            mainHandler.post(myRunnable);
        }
    };


    public WearService() {
    }

    @Override
    public void onCreate() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                Wearable.NodeApi.addListener(mGoogleApiClient, WearService.this);
                // Wearable.MessageApi.addListener(mGoogleApiClient, mMessageListener);

                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                        .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                            @Override
                            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                                if(getConnectedNodesResult.getStatus().isSuccess() && getConnectedNodesResult.getNodes().size() > 0) {
                                    List<Node> nodes = getConnectedNodesResult.getNodes();
                                    connectedDevices.clear();
                                    connectedDevices.addAll(nodes);

                                    for(Node node : nodes){
                                        String id  = node.getId();
                                        String displayName = node.getDisplayName();
                                        Log.v("WearService", "Connected wearable device displayName = " + displayName);

                                    }
                                } else {

                                }
                            }
                        });
            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        })
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        registerReceiver();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
        unregisterReceiver();
    }


    private void registerReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(oxygenNotificationReceiver, new IntentFilter(Constants.kNotification_WearOxygenNotification));
        LocalBroadcastManager.getInstance(this).registerReceiver(reminderNotificationReceiver, new IntentFilter(Constants.kNotification_WearReminderNotification));

    }


    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(oxygenNotificationReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(reminderNotificationReceiver);
    }

    public class WearServiceBinder extends Binder {
        public WearService getService() {
            return WearService.this;
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        stopSelf();
    }


    private void sendOxygenNotification(int oxygenRatio){
        if (mGoogleApiClient.isConnected()) {
            String NOTIFICATION_PATH = "/data";

            PutDataMapRequest dataMapRequest = PutDataMapRequest.create(NOTIFICATION_PATH);
            // Make sure the data item is unique. Usually, this will not be required, as the payload
            // (in this case the title and the content of the notification) will be different for almost all
            // situations. However, in this example, the text and the content are always the same, so we need
            // to disambiguate the data item by adding a field that contains teh current time in milliseconds.
            dataMapRequest.getDataMap().putString("action", "notification");
            dataMapRequest.getDataMap().putString("type", "oxygen");
            dataMapRequest.getDataMap().putInt("oxygenRatio", oxygenRatio);

            //http://stackoverflow.com/a/25252344/1709287
            dataMapRequest.getDataMap().putLong("time", System.currentTimeMillis());

            PutDataRequest putDataRequest = dataMapRequest.asPutDataRequest();
            putDataRequest.setUrgent();
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
        }
        else {
            Log.e(TAG, "No connection to wearable available!");
        }
    }

    private void sendReminderNotification(String reminderMessage){
        if (mGoogleApiClient.isConnected()) {
            String NOTIFICATION_PATH = "/data";

            PutDataMapRequest dataMapRequest = PutDataMapRequest.create(NOTIFICATION_PATH);
            // Make sure the data item is unique. Usually, this will not be required, as the payload
            // (in this case the title and the content of the notification) will be different for almost all
            // situations. However, in this example, the text and the content are always the same, so we need
            // to disambiguate the data item by adding a field that contains teh current time in milliseconds.
            dataMapRequest.getDataMap().putString("action", "notification");
            dataMapRequest.getDataMap().putString("type", "reminder");
            dataMapRequest.getDataMap().putString("reminderMessage", reminderMessage);

            //http://stackoverflow.com/a/25252344/1709287
            dataMapRequest.getDataMap().putLong("time", System.currentTimeMillis());

            PutDataRequest putDataRequest = dataMapRequest.asPutDataRequest();
            putDataRequest.setUrgent();
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);

        }
        else {
            Log.e(TAG, "No connection to wearable available!");
        }
    }

    @Override
    public void onPeerConnected(Node node) {
        String mRemoteNodeId = node.getId();
        String displayName = node.getDisplayName();

        Log.v("WearService", "Wearable device connected. displayName = " + displayName);

        connectedDevices.add(node);
        //Toast.makeText(getApplicationContext(), "Wearable device connected. displayName = " + displayName, Toast.LENGTH_SHORT).show();

        Log.v("WearService", "Wearable device list size is now " + connectedDevices.size());
    }

    @Override
    public void onPeerDisconnected(Node node) {
        String mRemoteNodeId = node.getId();
        String displayName = node.getDisplayName();

        Log.v("WearService", "Wearable device connected. displayName = " + displayName);

        //Toast.makeText(getApplicationContext(), "Wearable device connected. displayName = " + displayName, Toast.LENGTH_SHORT).show();

        ArrayList<Node> devicesToRemove = new ArrayList<>();
        for(Node device : connectedDevices){
            if(device.getId().equals(node.getId())){
                devicesToRemove.add(device);
            }
        }
        connectedDevices.removeAll(devicesToRemove);

        Log.v("WearService", "Wearable device list size is now " + connectedDevices.size());
    }

}
