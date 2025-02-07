package com.zoom.demo.other_features;

import us.zoom.androidlib.util.AndroidAppUtil;
import com.zoom.demo.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class MyInviteActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invite_activity);
		
		TextView txtUrl = (TextView)findViewById(R.id.txtUrl);
		TextView txtSubject = (TextView)findViewById(R.id.txtSubject);
		TextView txtMeetingId = (TextView)findViewById(R.id.txtMeetingId);
		TextView txtPassword = (TextView)findViewById(R.id.txtPassword);
		TextView txtRawPassword = (TextView)findViewById(R.id.txtRawPassword);
		EditText edtText = (EditText)findViewById(R.id.edtText);
		
		Intent intent = getIntent();
		Uri uri = intent.getData();
		
		if(uri != null)
			txtUrl.setText("URL:" + uri.toString());
		
		String subject = intent.getStringExtra(AndroidAppUtil.EXTRA_SUBJECT);
		if(subject != null)
			txtSubject.setText("Subject: " + subject);
		
		long meetingId = intent.getLongExtra(AndroidAppUtil.EXTRA_MEETING_ID, 0);
		if(meetingId > 0)
			txtMeetingId.setText("Meeting ID: " + meetingId);
		
		String meetingPassword = intent.getStringExtra(AndroidAppUtil.EXTRA_MEETING_PSW);
		if(meetingPassword != null)
			txtPassword.setText("Password: " + meetingPassword);
		
		String meetingRawPassword = intent.getStringExtra(AndroidAppUtil.EXTRA_MEETING_RAW_PSW);
		if(meetingRawPassword != null)
			txtRawPassword.setText("Raw Password: " + meetingRawPassword);
		
		String text = intent.getStringExtra(AndroidAppUtil.EXTRA_TEXT);
		if(text != null)
			edtText.setText(text);
	}

}
