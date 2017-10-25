package de.pecheur.colorbox.models;

import android.os.Parcel;
import android.os.Parcelable;
import de.pecheur.colorbox.database.columns.WordColumn;


public class Word implements Parcelable, Cloneable {
    //public static final boolean FRONT = true;
	//public static final boolean BACK = false;
	
	private long id = WordColumn.INVALID_ROW_ID;
	private Unit unit;
	private int box;
	
	//private boolean side = FRONT;

    // attachments
    //private ArrayList<Attachment> frontAttachments = new ArrayList<Attachment>();
    //private ArrayList<Attachment> backAttachments = new ArrayList<Attachment>();

	public Word(Unit unit) {
        this.unit = unit;
	}
	
	public Word(long id) {
        this.id = id;
	}

	public Word(Parcel in) {
		id = in.readLong();
		unit = in.readParcelable( Unit.class.getClassLoader());
		box = in.readInt();

        //frontAttachments = in.readArrayList(Attachment.class.getClassLoader());
        //backAttachments = in.readArrayList(Attachment.class.getClassLoader());
	}

	
	public Unit getUnit() {
		return unit;
	}

	public Word setUnit(Unit unit) {
		this.unit = unit;
		return this;
	}

	public long getId() {
		return id;
	}

    public void setId(long id) {
        this.id = id;
    }

	public boolean hasId() {
		return id != WordColumn.INVALID_ROW_ID;
	}

	public int getBox() {
		return box;
	}
	
	public Word setBox(int box) {
		this.box = box;
		return this;
	}




/*
    public ArrayList<Attachment> getAttachments() {
        return getAttachments(side);
    }

    public ArrayList<Attachment> getAttachments(boolean side) {
        return side ? frontAttachments : backAttachments;
    }

    public void setAttachments(ArrayList<Attachment> attachments, boolean side) {
        if(side) {
            frontAttachments = attachments;
        } else {
            backAttachments = attachments;
        }
    }

    public void setAttachments(ArrayList<Attachment> attachments) {
        setAttachments(attachments, side);
    }



    public boolean hasContent(boolean side) {
        return side ? !frontAttachments.isEmpty() : !backAttachments.isEmpty();
    }

    public boolean hasContent() {
        return  hasContent(side);
    }


	
	public void setSide(boolean side) {
		this.side = side;
	}
	
	public boolean isFront() {
		return side;
	}

    */
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeParcelable(unit, flags);
		dest.writeInt(box);

		// content
//		dest.writeList(frontAttachments);
//		dest.writeList(backAttachments);
	}

	public static final Parcelable.Creator<Word> CREATOR = new Parcelable.Creator<Word>() {
		public Word createFromParcel(Parcel in) {
			return new Word(in);
		}

		public Word[] newArray(int size) {
			return new Word[size];
		}
	};

	
	@Override
	public String toString() {
		return "id: "+ id;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Word) {
			return id == ((Word) o).id;
		} else {
			return false;
		}
	}
	

	@Override
	public Word clone() {
		Word word = new Word(id);
		word.box = box;
//		word.side = side;
		word.unit = unit;
//		word.frontAttachments = frontAttachments;
//        word.backAttachments = backAttachments;
		return word;
	}


}
