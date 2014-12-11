package tesler.will.geome;

import tesler.will.geome.Main.User;
import tesler.will.geome.MembersFragment.Member;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class Locater {

	static GoogleMap map;

	private static LocationClient locationClient;
	private static LocationListener listener;

	Monitor monitor;

	Handler gpsTimer = new Handler();
	Runnable r;

	long interval = 24000;

	public Integer counter = 0;

	// Amount to decrease/increase interval
	int wobble = 0;

	private LocationRequest REQUEST = LocationRequest.create()
			.setFastestInterval(1000).setInterval(1000)
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	public Boolean isRunning = false;

	private Boolean first = true;

	FragmentActivity activity;

	Context cont;

	User self;

	GCMMessage gcmMessage;

	public Locater(FragmentActivity activity) {
		this.activity = activity;
		this.cont = activity.getApplicationContext();
		this.self = Main.self;
		this.gcmMessage = Main.gcmMessage;

		map = ((SupportMapFragment) activity.getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		monitor = new Monitor(cont);
	}

	public void start() {

		if (!isRunning) {
			setup_GPS_Listener();
		}

		monitor.start();

	}

	public void stop() {

		if (locationClient != null) {
			locationClient.disconnect();
		}

		isRunning = false;

		first = true;

		monitor.stop();

	}

	private void setup_GPS_Listener() {

		if (listener == null) {

			//Create the GPS Listener Object
			listener = new LocationListener() {

				//What do we need to do when the location is changed?
				@Override
				public void onLocationChanged(Location loc) {

					//If Wifi
					if (monitor.isNetworkConnected()
							&& monitor.networkType == ConnectivityManager.TYPE_WIFI) {

						@SuppressWarnings("deprecation")
						String locationProviders = Settings.Secure.getString(
								cont.getContentResolver(),
								Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
						if (locationProviders != null
								&& !locationProviders.equals("")) {
							//Lock our location.
							monitor.locationLocked = true;
						}
					}

					//If we have had a steady lock
					if (monitor.locationLocked && (counter >= 2 || first)) {

						//Swap 
						Double oldLat = self.member.lat;
						Double oldLong = self.member.lon;

						self.member.lat = loc.getLatitude();
						self.member.lon = loc.getLongitude();

						if (self.member.pic == null) {
							self.member.pic = Images
									.paintMarkerBitmap(self.member);
						}

						placeMarker(self.member);

						Log.i("Position", self.member.lat.toString() + ", "
								+ self.member.lon.toString());

						GCMMessage.sendLocation(self.member.lat,
								self.member.lon);

						MembersFragment.stopSpinning();

						// gcmMessage.sendLocation(self.lat, self.lon);

						float[] results = new float[1];
						try {
							Location.distanceBetween(oldLat, oldLong,
									self.member.lat, self.member.lon, results);

							if (results[0] > 35 && wobble > -interval / 2) {

							} else if (results[0] <= 35
									&& wobble < interval / 2) {
							}

						} catch (NullPointerException npe) {
							Log.i("Locater", "Got first location");
						}

						gpsTimer.removeCallbacks(r);

						locationClient.disconnect();

						monitor.locationLocked = false;

						counter = 0;

						first = false;

					}

					counter++;
				}
			};
		}
	}

	public void attemptToRetrieveLocation() {

		r = new Runnable() {
			@Override
			public void run() {
				if (locationClient.isConnected())
					locationClient.disconnect();
			}
		};

		gpsTimer.postDelayed(r, 10000);

		if (locationClient == null || !locationClient.isConnected()) {

			locationClient = new LocationClient(cont,
					new ConnectionCallbacks() {

						@Override
						public void onConnected(Bundle arg0) {

							isRunning = true;
							try {

								locationClient.requestLocationUpdates(REQUEST,
										listener);

							} catch (IllegalStateException ise) {
								isRunning = false;
							} catch (NullPointerException npe) {
							}

						}

						@Override
						public void onDisconnected() {
							isRunning = false;
							locationClient = null;
						}
					},

					new OnConnectionFailedListener() {
						@Override
						public void onConnectionFailed(
								ConnectionResult connectionResult) {
							locationClient = null;
							isRunning = false;

							if (connectionResult.hasResolution()) {
								try {
									connectionResult.startResolutionForResult(
											activity, 9000);
								} catch (IntentSender.SendIntentException e) {
								}
							} else {
							}
						}
					});

			if (locationClient != null) {
				locationClient.connect();
			}
		}
	}

	public static void placeMarker(Member mem) {

		if (mem.marker != null) {
			mem.marker.remove();
		}

		animateCamera();

		mem.marker = map.addMarker(new MarkerOptions().position(
				new LatLng(mem.lat, mem.lon)).icon(mem.pic));

		Log.i("Locater", "Placed Marker");
	}

	public static void animateCamera() {

		try {

			double[] coords = new double[] { Double.MAX_VALUE,
					Double.NEGATIVE_INFINITY, Double.MAX_VALUE,
					Double.NEGATIVE_INFINITY };

			adjustBoundsToMembers(coords);

			map.animateCamera(CameraUpdateFactory.newLatLngBounds(
					new LatLngBounds(new LatLng(coords[0] - .002,
							coords[2] - .0015), new LatLng(coords[1] + .002,
							coords[3] + .0015)), Utils.screenWidth / 8),
					new CancelableCallback() {
						@Override
						public void onFinish() {

						}

						@Override
						public void onCancel() {

						}
					});
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
		} catch (IllegalStateException ise) {
			ise.printStackTrace();
		}
	}

	private static void adjustBoundsToMembers(double[] coords) {

		for (Member m : GroupsPane.curGroup.members) {

			if (m.lat != null) {

				if (m.lat < coords[0]) {
					coords[0] = m.lat;
				}
				if (m.lat > coords[1]) {
					coords[1] = m.lat;
				}
				if (m.lon < coords[2]) {
					coords[2] = m.lon;
				}
				if (m.lon > coords[3]) {
					coords[3] = m.lon;
				}
			}

			// Special case with self
			if (m.id.contentEquals(Main.self.member.id)
					&& Main.self.member.lat != null) {
				if (Main.self.member.lat < coords[0]) {
					coords[0] = Main.self.member.lat;
				}
				if (Main.self.member.lat > coords[1]) {
					coords[1] = Main.self.member.lat;
				}
				if (Main.self.member.lon < coords[2]) {
					coords[2] = Main.self.member.lon;
				}
				if (Main.self.member.lon > coords[3]) {
					coords[3] = Main.self.member.lon;
				}
			}

		}
	}
}
