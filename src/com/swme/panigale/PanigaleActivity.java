package com.swme.panigale;

import android.app.Activity;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.media.audiofx.NoiseSuppressor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class PanigaleActivity extends Activity implements 
							  ScaleEventListener, 
					      AudioThread.AudioReadyListener {

	public static final String LOG_TAG = "PanigaleActivity";
	private static final int CAPTURE_RATE = (int)
						(Visualizer.getMaxCaptureRate() -
						Visualizer.getMaxCaptureRate()* 0.0f);
	private static byte[] array;
	private Visualizer mVisualizer;
	private MediaPlayer mPlayer;
	private String mFileName;
	// set this to change visulization amplitude
	private static int mVolume = 200;

	private int[] eqViews = { R.id.bar_1, R.id.bar_2, R.id.bar_3,
				  R.id.bar_4, R.id.bar_5, R.id.bar_6,
				  R.id.bar_7, R.id.bar_8, R.id.bar_9 };

	
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
	private AudioThread mAudioThread;
	private View eq;
	private TextView description;
	
	private ArrayAdapter<CharSequence> placeSpinnerAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_panigale);
/*		mFileName = Environment
		  		.getExternalStorageDirectory()
				.getAbsolutePath();
		mFileName += "/audiorecordtest.3gp";
*/		
		
		// set header
//		createHeaderTypeface();
		
		// create spinners
		createActivitySpinner();
		createPlaceSpinner(R.array.places_commuting);
		
		eq = findViewById(R.id.eq_container);
		ScaleView sV = (ScaleView) findViewById(R.id.scale_view);
		sV.addScaleEventListener(this);

		startAudioThread();
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
		//startPlaying();
		
		description = (TextView) findViewById(R.id.description);
		ScaleView scaleView = (ScaleView) findViewById(R.id.scale_view);
		scaleView.addTextViewForUpdating(description);
	}
	
	

//	private void createHeaderTypeface() {
//		TextView header = (TextView) findViewById(R.id.header);
//		Typeface avenirTypeface = Typeface.createFromAsset(getAssets(), "AvenirLTStd-Light.otf");
//		header.setTypeface(avenirTypeface);
//	}

	private void createActivitySpinner() {
		Spinner activitySpinner = (Spinner) findViewById(R.id.activity_spinner);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.statuses, R.layout.spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		activitySpinner.setAdapter(adapter);
		
		activitySpinner.setOnItemSelectedListener(new ActivitySpinnerOnItemSelectedListener());
	}
	
	private void createPlaceSpinner(int itemResourceId) {
		Spinner placeSpinner = (Spinner) findViewById(R.id.place_spinner);
		placeSpinnerAdapter = ArrayAdapter.createFromResource(this, itemResourceId, R.layout.spinner_item);
		placeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		placeSpinner.setAdapter(placeSpinnerAdapter);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.panigale, menu);
		return true;
	}
	
	private class ActivitySpinnerOnItemSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if (id == 0) {
				createPlaceSpinner(R.array.places_commuting);	
			} else if (id == 1) {
				createPlaceSpinner(R.array.places_working);
			} else if (id == 2) {
				createPlaceSpinner(R.array.places_playing);
			} else if (id == 3) {
				createPlaceSpinner(R.array.places_bed);
				ScaleView scaleView = (ScaleView) findViewById(R.id.scale_view);
				scaleView.setBounds(50);
				description.setText(R.string.level_high);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// do nothing
		}
		
	}
	

	public void startAudioThread() {
	  if(mAudioThread == null) {
	    mAudioThread = new AudioThread(this);
	    //mAudioThread.getAudioSessionId();
	  }
	}

	public void onAudioReady(int audioSessionId ) {
	  
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
	  setupVisualizer(audioSessionId);
	  mVisualizer.setEnabled(true);
	}

	public void setupVisualizer(int audioSessionId) {
	  // Create the Visualizer object and attach it to our media player.
	  //YOU NEED android.permission.RECORD_AUDIO for that in AndroidManifest.xml
	  //steal the audio out
	  mVisualizer = new Visualizer(audioSessionId);
	  NoiseSuppressor.create(audioSessionId);
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
		int maxDelta = 150;
		int initOffset = 10;
		int shortRange = bytes.length - initOffset;
		float range = (float) shortRange-shortRange*0.9f;
		//number of bars evenly distributed over range
		int barMult = (int)(range / 9.0f);
		  for (int i=1; i < eqViews.length+1; i++) {
		      final View v = PanigaleActivity.this.findViewById(eqViews[i-1]);
		      byte rfk = bytes[barMult*i+initOffset];
		      byte ifk = bytes[barMult*i+1+initOffset];
		      float mag = (rfk*rfk + ifk*ifk);
		      int height = (int) (mVolume*Math.log10(mag));
		      final LayoutParams layoutParams = v.getLayoutParams();
		      int old_height = v.getMeasuredHeight();
		      int delta = (height - old_height);
		      if( delta < 0 ) {
			delta = (delta < -maxDelta)? -maxDelta : delta;
		      }else{
			delta = (delta > maxDelta)? maxDelta : delta;
		      }
		      layoutParams.height = old_height+delta;
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
/* Not for demo
  @Override
  public void onPause() {
        super.onPause();
        if (mVisualizer != null) {
            mVisualizer.release();
            mVisualizer = null;
        }
  }
  @Override
  public void onResume() {
	super.onResume();
	if (mVisualizer == null) {
	  startAudioThread();
	}
  }
*/

}
