package tesler.will.geome;

import org.bson.types.ObjectId;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class MicrophoneListener {

	// This acts as the Audio Input Stream. Using read() to get info.
	AudioRecord ar;

	// true when read audio should halt
	boolean shouldStop = false;

	boolean isRunning = false;

	// this gets set by initCorrectAudioRecord
	private int bufferSize;

	// holds the packet at each read() interval
	short[] packet;

	// Reflects changes to the graph
	Handler graphHandler = new Handler();

	// The Graph itself, passed in from Record Fragment
	LineGraphView graph;

	// Container that holds the graph's data
	GraphViewSeries series;

	// Current X position on the graph
	Long curX;

	Ogg ogg;

	// Create the Audio Input Stream and begin reading
	public void start(LineGraphView graph) {

		// Graph
		this.graph = graph;
		series = new GraphViewSeries(new GraphViewData[] {});
		this.graph.addSeries(series);

		// Audio Input Stream
		ar = initCorrectAudioRecord();

		// this stores the received packet at each sample
		packet = new short[bufferSize];

		ogg = new Ogg(ObjectId.get().toString());

		// Audio Input Stream Begin
		ar.startRecording();

		// Begin Reading from the Audio Input Stream
		ReadAudioAsync raa = new ReadAudioAsync();
		raa.execute();

		isRunning = true;

	}

	// Create the Audio Input Stream and begin reading
	public void start() {

		// Audio Input Stream
		ar = initCorrectAudioRecord();

		// this stores the received packet at each sample
		packet = new short[bufferSize];

		ogg = new Ogg(ObjectId.get().toString());

		// Audio Input Stream Begin
		ar.startRecording();

		// Begin Reading from the Audio Input Stream
		ReadAudioAsync raa = new ReadAudioAsync();
		raa.execute();

		isRunning = true;

	}

	public void stop() {
		shouldStop = true;
	}

	// This class has an infinite loop inside of the doInBackground. It uses a
	// blocking operation called read() which happens at a sample rate defined
	// by the AudioRecord
	private class ReadAudioAsync extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(String... arg0) {

			// INFINITE LOOP
			while (true) {

				// Base Case: Stop reading.
				if (shouldStop) {
					if (ar != null) {
						ar.stop();
						ar.release();
						ar = null;
						isRunning = false;
					}
					shouldStop = false;
					break;
				}

				// Read a packet in from the audio capture
				readPacket(packet, 0, bufferSize);

				if (curX == null) {
					curX = System.currentTimeMillis();
				}

				long diff = System.currentTimeMillis() - curX;
				curX += diff;

				if (graph != null) {
					for (int i = 0; i < packet.length;) {

						// Update the Graph with the data from the packet
						double Db = 50 * Math
								.log10((Math.abs(packet[i]) / 500.0));

						// Threshold at which sound is revealed to the graph
						if (Db > 5) {
							updateGraph(new GraphViewData(curX, Db));
						} else {
							updateGraph(new GraphViewData(curX, 0));
						}

						break;
					}
				}

				ogg.addData(packet);

			}

			return "SUCCESS";
		}

		@Override
		protected void onPostExecute(String state) {
			super.onPostExecute(state);
			if (state.contentEquals("SUCCESS")) {
				ogg.save();
			}
			if (state.contentEquals("FAILURE")) {
			}
		}
	}

	//
	public void updateGraph(final GraphViewData data) {
		graphHandler.post(new Runnable() {
			@Override
			public void run() {
				series.appendData(data, true, 100000);
			}
		});
	}

	// Read in a packet into the buffer
	private void readPacket(short[] data, int off, int length) {
		try {
			int read;
			while (length > 0) {
				read = ar.read(data, off, length);
				length -= read;
				off += read;
			}
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
	}

	// Graph Node Object
	class GraphViewData implements GraphViewDataInterface {
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

	// Utility Method: Configures the Microphone correctly, more aptly...
	// Configure The Audio Record to the correct settings for the device and
	// return an Audio Record Object
	// public AudioRecord initCorrectAudioRecord() {
	//
	// bufferSize = AudioRecord.getMinBufferSize(8000, 16, 2);
	//
	// AudioRecord recorder = new AudioRecord(AudioSource.DEFAULT, 8000, 16,
	// 2, bufferSize);
	//
	// if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
	// return recorder;
	// else
	// Log.w("Recorder", "Could not init the recorder");
	//
	// return null;
	// }

	public AudioRecord initCorrectAudioRecord() {
		int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
		for (int rate : mSampleRates) {
			for (short audioFormat : new short[] {
					AudioFormat.ENCODING_PCM_8BIT,
					AudioFormat.ENCODING_PCM_16BIT }) {
				for (short channelConfig : new short[] { 16, 12 }) {
					try {
						Log.d("Determining rate...", "Attempting rate " + rate
								+ "Hz, bits: " + audioFormat + ", channel: "
								+ channelConfig);
						bufferSize = AudioRecord.getMinBufferSize(rate,
								channelConfig, audioFormat);

						if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
							AudioRecord recorder = new AudioRecord(
									AudioSource.DEFAULT, rate, channelConfig,
									audioFormat, bufferSize);

							if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
								return recorder;
						}
					} catch (Exception e) {
						Log.e("Determining rate...", rate
								+ "Exception, keep trying.", e);
					}
				}
			}
		}
		return null;
	}
}
