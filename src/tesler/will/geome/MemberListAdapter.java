package tesler.will.geome;

import java.util.ArrayList;

import tesler.will.geome.MembersFragment.Member;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;

public class MemberListAdapter extends BaseAdapter {

	private ArrayList<Member> members;
	Context cont;

	public MemberListAdapter(Context context, ArrayList<Member> items) {

		this.members = items;

		cont = context;

	}

	public int getCount() {
		return members.size();
	}

	public Object getItem(int arg0) {
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}

	public View getView(int position, View v, ViewGroup parent) {
		if (v == null) {

			// Inflate the Row in the ListView
			LayoutInflater vi = (LayoutInflater) cont
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row_member, parent, false);
		}

		v.setId(position);

		final Member member = members.get(position);

		// NAME
		TextView name = (TextView) v.findViewById(R.id.tv_name);

		name.setText(member.name);

		v.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				try {

				} catch (NullPointerException npe) {
					npe.printStackTrace();
				}
			}
		});

		ProfilePictureView pic = (ProfilePictureView) v
				.findViewById(R.id.iv_pic);
		pic.setProfileId(member.fbid);

		Bitmap defaultPic = BitmapFactory.decodeResource(cont.getResources(),
				R.drawable.default_profile);

		pic.setDefaultProfilePicture(defaultPic);

		return v;
	}
}
