package tesler.will.geome;

import java.util.ArrayList;

import tesler.will.geome.GroupsPane.Group;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GroupListAdapter extends BaseAdapter {

	private ArrayList<Group> groups;
	Context cont;
	GroupsPane gPane;

	public GroupListAdapter(Context context, GroupsPane groupsPane,
			ArrayList<Group> items) {

		this.groups = items;

		this.gPane = groupsPane;

		cont = context;

	}

	public int getCount() {
		return groups.size();
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
			v = vi.inflate(R.layout.row_group, parent, false);
		}

		v.setId(position);

		final Group group = groups.get(position);

		// NAME
		TextView name = (TextView) v.findViewById(R.id.tv_name);

		name.setText(group.name);

		v.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {

				try {

					GroupsPane.curGroup = group;

					v.setBackgroundColor(cont.getResources().getColor(
							R.color.main_blue));

					Handler h = new Handler();
					h.postDelayed(new Runnable() {
						@Override
						public void run() {
							v.setBackgroundResource(0);
						}
					}, 3000);

					GroupsPane.beginSession();

				} catch (NullPointerException npe) {
					npe.printStackTrace();
				}
			}
		});

		return v;
	}
}
