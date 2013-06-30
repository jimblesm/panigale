package com.swme.panigale;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class PanigaleActivity extends Activity {

	private class EqRunnable implements Runnable {
		private int[] eqViews = { R.id.bar_1, R.id.bar_2, R.id.bar_3,
				R.id.bar_4, R.id.bar_5, R.id.bar_6, R.id.bar_7, R.id.bar_8,
				R.id.bar_9 };

		@Override
		public void run() {
			
			double modifier = Math.random();
			for (int eqView : eqViews) {
				
				final View v = PanigaleActivity.this.findViewById(eqView);
				int height = (int) (Math.random() * 500 * modifier);

				final LayoutParams layoutParams = v.getLayoutParams();
				layoutParams.height = height;
				mainHandler.post(new Runnable() {
					
					@Override
					public void run() {
						v.setLayoutParams(layoutParams);
						v.invalidate();
					}
				});
			}


			handler.postDelayed(new EqRunnable(), 50);
		}
	}
	
	private static class MainHandler extends Handler {
		private PanigaleActivity activity;
		
		public MainHandler(PanigaleActivity activity, Looper mainLooper) {
			super(mainLooper);
			this.activity = activity;
		}

		@Override
		public void handleMessage(Message msg) {
			activity.eq.invalidate();
		}
	}

	private HandlerThread handlerThread;
	private Handler handler;
	
	private Handler mainHandler;
	private View eq;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_panigale);
		
		// create spinner
		Spinner spinner = (Spinner) findViewById(R.id.status_spinner);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.statuses, R.layout.spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinner.setAdapter(adapter);
		eq = findViewById(R.id.eq_container);
		
		if (handlerThread == null) {
			handlerThread = new HandlerThread("EQ Thread");
			handlerThread.start();
		}
		
		if (mainHandler == null) {
			mainHandler = new MainHandler(this, getMainLooper());
		}
		
		if (handler == null) {
			handler = new Handler(handlerThread.getLooper());
			handler.postDelayed(new EqRunnable(), 50);
		}	
				
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.panigale, menu);
		return true;
	}
	


}
