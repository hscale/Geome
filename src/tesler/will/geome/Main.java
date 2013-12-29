package tesler.will.geome;

import java.net.UnknownHostException;
import java.util.ArrayList;

import org.bson.types.ObjectId;

import tesler.will.geome.MembersFragment.Member;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.LinearLayout;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;

public class Main extends SlidingFragmentActivity {

	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	static User self;

	SlidingMenu sm;

	GroupsPane gPane;

	SessionFragment sFrag;

	// MediaPlayer mpSheath, mpChoir;

	LinearLayout[] clouds = new LinearLayout[3];

	String uriString = "mongodb://wtesler:Violentomega13@ds053138-a0.mongolab.com:53138,ds053138-a1.mongolab.com:53138/pinpoint";
	MongoClientURI uri = new MongoClientURI(uriString);

	private MongoClient mc;
	private DB db;
	private DBCollection coll;

	public static Boolean inForeground = true;

	static GCM gcm;
	static GCMMessage gcmMessage;

	static Context cont;

	static SlidingFragmentActivity activity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		cont = this;

		activity = this;

		uiHelper = new UiLifecycleHelper(this, callback);

		new Utils(this);

		setBehindContentView(R.layout.sliding_menu_left);

		sm = getSlidingMenu();
		setupSlidingMenu();

		self = new User();

		requestFBInfo(Session.getActiveSession());

		gcmMessage = new GCMMessage();

		gPane = new GroupsPane();

		sFrag = new SessionFragment();
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.fl_session, sFrag);
		ft.commit();

		clouds[0] = (LinearLayout) findViewById(R.id.ll_clouds_0);
		clouds[1] = (LinearLayout) findViewById(R.id.ll_clouds_1);
		clouds[2] = (LinearLayout) findViewById(R.id.ll_clouds_2);

		// mpSheath = MediaPlayer.create(Main.this, R.raw.unsheath);
		//
		// mpChoir = MediaPlayer.create(Main.this, R.raw.choir);

	}

	private void setupSlidingMenu() {
		sm.setMode(SlidingMenu.LEFT);

		// sm.setSecondaryMenu(R.layout.sliding_menu_right);
		// sm.setSecondaryShadowDrawable(R.drawable.shadowright);

		sm.setSlidingEnabled(true);

		setBehindContentView(R.layout.sliding_menu_left);

		sm.setShadowDrawable(R.drawable.shadow);

		sm.setBehindScrollScale(0.0f);
		sm.setShadowWidth(20);
		sm.setBehindOffset(20);
		sm.setFadeDegree(0.50f);

		if (Utils.screenWidth >= 600) {
			sm.setBehindOffset(1 * Utils.screenWidth / 7);

		} else {
			sm.setBehindOffset(1 * Utils.screenWidth / 9);
		}

		sm.toggle(false);
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (session.isClosed()) {
			startActivity(new Intent(Main.this, Login.class));
			finish();
		}
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		unsheathSword();

		disperseClouds();
	}

	private void unsheathSword() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				sm.toggle();
				// mpSheath.setVolume((float) .7, (float) .7);
				// mpSheath.setOnCompletionListener(new OnCompletionListener() {
				// @Override
				// public void onCompletion(MediaPlayer mp) {
				// mp.reset();
				// mp.release();
				// }
				// });
				// mpSheath.start();
			}
		}, 4000);
	}

	private void disperseClouds() {
		final Handler h = new Handler();
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				Utils.fadeOut(clouds[counter++]);
				if (counter < 3)
					h.postDelayed(this, 1000);
			}
		};
		h.post(r);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// mpChoir.setVolume((float) .7, (float) .7);
				// mpChoir.setOnCompletionListener(new OnCompletionListener() {
				// @Override
				// public void onCompletion(MediaPlayer mp) {
				// mp.reset();
				// mp.release();
				// }
				// });
				// mpChoir.start();
			}
		}, 1000);
	}

	int counter = 0;

	private void requestFBInfo(Session session) {
		Request.newMeRequest(session, new Request.GraphUserCallback() {
			@Override
			public void onCompleted(GraphUser user, Response response) {
				if (user != null) {
					self.member.fbid = user.getId();
					self.member.name = user.getFirstName() + " "
							+ user.getLastName().charAt(0);

					Log.i("FB User", self.member.name + " - "
							+ self.member.fbid);

					retrieveSelf(self.member.fbid);
				}
			}
		}).executeAsync();
	}

	private void retrieveSelf(String fbid) {
		LoginAsync la = new LoginAsync();
		la.execute(fbid);
	}

	class User {

		Member member = new Member();

		ArrayList<String> groups = new ArrayList<String>();

	}

	class LoginAsync extends AsyncTask<String, String, String> {

		protected String doInBackground(String... parm) {

			String fbid = parm[0];

			Log.i("LA", "Retrieving Self...");

			if (prepare_database()) {
				try {

					// Directions to Self
					BasicDBObject query = new BasicDBObject().append(Keys.FBID,
							fbid);

					// Go to Self
					DBCursor iter = coll.find(query);

					if (iter.hasNext()) {

						Log.i("Main", "User has Geome Account");

						DBObject myProfile = iter.next();

						self.member.id = ((ObjectId) myProfile.get(Keys.ID))
								.toString();

						BasicDBList groups = (BasicDBList) myProfile
								.get(Keys.GROUPS);
						for (Object gr : groups) {
							self.groups.add((String) gr);
						}

						return "SUCCESS";
					} else {

						Log.i("Main", "User does not have Geome Account");
						// Then Create the User in the Mongo Database
						try {
							BasicDBObject myself = new BasicDBObject();
							myself.append(Keys.FBID, self.member.fbid);
							myself.append(Keys.GROUPS, new BasicDBList());

							coll.insert(myself);

							// Save your ID in safekeeping for later
							self.member.id = myself.getObjectId(Keys.ID)
									.toString();

							return "SUCCESS";

						} catch (MongoException me) {

							me.printStackTrace();

							return "FAILURE";
						}
					}
				} catch (MongoException me) {
					me.printStackTrace();
					return "FAILURE";
				}
			} else {
				return "FAILURE";
			}

		}

		protected void onPostExecute(String state) {

			if (state.contentEquals("SUCCESS")) {

				Log.i("LA", "Success.");

				gcm = new GCM();
				gcm.retrieveId();

				gPane.retrieveGroups();

			} else {
				Log.w("LA", "Failure!");

				mc = null;
			}

		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
		inForeground = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
		inForeground = false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (GroupsPane.groupList != null)
			GroupsPane.groupList.clear();

		gcm.unregisterReceiver();

		uiHelper.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	// Make sure database is ready for transaction
	private Boolean prepare_database() {

		// Open up the collection and begin the I/O Operations
		try {

			if (mc == null) {
				mc = new MongoClient(uri);
			}
			if (db == null) {
				db = mc.getDB("pinpoint");
			}
			if (coll == null) {
				coll = db.getCollection("users");
			}
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
	}
}
