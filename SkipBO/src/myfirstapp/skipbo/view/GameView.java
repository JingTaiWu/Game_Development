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
	
	//card movement variables
	private int movingCardIndex = -1;
	private int movingX;
	private int movingY;
	private boolean stackCardSelected;
	private int pileSelected = -1;
	private int discardPileSelected = -1;
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
		if(myTurn){
		//at the beginning of each turn, player's hand size must be filled to 5
			int cardsToDeal =  5 - myHand.size();
			for(int j = 0; j < cardsToDeal; j++){
				dealCards(myHand);
			}
		}
		if (discardNow){		
			switch(discardPileSelected){
			case 0:
				myDiscardOne.add(myHand.get(movingCardIndex));
				myHand.remove(movingCardIndex);
				break;
			case 1:
				myDiscardTwo.add(myHand.get(movingCardIndex));
				myHand.remove(movingCardIndex);
				break;
			case 2:
				myDiscardThree.add(myHand.get(movingCardIndex));
				myHand.remove(movingCardIndex);
				break;
			case 3:
				myDiscardFour.add(myHand.get(movingCardIndex));
				myHand.remove(movingCardIndex);
				break;
			}
			//after discarding, player turn ends
			myTurn = false;
			//make computer play
			
			//return to player
			myTurn = true;
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
				//----------------------------------------------------------------TEST
				int resourceId = getResources().getIdentifier("card" + 13, "drawable", myContext.getPackageName());
				//-------------------------------------------------------------------
				//int resourceId = getResources().getIdentifier("card" + tempId, "drawable", myContext.getPackageName());
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
			canvas.drawBitmap(discardP, 0*(scaledCardW + 35)+ (screenW - 105 - 4*discardP.getWidth())/2, (int) (screenH * 0.45), null);
		}
		//if it is not empty, draw the last card of the discard pile
		else {
			canvas.drawBitmap(myDiscardOne.get(myDiscardOne.size() - 1).getBitmap(), 0*(scaledCardW + 35)+ (screenW - 105 - 4*discardP.getWidth())/2, (int) (screenH * 0.45), null);
		}
		
		if (myDiscardTwo.isEmpty()){
			canvas.drawBitmap(discardP, 1*(scaledCardW + 35)+ (screenW - 105 - 4*discardP.getWidth())/2, (int) (screenH * 0.45), null);
		}

		else {
			canvas.drawBitmap(myDiscardTwo.get(myDiscardTwo.size() - 1).getBitmap(), 1*(scaledCardW + 35)+ (screenW - 105 - 4*discardP.getWidth())/2, (int) (screenH * 0.45), null);
		}
		
		if (myDiscardThree.isEmpty()){
			canvas.drawBitmap(discardP, 2*(scaledCardW + 35)+ (screenW - 105 - 4*discardP.getWidth())/2, (int) (screenH * 0.45), null);
		}

		else {
			canvas.drawBitmap(myDiscardThree.get(myDiscardThree.size() - 1).getBitmap(), 2*(scaledCardW + 35)+ (screenW - 105 - 4*discardP.getWidth())/2, (int) (screenH * 0.45), null);
		}
		
		if (myDiscardFour.isEmpty()){
			canvas.drawBitmap(discardP, 3*(scaledCardW + 35)+ (screenW - 105 - 4*discardP.getWidth())/2, (int) (screenH * 0.45), null);
		}
		else {
			canvas.drawBitmap(myDiscardFour.get(myDiscardFour.size() - 1).getBitmap(), 3*(scaledCardW + 35)+ (screenW - 105 - 4*discardP.getWidth())/2, (int) (screenH * 0.45), null);
		}
		
		//End drawing for player discard files
		
		
		//draw the discard pile for the computer
		//same as the player discard pile
		
		//discard pile 1
		if (oppDiscardOne.isEmpty()){
			canvas.drawBitmap(discardP, 0*(scaledCardW + 10) + discardP.getWidth()+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}
		//if it is not empty, draw the last card of the discard pile
		else {
			canvas.drawBitmap(oppDiscardOne.get(oppDiscardOne.size() - 1).getBitmap(), 0*(scaledCardW + 10) + discardP.getWidth()+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}
		
		//discard pile 2
		if (oppDiscardTwo.isEmpty()){
			canvas.drawBitmap(discardP, 1*(scaledCardW + 10) + discardP.getWidth()+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}

		else {
			canvas.drawBitmap(oppDiscardTwo.get(oppDiscardTwo.size() - 1).getBitmap(), 1*(scaledCardW + 10) + discardP.getWidth()+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}
		
		//discard pile 3
		if (oppDiscardThree.isEmpty()){
			canvas.drawBitmap(discardP, 2*(scaledCardW + 10) + discardP.getWidth()+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}

		else {
			canvas.drawBitmap(oppDiscardThree.get(oppDiscardThree.size() - 1).getBitmap(), 2*(scaledCardW + 10) + discardP.getWidth()+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}
		
		//discard pile 4
		if (oppDiscardFour.isEmpty()){
			canvas.drawBitmap(discardP, 3*(scaledCardW + 10) + discardP.getWidth()+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}
		else {
			canvas.drawBitmap(oppDiscardFour.get(oppDiscardFour.size() - 1).getBitmap(), 3*(scaledCardW + 10) + discardP.getWidth()+ 40, blackPaint.getTextSize() + (20*scale) ,null);
		}
		//end drawing discard pile for the computer
		
		
		//draw the building pile in the middle
		if (buildPileOne.isEmpty()){
			canvas.drawBitmap(buildingP, 0*(scaledCardW + 35)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.25), null);
		}
		else {
			canvas.drawBitmap(buildPileOne.get(buildPileOne.size() - 1).getBitmap(), 0*(scaledCardW + 35)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.25), null);
		}
		
		if (buildPileTwo.isEmpty()){
			canvas.drawBitmap(buildingP, 1*(scaledCardW + 35)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.25), null);
		}
		else {
			canvas.drawBitmap(buildPileTwo.get(buildPileTwo.size() - 1).getBitmap(), 1*(scaledCardW + 35)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.25), null);
		}
		
		if (buildPileThree.isEmpty()){
			canvas.drawBitmap(buildingP, 2*(scaledCardW + 35)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.25), null);
		}
		else {
			canvas.drawBitmap(buildPileThree.get(buildPileThree.size() - 1).getBitmap(), 2*(scaledCardW + 35)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.25), null);
		}
		
		if (buildPileFour.isEmpty()){
			canvas.drawBitmap(buildingP, 3*(scaledCardW + 35)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.25), null);
		}
		else {
			canvas.drawBitmap(buildPileFour.get(buildPileFour.size() - 1).getBitmap(), 3*(scaledCardW + 35)+ (screenW - 105 - 4*buildingP.getWidth())/2, (int) (screenH*0.25), null);
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
		for (int i = 0; i < myHand.size(); i++){
			if(i == movingCardIndex){
				//if the card is selected, in the onTouchEvent, the coordinates should be assigned to movingX and movingY
				canvas.drawBitmap(myHand.get(i).getBitmap(), movingX, movingY, null);
			}
			else{
				canvas.drawBitmap(myHand.get(i).getBitmap(), i*(scaledCardW + 10) + (screenW - 5*cardBack.getWidth() - 40)/2, screenH - (cardBack.getHeight() + 20), null);
			}
		}
		
		//draw the top card of player's stack
		if(myStack.size() != 0){
			//if the stack card is selected. paint the card according to the moving coordinate
			if(stackCardSelected){
				canvas.drawBitmap(myStack.get(myStack.size() - 1).getBitmap(), movingX, movingY ,null);
			}
			//if not, paint it at the original position
			else{
				canvas.drawBitmap(myStack.get(myStack.size() - 1).getBitmap(), screenW - myStack.get(0).getBitmap().getWidth() - 50, (int) (screenH*0.6) + 30 ,null);
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
			//collision detection for endTurnButton
			if ( x > (int)(screenW/6) &&
					 x < ((int)(screenW/6) + endTurnButtonUp.getWidth()) &&
					 y > (int) (screenH*0.67) &&
					 y < (int) (screenH*0.67) + endTurnButtonUp.getHeight()){
					endTurnButtonPressed = true;
				}
			
			//collision detection for picking up cards from player's hand
			if(myTurn){
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
					int stackCardX = screenW - myStack.get(0).getBitmap().getWidth() - 50;
					int stackCardY = (int) (screenH*0.6) + 30;
					if ( x > stackCardX && x < stackCardX + scaledCardW &&
							y > stackCardY && y < stackCardY + scaledCardH){
						stackCardSelected = true;
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
			//check for valid plays in the build pile
			for(int i = 0; i < 4; i++){
				//build pile card position X
				int buildPileStX = i*(scaledCardW + 35)+ (screenW - 105 - 4*buildingP.getWidth())/2;
				int buildPileEdX = i*(scaledCardW + 35)+ (screenW - 105 - 4*buildingP.getWidth())/2 + scaledCardW;
				//build pile card position Y
				int buildPileStY = (int) (screenH*0.25);
				int buildPileEdY = (int) (screenH*0.25) + scaledCardH;
				
				//collision detection
				if(movingCardIndex > -1 &&
					x > buildPileStX &&
					x < buildPileEdX &&
					y > buildPileStY &&
					y < buildPileEdY){
					pileSelected = i;
					}
				}
			
			//if the stack Card is selected, then check the stack card
			if(stackCardSelected){
				switch(pileSelected){
				//first pile is selected
				case 0:
					//if the build pile is empty, the first card must be either 1 or skipbo
					if(buildPileOne.isEmpty()){ 
						if (myStack.get(myStack.size() - 1).getId() == 1 || myStack.get(myStack.size() - 1).getId() == 13){
						//add the card to the pile
						buildPileOne.add(myStack.get(myStack.size() - 1));
						//remove the card from the stack
						myStack.remove(myStack.size() - 1);
						}
					}
					//if the top card is skipbo,gotta figure out what the skipbo card vlaue is 
					else if(buildPileOne.get(buildPileOne.size() - 1).getId() == 13){
						int playableRank = buildPileOne.size() + 1;
							if(myStack.get(myStack.size() - 1).getId() == 13 ||
									   myStack.get(myStack.size() - 1).getId() == playableRank){
								
								buildPileOne.add(myStack.get(myStack.size() - 1));
								
								myStack.remove(myStack.size() - 1);
							}
					}
					//if the card is a skipbo card or the card that follows the sequence
					else {
						if(myStack.get(myStack.size() - 1).getId() == 13 ||
						   myStack.get(myStack.size() - 1).getId() == (buildPileOne.get(buildPileOne.size() - 1).getId() + 1)){
							
							buildPileOne.add(myStack.get(myStack.size() - 1));
							
							myStack.remove(myStack.size() - 1);
						}
					}
					
					//reset the stack card
					break;
				//second
				case 1:
					//if the build pile is empty, the first card must be either 1 or skipbo
					if(buildPileTwo.isEmpty()){ 
						if (myStack.get(myStack.size() - 1).getId() == 1 || myStack.get(myStack.size() - 1).getId() == 13){
						//add the card to the pile
						buildPileTwo.add(myStack.get(myStack.size() - 1));
						//remove the card from my hand
						myStack.remove(myStack.size() - 1);
						}
					}
					else if(buildPileTwo.get(buildPileTwo.size() - 1).getId() == 13){
						int playableRank = buildPileOne.size() + 1;
							if(myStack.get(myStack.size() - 1).getId() == 13 ||
									   myStack.get(myStack.size() - 1).getId() == playableRank){
								
								buildPileTwo.add(myStack.get(myStack.size() - 1));
								
								myStack.remove(myStack.size() - 1);
							}
					}
					//if the card is a skipbo card or the card that follows the sequence
					else {
						if(myStack.get(myStack.size() - 1).getId() == 13 ||
						   myStack.get(myStack.size() - 1).getId() == (buildPileTwo.get(buildPileTwo.size() - 1).getId() + 1)){
							//add the card to this building pile
							buildPileTwo.add(myStack.get(myStack.size() - 1));
							//delete it from my hand
							myStack.remove(myStack.size() - 1);
						}
					}

					break;
					
				case 2:
					//if the build pile is empty, the first card must be either 1 or skipbo
					if(buildPileThree.isEmpty()){ 
						if (myStack.get(myStack.size() - 1).getId() == 1 || myStack.get(myStack.size() - 1).getId() == 13){
						//add the card to the pile
						buildPileThree.add(myStack.get(myStack.size() - 1));
						//remove the card from my hand
						myStack.remove(myStack.size() - 1);
						}
					}
					else if(buildPileThree.get(buildPileThree.size() - 1).getId() == 13){
						int playableRank = buildPileOne.size() + 1;
							if(myStack.get(myStack.size() - 1).getId() == 13 ||
									   myStack.get(myStack.size() - 1).getId() == playableRank){
								
								buildPileThree.add(myStack.get(myStack.size() - 1));
								
								myStack.remove(myStack.size() - 1);
							}
					}
					//if the card is a skipbo card or the card that follows the sequence
					else {
						if(myStack.get(myStack.size() - 1).getId() == 13 ||
						   myStack.get(myStack.size() - 1).getId() == (buildPileThree.get(buildPileThree.size() - 1).getId() + 1)){
							//add the card to this building pile
							buildPileThree.add(myStack.get(myStack.size() - 1));
							//delete it from my hand
							myStack.remove(myStack.size() - 1);
						}
					}
					
					break;
					
				case 3:
					//if the build pile is empty, the first card must be either 1 or skipbo
					if(buildPileFour.isEmpty()){ 
						if (myStack.get(myStack.size() - 1).getId() == 1 || myStack.get(myStack.size() - 1).getId() == 13){
						//add the card to the pile
						buildPileFour.add(myStack.get(myStack.size() - 1));
						//remove the card from my hand
						myStack.remove(myStack.size() - 1);
						}
					}
					else if(buildPileFour.get(buildPileFour.size() - 1).getId() == 13){
						int playableRank = buildPileFour.size() + 1;
							if(myStack.get(myStack.size() - 1).getId() == 13 ||
									   myStack.get(myStack.size() - 1).getId() == playableRank){
								
								buildPileFour.add(myStack.get(myStack.size() - 1));
								
								myStack.remove(myStack.size() - 1);
							}
					}
					//if the card is a skipbo card or the card that follows the sequence
					else {
						if(myStack.get(myStack.size() - 1).getId() == 13 ||
						   myStack.get(myStack.size() - 1).getId() == (buildPileFour.get(buildPileFour.size() - 1).getId() + 1)){
							//add the card to this building pile
							buildPileFour.add(myStack.get(myStack.size() - 1));
							//delete it from my hand
							myStack.remove(myStack.size() - 1);
						}
					}
					break;
				}
			}
			
			else{
			switch(pileSelected){
			//first pile is selected
			case 0:
				//if the build pile is empty, the first card must be either 1 or skipbo
				if(buildPileOne.isEmpty()){ 
					if (myHand.get(movingCardIndex).getId() == 1 || myHand.get(movingCardIndex).getId() == 13){
					//add the card to the pile
					buildPileOne.add(myHand.get(movingCardIndex));
					//remove the card from my hand
					myHand.remove(movingCardIndex);
					}
				}
				else if(buildPileOne.get(buildPileOne.size() - 1).getId() == 13){
					int playableRank = buildPileOne.size() + 1;
						if(myHand.get(movingCardIndex).getId() == 13 ||
								myHand.get(movingCardIndex).getId() == playableRank){
							
							buildPileOne.add(myHand.get(movingCardIndex));
							
							myHand.remove(movingCardIndex);
						}
				}
				//if the card is a skipbo card or the card that follows the sequence
				else {
					if(myHand.get(movingCardIndex).getId() == 13 ||
				       myHand.get(movingCardIndex).getId() == (buildPileOne.get(buildPileOne.size() - 1).getId() + 1)){
						//add the card to this building pile
						buildPileOne.add(myHand.get(movingCardIndex));
						//delete it from my hand
						myHand.remove(movingCardIndex);
					}
				}

				break;
			//second
			case 1:
				//if the build pile is empty, the first card must be either 1 or skipbo
				if(buildPileTwo.isEmpty()){ 
					if (myHand.get(movingCardIndex).getId() == 1 || myHand.get(movingCardIndex).getId() == 13){
					//add the card to the pile
					buildPileTwo.add(myHand.get(movingCardIndex));
					//remove the card from my hand
					myHand.remove(movingCardIndex);
					}
				}
				else if(buildPileTwo.get(buildPileTwo.size() - 1).getId() == 13){
					int playableRank = buildPileTwo.size() + 1;
						if(myHand.get(movingCardIndex).getId() == 13 ||
								myHand.get(movingCardIndex).getId() == playableRank){
							
							buildPileTwo.add(myHand.get(movingCardIndex));
							
							myHand.remove(myHand.get(movingCardIndex));
						}
				}
				//if the card is a skipbo card or the card that follows the sequence
				else {
					if(myHand.get(movingCardIndex).getId() == 13 ||
				       myHand.get(movingCardIndex).getId() == (buildPileTwo.get(buildPileTwo.size() - 1).getId() + 1)){
						//add the card to this building pile
						buildPileTwo.add(myHand.get(movingCardIndex));
						//delete it from my hand
						myHand.remove(movingCardIndex);
					}
				}
				break;
				
			case 2:
				if(buildPileThree.isEmpty()){ 
					if (myHand.get(movingCardIndex).getId() == 1 || myHand.get(movingCardIndex).getId() == 13){
					//add the card to the pile
					buildPileThree.add(myHand.get(movingCardIndex));
					//remove the card from my hand
					myHand.remove(movingCardIndex);
					}
				}
				else if(buildPileThree.get(buildPileThree.size() - 1).getId() == 13){
					int playableRank = buildPileThree.size() + 1;
						if(myHand.get(movingCardIndex).getId() == 13 ||
								myHand.get(movingCardIndex).getId() == playableRank){
							
							buildPileThree.add(myHand.get(movingCardIndex));
							
							myHand.remove(myHand.get(movingCardIndex));
						}
				}
				//if the card is a skipbo card or the card that follows the sequence
				else {
					if(myHand.get(movingCardIndex).getId() == 13 ||
				       myHand.get(movingCardIndex).getId() == (buildPileThree.get(buildPileThree.size() - 1).getId() + 1)){
						//add the card to this building pile
						buildPileThree.add(myHand.get(movingCardIndex));
						//delete it from my hand
						myHand.remove(movingCardIndex);
					}
				}
				break;
				
			case 3:
				if(buildPileFour.isEmpty()){ 
					if (myHand.get(movingCardIndex).getId() == 1 || myHand.get(movingCardIndex).getId() == 13){
					//add the card to the pile
					buildPileFour.add(myHand.get(movingCardIndex));
					//remove the card from my hand
					myHand.remove(movingCardIndex);
					}
				}
				else if(buildPileFour.get(buildPileFour.size() - 1).getId() == 13){
					int playableRank = buildPileFour.size() + 1;
						if(myHand.get(movingCardIndex).getId() == 13 ||
								myHand.get(movingCardIndex).getId() == playableRank){
							
							buildPileFour.add(myStack.get(myStack.size() - 1));
							
							myHand.remove(myHand.get(movingCardIndex));
						}
				}
				//if the card is a skipbo card or the card that follows the sequence
				else {
					if(myHand.get(movingCardIndex).getId() == 13 ||
				       myHand.get(movingCardIndex).getId() == (buildPileFour.get(buildPileFour.size() - 1).getId() + 1)){
						//add the card to this building pile
						buildPileFour.add(myHand.get(movingCardIndex));
						//delete it from my hand
						myHand.remove(movingCardIndex);
					}
				}
				break;
			}
			}
			//end checking
			
			//collision detection for discard pile
			for(int i = 0; i < 4; i++){
				int cardstX = i*(scaledCardW + 35)+ (screenW - 105 - 4*discardP.getWidth())/2;
				int cardedX = i*(scaledCardW + 35)+ (screenW - 105 - 4*discardP.getWidth())/2 + scaledCardW;
				int cardstY = (int) (screenH * 0.45);
				int cardedY = (int) (screenH * 0.45) + scaledCardH;
				
				if(movingCardIndex > -1 &&
						x > cardstX &&
						x < cardedX &&
						y > cardstY &&
						y < cardedY){
						discardPileSelected = i;
						}
			}
			
			//after pressing a discard button, one must discard a card from his hand
			if(endTurnButtonPressed){
				discardNow = true;
				endTurnButtonPressed = false;				
			}
			
			stackCardSelected = false;
			//reset selected card
			movingCardIndex = -1;
			break;
		}
	invalidate();
	return true;
	}

}
