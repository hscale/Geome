package tesler.will.geome;

import gnu.trove.list.array.TShortArrayList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.ideaheap.io.VorbisFileInputStream;
import com.ideaheap.io.VorbisFileOutputStream;
import com.ideaheap.io.VorbisInfo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

public class Ogg {

	static String FOLDERPATH = Environment.getExternalStorageDirectory()
			.getAbsolutePath();

	private String recordingId;
	private static String uriPath;

	private TShortArrayList recording = new TShortArrayList();

	static String uriString = "mongodb://wtesler:Violentomega13@ds053138-a0.mongolab.com:53138,ds053138-a1.mongolab.com:53138/pinpoint";
	static MongoClientURI uri = new MongoClientURI(uriString);
	private static MongoClient mc;
	private static DB db;
	private static DBCollection coll;

	public String getPath() {
		return uriPath;
	}

	public void setPath(String newPath) {
		uriPath = newPath;
	}

	// public Ogg() {
	// myPath = "";
	// Log.w("Wav", "Should specify filepath in constructor");
	// }

	public void addData(short[] data) {
		recording.add(data);
	}

	public void clearData() {
		recording.clear();
	}

	// constructor takes a wav path
	public Ogg(String recordingName) {
		this.recordingId = recordingName;
		uriPath = FOLDERPATH + "/" + recordingName;
	}

	// write out the wav file
	public boolean save() {
		try {

			write();

			Toast t = Toast.makeText(Main.cont, "Recording saved in Files",
					Toast.LENGTH_SHORT);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();

			SaveRecordingAsync sra = new SaveRecordingAsync();
			sra.execute();

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}

		return true;
	}

	public void write() throws Exception {

		VorbisInfo vi = new VorbisInfo();
		vi.channels = 1;
		vi.length = recording.size();
		vi.quality = (float) 0;
		vi.sampleRate = 8000;

		Log.i("Ogg", "Creating file at path: " + uriPath);

		VorbisFileOutputStream s = new VorbisFileOutputStream(uriPath, vi);

		short[] data = new short[recording.size()];

		for (int i = 0; i < recording.size(); i++) {
			data[i] = recording.get(i);
		}

		s.write(data);

		s.close();

	}

	public static void write(short[] data) throws Exception {

		VorbisInfo vi = new VorbisInfo();
		vi.channels = 1;
		vi.length = data.length;
		vi.quality = (float) .8;
		vi.sampleRate = 8000;

		VorbisFileOutputStream s = new VorbisFileOutputStream(uriPath, vi);

		s.write(data);

		s.close();

	}

	public short[] read() throws Exception {

		VorbisFileInputStream s = new VorbisFileInputStream(uriPath);

		short[] pcmData = new short[2048];
		Arrays.fill(pcmData, Short.MAX_VALUE);

		TShortArrayList pcmDataList = new TShortArrayList();

		while (s.read(pcmData) != -1) {
			for (short d : pcmData) {

				if (d == Short.MAX_VALUE)
					break;

				pcmDataList.add(d);
			}
		}

		s.close();

		short[] totalData = new short[pcmDataList.size()];
		for (int i = 0; i < pcmDataList.size(); i++)
			totalData[i] = pcmDataList.get(i);

		return totalData;
	}

	class SaveRecordingAsync extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		protected String doInBackground(String... parm) {

			Log.i("SRA", "Saving Recording...");

			if (prepare_database()) {
				try {

					GridFS gridfs = new GridFS(db, "recordings");
					GridFSInputFile gfsFile = gridfs.createFile(new File(
							uriPath));
					gfsFile.setFilename(recordingId);
					gfsFile.save();

					// BasicDBObject recording = new BasicDBObject();
					//
					// recording.append(Keys.AUDIO, data);
					//
					// coll.insert(recording);

					// Save your ID in safekeeping for later
					// recordingId = recording.getObjectId(Keys.ID).toString();

					return "SUCCESS";

				} catch (MongoException me) {

					me.printStackTrace();

					return "FAILURE";
				} catch (Exception e) {
					e.printStackTrace();
					return "FAILURE";
				}
			} else {
				return "FAILURE";
			}
		}

		protected void onPostExecute(String state) {

			clearData();

			if (state.contentEquals("SUCCESS")) {

				GCMMessage.sendAudioPoke(recordingId);

				Log.i("SRA", "Success.");

			} else {
				Log.w("SRA", "Failure!");

				mc = null;
			}

		}
	}

	static class RetrieveRecordingAsync extends
			AsyncTask<String, String, String> {

		String recordingId;

		public RetrieveRecordingAsync(String id) {
			recordingId = id;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		protected String doInBackground(String... parm) {

			Log.i("RRA", "Retrieving Recording...");

			if (prepare_database()) {

				// // Directions to Group
				// try {
				// BasicDBObject query = new BasicDBObject().append(Keys.ID,
				// new ObjectId(recordingId));
				//
				// // Go to Group
				// DBCursor iter = coll.find(query);
				//
				// if (iter != null) {
				//
				// // This is the Rec
				// DBObject recObj = iter.next();
				//
				// BasicDBList recording = (BasicDBList) recObj
				// .get(Keys.AUDIO);
				//
				// short[] data = new short[recording.size()];
				//
				// for (int i = 0; i < recording.size(); i++)
				// data[i] = ((Integer) recording.get(i)).shortValue();
				//
				// write(data, Environment.getExternalStorageDirectory()
				// .getAbsolutePath() + "/oggvorbis.ogg");
				//
				// return "SUCCESS";
				// }
				//
				// } catch (MongoException me) {
				// me.printStackTrace();
				// return "FAILURE";
				// } catch (Exception e) {
				// e.printStackTrace();
				// return "FAILURE";
				// }

				String databaseId = recordingId;
				GridFS gfsPhoto = new GridFS(db, "recordings");
				GridFSDBFile audioFile = gfsPhoto.findOne(databaseId);

				Log.i("Recording ID", recordingId);

				if (audioFile == null)
					Log.w("AudioFile", "NULL");

				try {

					FileOutputStream out = null;
					try {
						out = new FileOutputStream(FOLDERPATH + "/"
								+ recordingId);

						audioFile.writeTo(out);
					} finally {
						if (out != null)
							out.close();
					}

				} catch (IOException e) {
					e.printStackTrace();
				}

				// gfsPhoto.remove(gfsPhoto.findOne(databaseId));

				return "SUCCESS";

			} else {
				Log.w("RRA", "Could not prepare database");
			}
			return "FAILURE";
		}

		protected void onPostExecute(String state) {

			if (state.contentEquals("SUCCESS")) {

				if (Main.inForeground)
					GraphFragment.showGraphDialog(recordingId);
				else {
					MediaPlayer mp = MediaPlayer.create(Main.cont,
							Uri.parse(FOLDERPATH + "/" + recordingId));
					mp.setOnCompletionListener(new OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer mp) {
							mp.reset();
							mp.release();
						}
					});
					mp.start();
				}

				Log.i("RRA", "Success.");

			} else {
				Log.w("RRA", "Failure!");

				mc = null;
			}

		}
	}

	// Make sure database is ready for transaction
	private static Boolean prepare_database() {

		// Open up the collection and begin the I/O Operations
		try {

			if (mc == null) {
				mc = new MongoClient(uri);
			}
			if (db == null) {
				db = mc.getDB("pinpoint");
			}
			if (coll == null) {
				coll = db.getCollection("recordings");
			}
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
	}

}