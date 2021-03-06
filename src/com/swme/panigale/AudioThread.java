package com.swme.panigale;

import android.util.Log;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.AudioFormat;
import android.media.MediaRecorder.AudioSource;
import android.media.AudioManager;
import java.util.List;
import java.util.ArrayList;
/*
 * Thread to manage live recording/playback of voice input 
 * from the device's microphone.
 *
 * credit goes to:
 * http://stackoverflow.com/questions/6959930/android-need-to-record-mic-input
 */
public class AudioThread extends Thread
{ 

    public interface AudioReadyListener {
      public void onAudioReady( int audioSessionId );
    }

    private static List<AudioReadyListener> audioReadyListeners;

    private boolean stopped = false;
    private int sessionId;
    /**
     * Give the thread high priority so that it's not 
     * canceled unexpectedly, and start it
     */
    public AudioThread( AudioReadyListener l)
    { 
        if(audioReadyListeners == null) { 
	  audioReadyListeners = new ArrayList<AudioReadyListener>();
	}
	addAudioReadyListener(l);
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        start();
    }

    public void addAudioReadyListener(AudioReadyListener l) {
      audioReadyListeners.add(l);
    }

    @Override
    public void run()
    { 
        Log.i(PanigaleActivity.LOG_TAG, "Running Audio Thread");
        AudioRecord recorder = null;
        AudioTrack track = null;
        short[][]   buffers  = new short[256][160];
        int ix = 0;

        /*
         * Initialize buffer to hold continuously recorded audio data, start recording, and start
         * playback.
         */
        try
        {
            int N = AudioRecord.getMinBufferSize(8000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
            recorder = new AudioRecord(AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, N*10);
            track = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, 
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, N*10, AudioTrack.MODE_STREAM);
            recorder.startRecording();
            track.play();
	    for( AudioReadyListener l : audioReadyListeners) {
	      l.onAudioReady(track.getAudioSessionId());
	    }
            /*
             * Loops until something outside of this thread stops it.
             * Reads the data from the recorder and writes it to the audio track for playback.
             */
            while(!stopped)
            { 
                //Log.i(PanigaleActivity.LOG_TAG, "Writing new data to buffer");
                short[] buffer = buffers[ix++ % buffers.length];
                N = recorder.read(buffer,0,buffer.length);
                track.write(buffer, 0, buffer.length);
            }
        }
        catch(Throwable x)
        { 
            Log.w(PanigaleActivity.LOG_TAG, "Error reading voice audio", x);
        }
        /*
         * Frees the thread's resources after the loop completes so that it can be run again
         */
        finally
        { 
            recorder.stop();
            recorder.release();
            track.stop();
            track.release();
        }
    }

    /**
     * Called from outside of the thread in order to stop the recording/playback loop
     */
    public void close()
    { 
       stopped = true;
    }

}
