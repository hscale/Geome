package tesler.will.geome;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.bson.types.ObjectId;

import tesler.will.geome.MembersFragment.Member;
import tesler.will.geome.MessagesFragment.Message;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.Session;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;

public class GroupsPane {

	static LinearLayout gFrame;

	static ImageView spinningLogo;

	static ImageView addGroup;

	ImageView createGroup;

	Button logout;

	Handler timer = new Handler();
	Runnable r;

	static ArrayList<Group> groupList = new ArrayList<Group>();
	static GroupListAdapter gla;
	ListView lvGroup;

	static Group curGroup;

	// Database
	static String uriString = "mongodb://wtesler:Violentomega13@ds053138-a0.mongolab.com:53138,ds053138-a1.mongolab.com:53138/pinpoint";
	private static MongoClientURI uri = new MongoClientURI(uriString);
	private static MongoClient mc;
	private static DB db;
	private static DBCollection coll;
	private static MongoClient mc2;
	private static DB db2;
	private static DBCollection coll2;

	static Context cont;

	public GroupsPane() {

		cont = Main.activity.getApplicationContext();

		final SlidingMenu sm = Main.activity.getSlidingMenu();

		gFrame = (LinearLayout) sm.findViewById(R.id.ll_groups);

		spinningLogo = (ImageView) sm.findViewById(R.id.iv_spinner);
		spinningLogo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});

		addGroup = (ImageView) sm.findViewById(R.id.iv_rotator);

		logout = (Button) sm.findViewById(R.id.bt_logout);
		logout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Session.getActiveSession().closeAndClearTokenInformation();
				Main.activity.finish();

			}
		});

		createGroup = (ImageView) sm.findViewById(R.id.iv_add);
		createGroup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setGroupNameDialog(sm.getContext());
			}
		});

		gla = new GroupListAdapter(sm.getContext(), this, groupList);
		lvGroup = (ListView) sm.findViewById(R.id.lv_groups);
		lvGroup.setAdapter(gla);

		start();
	}

	static void start() {
		AnimationDrawable anim = (AnimationDrawable) spinningLogo.getDrawable();
		anim.start();

		anim = (AnimationDrawable) addGroup.getDrawable();
		anim.start();

		Utils.makeKeyboardPanOverScreen();
	}

	static void stop() {
		AnimationDrawable anim = (AnimationDrawable) spinningLogo.getDrawable();
		anim.stop();

		anim = (AnimationDrawable) addGroup.getDrawable();
		anim.stop();
	}

	public void retrieveGroups() {
		for (String groupid : Main.self.groups) {
			RetrieveGroupAsync rga = new RetrieveGroupAsync();
			rga.execute(groupid);
		}
	}

	public void retrieveGroup(String groupid, boolean activateWhenReady) {

		RetrieveGroupAsync.activateWhenReady = activateWhenReady;
		RetrieveGroupAsync rga = new RetrieveGroupAsync();
		rga.execute(groupid);
	}

	public void createGroup(String name) {
		CreateGroupAsync cga = new CreateGroupAsync();
		cga.execute(name);
	}

	private static class RetrieveGroupAsync extends
			AsyncTask<String, String, String> {

		static boolean activateWhenReady = false;

		ArrayList<Group> groups = new ArrayList<Group>();

		protected String doInBackground(String... args) {

			Log.i("RGA", "Retrieving groups...");

			if (prepare_groups_collection()) {

				String groupid = args[0];

				Log.i("RGA", "Retrieving: " + groupid);

				// Directions to Group
				BasicDBObject query = new BasicDBObject().append(Keys.ID,
						new ObjectId(groupid));

				// Go to Group
				DBCursor iter = coll.find(query);

				if (iter != null) {

					// This is the Group
					DBObject groupObj = iter.next();

					try {

						Group group = new Group();
						group.id = groupid;
						group.name = (String) groupObj.get(Keys.GNAME);
						BasicDBList members = (BasicDBList) groupObj
								.get(Keys.MEMBERS);
						for (Object mem : members) {
							BasicDBObject member = (BasicDBObject) mem;
							Member m = new Member();
							m.id = member.getString(Keys.ID);
							m.fbid = member.getString(Keys.FBID);
							m.name = member.getString(Keys.NAME);
							m.gcmid = member.getString(Keys.GCMID);
							group.members.add(m);
						}
						BasicDBList messages = (BasicDBList) groupObj
								.get(Keys.MESSAGES);
						for (Object mesObj : messages) {
							BasicDBObject message = (BasicDBObject) mesObj;
							Message mes = new Message();
							mes.author = message.getString(Keys.NAME);
							mes.id = message.getString(Keys.ID);
							mes.content = message.getString(Keys.MESSAGE);
							group.messages.add(mes);
						}
						groups.add(group);

					} catch (MongoException me) {
						me.printStackTrace();
						return "FAILURE";
					} catch (Exception e) {
						e.printStackTrace();
						return "FAILURE";
					}
				}
				return "SUCCESS";
			}
			Log.w("RGA", "Could not prepare MongoClient");
			return "FAILURE";
		}

		@Override
		protected void onPostExecute(String state) {
			super.onPostExecute(state);

			if (state.contentEquals("SUCCESS")) {
				Log.i("RGA", "Success.");
				for (Group g : groups) {
					groupList.add(g);
				}
				gla.notifyDataSetChanged();

				if (GCM.hasChanged) {
					UpdateGcmInGroupsAsync ugiga = new UpdateGcmInGroupsAsync();
					ugiga.execute();
				}

				if (activateWhenReady) {
					Main.sm.showMenu(false);
					GroupsPane.curGroup = groups.get(0);
					GroupsPane.beginSession();
					SessionFragment.pager.setCurrentItem(1);
				}
			}

			if (state.contentEquals("FAILURE")) {
				Log.w("RGA", "Failure!");
				Utils.toast("Could not load group", Toast.LENGTH_SHORT);
			}
		}
	}

	static class Group {
		String name;
		String id;
		ArrayList<Member> members = new ArrayList<Member>();
		ArrayList<Message> messages = new ArrayList<Message>();
	}

	private class CreateGroupAsync extends AsyncTask<String, String, String> {

		String groupId, groupName;

		public CreateGroupAsync() {
		}

		protected String doInBackground(String... args) {
			if (prepare_groups_collection()) {

				Log.i("CGA", "Creating Group...");

				groupName = args[0];

				try {
					BasicDBObject group = new BasicDBObject();

					group.append(Keys.GNAME, groupName);

					BasicDBList members = new BasicDBList();

					BasicDBObject myself = new BasicDBObject();
					myself.append(Keys.ID, Main.self.member.id);
					myself.append(Keys.FBID, Main.self.member.fbid);
					myself.append(Keys.NAME, Main.self.member.name);
					myself.append(Keys.GCMID, Main.self.member.gcmid);

					members.add(myself);

					group.append(Keys.MEMBERS, members);

					BasicDBList messages = new BasicDBList();

					group.append(Keys.MESSAGES, messages);

					coll.insert(group);

					Log.i("CGA", "Created Group in groups collection");

					groupId = group.getObjectId(Keys.ID).toString();

					if (prepare_users_collection()) {

						// Directions to Self
						BasicDBObject query = new BasicDBObject().append(
								Keys.FBID, Main.self.member.fbid);

						// Go to Self
						DBCursor iter = coll2.find(query);

						if (iter.hasNext()) {

							DBObject myProfile = iter.next();

							// Explain that the groupId belongs in the
							// groups list
							BasicDBObject envelope = new BasicDBObject();
							envelope.put(Keys.GROUPS, groupId);

							// Explain that we want the group to pushed
							// to the back of the
							// list
							BasicDBObject update = new BasicDBObject();
							update.put("$push", envelope);

							// Execute the update
							coll2.update(myProfile, update);

							Log.i("CGA",
									"Saved new group's id in users collection.");

							return "SUCCESS";

						} else {
							Log.w("CGA",
									"Could not find user in users collection...");
						}
					} else {
						Log.w("CGA", "Could not prepare users collection");
					}

				} catch (MongoException me) {
					me.printStackTrace();
					return "FAILURE";
				}
			}
			return "FAILURE";
		}

		@Override
		protected void onPostExecute(String state) {
			super.onPostExecute(state);

			if (state.contentEquals("SUCCESS")) {

				Log.i("CGA", "Success.");

				Group g = new Group();
				g.id = groupId;
				Member myself = new Member();
				myself.id = Main.self.member.id;
				myself.name = Main.self.member.name;
				myself.fbid = Main.self.member.fbid;
				myself.gcmid = Main.self.member.gcmid;
				g.members.add(myself);
				g.name = groupName;
				groupList.add(g);
				curGroup = g;
				gla.notifyDataSetChanged();

				beginSession();

			}

			if (state.contentEquals("FAILURE")) {
				Log.w("CGA", "Failure!");
			}
		}
	}

	private static class JoinGroupAsync extends
			AsyncTask<String, String, String> {

		String groupId, groupName;
		BasicDBList members;

		public JoinGroupAsync(String groupId) {
			this.groupId = groupId;
		}

		protected String doInBackground(String... args) {
			if (prepare_groups_collection()) {

				Log.i("JGA", "Joining Group...");

				try {

					// Directions to Group
					BasicDBObject query = new BasicDBObject().append(Keys.ID,
							new ObjectId(groupId));

					// Go to Group
					DBCursor iter = coll.find(query);

					if (iter.hasNext()) {

						DBObject groupObj = iter.next();

						groupName = (String) groupObj.get(Keys.GNAME);

						members = (BasicDBList) groupObj.get(Keys.MEMBERS);

						BasicDBObject myself = new BasicDBObject();
						myself.append(Keys.ID, Main.self.member.id);
						myself.append(Keys.FBID, Main.self.member.fbid);
						myself.append(Keys.NAME, Main.self.member.name);
						myself.append(Keys.GCMID, Main.self.member.gcmid);

						// Explain that you belong in the
						// groups list
						BasicDBObject envelope = new BasicDBObject();
						envelope.put(Keys.MEMBERS, myself);

						// Explain that we want you to be pushed
						// to the back of the list
						BasicDBObject update = new BasicDBObject();
						update.put("$push", envelope);

						// Execute the update
						coll.update(groupObj, update);

						Log.i("JGA", "Joined the group.");

						if (prepare_users_collection()) {

							// Directions to Self
							query = new BasicDBObject().append(Keys.FBID,
									Main.self.member.fbid);

							// Go to Self
							iter = coll2.find(query);

							if (iter.hasNext()) {

								DBObject myProfile = iter.next();

								// Explain that the groupId belongs in the
								// groups list
								envelope = new BasicDBObject();
								envelope.put(Keys.GROUPS, groupId);

								// Explain that we want the group to pushed
								// to the back of the
								// list
								update = new BasicDBObject();
								update.put("$push", envelope);

								// Execute the update
								coll2.update(myProfile, update);

								Log.i("JGA",
										"Saved new group's id in users collection.");

								return "SUCCESS";

							} else {
								Log.w("JGA",
										"Could not find user in users collection...");
							}
						} else {
							Log.w("JGA", "Could not prepare users collection");
						}
					} else {
						Log.w("JGA",
								"Could not find group in groups collection");
					}

				} catch (MongoException me) {
					me.printStackTrace();
				}

			}
			return "FAILURE";
		}

		@Override
		protected void onPostExecute(String state) {
			super.onPostExecute(state);

			if (state.contentEquals("SUCCESS")) {

				Log.i("JGA", "Success.");

				Group g = new Group();
				g.id = groupId;

				Member myself = new Member();
				myself.id = Main.self.member.id;
				myself.name = Main.self.member.name;
				myself.fbid = Main.self.member.fbid;
				myself.gcmid = Main.self.member.gcmid;
				g.members.add(myself);

				for (Object o : members) {
					BasicDBObject memObj = (BasicDBObject) o;
					Member member = new Member();
					member.id = memObj.getString(Keys.ID);
					member.fbid = memObj.getString(Keys.FBID);
					member.name = memObj.getString(Keys.NAME);
					member.gcmid = memObj.getString(Keys.GCMID);
					g.members.add(member);
				}

				g.name = groupName;
				groupList.add(g);
				curGroup = g;
				gla.notifyDataSetChanged();

				beginSession();
			}

			if (state.contentEquals("FAILURE")) {
				Log.w("JGA", "Failure!");
				Toast.makeText(cont, "Could not join group.", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	private static class UpdateGcmInGroupsAsync extends
			AsyncTask<String, String, String> {

		ArrayList<Group> groups = new ArrayList<Group>();

		public UpdateGcmInGroupsAsync() {
			groups.addAll(groupList);
		}

		protected String doInBackground(String... arg0) {
			if (prepare_groups_collection()) {
				try {

					for (Group g : groups) {

						// Directions to Group
						BasicDBObject query = new BasicDBObject().append(
								Keys.GROUPID, new ObjectId(g.id));

						// Go to Group
						DBCursor iter = coll.find(query);

						if (iter != null) {

							// This is the Group
							DBObject groupObj = iter.next();

							BasicDBList members = (BasicDBList) groupObj
									.get(Keys.MEMBERS);

							// Index variable
							Integer memberPosition = 0;

							// Go through members
							for (Object mem : members) {

								// Member who is under inspection
								BasicDBObject member = (BasicDBObject) mem;

								// If you just found yourself
								if (member.getString(Keys.ID).contentEquals(
										(Main.self.member.id))) {

									BasicDBObject newAttrs = new BasicDBObject();
									newAttrs.append(Keys.MEMBERS + "."
											+ memberPosition.toString() + "."
											+ Keys.GCMID,
											Main.self.member.gcmid);

									// Make it a set (swap) operation
									BasicDBObject groupUpdate = new BasicDBObject(
											"$set", newAttrs);

									// Execute the Database update
									coll.update(query, groupUpdate);

									break;
								}

								memberPosition++;
							}

						}
					}
					return "SUCCESS";

				} catch (NoSuchElementException nsee) {
					nsee.printStackTrace();
				} catch (MongoException me) {
					me.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				Log.w("UGIGA", "Could not prepare groups database.");
			}
			return "FAILURE";
		}

		@Override
		protected void onPostExecute(String state) {
			super.onPostExecute(state);

			if (state.contentEquals("SUCCESS")) {
				Log.i("UGIGA", "GCM id updated in all groups.");
			}

			if (state.contentEquals("FAILURE")) {
			}
		}

	}

	public static void beginSession() {

		Utils.slideViewDown(gFrame);
		SessionFragment.start();
		stop();

		GCMMessage.sendMarco(Main.self.member.fbid);
	}

	public void endSession() {
		groupList.clear();
		gla.notifyDataSetChanged();
	}

	// Make sure database is ready for transaction
	private static Boolean prepare_groups_collection() {

		// Open up the collection and begin the I/O Operations
		try {

			if (mc == null) {
				mc = new MongoClient(uri);
				db = mc.getDB("pinpoint");
				coll = db.getCollection("groups");
			}
			if (db == null) {
				db = mc.getDB("pinpoint");
				coll = db.getCollection("groups");
			}
			if (coll == null) {
				coll = db.getCollection("groups");
			}

			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
	}

	// Make sure database is ready for transaction
	private static Boolean prepare_users_collection() {

		// Open up the collection and begin the I/O Operations
		try {

			if (mc2 == null) {
				mc2 = new MongoClient(uri);
				db2 = mc2.getDB("pinpoint");
				coll2 = db2.getCollection("users");
			}
			if (db2 == null) {
				db2 = mc2.getDB("pinpoint");
				coll2 = db2.getCollection("users");
			}
			if (coll2 == null) {
				coll2 = db2.getCollection("users");
			}

			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
	}

	String[] groupIdeas = new String[] { "ie. \"Family\"", "ie. \"GoonSquad\"",
			"ie. \"Avengers\"", "ie. \"Friends\"", "ie. \"Co-workers\"",
			"ie. \"Hooligans\"", "ie. \"3 Musketeers\"", "ie. \"Weekenders\"",
			"ie. \"Dynamic Duo\"", "ie. \"Home Team\"", "ie. \"Siblings\"" };

	private void setGroupNameDialog(Context cont) {

		LinearLayout layout = new LinearLayout(cont);
		layout.setOrientation(LinearLayout.VERTICAL);

		final EditText message = new EditText(cont);
		message.setHint(groupIdeas[(int) (System.currentTimeMillis() % (groupIdeas.length))]);
		layout.addView(message);

		AlertDialog.Builder builder = new AlertDialog.Builder(cont)
				.setTitle("Name the group")
				.setView(layout)
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								dialog.dismiss();

							}
						})
				.setPositiveButton("Set",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								String content = message.getText().toString();

								if (!content.contentEquals("")) {
									createGroup(content);
									dialog.dismiss();
								}

							}
						});
		builder.create().show();

	}

	public static void ShowInviteDialog(final String name, final String group,
			final String groupId) {

		AlertDialog.Builder builder = new AlertDialog.Builder(Main.cont);
		builder.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
			}
		});

		builder.setTitle("Invitation")
				.setMessage(name + " wants you to join " + group)
				.setPositiveButton("Join",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								JoinGroupAsync jga = new JoinGroupAsync(groupId);
								jga.execute();
								dialog.dismiss();

							}
						})
				.setNegativeButton("Decline",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						});

		try {
			builder.setCancelable(false);
			builder.show();
		} catch (Exception e) {
		}
	}
}
