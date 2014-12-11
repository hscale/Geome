package tesler.will.geome;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tesler.will.geome.Main.User;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;

public class GCM {

	// Database
	String uriString = "YOUR DATABASE HERE!!!!";
	private MongoClientURI uri = new MongoClientURI(uriString);
	private MongoClient mc;
	private DB db;
	private DBCollection coll;

	public static final String EXTRA_MESSAGE = "message";
	private static final String PROPERTY_APP_VERSION = "appVersion";

	String SENDER_ID = "YOUR SENDER ID HERE";

	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();

	static boolean hasChanged = false;

	SharedPreferences preferences;

	static GcmBroadcastReceiver gbr;

	GCM() {

		preferences = Main.cont.getSharedPreferences("SETTINGS",
				Context.MODE_PRIVATE);

		gcm = GoogleCloudMessaging.getInstance(Main.cont);

		registerReceiver();

	}

	public void retrieveId() {

		getRegistrationId();

		if (Main.self.member.gcmid == null) {
			registerInBackground();
		}

	}

	private void getRegistrationId() {

		Main.self.member.gcmid = preferences.getString("gcmid", null);

		if (Main.self.member.gcmid == null) {
			Log.i("GCM", "Registration not found.");
		}

		int registeredVersion = preferences.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion();
		if (registeredVersion != currentVersion) {
			Log.i("GCM", "App version changed.");
		}
	}

	private static int getAppVersion() {
		try {
			PackageInfo packageInfo = Main.cont.getPackageManager()
					.getPackageInfo(Main.cont.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	private void registerInBackground() {
		GenerateIDAsync gia = new GenerateIDAsync();
		gia.execute();
	}

	private void storeRegistrationId(String regId) {
		int appVersion = getAppVersion();
		Log.i("GCM", "Saving regId " + regId + " on app version " + appVersion);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("gcmid", regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	private class GenerateIDAsync extends AsyncTask<String, String, String> {

		protected String doInBackground(String... arg0) {
			if (prepare_database()) {
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(Main.cont);
					}

					// REGISTER WITH GCM
					Main.self.member.gcmid = gcm.register(SENDER_ID);

					// NOW REMEMBER IN MONGO

					// Directions to Self
					BasicDBObject query = new BasicDBObject().append(Keys.ID,
							new ObjectId(Main.self.member.id));

					// Go to Self
					DBCursor iter = coll.find(query);

					if (iter != null) {

						// This is the Self
						DBObject myProfile = iter.next();

						try {

							BasicDBObject newValues = new BasicDBObject();
							newValues.put("gcmid", Main.self.member.gcmid);

							BasicDBObject set = new BasicDBObject();

							set.put("$set", newValues);

							coll.update(myProfile, set);

							return "SUCCESS";

						} catch (MongoException me) {
							me.printStackTrace();
							return "FAILURE";
						} catch (Exception e) {
							e.printStackTrace();
							return "FAILURE";
						}
					}
					return "FAILURE";

				} catch (IOException ex) {
					ex.printStackTrace();
					return "FAILURE";
				} catch (NoSuchElementException nsee) {
					nsee.printStackTrace();
					return "FAILURE";
				}
			}
			return "FAILURE";
		}

		@Override
		protected void onPostExecute(String state) {
			super.onPostExecute(state);

			if (state.contentEquals("SUCCESS")) {
				Log.i("Generate ID Async", "New GCM ID created.");
				storeRegistrationId(Main.self.member.gcmid);
				counter = 0;
				hasChanged = true;
			}

			if (state.contentEquals("FAILURE")) {
				if (counter < 2) {
					registerInBackground();
					counter++;
				} else {
					Toast.makeText(Main.cont,
							"Cannot Register for Cloud Messaging.",
							Toast.LENGTH_LONG).show();
				}
			}
		}

	}

	Integer counter = 0;

	static void registerReceiver() {

		// GCM Broadcast Receiver

		gbr = new GcmBroadcastReceiver();

		IntentFilter filter = new IntentFilter(GcmBroadcastReceiver.GCM_ACTION);

		LocalBroadcastManager.getInstance(Main.cont).registerReceiver(gbr,
				filter);
	}

	public void unregisterReceiver() {

		if (gbr != null) {

			LocalBroadcastManager.getInstance(Main.cont)
					.unregisterReceiver(gbr);
		}
	}

	public void pushToGroup(JSONObject data, ArrayList<String> gcmids) {

		if (gcmids.size() > 0) {
			PushToGroupAsync ptga = new PushToGroupAsync(data, Main.self,
					gcmids);
			ptga.execute();
		}

	}

	private class PushToGroupAsync extends AsyncTask<String, String, String> {

		JSONObject data;
		ArrayList<String> gcmIds;

		public PushToGroupAsync(JSONObject data, User self,
				ArrayList<String> gcmIds) {

			this.data = data;

			this.gcmIds = gcmIds;
		}

		@Override
		protected String doInBackground(String... params) {
			try {

				JSONObject jo = new JSONObject();

				// Registration Ids
				JSONArray ja = new JSONArray();

				for (String id : gcmIds) {
					ja.put(id);
				}

				jo.put("registration_ids", ja);
				jo.put("time_to_live", 3);
				jo.put("collapse_key", ObjectId.get().toString());

				jo.put("data", data);

				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(
						"https://android.googleapis.com/gcm/send");

				try {

					httppost.addHeader("Authorization",
							"key=AIzaSyAsQngodp4EVflcyytQ3kslxUTSXtSLK-k");
					httppost.addHeader("Content-Type", "application/json");

					httppost.setEntity(new StringEntity(jo.toString()));

					// Execute HTTP Post Request
					HttpResponse response = httpclient.execute(httppost);

					JSONObject jResponse = parseResponse(response);

					Log.i("GCM", "Send To Gang: " + jResponse);

					return "SUCCESS";

				} catch (ClientProtocolException cpe) {
					return "FAILURE";
				} catch (IOException ie) {
					return "FAILURE";
				}

			} catch (JSONException e) {
				e.printStackTrace();
				return "FAILURE";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}
	}

	public static JSONObject parseResponse(HttpResponse response) {

		// see the HTTP Response
		System.out.println("response: " + response);

		String text = "";

		// parse the results sent back by the server
		if (response != null) {

			// get the http response
			InputStream is;
			try {
				is = response.getEntity().getContent();

				// assign response to buffer
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));

				// define a string buffer
				StringBuilder sb = new StringBuilder();

				String line = null;

				try {
					// while the response buffer has data
					while ((line = reader.readLine()) != null) {
						if (line.length() > 0) {
							sb.append(line);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// return string buffer to a single string
				text = sb.toString().trim();

				Log.v("GCM Parse Response", "Parsed JSON as " + text);

				// try parse the string to a JSON object
				try {

					return new JSONObject(text);

				} catch (JSONException e) {
					Log.e("GCM",
							"String could not be parsed into JSON "
									+ e.toString() + '\n' + text);

					return null;
				}

			} catch (IllegalStateException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			return null;

		} else {
			System.out.println("response is null");
			return null;
		}
	}

	// Make sure database is ready for transaction
	private Boolean prepare_database() {

		// Open up the collection and begin the I/O Operations
		try {

			if (mc == null) {
				mc = new MongoClient(uri);
				db = mc.getDB("pinpoint");
				coll = db.getCollection("users");
			}
			if (db == null) {
				db = mc.getDB("pinpoint");
				coll = db.getCollection("users");
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
