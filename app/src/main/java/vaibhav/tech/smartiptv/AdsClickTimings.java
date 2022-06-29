package vaibhav.tech.smartiptv;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Vaibhav on 04-02-2019.
 */

public class AdsClickTimings {
    Context context;
    SharedPreferences sharedPreferences;
    String name="Smart_IPTV_lastAdClick";
    AdsClickTimings(Context c)
    {
        context=c;
        sharedPreferences=context.getSharedPreferences(name,Context.MODE_PRIVATE);
    }

    boolean showAdOrNot()
    {
        DateTime date=new DateTime();
        String last_ad_click_time=getLastTimeClick();
        //Log.d("VJXXX",last_ad_click_time);
        if(last_ad_click_time.equals("")) {
            return true;
        }
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        DateTime dt = formatter.parseDateTime(last_ad_click_time);
        //int diff=Minutes.minutesBetween(date,dt).getMinutes();
        int diffS= Seconds.secondsBetween(date,dt).getSeconds();
        diffS=Math.abs(diffS);
        if(diffS>=1800)
            return true;
        else
            return false;
    }

    String getLastTimeClick()
    {
        return sharedPreferences.getString(name,"");
    }

    void setLastTimeClick()
    {
        SharedPreferences.Editor editor=sharedPreferences.edit();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        editor.putString(name,formatter.format(date));
        editor.apply();
    }
    int getDiffS()
    {
        DateTime date=new DateTime();
        String last_ad_click_time=getLastTimeClick();
        if(last_ad_click_time.equals("")) {
            return 1801;
        }
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        DateTime dt = formatter.parseDateTime(last_ad_click_time);
        //int diff=Minutes.minutesBetween(date,dt).getMinutes();
        int diffS= Seconds.secondsBetween(date,dt).getSeconds();
        return Math.abs(diffS);
    }
}
