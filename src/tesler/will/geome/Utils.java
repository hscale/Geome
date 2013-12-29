package tesler.will.geome;

import android.app.Activity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class Utils {

	static Activity cont;

	public static Integer screenWidth;
	public static Integer screenHeight;
	public static Integer screenDensity;
	public static Boolean screenHasBeenLaidOut = false;

	@SuppressWarnings("deprecation")
	Utils(Activity cont) {
		Utils.cont = cont;
		Display display = cont.getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();
		screenDensity = (int) (cont.getResources().getDisplayMetrics().density * 160f);

		Log.i("Screen Dimensions",
				screenWidth.toString() + " " + screenHeight.toString() + " "
						+ screenDensity.toString());

	}

	public static void hideSoftKeyboard() {

		try {
			InputMethodManager inputMethodManager = (InputMethodManager) cont
					.getSystemService(Activity.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(cont.getCurrentFocus()
					.getWindowToken(), 0);
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
	}

	public static void fadeIn(final View view) {

		view.setVisibility(View.VISIBLE);

		AnimationSet anim = new AnimationSet(true);

		anim.setDuration(800);

		AlphaAnimation aa = new AlphaAnimation(0.0f, 1.0f);

		anim.addAnimation(aa);
		anim.setFillAfter(true);

		view.startAnimation(anim);
	}

	public static void fadeOut(final View view) {

		AnimationSet anim = new AnimationSet(true);
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.GONE);

			}
		});

		AlphaAnimation aa = new AlphaAnimation(1f, 0f);

		aa.setDuration(1000);

		anim.addAnimation(aa);
		anim.setFillAfter(false);

		view.startAnimation(anim);
	}

	public static void slideViewDown(final View view) {
		// Slide frame up a whole screen size
		Animation animation = new TranslateAnimation(0, 0, 0, screenHeight);
		// Do it in 1.3 seconds
		animation.setDuration(700);

		// Make the View Gone when its off screen
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.GONE);
			}
		});

		// Begin the Animation
		view.startAnimation(animation);
	}

	public static void slideViewUp(final View view) {
		// Slide frame up a whole screen size
		Animation animation = new TranslateAnimation(0, 0, screenHeight, 0);
		// Do it in 1.3 seconds
		animation.setDuration(700);

		// Make the View Gone when its off screen
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				view.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationEnd(Animation animation) {
			}
		});

		// Begin the Animation
		view.startAnimation(animation);
	}

	public static void makeKeyboardResizeScreen() {
		cont.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}

	public static void makeKeyboardPanOverScreen() {
		cont.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}

	public static void toast(String message, int length) {
		Toast.makeText(cont, message, length).show();
		;
	}
}
