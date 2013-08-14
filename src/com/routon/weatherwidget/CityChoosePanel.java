package com.routon.weatherwidget;

import java.util.zip.Inflater;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CityChoosePanel extends ListView {
	private TextView shieldView;

	public CityChoosePanel(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CityChoosePanel(Context context, AttributeSet attrs) {
		super(context, attrs);

		FrameLayout footer = new FrameLayout(context);
		FrameLayout header = new FrameLayout(context);
		footer.setMinimumHeight(135);
		header.setMinimumHeight(135);
		addFooterView(footer, null, false);
		addHeaderView(header, null, false);

		setSelector(new ColorDrawable(Color.TRANSPARENT));
		setScrollBarStyle(View.INVISIBLE);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		boolean handled = super.dispatchKeyEvent(event);
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP)) {
			int checkedIndex = getSelectedItemPosition();

//			setSelectionFromTop(checkedIndex, 135);
			smoothScrollToPositionFromTop(checkedIndex, 135);
		}
		return handled;
	}

	
	
	@Override
	public boolean performClick() {
		// TODO Auto-generated method stub
//		return super.performClick();
		//屏蔽按键
		Log.i("tag","performClick : isSoundEffectEnabled = "+isSoundEffectsEnabled());
		return true;
	}
	
	//返回已选item的clone
	public TextView getSelectedCloneView() {
		TextView cloneView = (TextView) inflate(getContext(), R.layout.search_list, null);
		TextView selectedView = (TextView) getSelectedView();

		if (selectedView != null) {
			cloneView.setText(selectedView.getText());
			Log.i("tag","Panel.x = "+getX());
			cloneView.setX(getX());
			cloneView.setY(235);
		}
		return cloneView;
	}
	
	public void setShieldView(TextView imageView){
		shieldView = imageView;
	}
	
	public TextView getShieldView(){
		return shieldView;
	}
}
