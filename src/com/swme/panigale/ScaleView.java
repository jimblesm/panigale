package com.swme.panigale;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Scales a rectangle. 
 */
public class ScaleView extends FrameLayout {

	private static List<ScaleEventListener> ScaleEventListeners = 
			  new ArrayList<ScaleEventListener>();


	private ScaleGestureDetector detector;
	private int bounds;
	private TextView description;
	private int MAX_BOUNDS = 250;
	
	public ScaleView(Context context) {
		super(context);
		init();
	}

	public ScaleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ScaleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		PanigaleOnScaleGestureDetectorListener listener = new PanigaleOnScaleGestureDetectorListener();
		detector = new ScaleGestureDetector(getContext(), listener); 
        
        bounds = 250;
//        rectangle = new View(getContext(), null, R.style.EqBackground);
//        rectangle.setBackgroundColor(getContext().getResources().getColor(R.color.light_blue));
//        this.addView(rectangle);
	}

	public void addScaleEventListener( ScaleEventListener l ){
		ScaleEventListeners.add(l);
	}
		
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    detector.onTouchEvent(event);
	    invalidate();
	    return true;
	}

	
	private class PanigaleOnScaleGestureDetectorListener extends 
					SimpleOnScaleGestureListener {
	  @Override
	  public boolean onScale (ScaleGestureDetector detector) {
	      float scaleFactor = detector.getScaleFactor();
	      if (Math.abs(1 - scaleFactor) < .05) {
		      return false;
	      }
	      int newBounds;
	      if (scaleFactor < 1) {
		      newBounds = bounds + (int) (MAX_BOUNDS * (1 - scaleFactor));	
	      } else {
		      newBounds = bounds - (int) (MAX_BOUNDS * (scaleFactor - 1));
	      }
	      
	      AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
	      int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		  audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Math.max(1, (int) ((maxVolume - ((float) newBounds / MAX_BOUNDS * maxVolume)) / 1.5f)), 0);  
	      
	      return scaleNow(newBounds);
	  }
	}

	public boolean scaleNow(int newBounds) {
	    newBounds = checkBounds(newBounds);
	    Log.i(PanigaleActivity.LOG_TAG, "and SCALE" + newBounds); 
	    for(ScaleEventListener l : ScaleEventListeners) {
	      l.onScaleEvent(newBounds);
	    }
	    
	    bounds = newBounds;
	    
	    if (description != null) {
		    if (bounds > 175) {
			    description.setText(R.string.level_high);
		    } else if (bounds > 100) {
			    description.setText(R.string.level_medium);
		    } else if (bounds > 25) {
			    description.setText(R.string.level_low);
		    } else {
			    description.setText(R.string.level_off);
		    }
	    }
	    return true;
	}
	
	public void addTextViewForUpdating(TextView textView) {
		this.description = textView;
	}
/*
	public void setBounds(int bounds) {
		this.bounds = checkBounds(bounds);
		invalidate();
	}
*/	
	private int checkBounds(int newBounds) {
		if (newBounds > 250) {
			newBounds = 250;
		}
		
		if (newBounds < 0) {
			newBounds = 0;
		}
		return newBounds;
	}

}
