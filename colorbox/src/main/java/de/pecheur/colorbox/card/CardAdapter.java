package de.pecheur.colorbox.card;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import de.pecheur.colorbox.R;
import de.pecheur.colorbox.models.Unit;

public class CardAdapter extends ArrayAdapter<Card> {
	private Unit mUnit;
	private int cardColor;
	private int mCount;
	private int mSide;
	public MediaPlayer mPlayer;
	
	public CardAdapter(Context context, Unit unit, int count) {
		super(context, 0);
		this.
		mUnit = unit;
		cardColor = mUnit.getColor(context);
		mCount = count;
	}
	
	
	
	public void setSide(int side) {
		mSide = side;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return Math.min(mCount, super.getCount());
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (view == null) {
        	view = LayoutInflater.from(parent.getContext()).
        			inflate(R.layout.card_view, parent, false);
        	view.setBackgroundResource(cardColor);
        }

		final Card card = getItem(position);
		
		// adjust side
		switch(mSide) {
			case Card.FRONT:
				card.setSide(true);
				break;
			case Card.BACK:
				card.setSide(false);
		}
			
		// title
        TextView title = (TextView) view.findViewById(R.id.text);
        //title.setText(card.getText());
        
  /*      // audio
        ImageButton play = (ImageButton) view.findViewById(R.id.play);
        play.setVisibility(card.hasAudio() ? View.VISIBLE : View.INVISIBLE);
        play.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					mPlayer = new MediaPlayer();
					mPlayer.setDataSource( getContext(),card.getAudio());
					mPlayer.prepare();
					mPlayer.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
        });
  */
		return view;
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}
}