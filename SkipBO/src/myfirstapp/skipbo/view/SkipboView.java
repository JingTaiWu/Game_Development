package myfirstapp.skipbo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

/**
 * This is a test screen, no longer needed
 * @author Jing Tai Wu
 *
 */
public class SkipboView extends View {

	private Paint redPaint;
	private int circleX;
	private int circleY;
	private float radius;
	
	public SkipboView(Context context) {
		super(context);
		redPaint = new Paint();
		redPaint.setAntiAlias(true);
		redPaint.setColor(Color.RED);
		circleX = 100;
		circleY = 100;
		radius = 30;
	}

	protected void onDraw(Canvas canvas) {
		canvas.drawCircle(circleX, circleY, radius, redPaint);
	}
	
	public boolean onTouchEvent(MotionEvent event){
		int eventAction = event.getAction();
		int x = (int) event.getX();
		int y = (int) event.getY();
		
		switch (eventAction){
		case MotionEvent.ACTION_DOWN:
			this.circleX = x;
			this.circleY = y;
			break;
			
		case MotionEvent.ACTION_UP:
			break;
			
		case MotionEvent.ACTION_MOVE:
			this.circleX = x;
			this.circleY = y;
			break;
			
		}
		
		//switch the back buffer to the front buffer
		invalidate();
		return true;
	}
}
