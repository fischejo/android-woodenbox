package de.pecheur.colorbox.database;

/**
 * Created by fischejo on 15.10.14.
 */
public class SqliteUtils {
    /**
     * Converts an array of ids to a comma seperated string.
     * @param ids Array of ids
     * @return Comma seperated String of ids
     */
    public static String convertIDs(long[] ids) {
        String result = "";
        for (int i = 0; i < ids.length; i++) result += ","+ String.valueOf(ids[i]);
        return result.substring(1);
    }
}
