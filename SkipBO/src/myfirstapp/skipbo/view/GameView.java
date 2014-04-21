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
	
	private boolean myTurn;
	
	//bitmaps for drawing piles
	private Bitmap discardP;
	private Bitmap buildingP;
	
	//hands
	private List<Card> myHand = new LinkedList<Card>();
	private List<Card> oppHand = new LinkedList<Card>();
	
	//player's discard piles
	private List<Card> myDiscardOne = new LinkedList<Card>();
	private List<Card> myDiscardTwo = new LinkedList<Card>();
	//private List<Card> myDiscardThree = new LinkedList<Card>();
	//private List<Card> myDiscardFour = new LinkedList<Card>();
	
	//opp's discard piles
	private List<Card> oppDiscardOne = new LinkedList<Card>();
	private List<Card> oppDiscardTwo = new LinkedList<Card>();
	//private List<Card> oppDiscardThree = new LinkedList<Card>();
	//private List<Card> oppDiscardFour = new LinkedList<Card>();
	
	//building piles
	private List<Card> buildPileOne = new LinkedList<Card>();
	private List<Card> buildPileTwo = new LinkedList<Card>();
	//private List<Card> buildPileThree = new LinkedList<Card>();
	//private List<Card> buildPileFour = new LinkedList<Card>();
	
	//stacks for each player
	private List<Card> myStack = new LinkedList<Card>();
	private List<Card> oppStack = new LinkedList<Card>();
	
	//control the card numbers/amount of duplicates (for testing)
	private int maxCardNum = 8;
	private int cardDup = 6;
	
	//card movement variables
	private int movingCardIndex = -1;
	private int movingX;
	private int movingY;
	private int validRankOne = 1;
	private int validRankTwo = 1;
	private boolean stackCardSelected;
	//private int pileSelected = -1;
	//private int discardPileSelected = -1;
	private boolean discardNow = false;

	
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
		for(int i = 0; i < 15; i++){
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

		//draw the discard pile for player
		//if the first discard pile is empty, draw the "Discard pile card"
		//must do this for all the discard pile (I don't know if there is a more efficient way of doing this)
		if (myDiscardOne.isEmpty()){
			canvas.drawBitmap(discardP, 0*(scaledCardW + 80)+ (screenW - 105 - 2*discardP.getWidth())/2, (int) (screenH * 0.45), null);
		}
		//if it is not empty, draw the last card of the discard pile
		else {
			canvas.drawBitmap(myDiscardOne.get(myDiscardOne.size() - 1).getBitmap(), 0*(scaledCardW + 80)+ (screenW - 105 - 2*discardP.getWidth())/2, (int) (screenH * 0.45), null);
		}
		
		if (myDiscardTwo.isEmpty()){
			canvas.drawBitmap(discardP, 1*(scaledCardW + 80)+ (screenW - 105 - 2*discardP.getWidth())/2, (int) (screenH * 0.45), null);
		}

		else {
			canvas.drawBitmap(myDiscardTwo.get(myDiscardTwo.size() - 1).getBitmap(), 1*(scaledCardW + 80)+ (screenW - 105 - 2*discardP.getWidth())/2, (int) (screenH * 0.45), null);
		}
		
		//End drawing for player discard files
		
		
		//draw the discard pile for the computer
		//same as the player discard pile
		
		//discard pile 1
		if (oppDiscardOne.isEmpty()){
			canvas.drawBitmap(discardP, 0*(scaledCardW + 40) + discardP.getWidth()*2+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}
		//if it is not empty, draw the last card of the discard pile
		else {
			canvas.drawBitmap(oppDiscardOne.get(oppDiscardOne.size() - 1).getBitmap(), 0*(scaledCardW + 40) + discardP.getWidth()*2+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}
		
		//discard pile 2
		if (oppDiscardTwo.isEmpty()){
			canvas.drawBitmap(discardP, 1*(scaledCardW + 40) + discardP.getWidth()*2+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}

		else {
			canvas.drawBitmap(oppDiscardTwo.get(oppDiscardTwo.size() - 1).getBitmap(), 1*(scaledCardW + 40) + discardP.getWidth()*2+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}
		
		//end drawing discard pile for the computer
		
		
		//draw the building pile in the middle
		if (buildPileOne.isEmpty()){
			canvas.drawBitmap(buildingP, 0*(scaledCardW + 80)+ (screenW - 105 - 2*buildingP.getWidth())/2, (int) (screenH*0.25), null);
		}
		else {
			canvas.drawBitmap(buildPileOne.get(buildPileOne.size() - 1).getBitmap(), 0*(scaledCardW + 120)+ (screenW - 105 - 2*buildingP.getWidth())/2, (int) (screenH*0.25), null);
			canvas.drawText("Next Card: " + (validRankOne), 10, (int) (screenH*0.25), blackPaint);
		}
		
		if (buildPileTwo.isEmpty()){
			canvas.drawBitmap(buildingP, 1*(scaledCardW + 80)+ (screenW - 105 - 2*buildingP.getWidth())/2, (int) (screenH*0.25), null);
		}
		else {
			canvas.drawBitmap(buildPileTwo.get(buildPileTwo.size() - 1).getBitmap(), 1*(scaledCardW + 80)+ (screenW - 105 - 2*buildingP.getWidth())/2, (int) (screenH*0.25), null);
			canvas.drawText("Next Card: " + (validRankTwo), screenW - blackPaint.getTextSize()*2 - 105, (int) (screenH*0.25), blackPaint);
		}
		
		//end drawing building pile
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
			//check constraints for building piles
			if(myTurn){
			if (movingCardIndex > -1 &&
				x > ((screenW - 105 - 2*buildingP.getWidth())/2) &&
				x < ((screenW - 105 - 2*buildingP.getWidth())/2 + scaledCardW) &&
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
			}
			
			else if (movingCardIndex > -1 &&
					x > (1*(scaledCardW + 80)+ (screenW - 105 - 2*buildingP.getWidth())/2) &&
					x < (1*(scaledCardW + 80)+ (screenW - 105 - 2*buildingP.getWidth())/2 + scaledCardW) &&
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
				}
			
			//if the stack card is selected, check differently
			if (stackCardSelected){
				if(x > ((screenW - 105 - 2*buildingP.getWidth())/2) &&
				   x < ((screenW - 105 - 2*buildingP.getWidth())/2 + scaledCardW) &&
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
				else if(x > (1*(scaledCardW + 80)+ (screenW - 105 - 2*buildingP.getWidth())/2) &&
						x < (1*(scaledCardW + 80)+ (screenW - 105 - 2*buildingP.getWidth())/2 + scaledCardW) &&
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
			}
			//check constraints for discard piles
			
			//when the hand is empty, fill the hand with 5 more cards
			if(myHand.isEmpty()){
				for(int j = 0; j < 5; j++){
					dealCards(myHand);
				}
				}
			}
			
			if(discardNow){
				if(movingCardIndex > -1 &&
				   x > 0*(scaledCardW + 80)+ (screenW - 105 - 2*discardP.getWidth())/2 &&
				   x < 0*(scaledCardW + 80)+ (screenW - 105 - 2*discardP.getWidth())/2 + scaledCardW &&
				   y > (int) (screenH * 0.45) &&
				   y < (int) (screenH * 0.45) + scaledCardH){
					
					//it lands in the first discard pile
					myDiscardOne.add(myHand.get(movingCardIndex));
					myHand.remove(movingCardIndex);
					discardNow = false;
					makeComputerPlay();
				}
				
				else if(movingCardIndex > -1 &&
						   x > 1*(scaledCardW + 80)+ (screenW - 105 - 2*discardP.getWidth())/2 &&
						   x < 1*(scaledCardW + 80)+ (screenW - 105 - 2*discardP.getWidth())/2 + scaledCardW &&
						   y > (int) (screenH * 0.45) &&
						   y < (int) (screenH * 0.45) + scaledCardH) {
					//it lands in the second discard pile
					myDiscardTwo.add(myHand.get(movingCardIndex));
					myHand.remove(movingCardIndex);
					discardNow = false;
					makeComputerPlay();
				}
			}
			
			stackCardSelected = false;
			movingCardIndex = -1;
			endTurnButtonPressed = false;
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
				showDiscardDialog();
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
			}
		});
		
		alerter.show();
	}
	
	//this dialog tells the player to discard a card
	private void showDiscardDialog(){
		AlertDialog.Builder alerter = new AlertDialog.Builder(myContext);
		alerter.setMessage(R.string.please_discard);
		alerter.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//do nothing
			}
		});
		
		alerter.show();
	}
	
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
			if(buildPileOne.size() >= maxCardNum){	
				for (int i = buildPileOne.size() - 1; i >= 0; i--){
					discardPile.add(buildPileOne.get(i));
					buildPileOne.remove(i);
				}
				validRankOne = 1;
			}
			//add the card to the build pile
			buildPileOne.add(oppStack.get(0));
			//if the building pile is more than 6, clear it
			oppStack.remove(0);
			validRankOne += 1;
		}
		if(oppStack.get(0).getId() == 13 ||
		   oppStack.get(0).getId() == validRankTwo){
			if(buildPileTwo.size() >= maxCardNum){	
				for (int i = buildPileTwo.size() - 1; i >= 0; i--){
					discardPile.add(buildPileTwo.get(i));
					buildPileTwo.remove(i);
				}
				validRankTwo = 1;
			buildPileTwo.add(oppStack.get(0));
			oppStack.remove(0);
			validRankTwo += 1;
			}
		}
		
		
		//fills players hand after the computer is done
		int cardsToDeal =  5 - myHand.size();
		for(int j = 0; j < cardsToDeal; j++){
			dealCards(myHand);
		}
		myTurn = true;
		//showTurnSwitch();
	}
}
