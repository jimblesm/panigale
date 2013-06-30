package com.swme.panigale;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Menu;

public class PanigaleActivity extends Activity {

	private static final String TAG = "Panigale";
	private static int bufferSize = -1;
	private static AudioRecord audioRecord;
	private static HandlerThread handlerThread;
	private static Handler handler;
	private static AudioTrack audioTrack;
	private static byte[] buffer;

	private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
	
	public PanigaleActivity() {
		super();

		if (bufferSize == -1) {
			bufferSize = findAudioRecord();
		}
		
		if (bufferSize == -1) {
			Log.e("Panigale", "Unable to initialize");
			return;
		}
		
		if (audioTrack == null) {
			audioTrack = new AudioTrack(
					AudioManager.STREAM_MUSIC,
					audioRecord.getSampleRate(),
					audioRecord.getChannelConfiguration() == AudioFormat.CHANNEL_IN_MONO ? AudioFormat.CHANNEL_OUT_MONO
							: AudioFormat.CHANNEL_OUT_STEREO,
							audioRecord.getAudioFormat(), bufferSize,
							AudioTrack.MODE_STREAM);
			audioTrack.setPlaybackRate(audioRecord.getSampleRate());
			audioTrack.play();			
		}
		
		if (buffer == null) {
			buffer = new byte[bufferSize];
		}
		
		if (handlerThread == null) {
			handlerThread = new HandlerThread("Playback");
			handlerThread.start();
		}

		if (handler == null) {
			handler = new Handler(handlerThread.getLooper());
			handler.post(new PlaybackRunnable(this));
		}

	}
	
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
	
	private static class PlaybackRunnable implements Runnable {
		
		private PanigaleActivity activity;
		
		public PlaybackRunnable(PanigaleActivity activity) {
			this.activity = activity;			
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			
			if (activity.bufferSize == -1) {
				Log.e("Panigale", "Unable to initialize");
				return;
			}
		}


		@Override
		public void run() {
			int res = audioRecord.read(activity.buffer, 0, activity.bufferSize);
			if (res > 0) {
				audioTrack.write(activity.buffer, 0, res);
			}
			handler.post(new PlaybackRunnable(activity));
		}
		

		
	}
	
	private int findAudioRecord() {
		int bufferSize = -1;
	    for (int rate : mSampleRates) {
	        for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
	            for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
	                try {
	                    Log.d("Panigale", "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
	                            + channelConfig);
	                    bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

	                    if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
	                        // check if we can instantiate and have a success
	                        audioRecord = new AudioRecord(AudioSource.MIC, rate, channelConfig, audioFormat, bufferSize);

	                        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED)
	                            return bufferSize;
	                    }
	                } catch (Exception e) {
	                    Log.e("Panigale", rate + "Exception, keep trying.",e);
	                }
	            }
	        }
	    }
	    		    
	    return -1;
	}

}
