package tesler.will.geome;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import tesler.will.geome.GroupsPane.Group;
import tesler.will.geome.MembersFragment.Member;

public class GCMMessage {

	public static void sendTextMessage(String text) {

		ArrayList<String> gcmIds = getGcmIdsFromGroup();

		JSONObject jo = new JSONObject();
		try {
			jo.put(Keys.ACTION, Keys.ACTIONMESSAGE);
			jo.put(Keys.ID, Main.self.member.id);
			jo.put(Keys.NAME, Main.self.member.name);
			jo.put(Keys.MESSAGE, text);
			jo.put(Keys.GROUPID, GroupsPane.curGroup.id);
			Main.gcm.pushToGroup(jo, gcmIds);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void sendLocation(Double lat, Double lon) {

		ArrayList<String> gcmIds = getGcmIdsFromGroup();

		JSONObject jo = new JSONObject();
		try {
			jo.put(Keys.ACTION, Keys.ACTIONLOCATION);
			jo.put(Keys.ID, Main.self.member.id);
			jo.put(Keys.NAME, Main.self.member.name);
			jo.put(Keys.GROUPID, GroupsPane.curGroup.id);
			jo.put(Keys.LAT, lat.toString());
			jo.put(Keys.LON, lon.toString());
			Main.gcm.pushToGroup(jo, gcmIds);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void sendColor(String color) {

		ArrayList<String> gcmIds = getGcmIdsFromGroup();

		JSONObject jo = new JSONObject();
		try {
			jo.put(Keys.ACTION, Keys.ACTIONCOLOR);
			jo.put(Keys.ID, Main.self.member.id);
			jo.put(Keys.COLOR, color);
			Main.gcm.pushToGroup(jo, gcmIds);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void sendMarco(String fbid) {

		ArrayList<String> gcmIds = getGcmIdsFromGroup();

		JSONObject jo = new JSONObject();
		try {
			jo.put(Keys.ACTION, Keys.ACTIONMARCO);
			jo.put(Keys.ID, Main.self.member.id);
			jo.put(Keys.FBID, fbid);
			jo.put(Keys.GROUPID, GroupsPane.curGroup.id);
			jo.put(Keys.NAME, Main.self.member.name);
			jo.put(Keys.GCMID, Main.self.member.gcmid);
			Main.gcm.pushToGroup(jo, gcmIds);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void sendPolo(String gcmIdOfRecipient) {

		ArrayList<String> gcmIds = new ArrayList<String>();
		gcmIds.add(gcmIdOfRecipient);

		JSONObject jo = new JSONObject();
		try {
			jo.put(Keys.ACTION, Keys.ACTIONPOLO);
			jo.put(Keys.ID, Main.self.member.id);
			Main.gcm.pushToGroup(jo, gcmIds);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void sendPleaseDisconnectMe(String gcmIdOfRecipient,
			String groupId) {

		ArrayList<String> gcmIds = new ArrayList<String>();
		gcmIds.add(gcmIdOfRecipient);

		JSONObject jo = new JSONObject();
		try {
			jo.put(Keys.ACTION, Keys.ACTIONDISCONNECT);
			jo.put(Keys.ID, Main.self.member.id);
			jo.put(Keys.GROUPID, groupId);
			Main.gcm.pushToGroup(jo, gcmIds);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void sendInvite(String gcmIdOfRecipient, Group g) {

		ArrayList<String> gcmIds = new ArrayList<String>();
		gcmIds.add(gcmIdOfRecipient);

		JSONObject jo = new JSONObject();
		try {
			jo.put(Keys.ACTION, Keys.ACTIONINVITE);
			jo.put(Keys.NAME, Main.self.member.name);
			jo.put(Keys.GROUPID, g.id);
			jo.put(Keys.GNAME, g.name);
			Main.gcm.pushToGroup(jo, gcmIds);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void sendPoke(String gcmIdOfRecipient) {

		ArrayList<String> gcmIds = new ArrayList<String>();
		gcmIds.add(gcmIdOfRecipient);

		JSONObject jo = new JSONObject();
		try {
			jo.put(Keys.ACTION, Keys.ACTIONPOKE);
			jo.put(Keys.NAME, Main.self.member.name);
			Main.gcm.pushToGroup(jo, gcmIds);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void sendUnite() {

		ArrayList<String> gcmIds = getGcmIdsFromGroup();

		JSONObject jo = new JSONObject();
		try {
			jo.put(Keys.ACTION, Keys.ACTIONUNITE);
			jo.put(Keys.NAME, Main.self.member.name);
			Main.gcm.pushToGroup(jo, gcmIds);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void sendDisconnect() {

		ArrayList<String> gcmIds = getGcmIdsFromGroup();

		JSONObject jo = new JSONObject();
		try {
			jo.put(Keys.ACTION, Keys.ACTIONDISCONNECT);
			jo.put(Keys.ID, Main.self.member.id);
			Main.gcm.pushToGroup(jo, gcmIds);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void sendRecommendation(String placeName, String placeId,
			Double lat, Double lon) {

		ArrayList<String> gcmIds = getGcmIdsFromGroup();

		JSONObject jo = new JSONObject();
		try {
			jo.put(Keys.ACTION, Keys.ACTIONRECOMMEND);
			jo.put(Keys.NAME, Main.self.member.name);
			jo.put(Keys.PNAME, placeName);
			jo.put(Keys.PLACEID, placeId);
			jo.put(Keys.LAT, lat.toString());
			jo.put(Keys.LON, lon.toString());
			Main.gcm.pushToGroup(jo, gcmIds);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void sendRemovePlace(String placeId) {

		ArrayList<String> gcmIds = getGcmIdsFromGroup();

		JSONObject jo = new JSONObject();
		try {
			jo.put(Keys.ACTION, Keys.ACTIONREMOVEPLACE);
			jo.put(Keys.NAME, Main.self.member.name);
			jo.put(Keys.PLACEID, placeId);
			Main.gcm.pushToGroup(jo, gcmIds);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void sendAudioPoke(String audioId) {

		ArrayList<String> gcmIds = getGcmIdsFromGroup();

		JSONObject jo = new JSONObject();
		try {
			jo.put(Keys.ACTION, Keys.ACTIONAUDIO);
			jo.put(Keys.NAME, Main.self.member.name);
			jo.put(Keys.AUDIOID, audioId);
			Main.gcm.pushToGroup(jo, gcmIds);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private static ArrayList<String> getGcmIdsFromGroup() {
		ArrayList<String> gcmIds = new ArrayList<String>();
		for (Member member : GroupsPane.curGroup.members) {
			if (!member.id.contentEquals(Main.self.member.id))
				gcmIds.add(member.gcmid);
		}
		return gcmIds;
	}
}
