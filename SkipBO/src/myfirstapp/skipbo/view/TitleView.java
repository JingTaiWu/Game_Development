package myfirstapp.skipbo.view;

import myfirstapp.skipbo.activity.GameActivity;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * This is the view class for title screen
 * @author Jing Tai Wu
 *
 */

public class TitleView extends View {
	
	private Bitmap titleGraphic;
	private Bitmap playButtonUp;
	private Bitmap playButtonDown;
	private Bitmap optionButtonUp;
	private Bitmap optionButtonDown;
	private int screenW;
	private int screenH;
	private boolean playButtonPressed;
	private boolean optionButtonPressed;
	private Context myContext;
	
	public TitleView(Context context) {
		super(context);
		//gets the resources from drawable and stores them into bitmaps
		myContext = context;
		titleGraphic = BitmapFactory.decodeResource(getResources(),myfirstapp.skipbo.activity.R.drawable.title_graphic);
		playButtonUp = BitmapFactory.decodeResource(getResources(),myfirstapp.skipbo.activity.R.drawable.play_button_up);
		playButtonDown = BitmapFactory.decodeResource(getResources(),myfirstapp.skipbo.activity.R.drawable.play_button_down);
		optionButtonUp = BitmapFactory.decodeResource(getResources(),myfirstapp.skipbo.activity.R.drawable.option_button_up);
		optionButtonDown = BitmapFactory.decodeResource(getResources(),myfirstapp.skipbo.activity.R.drawable.option_button_down);
		
	}
	
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		screenW = w;
		screenH = h;
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		canvas.drawBitmap(titleGraphic, (screenW - titleGraphic.getWidth())/2, (int) (screenH*0.05), null);
		
		//draws the play button depending on user's input
		if(playButtonPressed){
			canvas.drawBitmap(playButtonDown, (screenW - playButtonDown.getWidth())/2, (int) (screenH*0.6), null);
			}
		else {
			canvas.drawBitmap(playButtonUp, (screenW - playButtonUp.getWidth())/2, (int) (screenH*0.6), null);
		}
		
		//draws the option button depending on user's input
		if(optionButtonPressed){
			canvas.drawBitmap(optionButtonDown, (screenW - optionButtonDown.getWidth())/2, (int) (screenH*0.8), null);
			}
		else {
			canvas.drawBitmap(optionButtonUp, (screenW - optionButtonUp.getWidth())/2, (int) (screenH*0.8), null);
		}
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		int eventAction = event.getAction();
		int x = (int)event.getX();
		int y = (int)event.getY();
		switch (eventAction) {
			case MotionEvent.ACTION_DOWN:
				//actions for pressing the play button
				if ( x > ((screenW - playButtonUp.getWidth())/2) &&
					 x < ((screenW - playButtonUp.getWidth())/2 + playButtonUp.getWidth()) &&
					 y > (int) (screenH*0.6) &&
					 y < (int) (screenH*0.6) + playButtonUp.getHeight()){
					playButtonPressed = true;
				}
				
				//actions for pressing the option button
				if ( x > ((screenW - optionButtonUp.getWidth())/2) &&
						 x < ((screenW - optionButtonUp.getWidth())/2 + optionButtonUp.getWidth()) &&
						 y > (int) (screenH*0.8) &&
						 y < (int) (screenH*0.8) + optionButtonUp.getHeight()){
						optionButtonPressed = true;
					}
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
				//activity triggers after pressing the play button
				if(playButtonPressed){
					Intent gameIntent = new Intent(myContext, GameActivity.class);
					myContext.startActivity(gameIntent);
				}
				playButtonPressed = false;
				
				//activity after pressing the option button
				optionButtonPressed = false;
				break;
		}
		invalidate();
		return true;
		}
}
