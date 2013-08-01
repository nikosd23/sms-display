package com.upstreamsystems.apps.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class UpstreamSmsActivity extends Activity {

	private final static boolean SMS_INBOX_ADDER_SIMULATOR = false;

    private SMSReceiver mSMSReceiver;
    private EditText txtPhoneNo;
    private TextView smsList;
    private EditText txtMessage;
    private Switch sw;
    private String smsSum="";

	private TextView inboxList;
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		inboxList = (TextView) findViewById(R.id.listView);
        smsList = (TextView) findViewById(R.id.textView3);
        txtPhoneNo = (EditText) findViewById(R.id.edit_message4);
        txtMessage = (EditText) findViewById(R.id.edit_message6);
        sw = (Switch) findViewById(R.id.sw1);

		final ScheduledExecutorService scheduler =
				Executors.newScheduledThreadPool(1);

		if(SMS_INBOX_ADDER_SIMULATOR){
			final SmsInboxAddContentSimulator beeper = new SmsInboxAddContentSimulator(getContentResolver());

			final ScheduledFuture<?> beeperHandle =
					scheduler.scheduleAtFixedRate(beeper, 10, 10, TimeUnit.SECONDS);
		}


	}

    @Override
    public void onResume(){
        super.onResume();
        mSMSReceiver = new SMSReceiver();
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        mIntentFilter.setPriority(1000);
		this.registerReceiver(mSMSReceiver, mIntentFilter);

        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                RelativeLayout r=(RelativeLayout) findViewById(R.id.rl2);
                if (isChecked)
                {
                    r.getLayoutParams().height=400;
                    r.requestLayout();
                }
                else
                {
                    r.getLayoutParams().height=0;
                    r.requestLayout();
                }
            }
        };

        sw.setOnCheckedChangeListener(listener);
        sw.setChecked(false);
    }

    @Override
    protected void onPause(){
        super.onPause();
        this.unregisterReceiver(mSMSReceiver);
    }

//	public void checkInbox(View view){
//		Uri uriSMSURI = Uri.parse("content://sms/inbox");
//		Cursor cur = getContentResolver().query(uriSMSURI, null, null, null, null);
//		String sms = "";
//		if (cur != null) {
//			while (cur.moveToNext()) {
//				String body = cur.getString(cur.getColumnIndexOrThrow("body")).toString();
//				String number = cur.getString(cur.getColumnIndexOrThrow("address")).toString();
//				sms += "From :" + number + " : " + body+"\n";
//			}
//		}
//		inboxList.setText(sms);
//	}

    public void sendSMS(View view){
        String phoneNo = txtPhoneNo.getText().toString();
        String message = txtMessage.getText().toString();
        if (phoneNo.length()>0 && message.length()>0) {
            PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, UpstreamSmsActivity.class), 0);
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNo, null, message, pi, null);
            txtPhoneNo.setText("");
            txtMessage.setText("");
        }
        else {
            Toast.makeText(getBaseContext(),"Please enter both phone number and message.",Toast.LENGTH_SHORT).show();
        }
    }

	private class SMSReceiver extends BroadcastReceiver {
		private final String TAG = this.getClass().getSimpleName();

		@Override
		public void onReceive(Context context, Intent intent) {
            Log.d(this.getClass().getCanonicalName(), String.valueOf(isOrderedBroadcast()));
            //---get the SMS message passed in---
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String str = "";
            if (bundle != null) {
                //---retrieve the SMS message received---
                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    if (msgs[i].getOriginatingAddress().equals("15555215556")){
                        this.abortBroadcast();
                        str += "SMS from " + msgs[i].getOriginatingAddress();
                        str += " :";
                        str += msgs[i].getMessageBody().toString();
                        str += "\n";
                        smsSum += str;
                        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                    }
                }
                smsList.setText(smsSum);
                //---display the new SMS message---
            }
		}
	}

	private class SmsInboxAddContentSimulator implements Runnable{

		private final ContentResolver contentResolver;
		private AtomicInteger counter = new AtomicInteger(0);

		private SmsInboxAddContentSimulator(ContentResolver contentResolver) {
			this.contentResolver = contentResolver;
		}

		@Override
		public void run() {
			ContentValues values = new ContentValues();
			values.put("address", "SENDER");
			values.put("body", "Body:"+counter.incrementAndGet());
			contentResolver.insert(Uri.parse("content://sms/inbox"), values);
		}
	}

}
