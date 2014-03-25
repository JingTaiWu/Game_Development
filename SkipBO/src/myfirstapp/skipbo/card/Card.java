/**
 * 
 */
package myfirstapp.skipbo.card;

import android.graphics.Bitmap;

/**
 * @author Jing Tai Wu
 * This is the class for cards
 */
public class Card {
	/**
	 * fields and class varibles
	 */
	private int id;
	private Bitmap bmp;
	
	
	/**
	 * constructor
	 */
	public Card(int newId){
		id = newId;
	}
	
	/**
	 * Setter for bitmap drawing
	 */
	public void setBitmap(Bitmap newBitmap){
		bmp = newBitmap;
	}
	
	/**
	 * getter for bitmap
	 * @return bmp
	 */
	public Bitmap getBitmap(){
		return bmp;
	}
	
	/**
	 * getter for card Id
	 * @return id
	 */
	public int getId(){
		return id;
	}
}
