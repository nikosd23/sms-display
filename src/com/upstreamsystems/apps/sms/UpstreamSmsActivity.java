package com.upstreamsystems.apps.sms;

import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class UpstreamSmsActivity extends Activity {

	private final static boolean SMS_INBOX_ADDER_SIMULATOR = false;

	private TextView view;

	private TextView inboxList;
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		view = (TextView) findViewById(R.id.textView);
		inboxList = (TextView) findViewById(R.id.listView);

		final ScheduledExecutorService scheduler =
				Executors.newScheduledThreadPool(1);

		if(SMS_INBOX_ADDER_SIMULATOR){
			final SmsInboxAddContentSimulator beeper = new SmsInboxAddContentSimulator(getContentResolver());

			final ScheduledFuture<?> beeperHandle =
					scheduler.scheduleAtFixedRate(beeper, 10, 10, TimeUnit.SECONDS);
		}

//		SMSReceiver mSMSReceiver = new SMSReceiver();
//		IntentFilter mIntentFilter = new IntentFilter();
//		mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
//		this.registerReceiver(mSMSReceiver, mIntentFilter);


	}

	public void checkInbox(View view){
		Uri uriSMSURI = Uri.parse("content://sms/inbox");
		Cursor cur = getContentResolver().query(uriSMSURI, null, null, null, null);
		String sms = "";
		if (cur != null) {
			while (cur.moveToNext()) {
				String body = cur.getString(cur.getColumnIndexOrThrow("body")).toString();
				String number = cur.getString(cur.getColumnIndexOrThrow("address")).toString();
				sms += "From :" + number + " : " + body+"\n";
			}
		}
		inboxList.setText(sms);
	}

	public void cleanInbox(View view){
		Uri uriSMSURI = Uri.parse("content://sms/inbox");
		Cursor cur = getContentResolver().query(uriSMSURI, null, null, null, null);
		String sms = "";
		if (cur != null) {
			while (cur.moveToNext()) {
				String body = cur.getString(cur.getColumnIndexOrThrow("body")).toString();
				String number = cur.getString(cur.getColumnIndexOrThrow("address")).toString();
				sms += "From :" + number + " : " + body+"\n";
			}
		}
	}

	private class SMSReceiver extends BroadcastReceiver {
		private final String TAG = this.getClass().getSimpleName();

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();

			String strMessage = "";

			if (extras != null) {
				Object[] smsextras = (Object[]) extras.get("pdus");

				for (int i = 0; i < smsextras.length; i++) {
					SmsMessage smsmsg = SmsMessage.createFromPdu((byte[]) smsextras[i]);

					String strMsgBody = smsmsg.getMessageBody().toString();
					String strMsgSrc = smsmsg.getOriginatingAddress();

					strMessage += "SMS from " + strMsgSrc + " : " + strMsgBody;

					view.setText(strMessage);
					Toast.makeText(context, strMessage, Toast.LENGTH_SHORT).show();
				}

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
