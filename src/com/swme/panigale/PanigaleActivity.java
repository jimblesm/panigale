package com.swme.panigale;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Environment;
import android.view.Menu;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.util.Log;

import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;

import java.io.IOException;

public class PanigaleActivity extends Activity implements ScaleEventListener {

	private static final String LOG_TAG = "PanigaleActivity";
	private static final int CAPTURE_RATE = (int)
						(Visualizer.getMaxCaptureRate() -
						Visualizer.getMaxCaptureRate()* 0.2f);
	private static byte[] array;
	private Visualizer mVisualizer;
	private MediaPlayer mPlayer;
	private String mFileName;
	// set this to change visulization amplitude
	private static int mVolume = 200;

	private int[] eqViews = { R.id.bar_1, R.id.bar_2, R.id.bar_3,
				  R.id.bar_4, R.id.bar_5, R.id.bar_6,
				  R.id.bar_7, R.id.bar_8, R.id.bar_9 };
/*
	private class EqRunnable implements Runnable {
		@Override
		public void run() {
		}
	}
*/	
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
/*
	private HandlerThread handlerThread;
	private Handler handler;
*/	
	private Handler mainHandler;

	private View eq;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_panigale);
/*		mFileName = Environment
		  		.getExternalStorageDirectory()
				.getAbsolutePath();
		mFileName += "/audiorecordtest.3gp";
*/		
		// create spinner
		Spinner spinner = (Spinner) findViewById(R.id.status_spinner);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter
		  					.createFromResource(this, 
							    R.array.statuses,
							    R.layout.spinner_item);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		eq = findViewById(R.id.eq_container);
		eq = findViewById(R.id.eq_container);
		ScaleView sV = (ScaleView) findViewById(R.id.scale_view);
		sV.addScaleEventListener(this);
/*		
		if (handlerThread == null) {
			handlerThread = new HandlerThread("EQ Thread");
			handlerThread.start();
		}
	*/	
		if (mainHandler == null) {
			mainHandler = new MainHandler(this, getMainLooper());
		}
	/*	
		if (handler == null) {
			handler = new Handler(handlerThread.getLooper());
			handler.postDelayed(new EqRunnable(), 50);
		}*/
		startPlaying();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.panigale, menu);
		return true;
	}
	


	public void startPlaying() {
	  
	  /*Not entirely necessary.
	  mPlayer = new MediaPlayer();
	  //mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
	  try{
	    mPlayer.setDataSource(mFileName); // set data source our URL defined
	    mPlayer.prepare(); 
	  }catch (IOException e) {
	      Log.e(LOG_TAG, "prepare() failed");
	  }
	  mPlayer.start();*/
	  //setup your Vizualizer - call method
	  //setupVisualizerFxAndUI();        
	  setupVisualizer();
	  mVisualizer.setEnabled(true);
	}

	public void setupVisualizer() {
	  // Create the Visualizer object and attach it to our media player.
	  //YOU NEED android.permission.RECORD_AUDIO for that in AndroidManifest.xml
	  //steal the audio out
	  mVisualizer = new Visualizer(0);
	  //steal the file playback... not too exciting.
	  //mVisualizer = new Visualizer(mPlayer.getAudioSessionId());
	  mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
	  mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
	      public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
		      int samplingRate) {
		  //mVisualizerView.updateVisualizer(bytes);
	      }

	      public void onFftDataCapture(Visualizer vis, 
					   byte[] bytes, int samplingRate) {
		//double modifier = Math.random();
		//here I'm trying to drop the top X0 percent of values to 
		//make it look more full
		float range = (float) bytes.length-bytes.length*0.6f;
		//number of bars evenly distributed over range
		int barMult = (int)(range / 9.0f);
		  for (int i=1; i < eqViews.length+1; i++) {
		      final View v = PanigaleActivity.this.findViewById(eqViews[i-1]);
		      byte rfk = bytes[barMult*i];
		      byte ifk = bytes[barMult*i+1];
		      float mag = (rfk*rfk + ifk*ifk);
		      int height = (int) (mVolume*Math.log10(mag));
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
		//array = new byte[v.getCaptureSize()];
	      }
	  }, CAPTURE_RATE, false, true); 

	}

  public void onScaleEvent(int newSize) {
    mVolume = 300 - newSize;
  }

  @Override
  public void onPause() {
        super.onPause();
        if (mVisualizer != null) {
            mVisualizer.release();
            mVisualizer = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
  }


}
