package tesler.will.geome;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebook.OnInviteListener;
import com.sromku.simple.fb.entities.Profile;

public class FriendListAdapter extends ArrayAdapter<Profile> implements
		Filterable {

	private ArrayList<Profile> profiles;
	private ArrayList<Profile> allItems = new ArrayList<Profile>();

	Dialog d;

	Context cont;

	String uriString = "mongodb://wtesler:Violentomega13@ds053138-a0.mongolab.com:53138,ds053138-a1.mongolab.com:53138/pinpoint";
	MongoClientURI uri = new MongoClientURI(uriString);

	private MongoClient mc;
	private DB db;
	private DBCollection coll;

	public FriendListAdapter(Context context, ArrayList<Profile> items, Dialog d) {
		super(context, R.layout.row_profile, items);
		this.cont = context;
		this.profiles = items;

		this.d = d;

		allItems.addAll(profiles);
	}

	public int getCount() {
		return profiles.size();
	}

	public Profile getItem(int arg0) {
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}

	public View getView(final int position, View v, ViewGroup parent) {
		if (v == null) {

			// Inflate the Row in the ListView
			LayoutInflater vi = (LayoutInflater) cont
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row_profile, null);
		}

		v.setId(position);

		final Profile profile = profiles.get(position);

		// NAME
		TextView name = (TextView) v.findViewById(R.id.tv_name);

		name.setText(profile.getName());

		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String friendId = profile.getId();
				SendInviteAsync sia = new SendInviteAsync();
				sia.execute(friendId);
				d.dismiss();
			}
		});

		return v;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {

				if (results != null && results.values != null) {
					profiles.clear();
					profiles.addAll((ArrayList<Profile>) results.values);
				}

				notifyDataSetChanged();
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();

				profiles.clear();
				profiles.addAll(allItems);

				// We implement here the filter logic
				if (constraint == null || constraint.length() == 0) {
					results.values = profiles;
					results.count = profiles.size();
				} else {

					ArrayList<Profile> filteredList = new ArrayList<Profile>();
					for (Profile p : profiles) {
						if (p.getName().toLowerCase()
								.contains(constraint.toString().toLowerCase())) {
							filteredList.add(Profile.create(p.getGraphUser()));
						}
					}

					results.values = filteredList;
					results.count = filteredList.size();
				}
				return results;
			};

		};
	}

	class SendInviteAsync extends AsyncTask<String, String, String> {

		String fbid, gcmid;
		boolean noAccount = false;

		protected String doInBackground(String... parm) {

			fbid = parm[0];

			Log.i("SIA", "Finding Friend...");

			if (prepare_database()) {
				try {

					// Directions to Friend
					BasicDBObject query = new BasicDBObject().append(Keys.FBID,
							fbid);

					// Go to Self
					DBCursor iter = coll.find(query);

					if (iter.hasNext()) {

						Log.i("SIA", "Friend has Geome Account");

						DBObject friendProfile = iter.next();

						gcmid = (String) friendProfile.get(Keys.GCMID);

						return "SUCCESS";
					} else {

						Log.i("SIA", "User does not have Geome Account");

						noAccount = true;

						return "SUCCESS";
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

				if (noAccount) {
					SimpleFacebook.getInstance().invite(fbid,
							"I want you to join my group in Geome.",
							onInviteListener);
				} else {
					GCMMessage.sendInvite(gcmid, GroupsPane.curGroup);
				}

				Log.i("SIA", "Success.");

			} else {
				Log.w("SIA", "Failure!");

				mc = null;
			}

		}
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

	OnInviteListener onInviteListener = new SimpleFacebook.OnInviteListener() {

		@Override
		public void onFail(String reason) {
			// insure that you are logged in before inviting
			Log.w("App Invite", reason);
		}

		@Override
		public void onException(Throwable throwable) {
			Log.e("App Invite", "Bad thing happened", throwable);
		}

		@Override
		public void onComplete(List<String> invitedFriends, String requestId) {
			Log.i("App Invite",
					"Invitation was sent to " + invitedFriends.size()
							+ " users with request id " + requestId);
		}

		@Override
		public void onCancel() {
			Log.i("App Invite", "Canceled the dialog");
		}

	};

}
