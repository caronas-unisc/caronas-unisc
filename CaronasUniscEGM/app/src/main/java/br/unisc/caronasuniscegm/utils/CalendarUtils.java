package br.unisc.caronasuniscegm.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mfelipe on 19/10/2015.
 */
public class CalendarUtils {

    public static String dateToString( Date date ){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
}
