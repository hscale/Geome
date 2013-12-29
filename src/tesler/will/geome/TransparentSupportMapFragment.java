//THIS MAP IS USED INSTEAD OF SUPPORTMAPFRAGMENT BECAUSE IT FIXES A GLITCH WITH A FLOATING BLACK BOX

package tesler.will.geome;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * @author btate
 * 
 */
public class TransparentSupportMapFragment extends SupportMapFragment {

	public TouchableWrapper mTouchView;

	/**
     * 
     */
	public TransparentSupportMapFragment() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup view,
			Bundle savedInstance) {
		View layout = super.onCreateView(inflater, view, savedInstance);

		FrameLayout frameLayout = new FrameLayout(getActivity());
		frameLayout.setBackgroundColor(getResources().getColor(
				android.R.color.transparent));

		mTouchView = new TouchableWrapper(getActivity());
		mTouchView.addView(frameLayout);

		((ViewGroup) layout).addView(mTouchView, new ViewGroup.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return layout;
	}
}