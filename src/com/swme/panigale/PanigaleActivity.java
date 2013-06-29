package com.swme.panigale;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class PanigaleActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_panigale);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.panigale, menu);
		return true;
	}

}
