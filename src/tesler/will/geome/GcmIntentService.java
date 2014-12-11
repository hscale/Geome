package tesler.will.geome;

import tesler.will.geome.Ogg.RetrieveRecordingAsync;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {

	public GcmIntentService() {

		super("GcmIntentService");

		new Notifications(this);

	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Bundle extras = intent.getExtras();

		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				Log.w("GCM", "GCM Intent Service Error");
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				Log.w("GCM", "GCM Deleted Messages");
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {

				String action = extras.getString(Keys.ACTION);

				if (action.contentEquals(Keys.ACTIONMESSAGE)) {

					String id = intent.getStringExtra(Keys.ID);
					String name = intent.getStringExtra(Keys.NAME);
					String groupid = intent.getStringExtra(Keys.GROUPID);
					String message = intent.getStringExtra(Keys.MESSAGE);

					if (Main.inForeground
							&& GroupsPane.curGroup.id.contentEquals(groupid)) {

						Bundle b = new Bundle();
						b.putString(Keys.ID, id);
						b.putString(Keys.NAME, name);
						b.putString(Keys.GROUPID, groupid);
						b.putString(Keys.MESSAGE, message);

						// Pass the invite on to the main thread
						broadcast(Keys.ACTIONMESSAGE, b);

					} else {

						Notifications.create(name + ": " + message, true,
								groupid.hashCode(), Keys.ACTIONMESSAGE,
								groupid, null, null);
					}

				} else if (action.contentEquals(Keys.ACTIONLOCATION)) {

					String groupid = intent.getStringExtra(Keys.GROUPID);

					if (GroupsPane.curGroup != null
							&& GroupsPane.curGroup.id.contentEquals(groupid)) {

						Bundle b = new Bundle();
						b.putString(Keys.ID, intent.getStringExtra(Keys.ID));
						b.putString(Keys.NAME, intent.getStringExtra(Keys.NAME));
						b.putString(Keys.LAT, intent.getStringExtra(Keys.LAT));
						b.putString(Keys.LON, intent.getStringExtra(Keys.LON));

						broadcast(Keys.ACTIONLOCATION, b);

					}

				} else if (action.contentEquals(Keys.ACTIONCOLOR)) {

				} else if (action.contentEquals(Keys.ACTIONMARCO)) {

					String groupid = intent.getStringExtra(Keys.GROUPID);

					if (GroupsPane.curGroup != null
							&& GroupsPane.curGroup.id.contentEquals(groupid)) {

						Bundle b = new Bundle();
						b.putString(Keys.ID, intent.getStringExtra(Keys.ID));
						b.putString(Keys.NAME, intent.getStringExtra(Keys.NAME));
						b.putString(Keys.FBID, intent.getStringExtra(Keys.FBID));
						b.putString(Keys.GCMID,
								intent.getStringExtra(Keys.GCMID));

						broadcast(Keys.ACTIONMARCO, b);

					}

				} else if (action.contentEquals(Keys.ACTIONPOLO)) {

					Bundle b = new Bundle();
					b.putString(Keys.ID, intent.getStringExtra(Keys.ID));

					broadcast(Keys.ACTIONPOLO, b);

				} else if (action.contentEquals(Keys.ACTIONPDM)) {

				} else if (action.contentEquals(Keys.ACTIONINVITE)) {

					String name = intent.getStringExtra(Keys.NAME);
					String gname = intent.getStringExtra(Keys.GNAME);
					String groupid = intent.getStringExtra(Keys.GROUPID);

					if (Main.inForeground) {

						Bundle b = new Bundle();
						b.putString(Keys.NAME, name);
						b.putString(Keys.GNAME, gname);
						b.putString(Keys.GROUPID, groupid);

						// Pass the invite on to the main thread
						broadcast(Keys.ACTIONINVITE, b);

					} else {
						// Create notification in the background
						Notifications.create(name + " has invited you to join "
								+ gname, true, (name + groupid).hashCode(),
								Keys.ACTIONINVITE, groupid, name, gname);
					}

				} else if (action.contentEquals(Keys.ACTIONPOKE)) {

				} else if (action.contentEquals(Keys.ACTIONUNITE)) {

				} else if (action.contentEquals(Keys.ACTIONRECOMMEND)) {

				} else if (action.contentEquals(Keys.ACTIONDISCONNECT)) {

				} else if (action.contentEquals(Keys.ACTIONREMOVEPLACE)) {

				} else if (action.contentEquals(Keys.ACTIONAUDIO)) {

					Ogg.RetrieveRecordingAsync rra = new RetrieveRecordingAsync(
							extras.getString(Keys.AUDIOID));
					rra.execute();
				}
			}
		}

		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	public static void broadcast(String broadcastType, Bundle b) {

		Log.i("GCMIntentService", "Broadcasting Action");

		Intent i = new Intent(GcmBroadcastReceiver.GCM_ACTION).putExtra(
				Keys.BROADCAST, broadcastType).putExtras(b);

		// i.putExtra(Keys.NAME, username);
		// i.putStringArrayListExtra(Keys.GCMIDS, gcmIds);
		LocalBroadcastManager.getInstance(Main.cont).sendBroadcast(i);
	}
}