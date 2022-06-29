package vaibhav.tech.smartiptv.iptv;

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Created by fedor on 25.11.2016.
 */

public class M3UParser {

    private static final String EXT_M3U = "#EXTM3U";
    private static final String EXT_INF = "#EXTINF:";
    private static final String EXT_PLAYLIST_NAME = "#PLAYLIST";
    private static final String EXT_LOGO = "tvg-logo";
    private static final String EXT_URL = "http://";

    public String convertStreamToString(InputStream is) {
        try {
            return new Scanner(is).useDelimiter("\\A").next();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    public M3UPlaylist parseFile(InputStream inputStream) throws FileNotFoundException {

        M3UPlaylist m3UPlaylist = new M3UPlaylist();
        List<M3UItem> playlistItems = new ArrayList<>();
        String stream = convertStreamToString(inputStream);
       // Log.d("Logs",stream);
        if(!stream.startsWith("#EXTM3U"))
        {
            stream="#EXTM3U\n"+stream;
        }
        Log.d("Logs-2",stream);
        String linesArray[] = stream.split(EXT_INF);
        //Log.d("Logs-2","Len="+String.valueOf(linesArray.length));
//        for (int i=0;i<linesArray.length;i++)
//        {
//            Log.d("Logs-2","VAL="+linesArray[i]);
//        }
        for (int i = 0; i < linesArray.length; i++) {
            String currLine = linesArray[i];
             Log.d("Logs-2",currLine);
            if (currLine.contains(EXT_M3U))
            {
                Log.d("Logs-2","54");
                //header of file
                if (currLine.contains(EXT_PLAYLIST_NAME)) {
                    Log.d("Logs-2","57");
                    Log.d("VJXXX","three");
                    String fileParams = currLine.substring(EXT_M3U.length(), currLine.indexOf(EXT_PLAYLIST_NAME));
                    String playListName = currLine.substring(currLine.indexOf(EXT_PLAYLIST_NAME) + EXT_PLAYLIST_NAME.length()).replace(":", "");
                    m3UPlaylist.setPlaylistName(playListName);
                    m3UPlaylist.setPlaylistParams(fileParams);
                } else {
                    Log.d("Logs-2","64");
                    m3UPlaylist.setPlaylistName("Noname Playlist");
                    m3UPlaylist.setPlaylistParams("No Params");
                }
            }
            else {
                if(!currLine.contains("http"))
                    continue;
                Log.d("Logs-2","70");
                M3UItem playlistItem = new M3UItem();
                String newCurrLine ="";
                int cp=0;
                for (int ip=0;ip<currLine.length();ip++){
                    char ch=currLine.charAt(ip);
                    if (ch==',') {
                        if (cp!=0)
                            ch='.';
                        cp++;
                    }
                    newCurrLine=newCurrLine+ch;

                }
                currLine=newCurrLine;
                Log.d("Logs-2",""+currLine);
                String[] dataArray = currLine.split(",");

                if (dataArray[0].contains(EXT_LOGO))
                {

                    Log.d("Logs-2","91");
                    String duration = dataArray[0].substring(0, dataArray[0].indexOf(EXT_LOGO)).replace(":", "").replace("\n", "");
                    String icon = dataArray[0].substring(dataArray[0].indexOf(EXT_LOGO) + EXT_LOGO.length()).replace("=", "").replace("\"", "").replace("\n", "");
                    ;
                    playlistItem.setItemDuration(duration);
                    playlistItem.setItemIcon(icon);
                } else
                    {
                    Log.d("Logs-2","99");
                    String duration = dataArray[0].replace(":", "").replace("\n", "");
                    ;
                    playlistItem.setItemDuration(duration);
                    playlistItem.setItemIcon("");
                }


                dataArray[1]=dataArray[1].replaceAll("https","http");
                Log.d("dataArray",dataArray[1]);

                String name = dataArray[1].substring(0, dataArray[1].indexOf(EXT_URL)).replace("\n", "");

                String url = dataArray[1].substring(dataArray[1].indexOf(EXT_URL)).replace("\n", "").replace("\r", "");
                // Log.d("VJXXX",name+" "+url);
                playlistItem.setItemName(name);
                playlistItem.setItemUrl(url);
                playlistItems.add(playlistItem);

            }
            Log.d("Logs-2","val_i="+i+" Len="+linesArray.length);
        }
        m3UPlaylist.setPlaylistItems(playlistItems);
        return m3UPlaylist;
    }
}
