package br.unisc.caronasuniscegm.utils;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.unisc.caronasuniscegm.R;

/**
 * Created by mfelipe on 19/10/2015.
 */
public class CalendarUtils {

    public static String dateToString( Date date ){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    public static List<String> getUpcommingDaysOfTheWeek(Context context){
        List<String> listUpcommingDays = new ArrayList<String>();
        Calendar now = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        now.setTime(date);
        calendar.setTime(date);

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        if( now.getTimeInMillis() < calendar.getTimeInMillis() ){
            listUpcommingDays.add(context.getResources().getString(R.string.field_monday));
        }

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        if( now.getTimeInMillis() < calendar.getTimeInMillis() ){
            listUpcommingDays.add(context.getResources().getString(R.string.field_tuesday));
        }

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        if( now.getTimeInMillis() < calendar.getTimeInMillis() ){
            listUpcommingDays.add(context.getResources().getString(R.string.field_wednesday));
        }

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        if( now.getTimeInMillis() < calendar.getTimeInMillis() ){
            listUpcommingDays.add(context.getResources().getString(R.string.field_thursday));
        }

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        if( now.getTimeInMillis() < calendar.getTimeInMillis() ){
            listUpcommingDays.add(context.getResources().getString(R.string.field_friday));
        }
        return listUpcommingDays;
    }

    public static String dateToDayOfTheWeek( Context context, Date date ){

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if( calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY ){
            return context.getResources().getString(R.string.field_monday);
        }

        if( calendar.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY ){
            return context.getResources().getString(R.string.field_tuesday);
        }

        if( calendar.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY ){
            return context.getResources().getString(R.string.field_wednesday);
        }

        if( calendar.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY ){
            return context.getResources().getString(R.string.field_thursday);
        }

        if( calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY ){
            return context.getResources().getString(R.string.field_friday);
        }

        return null;
    }
}
