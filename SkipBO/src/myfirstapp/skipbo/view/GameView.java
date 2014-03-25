package myfirstapp.skipbo.view;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import myfirstapp.skipbo.activity.R;
import myfirstapp.skipbo.card.Card;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

/**
 * This is the main game screen
 * @author Jing Tai Wu
 *
 */
public class GameView extends View {

	/**
	 * fields and variables
	 */
	private Context myContext;
	
	//deck and discard pile(this discard pile only collects card when a sequence is complete in a build pile
	private List<Card> deck = new LinkedList<Card>();
	private List<Card> discardPile = new LinkedList<Card>();
	
	//Card scale control
	private int scaledCardW;
	private int scaledCardH;
	//screen width and height
	private int screenW;
	private int screenH;
	private float scale;
	
	//Paint for displaying text
	private Paint blackPaint;
	
	private Bitmap cardBack;
	
	//Bitmaps for the end turn button
	private Bitmap endTurnButtonUp;
	private Bitmap endTurnButtonDown;
	private boolean endTurnButtonPressed;
	
	//private boolean myTurn;
	
	//bitmaps for drawing piles
	private Bitmap discardP;
	private Bitmap buildingP;
	
	//hands
	private List<Card> myHand = new LinkedList<Card>();
	private List<Card> oppHand = new LinkedList<Card>();
	//player's discard piles
	//not needed for now
	//private List<Card> myDiscardOne = new LinkedList<Card>();
	//private List<Card> myDiscardTwo = new LinkedList<Card>();
	//private List<Card> myDiscardThree = new LinkedList<Card>();
	//private List<Card> myDiscardFour = new LinkedList<Card>();
	
	//opp's discard piles
	//private List<Card> oppDiscardOne = new LinkedList<Card>();
	//private List<Card> oppDiscardTwo = new LinkedList<Card>();
	//private List<Card> oppDiscardThree = new LinkedList<Card>();
	//private List<Card> oppDiscardFour = new LinkedList<Card>();
	
	//building piles
	//private List<Card> buildPileOne = new LinkedList<Card>();
	//private List<Card> buildPileTwo = new LinkedList<Card>();
	//private List<Card> buildPileThree = new LinkedList<Card>();
	//private List<Card> buildPileFour = new LinkedList<Card>();
	
	//stacks for each player
	private List<Card> myStack = new LinkedList<Card>();
	private List<Card> oppStack = new LinkedList<Card>();
	
	/**
	 * constructor
	 * @param context
	 */
	public GameView(Context context) {
		super(context);
		myContext = context;
		//scales the text size
		scale = myContext.getResources().getDisplayMetrics().density;
		//draw text
		blackPaint = new Paint();
		blackPaint.setAntiAlias(true);
		blackPaint.setColor(Color.BLACK);
		blackPaint.setStyle(Paint.Style.STROKE);
		blackPaint.setTextAlign(Paint.Align.LEFT);
		blackPaint.setTextSize(scale*15);
		//randomly decide who goes first
		//myTurn = new Random().nextBoolean();
	}
	
	/**
	 * onSizeChange Method
	 * This method is called before any other methods
	 */
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		//gets the screen size
        screenW = w;
        screenH = h;
        //initialize the deck
		initCards();
		//deal cards to each player's hand
		myHand = dealCards(myHand);
		oppHand = dealCards(oppHand);
		//add cards to each player's stack
		fillStack();
		
		//loads the backside of the card, discard pile card,and building pile card
		Bitmap tempCardbackBitmap = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.backside);
		Bitmap tempBuildpileBitmap = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.buildpile);
		Bitmap tempDiscardpileBitmap = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.discardpile);
		scaledCardW = (int)	(screenW/6);
		scaledCardH = (int) (scaledCardW * 1.28);
		//scale it to screen size
		cardBack = Bitmap.createScaledBitmap(tempCardbackBitmap, scaledCardW, scaledCardH, false);
		buildingP = Bitmap.createScaledBitmap(tempBuildpileBitmap, scaledCardW, scaledCardH, false);
		discardP = Bitmap.createScaledBitmap(tempDiscardpileBitmap, scaledCardW, scaledCardH, false);
		//loads the end turn buttons
		//button's width is about 3/4 of the screen width
		//height is about the same as the height of the card
		Bitmap tempTurnButton = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.end_turn_up);
		Bitmap tempTurnButtonPressed = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.end_turn_down);
		endTurnButtonUp = Bitmap.createScaledBitmap(tempTurnButton, (int) 2*screenW/5, 3*scaledCardH/4, false);
		endTurnButtonDown = Bitmap.createScaledBitmap(tempTurnButtonPressed, (int) 2*screenW/5, 3*scaledCardH/4, false);
	}
	
	/**
	 * drawCard draws a single card to a designated hand
	 * @param handToDraw
	 */
	private void drawCard(List<Card> handToDraw){
		//draws a card from the top of the deck to one's hand
		handToDraw.add(0, deck.get(0));
		//remove the card that was just drew
		deck.remove(0);
		
		//if deck is empty, draws from discarded deck
		if (deck.isEmpty()){
			for (int i = discardPile.size() - 1; i > 0; i--){
				deck.add(discardPile.get(i));
				discardPile.remove(i);
			}
			//shuffle the deck
			Collections.shuffle(deck, new Random());
		}
	}
	
	/**
	 * deals a maximum of 5 cards to each player
	 */
	private List<Card> dealCards(List<Card> hand){
		//deal 5 cards to each player
		//each player can only have 5 cards in their hands for the beginning of the turn
		List<Card> tempHand = hand;
		int handSize = tempHand.size();
		for (int i = handSize; i < 5; i++){
			drawCard(tempHand);
		}
		return tempHand;
	}
	/**
	 * Method for initializing the deck
	 * a skipbo deck contains twelve cards each for numbers 1 through 12
	 * and eighteen skipbo cards (wild cards)
	 */
	private void initCards(){
		//The outer for-loop loops through cards numbered from 1 to 12
		for (int i = 1; i < 13; i++){
			//The inner for-loops repeats the same card twelve times
			for (int j = 0; j < 12; j++){
				//The ID for skipbo cards are just the values itself
				int tempId = i;
				Card tempCard = new Card(tempId);
				//The getIdentifier has three arguments, (pictureName, folder,packageName)
				int resourceId = getResources().getIdentifier("card" + tempId, "drawable", myContext.getPackageName());
				//draw the Card
				Bitmap tempBitmap = BitmapFactory.decodeResource(myContext.getResources(), resourceId);
				//scale the card
				scaledCardW = (int) (screenW/6);
				scaledCardH = (int)	(scaledCardW * 1.28);
				//put the new scale into a new bitmap
				Bitmap scaledBitmap = Bitmap.createScaledBitmap(tempBitmap,	scaledCardW, scaledCardH, false);
				tempCard.setBitmap(scaledBitmap);
				deck.add(tempCard);
			}
		}
		
		//for-loop for generating skipbo card(wild card)
		//18 wild cards
		for (int x = 0; x < 18; x++){
			//skipbo card has a ID of 13
			int tempId = 13;
			Card tempCard = new Card(tempId);
			int resourceId = getResources().getIdentifier("card" + tempId, "drawable", myContext.getPackageName());
			Bitmap tempBitmap = BitmapFactory.decodeResource(myContext.getResources(), resourceId);
			scaledCardW = (int) (screenW/6);
			scaledCardH = (int)	(scaledCardW * 1.28);
			Bitmap scaledBitmap = Bitmap.createScaledBitmap(tempBitmap,	scaledCardW, scaledCardH, false);
			tempCard.setBitmap(scaledBitmap);
			deck.add(tempCard);
		}
		
		//shuffle the deck
		Collections.shuffle(deck);
	}
	
	/**
	 * Fills each player's stack
	 * This should only be called once
	 */
	private void fillStack(){
		//Each player gets 30 cards in their stack
		//Whoever finishes the stack first wins
		for(int i = 0; i < 30; i++){
			drawCard(myStack);
			drawCard(oppStack);
		}
	}
	
	/**
	 * draws all the contents
	 */
	@Override
	protected void onDraw(Canvas canvas) { 
		//draws the computer score onto the screen
		canvas.drawText("Computer stack size: " + Integer.toString(oppStack.size()), 10, blackPaint.getTextSize() + 10, blackPaint);
		//draws player score onto the screen
		//note that the text will show under player's stack
		String myStackSize = "My stack size: " + Integer.toString(myStack.size());
		canvas.drawText(myStackSize, screenW - (blackPaint.measureText(myStackSize) + 40),(int) (screenH*0.6) + 75 + cardBack.getHeight(), blackPaint);
		//draw cards on player's hands
		for (int i = 0; i < myHand.size(); i++) {
			canvas.drawBitmap(myHand.get(i).getBitmap(), i*(scaledCardW + 10) + (screenW - 5*cardBack.getWidth() - 40)/2, screenH - (cardBack.getHeight() + 20), null);
		}
		//draw the top card of opponent's stack
		//make sure when the stack is empty, it doesn't draw anything
		if(oppStack.size() != 0){
			canvas.drawBitmap(oppStack.get(0).getBitmap(), scale*5, blackPaint.getTextSize() + (20*scale), null);
		}
		//draw the top card of player's stack
		if(myStack.size() != 0){
			canvas.drawBitmap(myStack.get(0).getBitmap(), screenW - myStack.get(0).getBitmap().getWidth() - 50, (int) (screenH*0.6) + 30 ,null);
		}
		
		//draw the discard pile for player
		for (int i = 0; i < 4; i++){
			canvas.drawBitmap(discardP, i*(scaledCardW + 35)+ (screenW - 105 - 4*discardP.getWidth())/2, (int) (screenH * 0.45), null);
		}
		//draw the discard pile for the computer
		for (int i = 0; i < 4; i++){
			canvas.drawBitmap(discardP, i*(scaledCardW + 10) + discardP.getWidth()+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}
		//draw the building pile in the middle
		for (int i = 0; i < 4; i++){
			canvas.drawBitmap(buildingP, i*(scaledCardW + 35)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.25), null);
		}
		
		//draw the end turn button onto the screen
		if (endTurnButtonPressed){
			canvas.drawBitmap(endTurnButtonDown, screenW - endTurnButtonDown.getWidth() - 20, screenH + endTurnButtonDown.getHeight(), null );
		}
		else {
			canvas.drawBitmap(endTurnButtonUp,  screenW - endTurnButtonUp.getWidth() - 20, screenH + endTurnButtonDown.getHeight(), null );
		}
	}
	 
	public boolean onTouchEvent(MotionEvent event) {
		
	int eventAction = event.getAction();
	int x = (int)event.getX();
	int y = (int)event.getY();
	
	switch (eventAction) {
		case MotionEvent.ACTION_DOWN:
			//collision detection for endTurnButton
			if ( x > 400 &&
					 x < (400 + endTurnButtonUp.getWidth()) &&
					 y > (int) (blackPaint.getTextSize() + 30) &&
					 y < (int) (blackPaint.getTextSize() + 30) + endTurnButtonUp.getHeight()){
					endTurnButtonPressed = true;
				}
			break;
		
		case MotionEvent.ACTION_MOVE:
			break;
		
		case MotionEvent.ACTION_UP:
			endTurnButtonPressed = false;
			break;
		}
	invalidate();
	return true;
	}

}
