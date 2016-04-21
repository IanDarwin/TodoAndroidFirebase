package todomore.todoandroidfirebase;

import android.app.Application;
import android.content.res.Resources;
import android.util.Log;

import com.darwinsys.todo.model.Task;
import com.firebase.client.Firebase;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ApplicationClass extends Application {

    public static final String TAG = ApplicationClass.class.getSimpleName();

	/**
	 * Just a cheesy weay to share the list of Tasks
	 */
    public static List<KeyValueHolder<String,Task>> sTasks = new ArrayList<>();
	private Firebase mDatabase;

	@Override
	public void onCreate() {
		super.onCreate();
		Firebase.setAndroidContext(this);
		String baseUrl = getBaseUrl() + TaskListActivity.mCurrentUser + "/tasks/";
		Log.d(TAG, "Firebase base URL is " + baseUrl);
		mDatabase = new Firebase(baseUrl);
	}

	/** The base URL */
	private String mBaseUrl;

	public String getBaseUrl() {
		if (mBaseUrl == null) {
			loadKeys();
		}
		return mBaseUrl;
	}

	public Firebase getDatabase() {
		return mDatabase;
	}

	/**
	 *  Load a Props file from the APK zipped filesystem, extract our base_url from that.
	 */
	private void loadKeys() {
		Resources resources = getResources();
		if (resources == null) {
			throw new ExceptionInInitializerError("getResources() returned null");
		}
			
		try (InputStream is = resources.openRawResource(R.raw.config)) {
			// This is needed to communicate with the server!
			// If this line won't compile, see the instructions in 
			// res/raw/config_sample.properties
			// And do Project->Clean, all the usual stuff...
			if (is == null) {
				Log.w(TAG, "loadKeys: getResources().openRawResource() returned null");
				return;
			}
			Properties p = new Properties();
			p.load(is);
			mBaseUrl = p.getProperty("base_url");
		} catch (Exception e) {
            mBaseUrl = null;
			String message = "Error loading properties: " + e;
			Log.d(TAG, message);
			throw new ExceptionInInitializerError(message);
		} 
		if (mBaseUrl == null) {
			String message = "Could not find 'base_url' in properties file";
			throw new ExceptionInInitializerError(message);
		}
		Log.d(TAG, "Properties loaded OK");
	}
}
