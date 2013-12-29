package tesler.will.geome;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.sromku.simple.fb.Permissions;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebook.OnLoginListener;
import com.sromku.simple.fb.SimpleFacebook.OnLogoutListener;
import com.sromku.simple.fb.SimpleFacebookConfiguration;

public class Login extends Activity {

	SimpleFacebook facebook;

	Permissions[] permissions = new Permissions[] {};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		new Utils(this);

		AnimationDrawable anim = (AnimationDrawable) findViewById(R.id.rl_outer)
				.getBackground();
		anim.start();

		SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
				.setAppId("186056184920382").setNamespace("geome-app")
				.setPermissions(permissions).build();

		SimpleFacebook.setConfiguration(configuration);

	}

	public void login() {
		// login listener
		OnLoginListener onLoginListener = new SimpleFacebook.OnLoginListener() {

			@Override
			public void onFail(String reason) {
				Log.w("Login", reason);
				Utils.toast("Login Failed", Toast.LENGTH_LONG);
			}

			@Override
			public void onException(Throwable throwable) {
				Log.e("Login", "Bad thing happened", throwable);
				Utils.toast("Login Failed", Toast.LENGTH_LONG);
			}

			@Override
			public void onThinking() {
				// show progress bar or something to the user while login is
				// happening
				Log.i("Login", "In progress");
				Utils.toast("Login Failed", Toast.LENGTH_LONG);
			}

			@Override
			public void onLogin() {
				Log.i("Login", "Logged in.");
				startActivity(new Intent(Login.this, Main.class));
				finish();

			}

			@Override
			public void onNotAcceptingPermissions() {
				Log.w("Login", "User didn't accept read permissions");
				Utils.toast("Permissions not accepted.", Toast.LENGTH_LONG);
			}

		};

		// login
		facebook.login(onLoginListener);
	}

	public void logout() {
		// logout listener
		OnLogoutListener onLogoutListener = new SimpleFacebook.OnLogoutListener() {

			@Override
			public void onFail(String reason) {
				Log.w("Logout", reason);
			}

			@Override
			public void onException(Throwable throwable) {
				Log.e("Logout", "Bad thing happened", throwable);
			}

			@Override
			public void onThinking() {
				// show progress bar or something to the user while logout is
				// happening
				Log.i("Logout", "In progress");
			}

			@Override
			public void onLogout() {
				Log.i("Logout", "You are logged out");
			}

		};

		// logout
		facebook.logout(onLogoutListener);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		facebook.onActivityResult(this, requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onResume() {
		super.onResume();
		facebook = SimpleFacebook.getInstance(this);

		if (facebook.isLogin()) {
			startActivity(new Intent(Login.this, Main.class));
			finish();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}
