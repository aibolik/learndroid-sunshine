package kz.learndroid.app.sunshine;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by aibol on 3/18/16.
 */
public class Utility {
    static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }

    static String formatTemperature(double temperature) {
        return String.format("%.0f", temperature);
    }

}
