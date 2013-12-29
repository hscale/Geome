package tesler.will.geome;

import java.util.ArrayList;

import tesler.will.geome.MessagesFragment.Message;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageListAdapter extends BaseAdapter {

	private ArrayList<Message> messages;
	Context cont;

	public MessageListAdapter(Context context, ArrayList<Message> items) {

		this.messages = items;

		cont = context;

	}

	public int getCount() {
		return messages.size();
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
			v = vi.inflate(R.layout.row_messages, parent, false);
		}

		v.setId(position);

		final Message message = messages.get(position);

		// NAME
		TextView tv_name = (TextView) v.findViewById(R.id.tv_name);

		tv_name.setText(message.author + ":");

		// NAME
		TextView tv_message = (TextView) v.findViewById(R.id.tv_message);

		tv_message.setText(message.content);

		ImageView pic = (ImageView) v.findViewById(R.id.iv_pic);
		pic.setImageBitmap(MembersFragment.getProfilePic(message.id));

		return v;
	}
}
