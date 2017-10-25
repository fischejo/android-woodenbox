package de.pecheur.colorbox.database.columns;

public class WordColumn extends BaseColumn{



    public static final int MIN_BOX_VALUE = 0;
    public static final int MAX_BOX_VALUE = 6;
    public static final String TABLE = "words";

    /**
* Referring id to an existing unit, stored as integer
*
* @see UnitColumn#TABLE
*/
public static final String UNIT = "unit";
    /**
* Each card belongs to a box. A new inserted card comes into the box
* {@link WordColumn
* umns#MIN_BOX_VALUE}. This key is automatically set by an
* insert.
*
* @see WordColumn#MIN_BOX_VALUE
* @see WordColumn#MAX_BOX_VALUE
*/
public static final String BOX = "box";
    /**
* Returns last query timestamp.
*
* @see System#currentTimeMillis
*/
public static final String TIME = "time";
}
