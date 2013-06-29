//android.permission.MODIFY_AUDIO_SETTINGS for audio settings and also
//android.permission.INTERNET for internet streaming

package com.swme.panigale;

import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

public class VisActivity extends Activity {

      //Here is your URL defined
  private String mFileName = null;
      //Constants for vizualizator - HEIGHT 50dip
  private static final float VISUALIZER_HEIGHT_DIP = 50f;

      //Your MediaPlayer
  private MediaPlayer mp;

  //Vizualization
  private Visualizer mVisualizer;

      private LinearLayout mLinearLayout;
      private VisualizerView mVisualizerView;
      private TextView mStatusTextView;


  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);

      mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
      mFileName += "/audiorecordtest.3gp";

      //Info textView
      mStatusTextView = new TextView(this);

      //Create new LinearLayout ( because main.xml is empty )
      mLinearLayout = new LinearLayout(this);
      mLinearLayout.setOrientation(LinearLayout.VERTICAL);
      mLinearLayout.addView(mStatusTextView);

      //set content view to new Layout that we create
      setContentView(mLinearLayout);

      //start media player - like normal
      mp = new MediaPlayer();
      //mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

      try {
	  mp.setDataSource(mFileName); // set data source our URL defined
      } catch (IllegalArgumentException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
      } catch (IllegalStateException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
      } catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
      }       

      try {   //tell your player to go to prepare state
	  mp.prepare(); 
      } catch (IllegalStateException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
      } catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
      }
	      //Start your stream / player
      mp.start();

      //setup your Vizualizer - call method
      setupVisualizerFxAndUI();        

	      //enable vizualizer
	      mVisualizer.setEnabled(true);

	      //Info text
      mStatusTextView.setText("Playing audio...");
  }

      //Our method that sets Vizualizer
  private void setupVisualizerFxAndUI() {
      // Create a VisualizerView (defined below), which will render the simplified audio
      // wave form to a Canvas.

      //You need to have something where to show Audio WAVE - in this case Canvas
      mVisualizerView = new VisualizerView(this);
      mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
	      ViewGroup.LayoutParams.MATCH_PARENT,
	      (int)(VISUALIZER_HEIGHT_DIP * getResources().getDisplayMetrics().density)));
      mLinearLayout.addView(mVisualizerView);

      // Create the Visualizer object and attach it to our media player.
      //YOU NEED android.permission.RECORD_AUDIO for that in AndroidManifest.xml
      mVisualizer = new Visualizer(mp.getAudioSessionId());
      mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
      mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
	  public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
		  int samplingRate) {
	      mVisualizerView.updateVisualizer(bytes);
	  }

	  public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {}
      }, Visualizer.getMaxCaptureRate() / 2, true, false); 
  }

  @Override
  protected void onPause() {
      super.onPause();

      if (isFinishing() && mp != null) {
	  mVisualizer.release();
	  //mEqualizer.release();
	  mp.release();
	  mp = null;
      }
  }

  /**
   * A simple class that draws waveform data received from a
   * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture }
   */
  class VisualizerView extends View {
      private byte[] mBytes;
      private float[] mPoints;
      private Rect mRect = new Rect();

      private Paint mForePaint = new Paint();

      public VisualizerView(Context context) {
	  super(context);
	  init();
      }

      private void init() {
	  mBytes = null;

	  mForePaint.setStrokeWidth(1f);
	  mForePaint.setAntiAlias(true);
	  mForePaint.setColor(Color.rgb(0, 128, 255));
      }

      public void updateVisualizer(byte[] bytes) {
	  mBytes = bytes;
	  invalidate();
      }

      @Override
      protected void onDraw(Canvas canvas) {
	  super.onDraw(canvas);

	  if (mBytes == null) {
	      return;
	  }

	  if (mPoints == null || mPoints.length < mBytes.length * 4) {
	      mPoints = new float[mBytes.length * 4];
	  }

	  mRect.set(0, 0, getWidth(), getHeight());

	  for (int i = 0; i < mBytes.length - 1; i++) {
	      mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);
	      mPoints[i * 4 + 1] = mRect.height() / 2
		      + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;
	      mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length - 1);
	      mPoints[i * 4 + 3] = mRect.height() / 2
		      + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;
	  }

	  canvas.drawLines(mPoints, mForePaint);
      }
  }
}
