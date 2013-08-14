package com.routon.widgets;

import com.routon.weatherwidget.R;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustomDialog extends Dialog {

	private View mLayout = null;
	private View mTextLayout = null;
	private View mPrevFocus = null;
	private ValueAnimator mShowAni = null;
	private ValueAnimator mCancelAni = null;
	private final int mStartAlpha = 0;
	private final int mShowTime = 900;
	private DialogInterface.OnClickListener mEscapseListener = null;

	// private String mError;

	public CustomDialog(Context context, int theme) {
		super(context, theme);
	}

	public CustomDialog(Context context) {
		super(context);
	}

	private void dismissDialog() {
		// TtsSpeech.getInstance().stopSpeech();

		super.dismiss();
	}

	public void setTitle(String title) {
		if (mLayout == null)
			return;
		TextView textview = (TextView) (mLayout.findViewById(R.id.title));
		if (textview != null) {
			textview.setText(title);
		}
	}

	public void show() {
		if (mShowAni != null && mShowAni.isRunning() == true)
			return;
		super.show();
		if (this.mPrevFocus != null) {
			this.mPrevFocus.setFocusable(false);
		}

		// if (mError != null) {
		// TtsSpeech.getInstance().init(getContext());
		// TtsSpeech.getInstance().speechWithText(mError);
		// }

		if (mCancelAni != null && mCancelAni.isRunning() == true) {
			mCancelAni.cancel();
		}
		if (mShowAni == null) {
			// mShowAni=ObjectAnimator.ofFloat(mLayout, "alpha",
			// mStartAlpha,1f);
			mShowAni = ValueAnimator.ofFloat(0f, 1f);
			mShowAni.setDuration(mShowTime);
			mShowAni.setInterpolator(new DecelerateInterpolator());
			mShowAni.addUpdateListener(new AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					// TODO Auto-generated method stub
					float ratio = (Float) animation.getAnimatedValue();
					mLayout.setAlpha(ratio);
					if (mTextLayout != null) {
						mTextLayout.setScaleX(ratio);
						mTextLayout.setScaleY(ratio);
					}
				}

			});
			mShowAni.addListener(new AnimatorListener() {
				public void onAnimationEnd(Animator animation) {
				}

				@Override
				public void onAnimationStart(Animator animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationCancel(Animator animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animator animation) {
					// TODO Auto-generated method stub

				}
			});
		}
		mShowAni.start();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) {
			if (mEscapseListener != null) {
				mEscapseListener.onClick(this, -1);
			} else {
				this.dismiss();
			}
			return true;
		}
		return false;
	}

	public void dismiss() {
		dismiss(true);
	}

	public void dismiss(boolean anim) {
		if (anim) {
			if (mCancelAni != null && mCancelAni.isRunning() == true)
				return;
			if (mShowAni != null && mShowAni.isRunning() == true) {
				mShowAni.cancel();
			}
			if (this.mPrevFocus != null) {
				this.mPrevFocus.setFocusable(true);
				this.mPrevFocus.requestFocus();
			}
			if (mCancelAni == null) {
				mCancelAni = ValueAnimator.ofFloat(1f, 0f);
				mCancelAni.setDuration(mShowTime);
				mCancelAni.setInterpolator(new DecelerateInterpolator());
				mCancelAni.addUpdateListener(new AnimatorUpdateListener() {

					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						// TODO Auto-generated method stub
						float ratio = (Float) animation.getAnimatedValue();
						mLayout.setAlpha(ratio);
						if (mTextLayout != null) {
							mTextLayout.setScaleX(ratio);
							mTextLayout.setScaleY(ratio);
						}
					}

				});
				mCancelAni.setDuration(mShowTime);
				mCancelAni.setInterpolator(new AccelerateInterpolator());
				mCancelAni.addListener(new AnimatorListener() {
					public void onAnimationEnd(Animator animation) {
						CustomDialog.this.dismissDialog();
					}

					@Override
					public void onAnimationStart(Animator animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationCancel(Animator animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationRepeat(Animator animation) {
						// TODO Auto-generated method stub

					}
				});
			}
			mCancelAni.start();
		} else {
			super.dismiss();
		}
	}

	private boolean mCanceled = false;

	public void setCancelFlag(boolean flag) {
		mCanceled = flag;
	}

	public boolean getCancelFlag() {
		return mCanceled;
	}

	// private void setError(String error) {
	// mError = error;
	// }

	/**
	 * Helper class for creating a custom dialog
	 */
	public static class Builder {

		private Context context;
		private String title;
		private String message;
		private String positiveButtonText;
		private String negativeButtonText;
		private View contentView;
		private boolean mDefaultPositiveFocus = true;

		private String mError;

		private View mTitleView;
		private View mMesView;

		private View mPrevFocus;

		private int mMesColor = -1;
		private int mTitleColor = -1;

		private DialogInterface.OnClickListener positiveButtonClickListener, negativeButtonClickListener,
				escapseListener;

		public Builder(Context context) {
			this.context = context;
		}

		public Builder setPrevFocus(View focus) {
			this.mPrevFocus = focus;
			return this;
		}

		/**
		 * Set the Dialog message from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}

		/**
		 * Set the Dialog message from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage(int message) {
			this.message = (String) context.getText(message);
			return this;
		}

		/**
		 * Set the Dialog title from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(int title) {
			this.title = (String) context.getText(title);
			return this;
		}

		/**
		 * Set the Dialog title from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder setColor(int title_color, int mes_color) {
			this.mTitleColor = title_color;
			this.mMesColor = mes_color;
			return this;
		}

		public Builder setError(String error) {
			this.mError = error;
			return this;
		}

		/**
		 * Set a custom content view for the Dialog. If a message is set, the
		 * contentView is not added to the Dialog...
		 * 
		 * @param v
		 * @return
		 */
		public Builder setContentView(View v) {
			this.contentView = v;
			return this;
		}

		/**
		 * Set the positive button resource and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(int positiveButtonText, DialogInterface.OnClickListener listener) {
			this.positiveButtonText = (String) context.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the positive button text and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener listener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button resource and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(int negativeButtonText, DialogInterface.OnClickListener listener) {
			this.negativeButtonText = (String) context.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button text and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener listener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		public Builder setNegativeAsDefaultButton() {
			mDefaultPositiveFocus = false;
			return this;
		}

		public Builder setEscapseListener(DialogInterface.OnClickListener listener) {
			escapseListener = listener;
			return this;
		}

		public CustomDialog createWithView(View view) {
			final CustomDialog dialog = new CustomDialog(context, R.style.CustomDialog);
			dialog.setContentView(view);
			dialog.mLayout = view;
			dialog.mTextLayout = null;
			dialog.mPrevFocus = this.mPrevFocus;
			dialog.mEscapseListener = this.escapseListener;
			return dialog;
		}

		/**
		 * Create the custom dialog
		 */
		public CustomDialog create() {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			// instantiate the dialog with the custom Theme
			final CustomDialog dialog = new CustomDialog(context, R.style.CustomDialog);

			View layout = inflater.inflate(R.layout.customdialog, null);
			View text_layout = layout.findViewById(R.id.text_layout);
			// dialog.addContentView(layout, new LayoutParams(
			// LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

			// set the dialog title
			TextView textview = (TextView) (layout.findViewById(R.id.title));
			textview.setText(title);
			if (mTitleColor >= 0) {
				textview.setTextColor(mTitleColor);
			}
			mTitleView = textview;
			if (title != null) {
				textview.setVisibility(View.VISIBLE);
			}

			TextView errorView = (TextView) (layout.findViewById(R.id.error));
			errorView.setText(mError);
			// if (mError != null && mError.length() > 0)
			// dialog.setError(message);

			// set the confirm button
			if (positiveButtonText != null) {
				Button btn = (Button) (layout.findViewById(R.id.positiveButton));
				btn.setText(positiveButtonText);
				if (positiveButtonClickListener != null) {
					btn.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
						}
					});
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
			}
			// set the cancel button
			if (negativeButtonText != null) {
				Button btn = (Button) (layout.findViewById(R.id.negativeButton));
				btn.setText(negativeButtonText);
				if (negativeButtonClickListener != null) {
					btn.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							negativeButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
						}
					});
				}
				if (mDefaultPositiveFocus == false) {
					btn.requestFocus();
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.negativeButton).setVisibility(View.GONE);
			}
			// set the content message
			if (message != null) {
				((TextView) layout.findViewById(R.id.message)).setText(message);
				if (mMesColor >= 0) {
					((TextView) layout.findViewById(R.id.message)).setTextColor(mMesColor);
				}
				mMesView = layout.findViewById(R.id.message);
			} else if (contentView != null) {
				// if no message set
				// add the contentView to the dialog body
				LinearLayout content_layout = (LinearLayout) (layout.findViewById(R.id.content));
				content_layout.removeAllViews();
				((LinearLayout) content_layout.findViewById(R.id.content)).addView(contentView);
			}
			dialog.setContentView(layout);

			dialog.mLayout = layout;
			dialog.mTextLayout = text_layout;
			dialog.mPrevFocus = this.mPrevFocus;
			dialog.mEscapseListener = this.escapseListener;
			return dialog;
		}

	}

}
