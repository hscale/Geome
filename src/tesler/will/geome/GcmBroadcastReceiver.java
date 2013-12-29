package tesler.will.geome;

import tesler.will.geome.GroupsPane.Group;
import tesler.will.geome.MembersFragment.Member;
import tesler.will.geome.MessagesFragment.Message;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

	// WARNING: THIS MUST BE CHANGED IF YOU CHANGE THE PACKAGE NAME OF THE APP
	public final static String STATUS = "tesler.will.geome.STATUS";
	public final static String GCM_ACTION = "com.google.android.c2dm.intent.RECEIVE";
	public final static String MY_ACTION = "tesler.will.geome.RECEIVE";

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.i("GcmBroadcastReceiver", "OnReceive Called");

		String broadcast = intent.getStringExtra(Keys.BROADCAST);

		if (broadcast == null) {

			// Explicitly specify that GcmIntentService will handle the
			// intent.
			ComponentName comp = new ComponentName(context.getPackageName(),
					GcmIntentService.class.getName());
			// Start the service, keeping the device awake while it is
			// launching.
			startWakefulService(context, (intent.setComponent(comp)));
			setResultCode(Activity.RESULT_OK);
		} else {
			Log.i("Broadcast", broadcast);
			if (broadcast.contentEquals(Keys.ACTIONINVITE)) {

				String name = intent.getStringExtra(Keys.NAME);
				String groupName = intent.getStringExtra(Keys.GNAME);
				String groupId = intent.getStringExtra(Keys.GROUPID);

				GroupsPane.ShowMailDialog(name, groupName, groupId);

			} else if (broadcast.contentEquals(Keys.ACTIONMARCO)) {

				String id = intent.getStringExtra(Keys.ID);
				String gcmid = intent.getStringExtra(Keys.GCMID);

				boolean memFound = false;
				for (Member mem : GroupsPane.curGroup.members) {
					if (mem.id.contentEquals(id)) {
						memFound = true;
						break;
					}
				}

				if (memFound == false) {
					Member newMember = new Member();
					newMember.id = intent.getStringExtra(Keys.ID);
					newMember.fbid = intent.getStringExtra(Keys.FBID);
					newMember.name = intent.getStringExtra(Keys.NAME);
					newMember.gcmid = intent.getStringExtra(Keys.GCMID);
					GroupsPane.curGroup.members.add(newMember);
					MembersFragment.mla.notifyDataSetChanged();
				}

				GCMMessage.sendPolo(gcmid);

			} else if (broadcast.contentEquals(Keys.ACTIONPOLO)) {

			} else if (broadcast.contentEquals(Keys.ACTIONMESSAGE)) {
				String id = intent.getStringExtra(Keys.ID);
				String name = intent.getStringExtra(Keys.NAME);
				String message = intent.getStringExtra(Keys.MESSAGE);
				String groupid = intent.getStringExtra(Keys.GROUPID);

				Message m = new Message();
				m.author = name;
				m.content = message;
				m.id = id;

				for (Group g : GroupsPane.groupList) {
					if (g.id.contentEquals(groupid)) {
						g.messages.add(m);
						MessagesFragment.mla.notifyDataSetChanged();
						break;
					}
				}
			} else if (broadcast.contentEquals(Keys.ACTIONLOCATION)) {
				String id = intent.getStringExtra(Keys.ID);
				// String name = intent.getStringExtra(Keys.NAME);
				Double lat = Double
						.parseDouble(intent.getStringExtra(Keys.LAT));
				Double lon = Double
						.parseDouble(intent.getStringExtra(Keys.LON));

				for (Member m : GroupsPane.curGroup.members) {
					if (m.id.contentEquals(id)) {

						m.lat = lat;
						m.lon = lon;

						if (m.pic == null) {
							m.pic = Images.paintMarkerBitmap(m);
						}

						Locater.placeMarker(m);
						break;
					}
				}
			}
		}
	}
}
