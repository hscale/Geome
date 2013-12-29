package tesler.will.geome;

import android.content.Context;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

public class Monitor {

	Context cont;

	private final Handler h = new Handler();
	private Runnable r;

	public int networkType;

	public Boolean networkConnected = false;

	public Boolean locationLocked = true;

	private Integer counter = 0;

	ConnectivityManager connManager;
	NetworkInfo mWifi;

	final LocationManager lm;

	final GpsStatus.Listener listener;

	Monitor(Context cont) {

		this.cont = cont;

		// GPS Monitor
		lm = (LocationManager) cont.getSystemService(Context.LOCATION_SERVICE);
		lm.addGpsStatusListener(listener = new Listener() {
			@Override
			public void onGpsStatusChanged(int event) {

				// Only care about this when we are not using WIFI
				if (networkType != ConnectivityManager.TYPE_WIFI) {

					switch (event) {
					case GpsStatus.GPS_EVENT_FIRST_FIX:
						locationLocked = true;
						counter = 0;
						break;
					case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
						counter++;
						break;
					case GpsStatus.GPS_EVENT_STARTED:
						locationLocked = false;
						break;
					case GpsStatus.GPS_EVENT_STOPPED:
						locationLocked = false;
						break;
					}
				}
			}
		});

		// Periodically check Network
		r = new Runnable() {
			public void run() {
				if (isNetworkConnected()) {
					networkConnected = true;
				} else {
					networkConnected = false;
				}

				if (mWifi.isConnected()) {
					locationLocked = true;
				}

				if (Main.inForeground)
					h.postDelayed(this, 15000);
				else
					h.postDelayed(this, 25000);
			}
		};

		// For checking Wifi and Connectivity
		connManager = (ConnectivityManager) cont
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

	}

	public void start() {

		h.post(r);
	}

	public void stop() {
		try {
			lm.removeGpsStatusListener(listener);
		} catch (Exception e) {
			e.printStackTrace();
		}

		h.removeCallbacks(r);
	}

	// Set networkType and return true if currently connected
	public boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) cont
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			return false;
		} else {
			networkType = ni.getType();
			return true;
		}

	}
}
