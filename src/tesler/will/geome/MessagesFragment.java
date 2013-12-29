package tesler.will.geome;

import java.net.UnknownHostException;

import org.bson.types.ObjectId;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;

public class MessagesFragment extends Fragment {

	static MessageListAdapter mla;
	static ListView lv;

	static EditText et_message;
	ImageView iv_send;

	String uriString = "mongodb://wtesler:Violentomega13@ds053138-a0.mongolab.com:53138,ds053138-a1.mongolab.com:53138/pinpoint";
	MongoClientURI uri = new MongoClientURI(uriString);

	private MongoClient mc;
	private DB db;
	private DBCollection coll;

	static boolean isRunning = false;

	static int lvMax;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the Fragment
		final View rv = inflater.inflate(R.layout.messages_frag, container,
				false);

		et_message = (EditText) rv.findViewById(R.id.et_send);
		et_message.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView sendBox, int actionId,
					KeyEvent arg2) {
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					iv_send.performClick();
					return true;
				}
				return false;
			}
		});

		iv_send = (ImageView) rv.findViewById(R.id.iv_send);
		iv_send.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				String message = et_message.getText().toString();

				if (!message.contentEquals("")) {

					Utils.hideSoftKeyboard();

					Message m = new Message();
					m.id = Main.self.member.id;
					m.author = Main.self.member.name;
					m.content = message;

					SaveMessageAsync sma = new SaveMessageAsync(m);
					sma.execute();

					GroupsPane.curGroup.messages.add(m);

					mla.notifyDataSetChanged();

					GCMMessage.sendTextMessage(message);

					et_message.setText("");

				}
			}
		});

		lv = (ListView) rv.findViewById(R.id.lv_messages);
		lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

		return rv;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public static void start(Boolean restart) {

		if (!isRunning) {

			if (!restart) {
				mla = new MessageListAdapter(Main.cont,
						GroupsPane.curGroup.messages);
				lv.setAdapter(mla);
			}

			isRunning = true;
		}
	}

	static void stop() {

		isRunning = false;
	}

	public static void focus() {
		if (et_message != null) {
			et_message.requestFocus();

			Utils.makeKeyboardResizeScreen();

			mla.notifyDataSetChanged();
		}
	}

	private class SaveMessageAsync extends AsyncTask<String, String, String> {

		Message message;

		public SaveMessageAsync(Message message) {
			this.message = message;
		}

		protected String doInBackground(String... args) {

			try {
				if (prepare_database()) {

					// Directions to Group
					BasicDBObject query = new BasicDBObject().append(Keys.ID,
							new ObjectId(GroupsPane.curGroup.id));

					// Go to Group
					DBCursor iter = coll.find(query);

					if (iter.hasNext()) {

						DBObject group = iter.next();

						// Explain that the message belongs in the
						// messages list
						BasicDBObject envelope = new BasicDBObject();
						BasicDBObject modifier = new BasicDBObject();
						BasicDBObject messageObj = new BasicDBObject();
						messageObj.put(Keys.NAME, message.author);
						messageObj.put(Keys.ID, message.id);
						messageObj.put(Keys.MESSAGE, message.content);
						BasicDBList list = new BasicDBList();
						list.add(messageObj);
						modifier.put("$each", list);
						modifier.put("$slice", -100);
						envelope.put(Keys.MESSAGES, modifier);

						// Explain that we want the group to pushed
						// to the back of the
						// list
						BasicDBObject update = new BasicDBObject();
						update.put("$push", envelope);

						// Execute the update
						coll.update(group, update);

						Log.i("SMA", "Saved message");

						return "SUCCESS";

					} else {
						Log.w("SMA", "Could not find group "
								+ GroupsPane.curGroup.id
								+ " in groups collection...");
					}
				} else {
					Log.w("SMA", "Could not prepare groups collection");
				}

			} catch (MongoException me) {
				me.printStackTrace();
				return "FAILURE";
			}
			return "FAILURE";
		}

		@Override
		protected void onPostExecute(String state) {
			super.onPostExecute(state);

			if (state.contentEquals("SUCCESS")) {

				Log.i("SMA", "Success.");

			}

			if (state.contentEquals("FAILURE")) {
				Log.w("SMA", "Failure!");
				Toast.makeText(Main.cont, "Could not save message.",
						Toast.LENGTH_LONG).show();
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
				coll = db.getCollection("groups");
			}
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
	}

	static class Message {

		String author;
		String content;
		String id;

	}

}
