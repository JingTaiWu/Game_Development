package myfirstapp.skipbo.view;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import myfirstapp.skipbo.activity.R;
import myfirstapp.skipbo.card.Card;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

	//bitmaps for drawing piles
	private Bitmap discardP;
	private Bitmap buildingP;

	//hands
	private List<Card> myHand = new LinkedList<Card>();
	private List<Card> oppHand = new LinkedList<Card>();

	//player's discard piles
	private List<Card> myDiscardOne = new LinkedList<Card>();
	private List<Card> myDiscardTwo = new LinkedList<Card>();
	private List<Card> myDiscardThree = new LinkedList<Card>();
	private List<Card> myDiscardFour = new LinkedList<Card>();

	//opp's discard piles
	private List<Card> oppDiscardOne = new LinkedList<Card>();
	private List<Card> oppDiscardTwo = new LinkedList<Card>();
	private List<Card> oppDiscardThree = new LinkedList<Card>();
	private List<Card> oppDiscardFour = new LinkedList<Card>();

	//building piles
	private List<Card> buildPileOne = new LinkedList<Card>();
	private List<Card> buildPileTwo = new LinkedList<Card>();
	private List<Card> buildPileThree = new LinkedList<Card>();
	private List<Card> buildPileFour = new LinkedList<Card>();

	//stacks for each player
	private List<Card> myStack = new LinkedList<Card>();
	private List<Card> oppStack = new LinkedList<Card>();

	//control the card numbers/amount of duplicates (for testing)
	private int maxCardNum = 12;
	private int cardDup = 12;
	private int stackSize = 30;

	//card movement variables
	private int movingCardIndex = -1;
	private int movingX;
	private int movingY;
	private boolean stackCardSelected;
	private boolean discardOneSelected;
	private boolean discardTwoSelected;
	private boolean discardThreeSelected;
	private boolean discardFourSelected;
	//card constraints
	private int validRankOne = 1;
	private int validRankTwo = 1;
	private int validRankThree = 1;
	private int validRankFour = 1;

	//Turn management
	private boolean discardNow = false;
	private boolean myTurn;

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

		//test purpose, all skipbo card
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

		//Turn
		myTurn = true;

		if(!myTurn){
			makeComputerPlay();
		}
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
			Collections.shuffle(discardPile);
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
		for (int i = 1; i <= maxCardNum; i++){
			//The inner for-loops repeats the same card twelve times
			for (int j = 0; j <= cardDup; j++){
				//The ID for skipbo cards are just the values itself
				int tempId = i;
				Card tempCard = new Card(tempId);
				//The getIdentifier has three arguments, (pictureName, folder,packageName)
				//----------------------------------------------------------------TEST
				//int resourceId = getResources().getIdentifier("card" + 13, "drawable", myContext.getPackageName());
				//-------------------------------------------------------------------
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
		for(int i = 0; i < stackSize; i++){
			drawCard(myStack);
			drawCard(oppStack);
		}
	}

	/**
	 * draws all the contents
	 */
	@Override
	protected void onDraw(Canvas canvas) { 
		//draws the computer's stack size onto the screen
		canvas.drawText("Computer stack size: " + Integer.toString(oppStack.size()), 10, blackPaint.getTextSize() + 10, blackPaint);
		//draws player stack size onto the screen
		//note that the text will show under player's stack
		String myStackSize = "My stack size: " + Integer.toString(myStack.size());
		canvas.drawText(myStackSize, screenW - (blackPaint.measureText(myStackSize) + 40),(int) (screenH*0.6) + 75 + cardBack.getHeight(), blackPaint);

		//draw the top card of opponent's stack
		//make sure when the stack is empty, it doesn't draw anything
		if(oppStack.size() != 0){
			canvas.drawBitmap(oppStack.get(0).getBitmap(), scale*5, blackPaint.getTextSize() + (20*scale), null);
		}

		//draw the discard pile for the computer
		//same as the player discard pile

		//discard pile 1
		if (oppDiscardOne.isEmpty()){
			canvas.drawBitmap(discardP, 0*(scaledCardW + 20) + discardP.getWidth()+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}
		//if it is not empty, draw the last card of the discard pile
		else {
			canvas.drawBitmap(oppDiscardOne.get(oppDiscardOne.size() - 1).getBitmap(), 0*(scaledCardW + 20) + discardP.getWidth()+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}

		//discard pile 2
		if (oppDiscardTwo.isEmpty()){
			canvas.drawBitmap(discardP, 1*(scaledCardW + 20) + discardP.getWidth()+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}

		else {
			canvas.drawBitmap(oppDiscardTwo.get(oppDiscardTwo.size() - 1).getBitmap(), 1*(scaledCardW + 20) + discardP.getWidth()+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}

		//discard pile 3
		if (oppDiscardThree.isEmpty()){
			canvas.drawBitmap(discardP, 2*(scaledCardW + 20) + discardP.getWidth()+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}

		else {
			canvas.drawBitmap(oppDiscardThree.get(oppDiscardThree.size() - 1).getBitmap(), 2*(scaledCardW + 20) + discardP.getWidth()+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}
		
		//discard pile 4
		if (oppDiscardFour.isEmpty()){
			canvas.drawBitmap(discardP, 3*(scaledCardW + 20) + discardP.getWidth()+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}

		else {
			canvas.drawBitmap(oppDiscardFour.get(oppDiscardThree.size() - 1).getBitmap(), 3*(scaledCardW + 20) + discardP.getWidth()+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}
		//end drawing discard pile for the computer


		//draw the building pile in the middle
		if (buildPileOne.isEmpty()){
			canvas.drawBitmap(buildingP, 0*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.25), null);
		}
		else {
			canvas.drawBitmap(buildPileOne.get(buildPileOne.size() - 1).getBitmap(), 0*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.25), null);
			canvas.drawText("Next #: " + (validRankOne), 0*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.245), blackPaint);
		}

		if (buildPileTwo.isEmpty()){
			canvas.drawBitmap(buildingP, 1*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.25), null);
		}
		else {
			canvas.drawBitmap(buildPileTwo.get(buildPileTwo.size() - 1).getBitmap(), 1*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.25), null);
			canvas.drawText("Next #: " + (validRankTwo), 1*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.245), blackPaint);
		}

		if (buildPileThree.isEmpty()){
			canvas.drawBitmap(buildingP, 2*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.25), null);
		}
		else {
			canvas.drawBitmap(buildPileThree.get(buildPileThree.size() - 1).getBitmap(), 2*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.25), null);
			canvas.drawText("Next #: " + (validRankThree), 2*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.245), blackPaint);
		}

		if (buildPileFour.isEmpty()){
			canvas.drawBitmap(buildingP, 3*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.25), null);
		}
		else {
			canvas.drawBitmap(buildPileFour.get(buildPileFour.size() - 1).getBitmap(),3*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.25), null);
			canvas.drawText("Next #: " + (validRankFour), 3*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.245), blackPaint);
		}		
		//end drawing building pile

		//draw the discard pile for player
		//if the first discard pile is empty, draw the "Discard pile card"
		//must do this for all the discard pile (I don't know if there is a more efficient way of doing this)
		//discard pile one
		if (myDiscardOne.isEmpty()){
			canvas.drawBitmap(discardP, 0*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2, (int) (screenH * 0.45), null);
		}
		//if it is not empty, draw the last card of the discard pile
		else {
			if(discardOneSelected){
				canvas.drawBitmap(myDiscardOne.get(myDiscardOne.size() - 1).getBitmap(), movingX, movingY, null);
			}
			else {
				canvas.drawBitmap(myDiscardOne.get(myDiscardOne.size() - 1).getBitmap(), 0*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2, (int) (screenH * 0.45), null);
			}
		}

		//discard pile two
		if (myDiscardTwo.isEmpty()){
			canvas.drawBitmap(discardP, 1*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2, (int) (screenH * 0.45), null);
		}

		else {
			if(discardTwoSelected){
				canvas.drawBitmap(myDiscardTwo.get(myDiscardTwo.size() - 1).getBitmap(), movingX, movingY, null);
			}
			else {
				canvas.drawBitmap(myDiscardTwo.get(myDiscardTwo.size() - 1).getBitmap(), 1*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2, (int) (screenH * 0.45), null);
			}
		}

		//discard pile three
		if (myDiscardThree.isEmpty()){
			canvas.drawBitmap(discardP, 2*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2, (int) (screenH * 0.45), null);
		}

		else {
			if(discardThreeSelected){
				canvas.drawBitmap(myDiscardThree.get(myDiscardThree.size() - 1).getBitmap(), movingX, movingY, null);
			}
			else {
				canvas.drawBitmap(myDiscardThree.get(myDiscardThree.size() - 1).getBitmap(), 2*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2, (int) (screenH * 0.45), null);
			}
		}

		//discard pile four
		if (myDiscardFour.isEmpty()){
			canvas.drawBitmap(discardP, 3*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2, (int) (screenH * 0.45), null);
		}

		else {
			if(discardFourSelected){
				canvas.drawBitmap(myDiscardFour.get(myDiscardFour.size() - 1).getBitmap(), movingX, movingY, null);
			}
			else {
				canvas.drawBitmap(myDiscardFour.get(myDiscardFour.size() - 1).getBitmap(), 3*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2, (int) (screenH * 0.45), null);
			}
		}

		//End drawing for player discard files

		//draw the end turn button onto the screen
		if (endTurnButtonPressed){
			canvas.drawBitmap(endTurnButtonDown, (int)(screenW/6), (int) (screenH*0.67), null );
		}
		else {
			canvas.drawBitmap(endTurnButtonUp, (int)(screenW/6), (int) (screenH*0.67), null );
		}

		//draw the moving cards when the player selects
		if(!myHand.isEmpty()){
			for (int i = 0; i < myHand.size(); i++){
				if(i == movingCardIndex){
					//if the card is selected, in the onTouchEvent, the coordinates should be assigned to movingX and movingY
					canvas.drawBitmap(myHand.get(i).getBitmap(), movingX, movingY, null);
				}
				else{
					canvas.drawBitmap(myHand.get(i).getBitmap(), i*(scaledCardW + 10) + (screenW - 5*cardBack.getWidth() - 40)/2, screenH - (cardBack.getHeight() + 20), null);
				}
			}
		}
		//draw the top card of player's stack
		if(myStack.size() != 0){
			//if the stack card is selected. paint the card according to the moving coordinate
			if(stackCardSelected){
				canvas.drawBitmap(myStack.get(0).getBitmap(), movingX, movingY ,null);
			}
			//if not, paint it at the original position
			else{
				canvas.drawBitmap(myStack.get(0).getBitmap(), screenW - myStack.get(0).getBitmap().getWidth() - 50, (int) (screenH*0.6) + 30 ,null);
			}
		}

		//refresh the buffer
		invalidate();
	}

	public boolean onTouchEvent(MotionEvent event) {

		int eventAction = event.getAction();
		int x = (int)event.getX();
		int y = (int)event.getY();

		switch (eventAction) {
		case MotionEvent.ACTION_DOWN:
			//collision detection for picking up cards from player's hand
			if(myTurn){
				//collision detection for endTurnButton
				//when the player press the button, dialog shows up
				if ( x > (int)(screenW/6) &&
						x < ((int)(screenW/6) + endTurnButtonUp.getWidth()) &&
						y > (int) (screenH*0.67) &&
						y < (int) (screenH*0.67) + endTurnButtonUp.getHeight()){
					endTurnButtonPressed = true;
					showEndTurnDialog();
				}
				//hand cards
				for(int i = 0; i < 5; i++){
					//setting up the variables for where the card's bitmap starts and ends in terms of X and Y
					int cardStartX = i*(scaledCardW + 10) + (screenW - 5*cardBack.getWidth() - 40)/2;
					int cardEndX = i*(scaledCardW + 10) + (screenW - 5*cardBack.getWidth() - 40)/2 + scaledCardW;
					int cardStartY = screenH - (cardBack.getHeight() + 20);
					int cardEndY = screenH - (cardBack.getHeight() + 20) + scaledCardH;
					if( x > cardStartX && x < cardEndX &&
							y > cardStartY && y < cardEndY){
						movingCardIndex = i;
						//adjust the position of the card so that player
						//can see it
						movingX = x - (int) (30*scale);
						movingY = y - (int) (70*scale);
					}
					//stack card movement
					int stackCardX = screenW - scaledCardW - 50;
					int stackCardY = (int) (screenH*0.6) + 30;
					if ( x > stackCardX && x < stackCardX + scaledCardW &&
							y > stackCardY && y < stackCardY + scaledCardH){
						stackCardSelected = true;
						movingX = x - (int) (30*scale);
						movingY = y - (int) (70*scale);
					}

					//discard pile movement

					//discard pile one
					if ( x > 0*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2 &&
							x < 0*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2 + scaledCardW &&
							y > (int) (screenH *0.45) &&
							y < (int) (screenH *0.45 + scaledCardH)){
						discardOneSelected = true;
						movingX = x - (int) (30*scale);
						movingY = y - (int) (70*scale);
					}

					//discard pile two
					if ( x > 1*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2 &&
							x < 1*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2 + scaledCardW &&
							y > (int) (screenH *0.45) &&
							y < (int) (screenH *0.45 + scaledCardH)){
						discardTwoSelected = true;
						movingX = x - (int) (30*scale);
						movingY = y - (int) (70*scale);
					}

					//discard pile three
					if ( x > 2*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2 &&
							x < 2*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2 + scaledCardW &&
							y > (int) (screenH *0.45) &&
							y < (int) (screenH *0.45 + scaledCardH)){
						discardThreeSelected = true;
						movingX = x - (int) (30*scale);
						movingY = y - (int) (70*scale);
					}

					//discard pile four
					if ( x > 3*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2 &&
							x < 3*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2 + scaledCardW &&
							y > (int) (screenH *0.45) &&
							y < (int) (screenH *0.45 + scaledCardH)){
						discardFourSelected = true;
						movingX = x - (int) (30*scale);
						movingY = y - (int) (70*scale);
					}

					//end collision detection for discard piles
				}
			}

			//when the player press the discard button, they need to discard a card
			//they can only discard a card from their hand
			if(discardNow){
				for(int i = 0; i < 5; i++){
					//setting up the variables for where the card's bitmap starts and ends in terms of X and Y
					int cardStartX = i*(scaledCardW + 10) + (screenW - 5*cardBack.getWidth() - 40)/2;
					int cardEndX = i*(scaledCardW + 10) + (screenW - 5*cardBack.getWidth() - 40)/2 + scaledCardW;
					int cardStartY = screenH - (cardBack.getHeight() + 20);
					int cardEndY = screenH - (cardBack.getHeight() + 20) + scaledCardH;
					if( x > cardStartX && x < cardEndX &&
							y > cardStartY && y < cardEndY){
						movingCardIndex = i;
						//adjust the position of the card so that player
						//can see it
						movingX = x - (int) (30*scale);
						movingY = y - (int) (70*scale);
					}
				}
			}
			break;

		case MotionEvent.ACTION_MOVE:
			movingX = x - (int) (30*scale);
			movingY = y - (int) (70*scale);
			break;

		case MotionEvent.ACTION_UP:
			//TODO: there are two building piles and two discard piles
			//check constraints for building piles 1,2,3,4
			if(myTurn){
				//This code segment checks the hand cards against all four build piles//
				//BEGINS----------------------------------------------------------------------------------
				//Checks the selected hand card against the first build pile
				//---------------------------------------------
				if (movingCardIndex > -1 &&
						x > (0*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
						x < (0*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
						y > ((int) (screenH*0.25)) &&
						y < ((int) (screenH*0.25) + scaledCardH) &&
						((myHand.get(movingCardIndex).getId() == validRankOne) ||
								myHand.get(movingCardIndex).getId() == 13)){

					validRankOne += 1;
					buildPileOne.add(myHand.get(movingCardIndex));
					myHand.remove(movingCardIndex);
					//if the build pile reaches maximum sequence, discard all cards to discard deck
					//reset valid rank to 1
					if(buildPileOne.size() >= maxCardNum){	
						for (int i = buildPileOne.size() - 1; i >= 0; i--){
							discardPile.add(buildPileOne.get(i));
							buildPileOne.remove(i);
						}
						validRankOne = 1;
					}
				}		//END Checking Build Pile One
				//-----------------------------------------------

				//checks the selected hand card against the second build pile
				//-----------------------------------------------
				else if (movingCardIndex > -1 &&
						x > (1*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
						x < (1*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
						y > ((int) (screenH*0.25)) &&
						y < ((int) (screenH*0.25) + scaledCardH) &&
						((myHand.get(movingCardIndex).getId() == validRankTwo) ||
								myHand.get(movingCardIndex).getId() == 13)){

					validRankTwo += 1;
					buildPileTwo.add(myHand.get(movingCardIndex));
					myHand.remove(movingCardIndex);

					//discarding the pile
					if(buildPileTwo.size() >= maxCardNum){	
						for (int i = buildPileTwo.size() - 1; i >= 0; i--){
							discardPile.add(buildPileTwo.get(i));
							buildPileTwo.remove(i);
						}
						validRankTwo = 1;
					}
				}	//END Checking build pile two
				//----------------------------------------------

				//Checks the selected hand card against the third build pile
				//----------------------------------------------
				else if (movingCardIndex > -1 &&
						x > (2*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
						x < (2*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
						y > ((int) (screenH*0.25)) &&
						y < ((int) (screenH*0.25) + scaledCardH) &&
						((myHand.get(movingCardIndex).getId() == validRankThree) ||
								myHand.get(movingCardIndex).getId() == 13)){

					validRankThree += 1;
					buildPileThree.add(myHand.get(movingCardIndex));
					myHand.remove(movingCardIndex);

					//discarding the pile
					if(buildPileThree.size() >= maxCardNum){	
						for (int i = buildPileThree.size() - 1; i >= 0; i--){
							discardPile.add(buildPileThree.get(i));
							buildPileThree.remove(i);
						}
						validRankThree = 1;
					}
				}	//End checking build pile three
				//-----------------------------------------------

				//Checks the selected hand card against the forth build pile
				else if (movingCardIndex > -1 &&
						x > (3*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
						x < (3*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
						y > ((int) (screenH*0.25)) &&
						y < ((int) (screenH*0.25) + scaledCardH) &&
						((myHand.get(movingCardIndex).getId() == validRankFour) ||
								myHand.get(movingCardIndex).getId() == 13)){

					validRankFour += 1;
					buildPileFour.add(myHand.get(movingCardIndex));
					myHand.remove(movingCardIndex);

					//discarding the pile
					if(buildPileFour.size() >= maxCardNum){	
						for (int i = buildPileFour.size() - 1; i >= 0; i--){
							discardPile.add(buildPileFour.get(i));
							buildPileFour.remove(i);
						}
						validRankFour = 1;
					}
				}	//End checking build pile four
				//-----------------------------------------------
				//end checking constraints for hand card

				//check constraints for discard piles
				//TODO: check constraints for discard one pile against build piles

				//First discard pile against first build pile
				//-------------------------------------------------------
				if(discardOneSelected){
					if(x > ((screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < ((screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myDiscardOne.get(myDiscardOne.size() - 1).getId() == 13 ||
							myDiscardOne.get(myDiscardOne.size() - 1).getId() == validRankOne)){

						validRankOne += 1;
						buildPileOne.add(myDiscardOne.get(myDiscardOne.size() - 1));
						myDiscardOne.remove(myDiscardOne.size() - 1);

						//discarding the build pile
						if(buildPileOne.size() >= maxCardNum){	
							for (int i = buildPileOne.size() - 1; i >= 0; i--){
								discardPile.add(buildPileOne.get(i));
								buildPileOne.remove(i);
							}
							validRankOne = 1;
						}
					}
					//end checking 1
					//---------------------------------------------------------

					//check against build pile two
					//---------------------------------------------------------
					else if(x > (1*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < (1*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myDiscardOne.get(myDiscardOne.size() - 1).getId() == 13 ||
							myDiscardOne.get(myDiscardOne.size() - 1).getId() == validRankTwo)){

						validRankTwo += 1;
						buildPileTwo.add(myDiscardOne.get(myDiscardOne.size() - 1));
						myDiscardOne.remove(myDiscardOne.size() - 1);

						//discarding the build pile
						if(buildPileTwo.size() >= maxCardNum){	
							for (int i = buildPileTwo.size() - 1; i >= 0; i--){
								discardPile.add(buildPileTwo.get(i));
								buildPileTwo.remove(i);
							}
							validRankTwo = 1;
						}
					}
					//end checking 2
					//------------------------------------------------------------

					//checking 3
					//------------------------------------------------------------
					else if(x > (2*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < (2*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myDiscardOne.get(myDiscardOne.size() - 1).getId() == 13 ||
							myDiscardOne.get(myDiscardOne.size() - 1).getId() == validRankThree)){

						validRankThree += 1;
						buildPileThree.add(myDiscardOne.get(myDiscardOne.size() - 1));
						myDiscardOne.remove(myDiscardOne.size() - 1);

						//discarding the build pile
						if(buildPileThree.size() >= maxCardNum){	
							for (int i = buildPileThree.size() - 1; i >= 0; i--){
								discardPile.add(buildPileThree.get(i));
								buildPileThree.remove(i);
							}
							validRankThree = 1;
						}
					}
					//end checking 3
					//------------------------------------------------------------

					//checking 4
					else if(x > (3*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < (3*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myDiscardOne.get(0).getId() == 13 ||
							myDiscardOne.get(0).getId() == validRankFour)){

						validRankFour += 1;
						buildPileFour.add(myDiscardOne.get(myDiscardOne.size() - 1));
						myDiscardOne.remove(myDiscardOne.size() - 1);

						//discarding the build pile
						if(buildPileFour.size() >= maxCardNum){	
							for (int i = buildPileFour.size() - 1; i >= 0; i--){
								discardPile.add(buildPileFour.get(i));
								buildPileFour.remove(i);
							}
							validRankFour = 1;
						}
					}
				}
				//end checking 4
				//--------------------------------------------------
				//end checking discard pile one
				//----------------------------------------------------------

				//TODO: discard two against build piles
				if(discardTwoSelected){
					//check build pile one
					//------------------------------------------------------
					if(x > ((screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < ((screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myDiscardTwo.get(myDiscardTwo.size() - 1).getId() == 13 ||
							myDiscardTwo.get(myDiscardTwo.size() - 1).getId() == validRankOne)){

						validRankOne += 1;
						buildPileOne.add(myDiscardTwo.get(myDiscardTwo.size() - 1));
						myDiscardTwo.remove(myDiscardTwo.size() - 1);

						//discarding the build pile
						if(buildPileOne.size() >= maxCardNum){	
							for (int i = buildPileOne.size() - 1; i >= 0; i--){
								discardPile.add(buildPileOne.get(i));
								buildPileOne.remove(i);
							}
							validRankOne = 1;
						}
					}
					//end checking 1
					//---------------------------------------------

					//check against build pile two
					//---------------------------------------------
					else if(x > (1*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < (1*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myDiscardTwo.get(myDiscardTwo.size() - 1).getId() == 13 ||
							myDiscardTwo.get(myDiscardTwo.size() - 1).getId() == validRankTwo)){

						validRankTwo += 1;
						buildPileTwo.add(myDiscardTwo.get(myDiscardTwo.size() - 1));
						myDiscardTwo.remove(myDiscardTwo.size() - 1);

						//discarding the build pile
						if(buildPileTwo.size() >= maxCardNum){	
							for (int i = buildPileTwo.size() - 1; i >= 0; i--){
								discardPile.add(buildPileTwo.get(i));
								buildPileTwo.remove(i);
							}
							validRankTwo = 1;
						}
					}
					//end checking 2
					//---------------------------------------------

					//checking 3
					//---------------------------------------------
					else if(x > (2*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < (2*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myDiscardTwo.get(myDiscardTwo.size() - 1).getId() == 13 ||
							myDiscardTwo.get(myDiscardTwo.size() - 1).getId() == validRankThree)){

						validRankThree += 1;
						buildPileThree.add(myDiscardTwo.get(myDiscardTwo.size() - 1));
						myDiscardTwo.remove(myDiscardTwo.size() - 1);

						//discarding the build pile
						if(buildPileThree.size() >= maxCardNum){	
							for (int i = buildPileThree.size() - 1; i >= 0; i--){
								discardPile.add(buildPileThree.get(i));
								buildPileThree.remove(i);
							}
							validRankThree = 1;
						}
					}
					//end checking 3
					//---------------------------------------------

					//checking 4
					//---------------------------------------------
					else if(x > (3*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < (3*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myDiscardTwo.get(myDiscardTwo.size() - 1).getId() == 13 ||
							myDiscardTwo.get(myDiscardTwo.size() - 1).getId() == validRankFour)){

						validRankFour += 1;
						buildPileFour.add(myDiscardTwo.get(myDiscardTwo.size() - 1));
						myDiscardTwo.remove(myDiscardTwo.size() - 1);

						//discarding the build pile
						if(buildPileFour.size() >= maxCardNum){	
							for (int i = buildPileFour.size() - 1; i >= 0; i--){
								discardPile.add(buildPileFour.get(i));
								buildPileFour.remove(i);
							}
							validRankFour = 1;
						}
					}
					//end checking 4
				}
				//End discard pile two
				//----------------------------------------------------------------

				//TODO: discard three against build piles
				if(discardThreeSelected){
					//check pile one
					//---------------------------------------------------
					if(x > ((screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < ((screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myDiscardThree.get(myDiscardThree.size() - 1).getId() == 13 ||
							myDiscardThree.get(myDiscardThree.size() - 1).getId() == validRankOne)){

						validRankOne += 1;
						buildPileOne.add(myDiscardThree.get(myDiscardThree.size() - 1));
						myDiscardThree.remove(myDiscardThree.size() - 1);

						//discarding the build pile
						if(buildPileOne.size() >= maxCardNum){	
							for (int i = buildPileOne.size() - 1; i >= 0; i--){
								discardPile.add(buildPileOne.get(i));
								buildPileOne.remove(i);
							}
							validRankOne = 1;
						}
					}
					//end checking 1
					//---------------------------------------------------

					//check against build pile two
					//---------------------------------------------------
					else if(x > (1*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < (1*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myDiscardThree.get(myDiscardThree.size() - 1).getId() == 13 ||
							myDiscardThree.get(myDiscardThree.size() - 1).getId() == validRankTwo)){

						validRankTwo += 1;
						buildPileTwo.add(myDiscardThree.get(myDiscardThree.size() - 1));
						myDiscardThree.remove(myDiscardThree.size() - 1);

						//discarding the build pile
						if(buildPileTwo.size() >= maxCardNum){	
							for (int i = buildPileTwo.size() - 1; i >= 0; i--){
								discardPile.add(buildPileTwo.get(i));
								buildPileTwo.remove(i);
							}
							validRankTwo = 1;
						}
					}
					//end checking 2
					//-----------------------------------------------------

					//checking 3
					//-----------------------------------------------------
					else if(x > (2*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < (2*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myDiscardThree.get(myDiscardThree.size() - 1).getId() == 13 ||
							myDiscardThree.get(myDiscardThree.size() - 1).getId() == validRankThree)){

						validRankThree += 1;
						buildPileThree.add(myDiscardThree.get(myDiscardThree.size() - 1));
						myDiscardThree.remove(myDiscardThree.size() - 1);

						//discarding the build pile
						if(buildPileThree.size() >= maxCardNum){	
							for (int i = buildPileThree.size() - 1; i >= 0; i--){
								discardPile.add(buildPileThree.get(i));
								buildPileThree.remove(i);
							}
							validRankThree = 1;
						}
					}
					//end checking 3
					//--------------------------------------------------------

					//checking 4
					//--------------------------------------------------------
					else if(x > (3*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < (3*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myDiscardThree.get(myDiscardThree.size() - 1).getId() == 13 ||
							myDiscardThree.get(myDiscardThree.size() - 1).getId() == validRankFour)){

						validRankFour += 1;
						buildPileFour.add(myDiscardThree.get(myDiscardThree.size() - 1));
						myDiscardThree.remove(myDiscardThree.size() - 1);

						//discarding the build pile
						if(buildPileFour.size() >= maxCardNum){	
							for (int i = buildPileFour.size() - 1; i >= 0; i--){
								discardPile.add(buildPileFour.get(i));
								buildPileFour.remove(i);
							}
							validRankFour = 1;
						}
					}
				}
				//end discard three
				//-------------------------------------------------------------------------

				//TODO: discard four against build piles
				if(discardFourSelected){
					//check pile one
					//---------------------------------------------------------
					if(x > ((screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < ((screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myDiscardFour.get(myDiscardFour.size() - 1).getId() == 13 ||
							myDiscardFour.get(myDiscardFour.size() - 1).getId() == validRankOne)){

						validRankOne += 1;
						buildPileOne.add(myDiscardFour.get(myDiscardFour.size() - 1));
						myDiscardFour.remove(myDiscardFour.size() - 1);

						//discarding the build pile
						if(buildPileOne.size() >= maxCardNum){	
							for (int i = buildPileOne.size() - 1; i >= 0; i--){
								discardPile.add(buildPileOne.get(i));
								buildPileOne.remove(i);
							}
							validRankOne = 1;
						}
					}
					//end checking 1
					//----------------------------------------------------------

					//check against build pile two
					//----------------------------------------------------
					else if(x > (1*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < (1*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myDiscardFour.get(myDiscardFour.size() - 1).getId() == 13 ||
							myDiscardFour.get(myDiscardFour.size() - 1).getId() == validRankTwo)){

						validRankTwo += 1;
						buildPileTwo.add(myDiscardFour.get(myDiscardFour.size() - 1));
						myDiscardFour.remove(myDiscardFour.size() - 1);

						//discarding the build pile
						if(buildPileTwo.size() >= maxCardNum){	
							for (int i = buildPileTwo.size() - 1; i >= 0; i--){
								discardPile.add(buildPileTwo.get(i));
								buildPileTwo.remove(i);
							}
							validRankTwo = 1;
						}
					}
					//end checking 2
					//----------------------------------------------------------

					//checking 3
					//----------------------------------------------------------
					else if(x > (2*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < (2*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myDiscardFour.get(myDiscardFour.size() - 1).getId() == 13 ||
							myDiscardFour.get(myDiscardFour.size() - 1).getId() == validRankThree)){

						validRankThree += 1;
						buildPileThree.add(myDiscardThree.get(myDiscardThree.size() - 1));
						myDiscardThree.remove(myDiscardThree.size() - 1);

						//discarding the build pile
						if(buildPileThree.size() >= maxCardNum){	
							for (int i = buildPileThree.size() - 1; i >= 0; i--){
								discardPile.add(buildPileThree.get(i));
								buildPileThree.remove(i);
							}
							validRankThree = 1;
						}
					}
					//end checking 3
					//--------------------------------------------------------------

					//checking 4
					//--------------------------------------------------------------
					else if(x > (3*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < (3*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myDiscardFour.get(myDiscardFour.size() - 1).getId() == 13 ||
							myDiscardFour.get(myDiscardFour.size() - 1).getId() == validRankFour)){

						validRankFour += 1;
						buildPileFour.add(myDiscardFour.get(myDiscardFour.size() - 1));
						myDiscardFour.remove(myDiscardFour.size() - 1);

						//discarding the build pile
						if(buildPileFour.size() >= maxCardNum){	
							for (int i = buildPileFour.size() - 1; i >= 0; i--){
								discardPile.add(buildPileFour.get(i));
								buildPileFour.remove(i);
							}
							validRankFour = 1;
						}
					}
				}
				//ending checking constraints for discard piles
				//----------------------------------------------------------------------------

				//if the stack card is selected, check differently
				if (stackCardSelected){
					//check against build pile one
					if(x > ((screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < ((screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myStack.get(0).getId() == 13 ||
							myStack.get(0).getId() == validRankOne)){

						validRankOne += 1;
						buildPileOne.add(myStack.get(0));
						myStack.remove(0);

						//discarding the build pile
						if(buildPileOne.size() >= maxCardNum){	
							for (int i = buildPileOne.size() - 1; i >= 0; i--){
								discardPile.add(buildPileOne.get(i));
								buildPileOne.remove(i);
							}
							validRankOne = 1;
						}

						//if the player stack is empty, he/she wins the game
						if (myStack.size() == 0){
							showWinDialog();
						}
					}
					//end checking 1

					//check against build pile two
					else if(x > (1*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < (1*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myStack.get(0).getId() == 13 ||
							myStack.get(0).getId() == validRankTwo)){

						validRankTwo += 1;
						buildPileTwo.add(myStack.get(0));
						myStack.remove(0);

						//discarding the build pile
						if(buildPileTwo.size() >= maxCardNum){	
							for (int i = buildPileTwo.size() - 1; i >= 0; i--){
								discardPile.add(buildPileTwo.get(i));
								buildPileTwo.remove(i);
							}
							validRankTwo = 1;
						}
						//wins the game
						if (myStack.size() == 0){
							showWinDialog();
						}
					}
					//end checking 2

					//checking 3
					else if(x > (2*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < (2*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myStack.get(0).getId() == 13 ||
							myStack.get(0).getId() == validRankThree)){

						validRankThree += 1;
						buildPileThree.add(myStack.get(0));
						myStack.remove(0);

						//discarding the build pile
						if(buildPileThree.size() >= maxCardNum){	
							for (int i = buildPileThree.size() - 1; i >= 0; i--){
								discardPile.add(buildPileThree.get(i));
								buildPileThree.remove(i);
							}
							validRankThree = 1;
						}
						//wins the game
						if (myStack.size() == 0){
							showWinDialog();
						}
					}
					//end checking 3

					//checking 4
					else if(x > (3*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2) &&
							x < (3*(scaledCardW + 40)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW) &&
							y > ((int) (screenH*0.25)) &&
							y < ((int) (screenH*0.25) + scaledCardH) &&
							(myStack.get(0).getId() == 13 ||
							myStack.get(0).getId() == validRankFour)){

						validRankFour += 1;
						buildPileFour.add(myStack.get(0));
						myStack.remove(0);

						//discarding the build pile
						if(buildPileFour.size() >= maxCardNum){	
							for (int i = buildPileFour.size() - 1; i >= 0; i--){
								discardPile.add(buildPileFour.get(i));
								buildPileFour.remove(i);
							}
							validRankFour = 1;
						}
						//wins the game
						if (myStack.size() == 0){
							showWinDialog();
						}
					}
					//end checking 4
				}
				//end check constraints for stack card

				//when the hand is empty, fill the hand with 5 more cards
				if(myHand.isEmpty()){
					//if the deck size is less than 10, refill the deck with discard pile
					if(deck.size() < 10){
						deck.addAll(discardPile);
						discardPile.clear();
						Collections.shuffle(deck);
						//add cards to the hand
						for(int j = 0; j < 5; j++){
							dealCards(myHand);
						}

					}
					else{
						//if not, then just add cards
						for(int j = 0; j < 5; j++){
							dealCards(myHand);
						}
					}
				}
			}

			//discard turn
			if(discardNow){
				//collision detection for discard one and constraint checks
				if(movingCardIndex > -1 &&
						x > 0*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2 &&
						x < 0*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2 + scaledCardW &&
						y > (int) (screenH * 0.45) &&
						y < (int) (screenH * 0.45) + scaledCardH){

					//it lands in the first discard pile
					myDiscardOne.add(myHand.get(movingCardIndex));
					myHand.remove(movingCardIndex);
					discardNow = false;
					makeComputerPlay();
				}

				//discard two pile
				else if(movingCardIndex > -1 &&
						x > 1*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2 &&
						x < 1*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2 + scaledCardW &&
						y > (int) (screenH * 0.45) &&
						y < (int) (screenH * 0.45) + scaledCardH) {
					//it lands in the second discard pile
					myDiscardTwo.add(myHand.get(movingCardIndex));
					myHand.remove(movingCardIndex);
					discardNow = false;
					makeComputerPlay();
				}

				//discard three pile
				else if(movingCardIndex > -1 &&
						x > 2*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2 &&
						x < 2*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2 + scaledCardW &&
						y > (int) (screenH * 0.45) &&
						y < (int) (screenH * 0.45) + scaledCardH) {
					//it lands in the second discard pile
					myDiscardThree.add(myHand.get(movingCardIndex));
					myHand.remove(movingCardIndex);
					discardNow = false;
					makeComputerPlay();
				}

				//discard four pile
				else if(movingCardIndex > -1 &&
						x > 3*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2 &&
						x < 3*(scaledCardW + 40)+ (screenW - 105 - 4*discardP.getWidth())/2 + scaledCardW &&
						y > (int) (screenH * 0.45) &&
						y < (int) (screenH * 0.45) + scaledCardH) {
					//it lands in the second discard pile
					myDiscardFour.add(myHand.get(movingCardIndex));
					myHand.remove(movingCardIndex);
					discardNow = false;
					makeComputerPlay();
				}
			}
			stackCardSelected = false;
			movingCardIndex = -1;
			endTurnButtonPressed = false;
			discardOneSelected = false;
			discardTwoSelected = false;
			discardThreeSelected = false;
			discardFourSelected = false;
			break;
		}

		invalidate();
		return true;
	}

	//Dialog
	private void showEndTurnDialog(){
		AlertDialog.Builder alert = new AlertDialog.Builder(myContext);
		//alert dialog is broken down into three part
		alert.setTitle(R.string.end_turn);
		alert.setMessage(R.string.discard_now);
		alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//when the player press yes, discard one card their turn ends
				//showDiscardDialog();
				discardNow = true;
				myTurn = false;
			}
		});

		alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// when the player press no, do nothing		
			}
		});

		alert.show();
	}


	//this dialog tells the player that he/she won the game

	private void showWinDialog(){
		AlertDialog.Builder alerter = new AlertDialog.Builder(myContext);
		alerter.setMessage(R.string.you_win);
		alerter.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//TODO: maybe start another game?
				myTurn = false;
				//add all piles to the deck
				deck.addAll(buildPileOne);
				deck.addAll(buildPileTwo);
				deck.addAll(buildPileThree);
				deck.addAll(buildPileFour);
				deck.addAll(discardPile);
				deck.addAll(myDiscardOne);
				deck.addAll(myDiscardTwo);
				deck.addAll(myDiscardThree);
				deck.addAll(myDiscardFour);
				deck.addAll(myHand);
				deck.addAll(oppHand);
				deck.addAll(oppStack);
				deck.addAll(myStack);
				deck.addAll(oppDiscardOne);
				deck.addAll(oppDiscardTwo);
				deck.addAll(oppDiscardThree);
				deck.addAll(oppDiscardFour);
				Collections.shuffle(deck);
				
				//clear everything
				buildPileOne.clear();
				buildPileTwo.clear();
				buildPileThree.clear();
				buildPileFour.clear();
				myDiscardOne.clear();
				myDiscardTwo.clear();
				myDiscardThree.clear();
				myDiscardFour.clear();
				myHand.clear();
				oppHand.clear();
				oppDiscardOne.clear();
				oppDiscardTwo.clear();
				oppDiscardThree.clear();
				oppDiscardFour.clear();
				myStack.clear();
				oppStack.clear();
				discardPile.clear();
				
				//refill the hand and the stack
				for(int i = 0; i < 5; i++){
					dealCards(oppHand);
					dealCards(myHand);
				}
				
				fillStack();
				
				//give the player the turn
				myTurn = true;
				
			}
		});
		
		alerter.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// when the player press no, do nothing		
				myTurn = false;
			}
		});

		alerter.show();
	}

	//this dialog tells the player to discard a card
	/*private void showDiscardDialog(){
		AlertDialog.Builder alerter = new AlertDialog.Builder(myContext);
		alerter.setMessage(R.string.please_discard);
		alerter.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//do nothing
			}
		});

		alerter.show();
	}*/

	/*private void showTurnSwitch(){
		AlertDialog.Builder alerter = new AlertDialog.Builder(myContext);
		alerter.setMessage(R.string.Turn_switch);
		alerter.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			//do nothing
		}
	});

	alerter.show();}*/
	//easy AI
	private void makeComputerPlay(){
		//clear the building pile first
		if(oppStack.get(0).getId() == 13 ||
				oppStack.get(0).getId() == validRankOne){
			//add the card to the build pile
			buildPileOne.add(oppStack.get(0));
			//if the building pile is more than 6, clear it
			oppStack.remove(0);
			validRankOne += 1;
			
			if(buildPileOne.size() >= maxCardNum){	
				for (int i = buildPileOne.size() - 1; i >= 0; i--){
					discardPile.add(buildPileOne.get(i));
					buildPileOne.remove(i);
				}
				validRankOne = 1;
			}
		}
		if(oppStack.get(0).getId() == 13 ||
				oppStack.get(0).getId() == validRankTwo){
			
				buildPileTwo.add(oppStack.get(0));
				oppStack.remove(0);
				validRankTwo += 1;
				
				if(buildPileTwo.size() >= maxCardNum){	
					for (int i = buildPileTwo.size() - 1; i >= 0; i--){
						discardPile.add(buildPileTwo.get(i));
						buildPileTwo.remove(i);
					}
					validRankTwo = 1;
				}
		}
		
		if(oppStack.get(0).getId() == 13 ||
				oppStack.get(0).getId() == validRankThree){
				buildPileThree.add(oppStack.get(0));
				oppStack.remove(0);
				validRankThree += 1;
				if(buildPileThree.size() >= maxCardNum){	
					for (int i = buildPileThree.size() - 1; i >= 0; i--){
						discardPile.add(buildPileThree.get(i));
						buildPileThree.remove(i);
					}
					validRankThree = 1;
				}
		}
		
		if(oppStack.get(0).getId() == 13 ||
				oppStack.get(0).getId() == validRankFour){
				buildPileFour.add(oppStack.get(0));
				oppStack.remove(0);
				validRankFour += 1;
				
				if(buildPileFour.size() >= maxCardNum){	
					for (int i = buildPileFour.size() - 1; i >= 0; i--){
						discardPile.add(buildPileFour.get(i));
						buildPileFour.remove(i);
					}
					validRankFour = 1;
				}	
		}
		//end checking stack card
		//check hand card
		for(int j = 0; j < oppHand.size(); j++){
			//build pile one check
			if(oppHand.get(j).getId() == 13 || oppHand.get(j).getId() == validRankOne){
				buildPileOne.add(oppHand.get(j));
				//if the building pile is more than 6, clear it
				oppHand.remove(j);
				validRankOne += 1;
				
				//refill oppHand's hand if it is less than 0
				if(oppHand.size() <= 0){
					//deal cards to oppHand
					for(int i = 0; i < 5; i++){
						dealCards(oppHand);
					}
				}
				
				if(buildPileOne.size() >= maxCardNum){	
					for (int i = buildPileOne.size() - 1; i >= 0; i--){
						discardPile.add(buildPileOne.get(i));
						buildPileOne.remove(i);
					}
					validRankOne = 1;
				}
			}
		}
			
		for(int j = 0; j < oppHand.size(); j++){
			//build pile two check
			if(oppHand.get(j).getId() == 13 || oppHand.get(j).getId() == validRankTwo){
				buildPileTwo.add(oppHand.get(j));
				oppHand.remove(j);
				validRankTwo += 1;
				
				//deal cards if opp hand less than one
				if(oppHand.size() <= 0){
					//deal cards to oppHand
					for(int i = 0; i < 5; i++){
						dealCards(oppHand);
					}
				}
				
				if(buildPileTwo.size() >= maxCardNum){	
					for (int i = buildPileTwo.size() - 1; i >= 0; i--){
						discardPile.add(buildPileTwo.get(i));
						buildPileTwo.remove(i);
					}
					validRankTwo = 1;
				}
			}
		}
			
		for(int j = 0; j < oppHand.size(); j++){
			//build pile three check
			if(oppHand.get(j).getId() == 13 || oppHand.get(j).getId() == validRankThree){
				buildPileThree.add(oppHand.get(j));
				oppHand.remove(j);
				validRankThree += 1;
				
				//deal cards if opp hand less than one
				if(oppHand.size() <= 0){
					//deal cards to oppHand
					for(int i = 0; i < 5; i++){
						dealCards(oppHand);
					}
				}
				
				if(buildPileThree.size() >= maxCardNum){	
					for (int i = buildPileThree.size() - 1; i >= 0; i--){
						discardPile.add(buildPileThree.get(i));
						buildPileThree.remove(i);
					}
					validRankThree = 1;
				}
			}
		}
		
		for(int j = 0; j < oppHand.size(); j++){
			//build pile four check
			if(oppHand.get(j).getId() == 13 || oppHand.get(j).getId() == validRankFour){
				buildPileFour.add(oppHand.get(j));
				//if the building pile is more than 6, clear it
				oppHand.remove(j);
				validRankFour += 1;
				
				//deal cards if opp hand less than zero
				if(oppHand.size() <= 0){
					//deal cards to oppHand
					for(int i = 0; i < 5; i++){
						dealCards(oppHand);
					}
				}
				
				if(buildPileFour.size() >= maxCardNum){	
					for (int i = buildPileFour.size() - 1; i >= 0; i--){
						discardPile.add(buildPileFour.get(i));
						buildPileFour.remove(i);
					}
					validRankFour = 1;
				}
			}
		}
		
		//discard the first card in opp's hand into the discard pile
		oppDiscardOne.add(oppHand.get(0));
		oppHand.remove(0);
		
		//if the deck size is less than 10, refill the deck
		if(deck.size() < 10){
			deck.addAll(discardPile);
			discardPile.clear();
			Collections.shuffle(deck);
		}
		//fills players hand after the computer is done
		int cardsToDeal =  5 - myHand.size();
		for(int j = 0; j < cardsToDeal; j++){
			dealCards(myHand);
		}
		
		//fills computer's hand
		cardsToDeal = 5 - oppHand.size();
		for(int j = 0; j < cardsToDeal; j++){
			dealCards(oppHand);
		}
		myTurn = true;
		//showTurnSwitch();
	}
}
