package myfirstapp.skipbo.activity;

import myfirstapp.skipbo.view.GameView;
//import myfirstapp.skipbo.view.SkipboView;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

/**
 * This launches the game screen
 * @author Jing Tai Wu
 *
 */
public class GameActivity extends Activity {
	//The main activity loads the title view
	//after pressing the button in the title view
	//the application swtiches to game view (in this case the red paint)
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		GameView gView = new GameView(this);
		
		gView.setKeepScreenOn(true);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(gView);
	}
}
