package tesler.will.geome;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.sromku.simple.fb.Properties;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebook.OnFriendsRequestListener;
import com.sromku.simple.fb.entities.Profile;

public class SessionFragment extends Fragment {

	final int NUM_PAGES = 2;

	ViewPager pager;
	PagerAdapter adapter;

	LinearLayout groupsPanel;

	static Locater locater;

	static TextView title;
	ImageView arrow_back, record;
	static ImageView add_user;

	MicrophoneListener listener = new MicrophoneListener();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		locater = new Locater(getActivity());

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the Fragment
		View rv = inflater.inflate(R.layout.session_frag, container, false);

		pager = (ViewPager) rv.findViewById(R.id.pager);
		adapter = new SessionPagerAdapter(getActivity()
				.getSupportFragmentManager());
		pager.setAdapter(adapter);

		pager.setOnPageChangeListener(new OnPageChangeListener() {
			boolean sliding = false;
			boolean right = false;

			@Override
			public void onPageSelected(int position) {
				switch (position) {
				case 0:
					MembersFragment.focus();
					break;
				case 1:
					MessagesFragment.focus();
					break;
				}
			}

			@Override
			public void onPageScrolled(int position, float offset,
					int positionOffsetPixels) {
				if (!sliding) {
					if (!right && offset > .12) {
						pager.setCurrentItem(1);
						sliding = true;
						right = true;
					} else if (right && offset > .01 && offset < .88) {
						pager.setCurrentItem(0);
						sliding = true;
						right = false;
					}
				}
			}

			@Override
			public void onPageScrollStateChanged(int position) {
				sliding = false;
			}
		});

		SlidingMenu sm = ((SlidingFragmentActivity) getActivity())
				.getSlidingMenu();

		groupsPanel = (LinearLayout) sm.findViewById(R.id.ll_groups);

		title = (TextView) rv.findViewById(R.id.tv_session);

		arrow_back = (ImageView) rv.findViewById(R.id.iv_arrow_up);
		arrow_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.slideViewUp(groupsPanel);

				MembersFragment.stop();

				MessagesFragment.stop();

				GroupsPane.start();

				AnimationDrawable anim = (AnimationDrawable) add_user
						.getDrawable();
				anim.stop();

				stopLocater();
			}
		});

		record = (ImageView) rv.findViewById(R.id.iv_record);
		record.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener.isRunning)
					listener.stop();
				else
					listener.start();
			}
		});

		add_user = (ImageView) rv.findViewById(R.id.iv_session_rotator);
		add_user.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Properties properties = new Properties.Builder()
						.add(Properties.ID).add(Properties.NAME).build();

				SimpleFacebook.getInstance(getActivity()).getFriends(
						properties, onFriendsRequestListener);

			}
		});

		return rv;
	}

	public void displayFriendPickerDialog(ArrayList<Profile> friends) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();

		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.alert_friends, null);

		builder.setView(layout);

		// Set the dialog title
		builder.setTitle("Invite Facebook friend to "
				+ GroupsPane.curGroup.name);

		Dialog d = builder.create();
		Window window = d.getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();
		wlp.gravity = Gravity.TOP;
		wlp.y += 50;
		window.setAttributes(wlp);

		FriendListAdapter pla = new FriendListAdapter(getActivity(), friends, d);

		AutoCompleteTextView textView = (AutoCompleteTextView) layout
				.findViewById(R.id.actv_friends);

		textView.setAdapter(pla);

		d.show();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private class SessionPagerAdapter extends FragmentStatePagerAdapter {
		public SessionPagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return new MembersFragment();

			case 1:
				return new MessagesFragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position];
		}
	}

	String[] titles = new String[] { "Members", "Messages" };

	public static void start() {

		locater.start();

		title.setText(GroupsPane.curGroup.name);

		AnimationDrawable anim = (AnimationDrawable) add_user.getDrawable();
		anim.start();

		MembersFragment.start(false);

		MessagesFragment.start(false);

	}

	static void stopLocater() {

		locater.stop();

	}

	static void activateLocater() {

		locater.attemptToRetrieveLocation();

	}

	OnFriendsRequestListener onFriendsRequestListener = new SimpleFacebook.OnFriendsRequestListener() {

		@Override
		public void onFail(String reason) {
			// insure that you are logged in before getting the friends
			Log.w("Get Friends", reason);
		}

		@Override
		public void onException(Throwable throwable) {
			Log.e("Get Friends", "Bad thing happened", throwable);
		}

		@Override
		public void onThinking() {
			// show progress bar or something to the user while fetching friends
			Log.i("Get Friends", "Thinking...");
		}

		@Override
		public void onComplete(List<Profile> friends) {
			Log.i("Get Friends", "Number of friends = " + friends.size());
			// CharSequence[] friendNames = new CharSequence[friends.size()];
			// for (int i = 0; i < friends.size(); i++) {
			// friendNames[i] = friends.get(i).getName();
			// }
			displayFriendPickerDialog((ArrayList<Profile>) friends);

		};
	};

}
