package tesler.will.geome;

import java.util.HashMap;

import tesler.will.geome.MembersFragment.Member;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class Images {

	HashMap<String, Integer> colors = new HashMap<String, Integer>();

	HashMap<String, Bitmap> icons = new HashMap<String, Bitmap>();

	// public void showPlaceMarker(Place place) {
	//
	// I("Placing Place Marker");
	// if (place.marker == null) {
	// Bitmap pic = icons.get(place.picUrl);
	// if (pic == null) {
	// URLImageAsync uia = new URLImageAsync(place);
	// uia.execute();
	// } else {
	// Global.map.placePlace(place, pic);
	// }
	// }
	// }

	public static BitmapDescriptor paintMarkerBitmap(Member mem) {

		// Integer titleHeight = ((int) 30 * Utils.screenDensity / 250) + 2;

		Integer moveDown, moveRight = 0;

		// marker background
		Bitmap template = getMarkerTemplate("orange", 1, Main.cont);
		Integer t_width = template.getWidth();
		Integer t_height = template.getHeight();

		// holds template + 4 layers on top
		Bitmap marker = Bitmap.createBitmap(t_width, t_height,
				Bitmap.Config.ARGB_8888);

		// Prepare marker for creation and paint on the
		// marker_window
		Canvas canvas = new Canvas(marker);

		drawAndRecycle(template, canvas, 0, 0, 0, 230);

		Integer m_width = t_width;
		Integer m_height = (int) (t_height - Math.floor(t_height / 4));

		// Where to stick the markerPic...
		int amountAfterMiddle = (m_width - (m_width / 2));
		moveRight = amountAfterMiddle / 6;

		amountAfterMiddle = (m_height - (m_height / 2));
		moveDown = amountAfterMiddle / 6;

		if (mem.bitmap == null) {
			mem.bitmap = MembersFragment.getProfilePic(mem.id);
			if (mem.bitmap == null) {
				mem.bitmap = ((BitmapDrawable) Main.cont.getResources()
						.getDrawable(R.drawable.default_profile)).getBitmap();
			}
			mem.bitmap = scaleBitmap(mem.bitmap, t_width - 2 * moveRight,
					t_height - 2 * moveDown, Main.cont);
		}

		drawAndRecycle(mem.bitmap, canvas, moveRight, moveDown, 0, 230);

		// TextView textView = new TextView(cont);
		// textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		// textView.layout(0, 0, t_width, (int) 30 * Utils.screenDensity / 250);
		//
		// textView.setTextColor(Color.BLACK);
		// textView.setGravity(Gravity.CENTER);
		// textView.setTypeface(null, Typeface.BOLD);
		// textView.setBackgroundResource(R.color.gang_orange);
		// textView.setText(mem.name);
		//
		// textView.setDrawingCacheEnabled(true);
		//
		// drawAndRecycle(textView.getDrawingCache(), canvas, 0, 0, 0, 175);

		return BitmapDescriptorFactory.fromBitmap(marker);

	}

	private static Bitmap scaleBitmap(Bitmap bm, Integer t_width,
			Integer t_height, Context cont) {

		return Bitmap.createScaledBitmap(bm, t_width,
				(int) (t_height - Math.floor(t_height / 5)), false);

	}

	// private void paintPlaceBitmap(Place place, Bitmap pic) {
	//
	// I("Painting Place Bitmap Marker");
	//
	// Integer moveDown, moveRight = 0;
	//
	// // marker background
	//
	// Bitmap marker = getMarkerTemplate("pink", 2);
	//
	// Integer t_width = marker.getWidth();
	// Integer t_height = marker.getHeight();
	//
	// Bitmap template = Bitmap.createBitmap(t_width, t_height,
	// Bitmap.Config.ARGB_8888);
	//
	// // Prepare marker for creation and paint on the
	// // marker_window
	// Canvas canvas = new Canvas(template);
	//
	// drawAndRecycle(marker, canvas, 0, 0, 0, 170);
	//
	// // Layers
	// Bitmap layer3 = null;
	//
	// layer3 = Bitmap.createScaledBitmap(Bitmap.createBitmap(pic),
	// t_width - 7, (int) (t_height - Math.floor(t_height / 4)) - 7,
	// true);
	//
	// Integer m_width = layer3.getWidth();
	// Integer m_height = layer3.getHeight();
	//
	// // Where to stick the markerPic...
	// int amountAfterMiddle = (m_width - (t_width / 2));
	// moveRight = (m_width - (2 * amountAfterMiddle)) / 2;
	//
	// amountAfterMiddle = (m_height - (t_height / 2));
	// moveDown = (int) (((m_height - (2 * amountAfterMiddle)) / 2) - Math
	// .floor(m_height / 10));
	//
	// drawAndRecycle(layer3, canvas, moveRight, moveDown, 0, 170);
	//
	// icons.put(place.picUrl, template);
	//
	// Global.map.placePlace(place, template);
	//
	// }

	private static void drawAndRecycle(Bitmap layer, Canvas canvas,
			Integer moveRight, Integer moveDown, Integer modifier, Integer alpha) {

		if (layer != null) {

			Paint paint = new Paint();
			paint.setAlpha(alpha);

			// Paint the markerPic onto the marker
			canvas.drawBitmap(layer, moveRight, moveDown + modifier, paint);

			layer.recycle();

		}

	}

	private static Bitmap getMarkerTemplate(String color, int sampleSize,
			Context cont) {

		Options o = new Options();
		o.inSampleSize = sampleSize;

		int resId = cont.getResources().getIdentifier("marker_" + color,
				"drawable", cont.getPackageName());

		return BitmapFactory.decodeResource(cont.getResources(), resId, o);

	}

	// private Bitmap scaleBitmap(char[] layers, Integer i, Integer t_width,
	// Integer t_height) {
	//
	// char gender = layers[0];
	// return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
	// cont.getResources(),
	// Global.profile.getPicId(gender, i,
	// Character.getNumericValue(layers[i + 1]))), t_width,
	// (int) (t_height - Math.floor(t_height / 5)), false);
	//
	// }

	// private Bitmap loadBitmapFromResources(String resourceName, Context cont)
	// {
	//
	// int resId = cont.getResources().getIdentifier(resourceName, "drawable",
	// cont.getPackageName());
	//
	// return BitmapFactory.decodeResource(cont.getResources(), resId);
	// }
}
