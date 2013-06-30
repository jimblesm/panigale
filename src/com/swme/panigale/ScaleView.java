package com.swme.panigale;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Scales a rectangle. 
 */
public class ScaleView extends FrameLayout {

	private static List<ScaleEventListener> ScaleEventListeners = 
			  new ArrayList<ScaleEventListener>();


	private ScaleGestureDetector detector;
	private View rectangle;
	
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
        
        rectangle = LayoutInflater.from(getContext()).inflate(R.layout.eq_threshold, this);
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

	
	private class PanigaleOnScaleGestureDetectorListener extends SimpleOnScaleGestureListener {
		
		@Override
		public boolean onScale (ScaleGestureDetector detector) {
			
			
			float scaleFactor = detector.getScaleFactor();
			if (Math.abs(1 - scaleFactor) < .05) {
				return false;
			}
			
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rectangle.getLayoutParams();
			int bottomMargin = layoutParams.bottomMargin;
			
			int newMargin;
			if (scaleFactor < 1) {
				newMargin = bottomMargin + (int) (250 * (1 - scaleFactor));	
			} else {
				newMargin = bottomMargin - (int) (250 * (scaleFactor - 1));
			}
			 
			if (newMargin > 250) {
				newMargin = 250;
			}
			
			if (newMargin < 0) {
				newMargin = 0;
			}
		
			for(ScaleEventListener l : ScaleEventListeners) {
			  l.onScaleEvent(newMargin);
			}
			
			layoutParams.bottomMargin = newMargin;
			layoutParams.topMargin = newMargin;
			
			rectangle.setLayoutParams(layoutParams);
			
			return true;
		}
	}

}
