package com.goldrushcomputing.wearnotificationdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.goldrushcomputing.wearnotificationdemo.utilities.Constants;
import com.goldrushcomputing.wearnotificationdemo.utilities.DismissNotificationCommand;


/**
 * Created by Takamitsu Mizutori on 11/23/2015.
 */
public class NotificationActivity extends Activity {
    BoxInsetLayout boxInsetLayout;
    TextView titleTextView;
    TextView messageTextView;
    ImageView iconImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        boxInsetLayout = (BoxInsetLayout)findViewById(R.id.boxLayout);
        titleTextView = (TextView)findViewById(R.id.notification_title);
        messageTextView = (TextView)findViewById(R.id.notification_message);
        iconImageView = (ImageView)findViewById(R.id.notification_icon);

        if(getIntent()!=null)
        {
            if(getIntent().hasExtra(Constants.DEMO_NOTIFICATION_TITLE)){
                titleTextView.setText(getIntent().getStringExtra(Constants.DEMO_NOTIFICATION_TITLE));
            }

            if(getIntent().hasExtra(Constants.DEMO_NOTIFICATION_MESSAGE)){
                messageTextView.setText(getIntent().getStringExtra(Constants.DEMO_NOTIFICATION_MESSAGE));
            }


            if(getIntent().hasExtra(Constants.DEMO_NOTIFICATION_ICON_ID)){
                int resId = getIntent().getIntExtra(Constants.DEMO_NOTIFICATION_ICON_ID, 0);
                if(resId > 0){
                    Bitmap bm = BitmapFactory.decodeResource(getResources(), resId);
                    iconImageView.setImageBitmap(bm);
                    iconImageView.setVisibility(View.VISIBLE);
                }
            }else{
                iconImageView.setVisibility(View.INVISIBLE);
            }
        }
       boxInsetLayout.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

           }
       });
    }


    private void dismissNotification() {
        new DismissNotificationCommand(this).execute();
    }


}
