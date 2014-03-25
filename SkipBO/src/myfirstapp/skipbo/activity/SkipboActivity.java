package myfirstapp.skipbo.activity;

import myfirstapp.skipbo.view.TitleView;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
/**
 * This launches the title screen of the game
 * @author Jing Tai Wu
 *
 */
public class SkipboActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState)
		;
		TitleView tView = new TitleView(this);

		tView.setKeepScreenOn(true);
		
		//remove the title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		//set the windows to full screen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(tView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.skipbo, menu);
		return true;
	}

}
