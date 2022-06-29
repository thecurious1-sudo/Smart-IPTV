package vaibhav.tech.smartiptv.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import vaibhav.tech.smartiptv.media.Video;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.mozilla.universalchardet.Constants;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by sergey on 25.02.17.
 */

public class Utils {
    public static String getHost(String str) {
        try {
            String toLowerCase = str.toLowerCase();
            int indexOf = toLowerCase.indexOf(47, 8);
            if (indexOf != -1) {
                toLowerCase = toLowerCase.substring(0, indexOf);
            }
            String host = new URI(toLowerCase).getHost();
            return host == null ? toLowerCase : host.startsWith("www.") ? host.substring(4) : host;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Quietly closes a closeable object like an InputStream or OutputStream without
     * throwing any errors or requiring you do do any checks.
     *
     * @param closeable the object to close
     */
    public static void close(Closeable closeable) {
        if (closeable == null)
            return;
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getWebPage(String str) {
        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader;
        Throwable th;
        Throwable th2;
        BufferedReader bufferedReader2 = null;
        try {
            HttpURLConnection httpURLConnection2 = (HttpURLConnection) new URL(str).openConnection();
            try {
                httpURLConnection2.setRequestMethod("GET");
                httpURLConnection2.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0");
                httpURLConnection2.connect();
                InputStream inputStream = httpURLConnection2.getInputStream();
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader bufferedReader3 = new BufferedReader(new InputStreamReader(inputStream));
                while (true) {
                    try {
                        String readLine = bufferedReader3.readLine();
                        if (readLine == null) {
                            break;
                        }
                        stringBuilder.append(readLine).append("\n");
                    } catch (Exception e) {
                        BufferedReader bufferedReader4 = bufferedReader3;
                        httpURLConnection = httpURLConnection2;
                        bufferedReader = bufferedReader4;
                    } catch (Throwable th3) {
                        th = th3;
                        bufferedReader2 = bufferedReader3;
                        httpURLConnection = httpURLConnection2;
                        th2 = th;
                    }
                }
                String stringBuilder2 = stringBuilder.toString();
                if (httpURLConnection2 != null) {
                    httpURLConnection2.disconnect();
                }
                if (bufferedReader3 == null) {
                    return stringBuilder2;
                }
                try {
                    bufferedReader3.close();
                    return stringBuilder2;
                } catch (IOException e2) {
                    // FirebaseCrash.report(new Exception("Error closing stream"));
                    return stringBuilder2;
                }
            } catch (Exception e3) {
                httpURLConnection = httpURLConnection2;
                bufferedReader = bufferedReader2;
                try {
                    // FirebaseCrash.report(new Exception("Error get page response"));
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                    if (bufferedReader != null) {
                        return bufferedReader2.toString();
                    }
                    try {
                        bufferedReader.close();
                        return bufferedReader2.toString();
                    } catch (IOException e4) {
                        //FirebaseCrash.report(new Exception("Error closing stream"));
                        return bufferedReader2.toString();
                    }
                } catch (Throwable th32) {
                    th = th32;
                    bufferedReader2 = bufferedReader;
                    th2 = th;
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                    if (bufferedReader2 != null) {
                        try {
                            bufferedReader2.close();
                        } catch (IOException e5) {
                            //    FirebaseCrash.report(new Exception("Error closing stream"));
                        }
                    }
                    throw th2;
                }
            } catch (Throwable th4) {
                th = th4;
                httpURLConnection = httpURLConnection2;
                th2 = th;
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (bufferedReader2 != null) {
                    bufferedReader2.close();
                }
                throw th2;
            }
        } catch (Exception e6) {
            bufferedReader = bufferedReader2;
            Object obj = bufferedReader2;
            //FirebaseCrash.report(new Exception("Error get page response"));
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (bufferedReader != null) {
                return bufferedReader2.toString();
            }
            return "";
        } catch (Throwable th5) {
            /*th2 = th5;
            httpURLConnection = bufferedReader2;
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (bufferedReader2 != null) {
                bufferedReader2.close();
            }
            throw th2;*/
            return bufferedReader2.toString();
        }
    }

    public static String substringByDelimiters(String str, String openingDelimiter, String closingDelimiter) {
        Log.d("DM", "WEB: " + str);
        if (str == null || !str.contains(openingDelimiter) || !str.contains(closingDelimiter)) {
            return null;
        }
        int indexOf = str.indexOf(openingDelimiter);
        return str.substring(indexOf + openingDelimiter.length(), str.indexOf(closingDelimiter, indexOf));
    }

    public static boolean isAdsBlocker(Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        boolean isadblock = prefs.getBoolean("isadblock", false);

        return isadblock;
    }

    public static Video prepareVideo(String video, String webServer) {
        File videoFile = new File(video);
        String url = webServer + preparedVideoPath(videoFile); //videoFile.getAbsolutePath().replace(" ", "%20");
        Video cVideo = new Video(videoFile.getName(), url);
        String fileNameWithOutExt = FilenameUtils.removeExtension(videoFile.getName());
        String workingDir = videoFile.getParent();
        File subtitlevtt = new File(workingDir, fileNameWithOutExt + ".vtt");
        if (subtitlevtt.exists() == true) {
            cVideo.setSubtitle(webServer + Utils.preparedVideoPath(subtitlevtt));
        }

        File subtitlettml = new File(workingDir, fileNameWithOutExt + ".ttml");
        if (subtitlettml.exists() == true) {
            cVideo.setSubtitle(webServer + Utils.preparedVideoPath(subtitlettml));
        }

        subtitlettml = new File(workingDir, fileNameWithOutExt + ".dfxp");
        if (subtitlettml.exists() == true) {
            cVideo.setSubtitle(webServer + Utils.preparedVideoPath(subtitlettml));
        }

        subtitlettml = new File(workingDir, fileNameWithOutExt + ".xml");
        if (subtitlettml.exists() == true) {
            cVideo.setSubtitle(webServer + Utils.preparedVideoPath(subtitlettml));
        }
        String mime = getMimeType("file://" + video);
        cVideo.setMimeType(mime);
        return cVideo;
    }

    public static String followRedirectsWithCookies(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.connect();
            Map<String, List<String>> responseHeaders = connection.getHeaderFields();
            for (String key : responseHeaders.keySet()) {
                for (String val : responseHeaders.get(key)) {
                    Log.d("followRedirectsWithCook", key + ": " + val);
                }
            }
            String location = connection.getHeaderField("Location");
            location = location == null ? connection.getHeaderField("location") : location;
            return location == null ? connection.getURL().toString() : location;

        } catch (MalformedURLException e) {
            Log.d("followRedirectsWithCook", "MalformedURLException for URL " + url);
            return null;
        } catch (IOException e) {
            Log.d("followRedirectsWithCook", "IOException for URL " + url);
            return null;
        }
    }

    public static String getMimeTypeFromNetwork(String url) {
        String contentType = "";
        try {
            HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            contentType = connection.getContentType();
        } catch (ProtocolException e) {
            Log.d("FTL", "ProtocolException in Utils.getMimeTypeFromNetwork: " + e.getMessage());
        } catch (MalformedURLException e) {
            Log.d("FTL", "MalformedURLException in Utils.getMimeTypeFromNetwork: " + e.getMessage());
        } catch (IOException e) {
            Log.d("FTL", "IOException in Utils.getMimeTypeFromNetwork: " + e.getMessage());
        }

        if (TextUtils.isEmpty(contentType)) {
            contentType = getMimeType(url);
        }

        return contentType;
    }

    public static String getMimeTypeFromNetwork(String url, String referer) {
        String contentType = "";
        try {
            HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("referer", referer);
            connection.connect();
            contentType = connection.getContentType();
        } catch (ProtocolException e) {
            Log.d("FTL", "ProtocolException in Utils.getMimeTypeFromNetwork: " + e.getMessage());
        } catch (MalformedURLException e) {
            Log.d("FTL", "MalformedURLException in Utils.getMimeTypeFromNetwork: " + e.getMessage());
        } catch (IOException e) {
            Log.d("FTL", "IOException in Utils.getMimeTypeFromNetwork: " + e.getMessage());
        }

        if (TextUtils.isEmpty(contentType)) {
            contentType = getMimeType(url);
        }

        return contentType;
    }

    public static String getMimeType(String url) {
        if (url.toLowerCase().endsWith("manifest")) {
            return "application/vnd.ms-sstr+xml";
        } else {
            String type;
            String extension = MimeTypeMap.getFileExtensionFromUrl(url);
            if (!TextUtils.isEmpty(extension)) {
                if (extension.equalsIgnoreCase("m3u8")) {
                    type = "application/x-mpegURL";
                } else if (extension.equalsIgnoreCase("mpd")) {
                    type = "application/dash+xml";
                } else
                    type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            } else {
                int index = url.lastIndexOf(".");
                extension = url.substring(index + 1);
                if (!TextUtils.isEmpty(extension)) {
                    type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                } else {
                    // logger.info("Unexpected error to receive mime. Use default video/mp4. " + url);
                    type = "video/mp4";
                }
            }

            Log.d("MIME TYPE", "Type: " + type);
            return type;
        }
    }

    public static String getEncoding(File file) {
        try {
            String encoding = UniversalDetector.detectCharset(file);
            if (encoding != null) {
                return encoding;
            } else {
                return Constants.CHARSET_WINDOWS_1252;
            }
        } catch (Exception ex) {
            return Constants.CHARSET_WINDOWS_1252;
        }
    }

    public static String getRedirect(String url) {
        try {
            Connection.Response response = Jsoup.connect(url).followRedirects(false).execute();

            Log.d("REDIRECT", "Status code: " + response.statusCode());

            if (response.statusCode() >= 300 && response.statusCode() < 400 && response.hasHeader("location") == true) {
                return response.header("location");
            }
        } catch (Exception e) {

        }

        return url;
    }

    public static String preparedVideoPath(File file) {
        String[] paths = file.getAbsolutePath().split("/");
        String result = "";
        for (String path : paths) {
            if (TextUtils.isEmpty(path) == false) {
                result += "/";
                try {
                    result += java.net.URLEncoder.encode(path, "UTF-8").replace("+", "%20");
                } catch (UnsupportedEncodingException ex) {
                }
            }
        }

        return result;
    }

    /**
     * Check if a string is in a list or not.
     *
     * @param searchString  String to search for in list.
     * @param list          Strings to look for searchString amongst.
     * @return              true if searchString is in list, false otherwise.
     */
    public static boolean stringInList(String searchString, List<String> list) {
        for (String stringFromList : list) {
            if (stringFromList.equals(searchString)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a string contains at least one substring from a list.
     *
     * @param checkString   String to check for substrings in.
     * @param substrings    List of substrings to look for in checkString.
     * @return              true if checkString contains at least one substring, false otherwise.
     */
    public static boolean stringContainsAny(String checkString, List<String> substrings) {
        for (String substring : substrings) {
            if (checkString.contains(substring)) {
                return true;
            }
        }
        return false;
    }
}
