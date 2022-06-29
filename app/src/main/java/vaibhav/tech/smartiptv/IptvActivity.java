package vaibhav.tech.smartiptv;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.appbrain.AdId;
import com.appbrain.AppBrain;
import com.appbrain.InterstitialBuilder;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;

import vaibhav.tech.smartiptv.adapters.IptvAdapter;
import vaibhav.tech.smartiptv.app.ChromecastApp;
import vaibhav.tech.smartiptv.cast.CastVideo;
import vaibhav.tech.smartiptv.cast.ICastStatus;
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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;


public class IptvActivity extends AppCompatActivity implements ICastStatus {
    WebBrowserDbHelper dbHelper;
    IptvAdapter adapter;
    Video playAfterConnect;
    private CastStateListener mCastStateListener;
    private final int GET_FILE_DIALOG_CODE = 1005;
    private EditText addressET = null;
    private EditText titleET = null;
    M3UParser parser = new M3UParser();
    private ProgressBar spinner;
    private InterstitialBuilder interstitialBuilder;
    private String interstitial_main="int-208630";
    private static final String TAG = "IptvActivityLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iptv);



        //PRELOADING THE AD WHICH ON CLOSING OPENS PLAYLISTWO2.CLASS AS SUCCESSFULLY INSERTED FIL
        //AppBrain.addTestDevice("d3566dc4d6b84807");
        interstitialBuilder = InterstitialBuilder.create()
                .setAdId(AdId.custom(interstitial_main))
                .setOnDoneCallback(new Runnable() {
                    @Override
                    public void run() {
                        // Preload again, so we can use interstitialBuilder again.
                        interstitialBuilder.preload(getApplicationContext());
                        startActivity(new Intent(getApplicationContext(), PlaylistShow2.class));
                    }
                }).preload(this);





        //CHECKING IF THERE IS AN APP UPDATE
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://bhaluapps.info/VJ/Smart%20IPTV/main_19.txt";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        if (response.trim().equals("YES")) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(IptvActivity.this);
                            builder1.setTitle("Update App");
                            builder1.setMessage("New update available for Smart IPTV.");
                            builder1.setCancelable(false);
                            builder1.setNeutralButton("Update",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            try {
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setData(Uri.parse("market://details?id=vaibhav.tech.smartiptv"));
                                                startActivity(intent);
                                            } catch (Exception e) {
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=vaibhav.tech.smartiptv"));
                                                startActivity(intent);
                                            }
                                        }
                                    });

                            AlertDialog alert11 = builder1.create();
                            alert11.show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(stringRequest);
        //CHECKING ENDS HERE







        //CHECKING IF THE USER IS SABOTAGER
        RequestQueue queue2 = Volley.newRequestQueue(this);
        String url2 = "http://bhaluapps.info/VJ/Smart%20IPTV/banned_list.txt";
        StringRequest stringRequest2 = new StringRequest(Request.Method.GET, url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            //Log.d("VJXXX",response);
                            String res[] = response.split("#####@@@@@");
                            Scanner scanner = new Scanner(res[0]);
                            Scanner scanner1 = new Scanner(res[1]);
                            String device_imei = "NO-PERMISSION";
                            String device_ipv4 = getIPAddress(true);
                            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                device_imei = "NO-PERMISSION";
                            } else
                                device_imei = telephonyManager.getDeviceId();
                            while (scanner.hasNext()) {
                                if (scanner.next().equals(device_imei)) {
                                    System.exit(0);
                                }
                            }
                            while (scanner1.hasNext()) {
                                if (scanner1.next().equals(device_ipv4)) {
                                    System.exit(0);
                                }
                            }
                        } catch (Exception e) {
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


            }
        });

        // Add the request to the RequestQueue.
        queue2.add(stringRequest2);
        //END CHECKING IF THE USER IS SABOTAGER









        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Smart IPTV");

        ChromecastApp.Instance().mCastContext = CastContext.getSharedInstance(this);

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.MANAGE_DOCUMENTS
//                Manifest.permission.READ_PHONE_STATE
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        dbHelper = new WebBrowserDbHelper(this);
        mCastStateListener = new CastStateListener() {
            @Override
            public void onCastStateChanged(int newState) {
                if (newState != CastState.NO_DEVICES_AVAILABLE) {
                    //showIntroductoryOverlay();
                }

                if (newState == CastState.CONNECTED) {
                    if (playAfterConnect != null) {
                        Cast(playAfterConnect.getTitle(), playAfterConnect.getUrl(), playAfterConnect.getMimeType(), (ArrayList<Video>) playAfterConnect.getOtherVideos());
                        playAfterConnect = null;
                    }
                }

            }
        };

        ListView lv = (ListView) findViewById(R.id.iptvList);
        LayoutInflater inflater = LayoutInflater.from(IptvActivity.this); // 1
        final View headerView = inflater.inflate(R.layout.iptv_listview_header, null);
        addressET = (EditText) headerView.findViewById(R.id.addressText);
        titleET = (EditText) headerView.findViewById(R.id.iptvName);
        final Button chooseButton = (Button) headerView.findViewById(R.id.choosefile);
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent()
                        .setType("*/*")
                        .setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.iptv_select_file)), GET_FILE_DIALOG_CODE);
            }
        });

        final Button addButton = (Button) headerView.findViewById(R.id.iptvAdd);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText addressText = (EditText) headerView.findViewById(R.id.addressText);
                EditText titleText = (EditText) headerView.findViewById(R.id.iptvName);
                final String title = titleText.getText().toString();
                final String url = addressText.getText().toString();
                if (TextUtils.isEmpty(url)) {
                    Toast.makeText(IptvActivity.this, getString(R.string.iptv_error_message), Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "addButton: "+title+"\n"+url+"\n");
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            startSpinner();
                            if (isValidate(url)) {
                                String titleStr = title;
                                if (TextUtils.isEmpty(titleStr)) {
                                    titleStr = FilenameUtils.getName(url);
                                }


                                dbHelper.addIptv(titleStr, url);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(IptvActivity.this, "Playlist Added Successfully!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                interstitialBuilder.show(getApplicationContext());

                                sendMessage(0, null);
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(IptvActivity.this, "Unsupported File Type", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                sendMessage(1, null);
                            }
                            stopSpinner();
                        }
                    });
                    thread.start();

                }
            }
        });


        lv.addHeaderView(headerView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                cursor.moveToFirst();
                final String title = cursor.getString(1);
                final String url = cursor.getString(2);
                cursor.close();
                //logger.info("Item click: Title " + title  + " URL " + url);
                new Thread(new Runnable() {
                    public void run() {
                        processFile(title, url);
                    }
                }).start();
            }
        });
        lv.setAdapter(adapter);
        this.spinner = (ProgressBar) findViewById(R.id.spinner);
        this.spinner.setVisibility(View.GONE);
        registerForContextMenu(lv);
    }






    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }






    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        } else {
            Toast.makeText(this, "Permission not granted. Please grant permission to run the app.", Toast.LENGTH_LONG).show();
        }
    }

    private void sendMessage(int what, Bundle payload) {
        Message msg = handler.obtainMessage();
        msg.what = what;
        if (payload != null)
            msg.setData(payload);
        handler.sendMessage(msg);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == 0) {

                addressET.getText().clear();
                titleET.getText().clear();
            }

            if (msg.what == 1) {
                Toast.makeText(IptvActivity.this, getString(R.string.iptv_not_supported), Toast.LENGTH_LONG).show();

            }

            if (msg.what == 2) {
                ArrayList<Video> videos = (ArrayList<Video>) msg.getData().getSerializable("videos");
                FindVideos(videos);
            }

            if (msg.what == 3) {
                Video video = (Video) msg.getData().getSerializable("video");
                Cast(video.getTitle(), video.getUrl(), video.getMimeType(), null);

            }
        }
    };

    private void processFile(String title, String url) {
        File file = new File(url);
        String ext = FilenameUtils.getExtension(file.getName());

        if (ext.equalsIgnoreCase("mpd") || ext.equalsIgnoreCase("xml")) {
            Video video = new Video(url, "application/dash+xml", "", title);
            Bundle data = new Bundle();
            data.putSerializable("video", video);
            sendMessage(3, data);
        } else if (ext.equalsIgnoreCase("m3u8")) {
            Video video = new Video(url, "application/x-mpegURL", "", title);
            Bundle data = new Bundle();
            data.putSerializable("video", video);
            sendMessage(3, data);
        } else if (ext.equalsIgnoreCase("m3u")) {
            try {
                InputStream is = null;
                if (url.startsWith("http")) {
                    is = new URL(url).openStream();
                } else {
                    is = new FileInputStream(url.replace("file://", ""));
                }

                M3UPlaylist playlist = parser.parseFile(is);
                if (playlist.getPlaylistItems().size() > 0) {
                    ArrayList<Video> videos = new ArrayList<>();
                    for (int i = 0; i < playlist.getPlaylistItems().size(); i++) {
                        Video vid = new Video(playlist.getPlaylistItems().get(i).getItemName(), playlist.getPlaylistItems().get(i).getItemUrl());
                        String mime = Utils.getMimeType(vid.getUrl());
                        vid.setMimeType(mime);
                        videos.add(vid);
                    }
                    Bundle payload = new Bundle();
                    payload.putSerializable("videos", videos);
                    sendMessage(2, payload);
                } else {
                    Video video = new Video(url, "application/x-mpegURL", "", title);
                    Bundle data = new Bundle();
                    data.putSerializable("video", video);
                    sendMessage(3, data);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            //  logger.debug("Item is other format");

            String mime = Utils.getMimeType(ext);
            if (TextUtils.isEmpty(mime)) {
                mime = "video/mp4";
            }

            //Cast(title,url,mime,null);
            Video video = new Video(url, mime, "", title);
            Bundle data = new Bundle();
            data.putSerializable("video", video);
            sendMessage(3, data);
        }
    }

    int selectedVideo = 0;

    public void FindVideos(final ArrayList<Video> videos) {

        final List<CharSequence> titles = new ArrayList<CharSequence>();
        for (Video video : videos) {
            titles.add(video.getTitle());
        }
        selectedVideo = 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(IptvActivity.this);
        builder.setTitle(R.string.select_video_title).setSingleChoiceItems(titles.toArray(new CharSequence[titles.size()]), 0, new DialogInterface.OnClickListener() {
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
                                    Cast(titles.get(selectedVideo).toString(), videos.get(selectedVideo).getUrl(), videos.get(selectedVideo).getMimeType(), videos);
                                    selectedVideo = -1;
                                } else {
                                    dialog.cancel();
                                    Video video = videos.get(selectedVideo);
                                    video.setOtherVideo(videos);
                                    playAfterConnect = video;
                                    ActionProvider provider = MenuItemCompat.getActionProvider(mediaRouteMenuItem);
                                    provider.onPerformDefaultAction();
                                }
                            }
                        } else {
                            dialog.cancel();
                            if (!((Activity) IptvActivity.this).isFinishing()) {
                                Toast.makeText(IptvActivity.this, getString(R.string.not_device), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }

        ).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void Cast(String title, String url, String mime, ArrayList<Video> videos) {
        Video video = new Video(title, url);
        video.setMimeType(mime);
        if (videos != null) {
            video.setOtherVideo(videos);
        }
        if (mediaRouteMenuItem.isVisible() == true) {

            if (ChromecastApp.Instance().mCastContext.getCastState() == CastState.CONNECTED) {
                CastVideo castVideo = new CastVideo(IptvActivity.this);
                castVideo.setStatusListener(this);
                castVideo.Cast(video);

            } else {
                if (mediaRouteMenuItem != null) {
                    ActionProvider provider = MenuItemCompat.getActionProvider(mediaRouteMenuItem);

                    playAfterConnect = video;
                    provider.onPerformDefaultAction();
                }
            }
        } else {
            if (!((Activity) IptvActivity.this).isFinishing()) {
                Toast.makeText(IptvActivity.this, getString(R.string.not_device), Toast.LENGTH_LONG).show();
            }
        }
    }

    private MenuItem mediaRouteMenuItem;

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.iptv_options, menu);
        getMenuInflater().inflate(R.menu.browse, menu);
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_FILE_DIALOG_CODE) {
            if (resultCode != RESULT_OK) return;
            Uri selectedfile = data.getData(); //The uri with the location of the file
            try {
//                File file1 = new File(selectedfile.getPath());
//                String path1 = file1.getAbsolutePath();
                FileUtils fileUtils=new FileUtils(this);
                String path1=fileUtils.getPath(selectedfile);
                Log.d(TAG, "onActivityResult:myWayPAth="+fileUtils.getPath(selectedfile));
//                String path = FileManager.getPath(IptvActivity.this, selectedfile);
                String path=path1;
                Log.d(TAG, "onActivityResult: "+path);
                addressET.setText(path);
                File file = new File("file://" + path);
                titleET.setText(file.getName());
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.d(TAG, "onActivityResult: Exception occurred "+ex.toString());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.player) {
            startActivity(new Intent(getApplicationContext(), SetPlayer.class));
        } else if (item.getItemId() == R.id.playlist) {
            startActivity(new Intent(getApplicationContext(), PlaylistShow2.class));
        } else if (item.getItemId() == R.id.howToUse) {
            startActivity(new Intent(getApplicationContext(), HowToUse.class));
        } else if (item.getItemId() == R.id.removeAds) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=vaibhav.tech.smartiptv.pro"));
                startActivity(intent);
            } catch (Exception e) { //google play app is not installed
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=vaibhav.tech.smartiptv.pro"));
                startActivity(intent);
            }
        } else if (item.getItemId() == R.id.rate) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=vaibhav.tech.smartiptv"));
                startActivity(intent);
            } catch (Exception e) { //google play app is not installed
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=vaibhav.tech.smartiptv"));
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean isValidate(String url) {
        boolean result = false;
        try {
            if (url.startsWith("http")) {
                if (url.endsWith(".mp4") || url.endsWith(".m3u8") || url.endsWith(".mkv")) {
                    result = true;
                } else {
                    InputStream playlist = null;
                    playlist = OpenHttpConnection(url);
                    if (playlist != null) {
                        Scanner s = new Scanner(playlist).useDelimiter("#");
                        String first = s.next();
                        String toMatch = "EXTM3U";
                        String toMatch2 = "EXTINF";
                        int temp = 0;
                        if (first.trim().equals(toMatch))
                            temp = 1;
                        if (first.trim().equals(toMatch2))
                            temp = 1;
                        if (temp == 1) {
                            result = true;
                        }
                    }

                }

            } else {
                File file = new File(url);
                String ext = FilenameUtils.getExtension(file.getName());

                if (ext.equalsIgnoreCase("m3u") || ext.equalsIgnoreCase("m3u8") || ext.equalsIgnoreCase("mpd") || ext.equalsIgnoreCase("xml") || ext.equalsIgnoreCase("mp4") || ext.equalsIgnoreCase("webm")
                        || ext.equalsIgnoreCase("mkv")) {
                    result = true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(IptvActivity.this);
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

    private InputStream OpenHttpConnection(final String url) throws IOException {
        InputStream inputStream = null;

        try {
            URLConnection conn = null;

            URL url1 = new URL(url);
            conn = url1.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) conn;
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream();
            }

        } catch (Exception e) {

        }
        return inputStream;
    }


    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        } // for now eat exceptions
        return "";
    }






    public String getPath(Uri uri) {
        String path = null;
        String[] projection = { MediaStore.Files.FileColumns.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if(cursor == null){
            path = uri.getPath();
        }
        else{
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(projection[0]);
            path = cursor.getString(column_index);
            cursor.close();
        }

        return ((path == null || path.isEmpty()) ? (uri.getPath()) : path);
    }


}



