package tesler.will.geome;

import tesler.will.geome.Ogg.RetrieveRecordingAsync;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {

	public GcmIntentService() {

		super("GcmIntentService");

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

					String groupid = intent.getStringExtra(Keys.GROUPID);

					if (Main.inForeground
							&& GroupsPane.curGroup.id.contentEquals(groupid)) {

						Bundle b = new Bundle();
						b.putString(Keys.NAME, intent.getStringExtra(Keys.NAME));
						b.putString(Keys.GROUPID, groupid);
						b.putString(Keys.ID, intent.getStringExtra(Keys.ID));
						b.putString(Keys.MESSAGE,
								intent.getStringExtra(Keys.MESSAGE));

						// Pass the invite on to the main thread
						broadcast(Keys.ACTIONMESSAGE, b);

					} else {

						newMessage(intent.getStringExtra(Keys.MESSAGE), true,
								80085);
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

					if (Main.inForeground) {

						Bundle b = new Bundle();
						b.putString(Keys.NAME, intent.getStringExtra(Keys.NAME));
						b.putString(Keys.GNAME,
								intent.getStringExtra(Keys.GNAME));
						b.putString(Keys.GROUPID,
								intent.getStringExtra(Keys.GROUPID));

						// Pass the invite on to the main thread
						broadcast(Keys.ACTIONINVITE, b);

					} else {
						// Create notification in the background
						createInviteNotification(extras.getString(Keys.NAME));
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

	public void createInviteNotification(String name) {

		Log.i("GCMIntentService", "Creating notification");

		NotificationManager nm = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		nm.cancel(name.hashCode());

		// NOTOFICATION BUILDER
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Invitation from " + name)
				.setContentText("Touch to Respond");

		mBuilder.setSound(RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

		// INTENT
		Intent resultIntent = new Intent(this, Main.class);
		resultIntent.putExtra(Keys.NAME, name);
		// resultIntent.putStringArrayListExtra("gcmids", gcmIdList);

		// ANDROID STACK HOCUS POCUS
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(Main.class);
		stackBuilder.addNextIntent(resultIntent);

		// PREPARE THE NOTIFICATION
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);

		// PLACE THE NOTIFICATION
		Notification notification = mBuilder.build();
		notification.flags = Notification.FLAG_AUTO_CANCEL;

		// mId allows you to update the notification later on.
		nm.notify(name.hashCode(), notification);
	}

	public void newMessage(String message, Boolean hasSound, int id) {

		NotificationManager nm = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		nm.cancel(id);

		// NOTOFICATION BUILDER
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(message).setContentText("");

		if (hasSound) {
			mBuilder.setSound(RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		}

		// INTENT
		Intent resultIntent = new Intent(this, Main.class);
		resultIntent.putExtra("message", "message");

		// ANDROID STACK HOCUS POCUS
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(Main.class);
		stackBuilder.addNextIntent(resultIntent);

		// PREPARE THE NOTIFICATION
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);

		// PLACE THE NOTIFICATION
		Notification notification = mBuilder.build();
		notification.flags = Notification.FLAG_AUTO_CANCEL;

		// mId allows you to update the notification later on.
		nm.notify(id, notification);

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