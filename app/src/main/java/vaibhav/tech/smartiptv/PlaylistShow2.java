package vaibhav.tech.smartiptv;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;

import vaibhav.tech.smartiptv.adapters.IptvAdapter;
import vaibhav.tech.smartiptv.app.ChromecastApp;
import vaibhav.tech.smartiptv.cast.CastVideo;
import vaibhav.tech.smartiptv.cast.ICastStatus;
import vaibhav.tech.smartiptv.data.IptvDataSource;
import vaibhav.tech.smartiptv.iptv.M3UItem;
import vaibhav.tech.smartiptv.iptv.M3UParser;
import vaibhav.tech.smartiptv.iptv.M3UPlaylist;
import vaibhav.tech.smartiptv.media.Video;
import vaibhav.tech.smartiptv.utils.FileManager;
import vaibhav.tech.smartiptv.utils.Utils;
import vaibhav.tech.smartiptv.web.db.WebBrowserDbHelper;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class PlaylistShow2 extends AppCompatActivity implements ICastStatus {
    WebBrowserDbHelper dbHelper;
    IptvAdapter adapter;
    Video playAfterConnect;
    private CastStateListener mCastStateListener;
    private final int GET_FILE_DIALOG_CODE = 1005;
    private EditText addressET = null;
    private EditText titleET = null;
    M3UParser parser = new M3UParser();
    private String log="Smart_IPTV";
//    LoggerWrapper logger = null;

    private ProgressBar spinner;
    String last_played_title="",last_played_url="";
    @Override
    protected void onPostResume()
    {
        super.onPostResume();
        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        if (!last_played_title.equalsIgnoreCase("")&&!last_played_url.equalsIgnoreCase("")&&!last_played_url.endsWith(".m3u8")
                &&!last_played_url.endsWith(".mp4")&&!last_played_url.endsWith(".mkv"))
        {
             processFile(last_played_title,last_played_url);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_show2);

        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //AppBrain.addTestDevice("9993f2aa81232f67");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Playlists");
        Button button2=(Button)findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),IptvActivity.class));
            }
        });

        ChromecastApp.Instance().mCastContext = CastContext.getSharedInstance(this);


        if(Build.VERSION.SDK_INT>=23)
        {
            requestStoragePermission();
        }
        else {

        }
        dbHelper = new WebBrowserDbHelper(this);
           adapter = new IptvAdapter(this, new IptvDataSource(this));

        mCastStateListener = new CastStateListener() {
            @Override
            public void onCastStateChanged(int newState) {
                if (newState != CastState.NO_DEVICES_AVAILABLE) {
                    //showIntroductoryOverlay();
                }

                if (newState == CastState.CONNECTED) {
                    if (playAfterConnect != null) {
                        Cast(playAfterConnect.getTitle(), playAfterConnect.getUrl(),playAfterConnect.getMimeType(),(ArrayList<Video>)playAfterConnect.getOtherVideos());
                        playAfterConnect = null;
                    }
                }

            }
        };

        ListView lv = (ListView) findViewById(R.id.listView3);

        LayoutInflater inflater = LayoutInflater.from(PlaylistShow2.this); // 1
        final View headerView = inflater.inflate(R.layout.iptv_listview_header, null);

        addressET = (EditText) headerView.findViewById(R.id.addressText);
        titleET = (EditText) headerView.findViewById(R.id.iptvName);
        lv.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                //Log.d("IPTV", "Context menu");
                PlaylistShow2.super.onCreateContextMenu(menu, v, menuInfo);

                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                if (info.id >= 0) {
                    getMenuInflater().inflate(R.menu.iptv_list_context_menu, menu);
                }

            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                cursor.moveToFirst();
                final String title = cursor.getString(1);
                final String url = cursor.getString(2);
                Log.d("TEST",title+" "+url);
                cursor.close();
                last_played_title=title;
                last_played_url=url;
                new Thread(new Runnable() {
                    public void run() {
                        startSpinner();
                        processFile(title, url);
                        stopSpinner();
                    }}).start();
            }
        });
        lv.setAdapter(adapter);
        this.spinner = (ProgressBar) findViewById(R.id.spinner2);
        this.spinner.setVisibility(View.GONE);
    }
    private void requestStoragePermission()
    {
        String permission = android.Manifest.permission.READ_EXTERNAL_STORAGE;
        int grant = ContextCompat.checkSelfPermission(this,permission);

        String permission1 = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int grant1 = ContextCompat.checkSelfPermission(this,permission1);


        if(grant != PackageManager.PERMISSION_GRANTED && grant1 != PackageManager.PERMISSION_GRANTED)
        {
            String[] pl = new String[2];
            pl[0] = permission;
            pl[1] = permission1;
            ActivityCompat.requestPermissions(this, pl,1);

        }
        else{

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        }
        else{

            Toast.makeText(this,"Permission not granted",Toast.LENGTH_LONG).show();
        }
    }

    private void sendMessage(int what, Bundle payload) {
        Message msg = handler.obtainMessage();
        msg.what = what;
        if(payload!=null)
            msg.setData(payload);
        handler.sendMessage(msg);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == 0) {
                adapter.Update();
                addressET.getText().clear();
                titleET.getText().clear();
            }

            if (msg.what == 1) {
                Toast.makeText(PlaylistShow2.this, getString(R.string.iptv_not_supported), Toast.LENGTH_LONG).show();
                ;
            }

            if(msg.what == 2)
            {
                ArrayList<Video> videos=(ArrayList<Video>)msg.getData().getSerializable("videos");
                FindVideos(videos);
            }

            if(msg.what == 3)
            {
                Video video = (Video) msg.getData().getSerializable("video");
                Cast(video.getTitle(),video.getUrl(),video.getMimeType(),null);

            }
        }
    };

    private void processFile(final String title, String url)
    {
        Log.d("Smart_IPTV",title+" "+url);
        File file = new File(url);
        String ext = FilenameUtils.getExtension(file.getName());
        Log.d("Smart_IPTV",ext);
        if(url.endsWith(".m3u8")||url.endsWith(".mp4")||url.endsWith(".mkv"))
        {
            String redirected_url="";
            try {
                URLConnection con = new URL( url ).openConnection();
                String original_url=con.getURL().toString();
                con.connect();
                InputStream is = con.getInputStream();
              redirected_url=con.getURL().toString();
                is.close();
                if(!original_url.equals(redirected_url))
                    url=redirected_url;
            } catch (final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "An Error Occurred \n"+e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            final String ff=redirected_url;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {


        if (mediaRouteMenuItem.isVisible() == true)
        {
            if (ChromecastApp.Instance().mCastContext.getCastState() == CastState.CONNECTED)
                Cast(title, ff,"",null);
        }
               else

        {
            SharedPreferences sharedPreferences=getSharedPreferences("smartiptv_mediaPlayer", Context.MODE_PRIVATE);
            int med_id=sharedPreferences.getInt("media_player_id",R.id.one);
            String url_onClick= ff;
            if(med_id==R.id.one) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setPackage("com.mxtech.videoplayer.ad");
                    i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                    startActivity(i);
                }
                catch(Exception e){
                    Toast.makeText(getApplicationContext(), "MX Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
                }
            }
            if(med_id==R.id.three)
            {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setPackage("com.bsplayer.bspandroid.free");
                    i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                    startActivity(i);
                }
                catch(Exception e){
                    Toast.makeText(PlaylistShow2.this, "BS Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
                }
            }
            if(med_id==R.id.five)
            {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setPackage("com.wondershare.player");
                    i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                    startActivity(i);
                }
                catch(Exception e){
                    Toast.makeText(PlaylistShow2.this, "Wondershare Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
                }


            }
            if(med_id==R.id.four)
            {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setPackage("com.kmplayer");
                    i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                    startActivity(i);
                }
                catch(Exception e){
                    Toast.makeText(PlaylistShow2.this, "KM Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
                }


            }
            if(med_id==R.id.two)
            {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setPackage("org.videolan.vlc");
                    i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                    startActivity(i);
                }
                catch(Exception e){
                    Toast.makeText(PlaylistShow2.this, "VLC Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
                }


            }
        }
                }
            });
        }
        else if(ext.equalsIgnoreCase("mpd") || ext.equalsIgnoreCase("xml"))
        {
            Video video = new Video(url,"application/dash+xml","",title);
            Bundle data = new Bundle();
            data.putSerializable("video",video);
            sendMessage(3,data);
        }
        else if(ext.equalsIgnoreCase("m3u8"))
        {
            try {
                InputStream is = null;
                if (url.startsWith("http")) {
                    is = new URL(url).openStream();
                } else {
                    is = new FileInputStream(url.replace("file://",""));
                }
                M3UPlaylist playlist = parser.parseFile(is);
                if(playlist.getPlaylistItems().size()>0)
                {
                    ArrayList<Video> videos = new ArrayList<>();
                    for(int i = 0;i<playlist.getPlaylistItems().size();i++)
                    {
                        File fl = new File(playlist.getPlaylistItems().get(i).getItemUrl());
                        Video vid  = new Video(playlist.getPlaylistItems().get(i).getItemName(),playlist.getPlaylistItems().get(i).getItemUrl());
                        String mime = Utils.getMimeType(vid.getUrl());
                        vid.setMimeType(mime);
                        videos.add(vid);
                    }
                    String Titles[]=new String[videos.size()];
                    String Urls[]=new String[videos.size()];
                    for(int i=0;i<videos.size();i++){
                        Titles[i]=videos.get(i).getTitle();
                        Urls[i]=videos.get(i).getUrl();
                    }
                    if(videos.size()>0) {
                        Intent i = new Intent(getApplicationContext(), SinglePlaylist.class);
                        i.putExtra("names", Titles);
                        i.putExtra("urls", Urls);
                        startActivity(i);
                        finish();
                    }
                    else Toast.makeText(this, "Invalid File", Toast.LENGTH_SHORT).show();
                }
                else{
                    Video video = new Video(url,"application/x-mpegURL","",title);
                    Bundle data = new Bundle();
                    data.putSerializable("video",video);
                    sendMessage(3,data);
                }
            }
            catch (Exception ex)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PlaylistShow2.this, "Invalid Format!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
        else if (ext.equalsIgnoreCase("m3u"))
        {
            try {
                InputStream is;
                if (url.startsWith("http")) {
                    is = new URL(url).openStream();
                } else {
                    is = new FileInputStream(url.replace("file://",""));
                }

                M3UPlaylist playlist = parser.parseFile(is);
                int temp=playlist.getPlaylistItems().size();
                if(temp>0)
                {
                    ArrayList<Video> videos = new ArrayList<>();
                    for(int i = 0;i<temp;i++)
                    {
//                        File fl = new File(playlist.getPlaylistItems().get(i).getItemUrl());
//                        String ext1 = FilenameUtils.getExtension(fl.getName());

                        Video vid  = new Video(playlist.getPlaylistItems().get(i).getItemName(),playlist.getPlaylistItems().get(i).getItemUrl());
                        String mime = Utils.getMimeType(vid.getUrl());
                        vid.setMimeType(mime);
                        videos.add(vid);
                    }
                    Log.d(log,String.valueOf(temp));
                    if(videos.size()>0) {
                        String Titles[]=new String[temp];
                        String Urls[]=new String[temp];
                        for(int i=0;i<temp;i++){
                            Titles[i]=videos.get(i).getTitle();
                            Urls[i]=videos.get(i).getUrl();
                        }
                        Intent i = new Intent(getApplicationContext(), SinglePlaylist.class);
                        i.putExtra("names", Titles);
                        i.putExtra("urls", Urls);
                        startActivity(i);
                        finish();
                    }
                    else
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(PlaylistShow2.this, "Invalid file or broken link.", Toast.LENGTH_SHORT).show();

                            }
                        });
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PlaylistShow2.this, "Invalid file or broken link.", Toast.LENGTH_SHORT).show();

                        }
                    });

                }
            }
            catch (Exception ex)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PlaylistShow2.this, "Invalid file or broken link.", Toast.LENGTH_SHORT).show();      }
                });
                Log.d("Smart_IPTV",ex.toString());
            }
        }


        else {
            try {

                InputStream is;
                if (url.startsWith("http")) {
                    is = new URL(url).openStream();
                } else {
                    is = new FileInputStream(url.replace("file://", ""));
                }

                M3UPlaylist playlist = parser.parseFile(is);

                M3UItem item = playlist.getPlaylistItems().get(0);


                if (playlist.getPlaylistItems().size() > 0) {
                    ArrayList<Video> videos = new ArrayList<>();
                    for (int i = 0; i < playlist.getPlaylistItems().size(); i++) {
                        File fl = new File(playlist.getPlaylistItems().get(i).getItemUrl());
                        String ext1 = FilenameUtils.getExtension(fl.getName());

                        Video vid = new Video(playlist.getPlaylistItems().get(i).getItemName(), playlist.getPlaylistItems().get(i).getItemUrl());
                        String mime = Utils.getMimeType(vid.getUrl());
                        vid.setMimeType(mime);
                        videos.add(vid);
                    }

                    String Titles[] = new String[videos.size()];
                    String Urls[] = new String[videos.size()];
                    for (int i = 0; i < videos.size(); i++) {
                        Titles[i] = videos.get(i).getTitle();
                        Urls[i] = videos.get(i).getUrl();
                    }
                    if(videos.size()>0)
                    {
                    Intent i = new Intent(getApplicationContext(), SinglePlaylist.class);
                    i.putExtra("names", Titles);
                    i.putExtra("urls", Urls);
                    startActivity(i);
                    finish();
                    }
                    else
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Invalid File", Toast.LENGTH_SHORT).show();
                            }
                        });

                } else {
                    Video video = new Video(url, "application/x-mpegURL", "", title);
                    Bundle data = new Bundle();
                    data.putSerializable("video", video);
                    sendMessage(3, data);
                }
            } catch (final Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PlaylistShow2.this, "Invalid Format!", Toast.LENGTH_SHORT).show();
                    }
                });
                ex.printStackTrace();
            }
        }
    }
    int selectedVideo = 0;

    public void FindVideos(final ArrayList<Video> videos) {

        final List<CharSequence> titles = new ArrayList<CharSequence>();
        for (Video video : videos) {
            //Log.d("ChromecastApp", video.getTitle() + " || " + video.getUrl());
            titles.add(video.getTitle());
        }
        selectedVideo = 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(PlaylistShow2.this);
        builder.setTitle(R.string.select_video_title).setSingleChoiceItems(titles.toArray(new CharSequence[titles.size()]),
                0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedVideo = which;
            }
        }).setPositiveButton(R.string.cast, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mediaRouteMenuItem.isVisible() == true) {
                            dialog.cancel();
                            if (selectedVideo > -1) {

                                if (ChromecastApp.Instance().mCastContext.getCastState() == CastState.CONNECTED) {
                                    Cast(titles.get(selectedVideo).toString(),videos.get(selectedVideo).getUrl(),videos.get(selectedVideo).getMimeType(),videos);
                                    selectedVideo = -1;
                                } else {

                                    if (!((Activity) PlaylistShow2.this).isFinishing()) {

                                        if (!((Activity) PlaylistShow2.this).isFinishing()) {
                                            SharedPreferences sharedPreferences=getSharedPreferences("smartiptv_mediaPlayer", Context.MODE_PRIVATE);
                                            int med_id=sharedPreferences.getInt("media_player_id",R.id.one);
                                            String url_onClick=videos.get(selectedVideo).getUrl();
                                            if(med_id==R.id.one) {
                                                try {
                                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                                    i.setPackage("com.mxtech.videoplayer.ad");
                                                    i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                                                    startActivity(i);
                                                }
                                                catch(Exception e){
                                                    Toast.makeText(getApplicationContext(), "MX Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            if(med_id==R.id.three)
                                            {
                                                try {
                                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                                    i.setPackage("com.bsplayer.bspandroid.free");
                                                    i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                                                    startActivity(i);
                                                }
                                                catch(Exception e){
                                                    Toast.makeText(PlaylistShow2.this, "BS Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            if(med_id==R.id.five)
                                            {
                                                try {
                                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                                    i.setPackage("com.wondershare.player");
                                                    i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                                                    startActivity(i);
                                                }
                                                catch(Exception e){
                                                    Toast.makeText(PlaylistShow2.this, "Wondershare Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
                                                }


                                            }
                                            if(med_id==R.id.four)
                                            {
                                                try {
                                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                                    i.setPackage("com.kmplayer");
                                                    i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                                                    startActivity(i);
                                                }
                                                catch(Exception e){
                                                    Toast.makeText(PlaylistShow2.this, "KM Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
                                                }


                                            }
                                            if(med_id==R.id.two)
                                            {
                                                try {
                                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                                    i.setPackage("org.videolan.vlc");
                                                    i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                                                    startActivity(i);
                                                }
                                                catch(Exception e){
                                                    Toast.makeText(PlaylistShow2.this, "VLC Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
                                                }


                                            }
                                            //  Toast.makeText(IptvActivity.this, titles.get(selectedVideo).toString()+" "+videos.get(selectedVideo).getUrl(), Toast.LENGTH_SHORT).show();
                                            // Toast.makeText(IptvActivity.this, getString(R.string.not_device), Toast.LENGTH_LONG).show();
                                        }

                                        //Toast.makeText(PlaylistShow2.this, getString(R.string.not_device), Toast.LENGTH_LONG).show();
                                    }



                                    // Toast.makeText(WebViewActivity.this,getString(R.string.not_connected),Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        else {
                            //dialog.cancel();
                            if (!((Activity) PlaylistShow2.this).isFinishing()) {

                                //dialog.cancel();
                                if (!((Activity) PlaylistShow2.this).isFinishing()) {
                                    SharedPreferences sharedPreferences=getSharedPreferences("smartiptv_mediaPlayer", Context.MODE_PRIVATE);
                                    int med_id=sharedPreferences.getInt("media_player_id",R.id.one);
                                    String url_onClick=videos.get(selectedVideo).getUrl();
                                    if(med_id==R.id.one) {
                                        try {
                                            Intent i = new Intent(Intent.ACTION_VIEW);
                                            i.setPackage("com.mxtech.videoplayer.ad");
                                            i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                                            startActivity(i);
                                        }
                                        catch(Exception e){
                                            Toast.makeText(getApplicationContext(), "MX Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    if(med_id==R.id.three)
                                    {
                                        try {
                                            Intent i = new Intent(Intent.ACTION_VIEW);
                                            i.setPackage("com.bsplayer.bspandroid.free");
                                            i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                                            startActivity(i);
                                        }
                                        catch(Exception e){
                                            Toast.makeText(PlaylistShow2.this, "BS Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    if(med_id==R.id.five)
                                    {
                                        try {
                                            Intent i = new Intent(Intent.ACTION_VIEW);
                                            i.setPackage("com.wondershare.player");
                                            i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                                            startActivity(i);
                                        }
                                        catch(Exception e){
                                            Toast.makeText(PlaylistShow2.this, "Wondershare Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
                                        }


                                    }
                                    if(med_id==R.id.four)
                                    {
                                        try {
                                            Intent i = new Intent(Intent.ACTION_VIEW);
                                            i.setPackage("com.kmplayer");
                                            i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                                            startActivity(i);
                                        }
                                        catch(Exception e){
                                            Toast.makeText(PlaylistShow2.this, "KM Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
                                        }


                                    }
                                    if(med_id==R.id.two)
                                    {
                                        try {
                                            Intent i = new Intent(Intent.ACTION_VIEW);
                                            i.setPackage("org.videolan.vlc");
                                            i.setDataAndType(Uri.parse(url_onClick), "video/h264");
                                            startActivity(i);
                                        }
                                        catch(Exception e){
                                            Toast.makeText(PlaylistShow2.this, "VLC Player Not Installed.Please Install it using Play Store.", Toast.LENGTH_SHORT).show();
                                        }


                                    }

                                }

                                //Toast.makeText(PlaylistShow2.this, getString(R.string.not_device), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }

        ).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                last_played_title="";
                last_played_url="";
                dialog.cancel();
            }
        });

               /* if (findFilesDialog != null) {
                    findFilesDialog.dismiss();
                }
                findFilesDialog = builder.create();*/
        if (!((Activity) PlaylistShow2.this).isFinishing()) {
            //    findFilesDialog.show();
        }


        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void Cast(String title, String url,String mime,ArrayList<Video> videos) {
        Video video = new Video(title, url);

        video.setMimeType(mime);
        if(videos!=null)
        {
            video.setOtherVideo(videos);
        }
        if (mediaRouteMenuItem.isVisible() == true) {

            if (ChromecastApp.Instance().mCastContext.getCastState() == CastState.CONNECTED) {

                CastVideo castVideo = new CastVideo(PlaylistShow2.this);
                castVideo.setStatusListener(this);
                castVideo.Cast(video);

            } else {
                if (mediaRouteMenuItem != null) {
                    ActionProvider provider = MenuItemCompat.getActionProvider(mediaRouteMenuItem);

                    playAfterConnect = video;
                    provider.onPerformDefaultAction();
                }
                //Toast.makeText(WebViewActivity.this,getString(R.string.not_connected),Toast.LENGTH_LONG).show();
            }
        } else {

            if (!((Activity) PlaylistShow2.this).isFinishing()) {
             //   Toast.makeText(PlaylistShow2.this, getString(R.string.not_device)+"705APPROX", Toast.LENGTH_LONG).show();
            }
        }
    }

    private MenuItem mediaRouteMenuItem;

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //getMenuInflater().inflate(R.menu.iptv_options,menu);
        getMenuInflater().inflate(R.menu.browse, menu);
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.position >= 0) {
            getMenuInflater().inflate(R.menu.iptv_list_context_menu, menu);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_FILE_DIALOG_CODE) {
            if (resultCode != RESULT_OK) return;

            Uri selectedfile = data.getData(); //The uri with the location of the file

            String message = String.format("%s", selectedfile);


            try {
                String path = FileManager.getPath(PlaylistShow2.this, selectedfile);


                addressET.setText("file://" + path);

                File file = new File(path);
                titleET.setText(file.getName());
                //   Toast.makeText(IptvActivity.this, path, Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.player)
        {
            startActivity(new Intent(getApplicationContext(),SetPlayer.class));
        }
        else if(item.getItemId()==R.id.playlist)
        {
            startActivity(new Intent(getApplicationContext(),PlaylistShow2.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final ListView grid = (ListView) findViewById(R.id.listView3);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        Cursor cur = (Cursor) grid.getItemAtPosition(info.position);
        cur.moveToFirst();
        Long id = cur.getLong(0);
        final String title = cur.getString(1);
        final String url = cur.getString(2);
        cur.close();
        switch (item.getItemId()) {

            case R.id.menuOpen: {
                new Thread(new Runnable() {
                    public void run() {
                        processFile(title, url);
                    }}).start();
                return true;
            }
            case R.id.menuRemove:
                dbHelper.deleteIptv(id);
                adapter.Update();
                return true;
        }

        return false;
    }



    @Override
    public void onStopped() {

    }


    @Override
    protected void onPause() {
        ChromecastApp.Instance().mCastContext.removeCastStateListener(mCastStateListener);
        super.onPause();
    }

    @Override
    protected void onResume() {
        ChromecastApp.Instance().mCastContext.addCastStateListener(mCastStateListener);

        super.onResume();
    }

    @Override
    public void onError(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PlaylistShow2.this);
        builder.setTitle(R.string.iptv_unable_cast_title)
                .setMessage(R.string.iptv_unable_cast)
                .setCancelable(true)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void startSpinner() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(View.VISIBLE);
            }
        });
    }

    private void stopSpinner() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(View.GONE);
            }
        });
    }



    private InputStream OpenHttpConnection(final String url) throws IOException
    {
        InputStream inputStream=null;

        try {
            URLConnection conn = null;

            URL url1 = new URL(url);
            conn = url1.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) conn;
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream();
                //Log.d("STRY","ALL OK");

            }

        }
        catch (Exception e){

            //Log.d("STRY",e.toString());
        }
        return inputStream;
    }
}


