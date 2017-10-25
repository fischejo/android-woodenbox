package de.pecheur.colorbox.card;

import de.pecheur.colorbox.models.Word;

public class Card extends Word {
	public static final int FRONT = 0;
	public static final int BACK = 1;
	public static final int BOTH = 2;
	
	private boolean front;
	private boolean back;
	private boolean turn = false;
	
	public Card(long id) {
		super(id);
	}
	
	
	public void setSide(boolean side) {
		turn = false;	// reset turn
//		super.setSide(side);
	}
	
	public boolean isFront() {
		return true; //return super.isFront() ^ turn;
	}
	
	 
	public void correct() {
        /*
		if(super.isFront()) {
			front = true;
		} else {
			back = true;
		}
		*/
		turn = false;	// reset turn
	}
	
	public void incorrect() {
        /*
		if(super.isFront()) {
			front = false;
		} else {
			back = false;
		}
		*/
		turn = false;	// reset turn
	}
	
	public boolean isFinished(int side) {
		switch(side) {
		case FRONT:
			return front;
		case BACK:
			return back;
		default:
			return front && back;
		}
	}
	
	public boolean turn() {
		turn = !turn;
		return isFront();
	}
}
