package com.routon.widgets;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

public class TurnAniView extends ImageView{
	public TurnAniView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public TurnAniView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private int mTime = 3000;
	private ValueAnimator mAnimator = null;
	
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		stopTimer();
	}
	
	protected void onAttachedToWindow() {
		super.onDetachedFromWindow();
		startTimer();
	}
	
	
	private void turnAni(){
		if( mAnimator == null ){
			mAnimator = ObjectAnimator.ofFloat(this, "rotationY", 0, 360);
			mAnimator.setRepeatCount(0);
			//mAnimator.setInterpolator(new AccelerateInterpolator());
			mAnimator.setDuration(1000);
		}
		mAnimator.start();
	}
	
	private Handler mTimerHandler = new Handler() {
		public void handleMessage(Message msg) {
			//Log.d("turn ani");
			turnAni();
			this.sendEmptyMessageDelayed(0, mTime);
		}
	};
	
	public void startTimer(){
		mTimerHandler.removeMessages(0);
		mTimerHandler.sendEmptyMessageDelayed(0, mTime);
	}
	
	public void stopTimer(){
		mTimerHandler.removeMessages(0);
	}
	
	public void setTime(int time){
		mTime = time;
	}
	
	
}
