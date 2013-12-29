package tesler.will.geome;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class TouchableWrapper extends FrameLayout {

	public TouchableWrapper(Context context) {
		super(context);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// if (!Global.tasker.visible && !Global.performance.visible
			// && !Global.profile.visible && !Global.overlay.visible
			// && !Global.explorer.visible) {
			// Global.map.wasManualPress = false;
			// Global.map.auto_manual.setChecked(false);
			// }
			break;
		case MotionEvent.ACTION_UP:
			// MainActivity.mMapIsTouched = false;
			break;
		}
		return super.dispatchTouchEvent(event);
	}
}
