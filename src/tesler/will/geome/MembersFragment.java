package tesler.will.geome;

import com.facebook.widget.ProfilePictureView;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Marker;

import tesler.will.geome.Main.User;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MembersFragment extends Fragment {

	static MemberListAdapter mla;
	static ListView lv;

	User self;

	static Handler timer = new Handler();
	static Runnable r;

	static SeekBar sb_timer;
	SeekBar sb_limit;

	ToggleButton toggle;
	static ImageView globe;
	static ImageView globeBackground;
	static ImageView greyBackground;

	static boolean isRunning = false;

	public MembersFragment() {
		self = Main.self;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the Fragment
		View rv = inflater.inflate(R.layout.members_frag, container, false);

		sb_timer = (SeekBar) rv.findViewById(R.id.sb_geo);
		sb_timer.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (progress <= 0) {
					SessionFragment.activateLocater();

					sb_timer.setProgress(sb_limit.getProgress() + 20);

					spin_globe();

				}
			}
		});

		sb_limit = (SeekBar) rv.findViewById(R.id.sb_limit);

		globe = (ImageView) rv.findViewById(R.id.iv_geo);
		globe.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SessionFragment.activateLocater();
				sb_timer.setProgress(sb_limit.getProgress() + 20);
				spin_globe();
			}
		});

		globeBackground = (ImageView) rv.findViewById(R.id.iv_circle_1);

		greyBackground = (ImageView) rv.findViewById(R.id.iv_circle_3);

		toggle = (ToggleButton) rv.findViewById(R.id.tb_geo);
		toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					start(true);
				} else {
					stop();
				}
			}
		});

		lv = (ListView) rv.findViewById(R.id.lv_members);

		lv.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@SuppressWarnings("deprecation")
					@Override
					public void onGlobalLayout() {
						LayoutParams lp = new RelativeLayout.LayoutParams(lv
								.getLayoutParams());
						lp.height = lv.getHeight() - 100;
						lv.setLayoutParams(lp);
						lv.getViewTreeObserver().removeGlobalOnLayoutListener(
								this);
					}
				});

		return rv;
	}

	public static void start(Boolean restart) {

		Log.i("Members", "Starting");

		if (!isRunning) {
			if (!restart) {
				mla = new MemberListAdapter(Main.cont,
						GroupsPane.curGroup.members);
				lv.setAdapter(mla);
			}

			r = new Runnable() {
				@Override
				public void run() {
					sb_timer.setProgress(sb_timer.getProgress() - 1);
					timer.postDelayed(r, 1000);
				}
			};

			timer.post(r);
			sb_timer.setVisibility(View.VISIBLE);
			greyBackground.setVisibility(View.GONE);

			// Make sure the globe doesn't spin
			AnimationDrawable anim = (AnimationDrawable) globe.getDrawable();
			anim.stop();

			isRunning = true;
		}
	}

	public static void focus() {

		// mla.notifyDataSetChanged();

		Utils.makeKeyboardPanOverScreen();
	}

	static void stop() {

		SessionFragment.stopLocater();

		timer.removeCallbacks(r);
		sb_timer.setVisibility(View.INVISIBLE);
		greyBackground.setVisibility(View.VISIBLE);

		AnimationDrawable anim = (AnimationDrawable) globe.getDrawable();
		anim.stop();

		isRunning = false;
	}

	private void spin_globe() {

		globe.setClickable(false);

		globeBackground.setImageResource((R.drawable.outer_circle_solid));

		AnimationDrawable anim = (AnimationDrawable) globe.getDrawable();
		anim.start();
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				globeBackground
						.setImageResource(R.drawable.outer_circle_glowing);
				AnimationDrawable anim = (AnimationDrawable) globe
						.getDrawable();
				anim.stop();
				globe.setClickable(true);
			}
		}, 10000);
	}

	public static void stopSpinning() {
		if (globe != null) {
			globeBackground.setImageResource(R.drawable.outer_circle_glowing);
			AnimationDrawable anim = (AnimationDrawable) globe.getDrawable();
			anim.stop();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	static Bitmap getProfilePic(String memId) {
		for (int i = 0; i < GroupsPane.curGroup.members.size(); i++) {
			try {
				Log.i("myId/otherId", memId + " / "
						+ GroupsPane.curGroup.members.get(i).id);
				if (memId.contentEquals(GroupsPane.curGroup.members.get(i).id)) {
					ProfilePictureView ppv = (ProfilePictureView) ((RelativeLayout) lv
							.getChildAt(i)).getChildAt(0);
					ImageView fbImage = ((ImageView) ppv.getChildAt(0));
					return ((BitmapDrawable) fbImage.getDrawable()).getBitmap();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return BitmapFactory.decodeResource(Main.cont.getResources(),
						R.drawable.default_profile);
			}
		}
		return null;
	}

	static class Member {

		String id, fbid, gcmid, name, phone, color;

		Double lat, lon;

		Bitmap bitmap;

		BitmapDescriptor pic;

		Marker marker;

	}

}
