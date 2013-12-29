package tesler.will.geome;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;

public class GraphFragment extends DialogFragment {

	static short[] data;

	static Ogg ogg;

	static LineGraphView graph;

	LinearLayout graphLayout;

	static GraphViewSeries series;

	static Handler graphHandler = new Handler();

	static Runnable r;

	static MediaPlayer mp;

	static int curX = 0;

	static boolean oomeOccurred = false;

	static boolean isShowing = false;

	/**
	 * Create a new instance of MyDialogFragment, providing "num" as an
	 * argument.
	 */
	static GraphFragment newInstance(int num, String fileName) {

		GraphFragment f = new GraphFragment();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putInt("num", num);
		f.setArguments(args);

		ogg = new Ogg(fileName);

		setupGraphHandler();

		setupMediaPlayer(Ogg.FOLDERPATH + "/" + fileName);

		return f;
	}

	public GraphFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		pickStyle(getArguments().getInt("num"));

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rv = inflater.inflate(R.layout.dialog_graph, container, false);

		// Graph
		graph = new LineGraphView(Main.cont, "Transmission");
		graph.setScrollable(true);
		graph.setViewPort(1, 1000);
		graph.setScalable(true);
		graph.setHorizontalLabels(new String[] { "Time" });
		graph.setVerticalLabels(new String[] { "Noise" });
		GraphViewStyle style = graph.getGraphViewStyle();
		style.setVerticalLabelsWidth(20);
		style.setNumHorizontalLabels(10);
		style.setTextSize(12);
		graph.setGraphViewStyle(style);
		graphLayout = (LinearLayout) rv.findViewById(R.id.ll_graph);
		graphLayout.addView(graph);
		series = new GraphViewSeries(new GraphViewData[] {});
		graph.addSeries(series);

		try {
			data = ogg.read();
		} catch (OutOfMemoryError oome) {
			oome.printStackTrace();
			oomeOccurred = true;
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}

		isShowing = true;

		return rv;
	}

	@Override
	public void onPause() {
		super.onPause();

		curX = 0;
		data = null;
		ogg = null;
		graph = null;
		series = null;

		isShowing = false;

		if (mp != null && mp.isPlaying()) {
			mp.stop();
			resetMediaPlayer();
		}

		System.gc();

		this.dismiss();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (oomeOccurred) {
			Toast.makeText(Main.cont,
					"Wait a moment while Android frees some space.",
					Toast.LENGTH_LONG).show();
			this.dismiss();
			oomeOccurred = false;
			return;
		}

		mp.start();
		graphHandler.post(r);
	}

	static void resetMediaPlayer() {
		mp.reset();
		mp.release();
		mp = null;
	}

	public static void setupMediaPlayer(String uri) {
		mp = MediaPlayer.create(Main.cont, Uri.parse(uri));
		mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				graphHandler.removeCallbacks(r);
			}
		});
	}

	static int counter = 0;

	public static void setupGraphHandler() {
		graphHandler = new Handler();
		r = new Runnable() {
			@Override
			public void run() {

				try {
					int position = (int) (((double) mp.getCurrentPosition() / mp
							.getDuration()) * data.length);

					// Update the Graph with the data from the packet
					double Db = 50 * Math
							.log10((Math.abs(data[position]) / 500.0));

					// Threshold at which sound is revealed to the graph
					if (Db > 0) {
						series.appendData(new GraphViewData(curX, Db), true,
								100000);
					} else {
						series.appendData(new GraphViewData(curX, 0), true,
								100000);
					}

					curX += 15;

					graphHandler.postDelayed(this, 15);
				} catch (Exception e) {
					Log.w("MediaPlayer Handler", "Stopping");
					// sb_position.setProgress(data.length);
				}
			}
		};
	}

	static class GraphViewData implements GraphViewDataInterface {
		long x;
		double y;

		public GraphViewData(long x, double y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public double getX() {
			return x;
		}

		@Override
		public double getY() {
			return y;
		}

	}

	private void pickStyle(int num) {
		// Pick a style based on the num.
		int style = DialogFragment.STYLE_NORMAL, theme = 0;
		switch ((num - 1) % 6) {
		case 1:
			style = DialogFragment.STYLE_NO_TITLE;
			break;
		case 2:
			style = DialogFragment.STYLE_NO_FRAME;
			break;
		case 3:
			style = DialogFragment.STYLE_NO_INPUT;
			break;
		case 4:
			style = DialogFragment.STYLE_NORMAL;
			break;
		case 5:
			style = DialogFragment.STYLE_NORMAL;
			break;
		case 6:
			style = DialogFragment.STYLE_NO_TITLE;
			break;
		case 7:
			style = DialogFragment.STYLE_NO_FRAME;
			break;
		case 8:
			style = DialogFragment.STYLE_NORMAL;
			break;
		}

		setStyle(style, theme);
	}

	static void showGraphDialog(String recordingId) {

		// DialogFragment.show() will take care of adding the fragment
		// in a transaction. We also want to remove any currently showing
		// dialog, so make our own transaction and take care of that here.
		FragmentTransaction ft = Main.activity.getSupportFragmentManager()
				.beginTransaction();
		Fragment prev = Main.activity.getSupportFragmentManager()
				.findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		DialogFragment newFragment = GraphFragment.newInstance(8, recordingId);
		newFragment.show(ft, "dialog");
	}

}
