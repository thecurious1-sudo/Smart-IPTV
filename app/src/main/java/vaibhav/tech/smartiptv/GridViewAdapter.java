package vaibhav.tech.smartiptv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import vaibhav.tech.smartiptv.R;

import vaibhav.tech.smartiptv.app.ChromecastApp;
import vaibhav.tech.smartiptv.cast.CastVideo;
import vaibhav.tech.smartiptv.media.Video;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by hp on 6/14/2018.
 */

public class GridViewAdapter extends BaseAdapter {
    private Context context;
    String[] names,urls;
    int ids_material_icons[]={R.drawable.a,R.drawable.b,R.drawable.c,R.drawable.d,R.drawable.e,R.drawable.f,R.drawable.g,R.drawable.h
    ,R.drawable.i,R.drawable.j,R.drawable.k,R.drawable.l,R.drawable.m,R.drawable.n,R.drawable.o,R.drawable.p,R.drawable.q,R.drawable.r,
    R.drawable.s,R.drawable.t,R.drawable.u,R.drawable.v,R.drawable.w,R.drawable.x,R.drawable.y,R.drawable.z};
    GridViewAdapter(Context ctx,String[] name,String[] url)
    {
        context=ctx;
        names=name;
        urls=url;
        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }
    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView=layoutInflater.inflate(R.layout.custom_grid_playlist,viewGroup,false);
        TextView textView=(TextView)customView.findViewById(R.id.textView2);
        ImageView imageView=(ImageView)customView.findViewById(R.id.imageView2);

        //int idA=R.drawable.;
        //imageView.setImageResource(R.drawable.);
        try {
            textView.setText(names[i]);
            Log.d("TESTING_", names[i]);
            char ch = Character.toUpperCase(names[i].charAt(0));
            int ch1 = ch - 65;
            if (ch1 >= 0 && ch1 <= 25)
                imageView.setImageResource(ids_material_icons[ch1]);
            else
                imageView.setImageResource(ids_material_icons[0]);
        }catch (Exception e){
            return view;
        }
        //imageView.setImageResource(ids_material_icons[ch1]);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChromecastApp.Instance().mCastContext = CastContext.getSharedInstance(context);
                if (ChromecastApp.Instance().mCastContext.getCastState() == CastState.CONNECTED)
                {
                    String url=urls[i].replace(".ts",".m3u8");
                    String redirected_url="";
                    try {
                        URLConnection con = new URL( url ).openConnection();
                        String original_url=con.getURL().toString();
                        con.connect();
                        String connected_url=con.getURL().toString();
                        InputStream is = con.getInputStream();
                        redirected_url=con.getURL().toString();

                        is.close();
                        if(!original_url.equals(redirected_url))
                            url=redirected_url;
                        //  Log.d("VAIBHAV",scanner.nextLine());
                    } catch (IOException e) {
                        Log.d("VAIBHAV",e.toString());
                    }
                    final String ff=redirected_url;
                  //  Toast.makeText(context,ff, Toast.LENGTH_SHORT).show();
                    Cast(names[i],ff, "", null);
                }
                else
                playMobile(urls[i]);
            }
        });
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChromecastApp.Instance().mCastContext = CastContext.getSharedInstance(context);
                if (ChromecastApp.Instance().mCastContext.getCastState() == CastState.CONNECTED) {
                    String url=urls[i].replace(".ts",".m3u8");
                    String redirected_url="";
                    try {
                        URLConnection con = new URL( url ).openConnection();
                        String original_url=con.getURL().toString();
                        con.connect();
                        String connected_url=con.getURL().toString();
                        InputStream is = con.getInputStream();
                        redirected_url=con.getURL().toString();

                        is.close();
                        if(!original_url.equals(redirected_url))
                            url=redirected_url;
                        //  Log.d("VAIBHAV",scanner.nextLine());
                    } catch (IOException e) {
                        Log.d("VAIBHAV",e.toString());
                    }
                    final String ff=redirected_url;
                 //  Toast.makeText(context,ff, Toast.LENGTH_SHORT).show();
                    Cast(names[i],ff, "", null);
                }
                //
                else
                playMobile(urls[i]);
            }
        });
        return customView;
    }
    public void playMobile(String url_onClick)
    {
        SharedPreferences sharedPreferences=context.getSharedPreferences("smartiptv_mediaPlayer", Context.MODE_PRIVATE);
        int med_id=sharedPreferences.getInt("media_player_id",R.id.one);

        if(med_id==R.id.one) {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setPackage("com.mxtech.videoplayer.ad");
                i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
            catch(Exception e){
                Log.d("TEST",e.toString());
                Toast.makeText(context, "MX Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
            }
        }
        if(med_id==R.id.three)
        {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setPackage("com.bsplayer.bspandroid.free");
                i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(i);
            }
            catch(Exception e){
                Toast.makeText(context, "BS Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
            }
        }
        if(med_id==R.id.five)
        {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setPackage("com.wondershare.player");
                i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(i);
            }
            catch(Exception e){
                Toast.makeText(context, "Wondershare Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
            }


        }
        if(med_id==R.id.four)
        {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setPackage("com.kmplayer");
                i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(i);
            }
            catch(Exception e){
                Toast.makeText(context, "KM Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
            }


        }
        if(med_id==R.id.two)
        {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setPackage("org.videolan.vlc");
                i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
            catch(Exception e){
                Toast.makeText(context, "VLC Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
            }


        }
        //  Toast.makeText(IptvActivity.this, titles.get(selectedVideo).toString()+" "+videos.get(selectedVideo).getUrl(), Toast.LENGTH_SHORT).show();
        // Toast.makeText(IptvActivity.this, getString(R.string.not_device), Toast.LENGTH_LONG).show();


    }


    private void Cast(String title, String url,String mime,ArrayList<Video> videos) {
        try {
            Video video = new Video(title, url);
            //logger.debug("Cast video " + url);
            video.setMimeType(mime);
            if (videos != null) {
                video.setOtherVideo(videos);
            }
            if (ChromecastApp.Instance().mCastContext.getCastState() == CastState.CONNECTED) {

                CastVideo castVideo = new CastVideo(context);
                //castVideo.setStatusListener(this);
                castVideo.Cast(video);

            } else {
                Toast.makeText(context, "Not Connected To Chromecast!", Toast.LENGTH_SHORT).show();
            }
            //Toast.makeText(WebViewActivity.this,getString(R.string.not_connected),Toast.LENGTH_LONG).show();

        }
        catch (Exception e)
        {
            Toast.makeText(context, "Cannot play this Video.", Toast.LENGTH_SHORT).show();
        }
    }

}
