package get.saga;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * Created by prempal on 21/2/15.
 */
public class Utils {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    public static void viewURL(Context context, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
    }

    public static String getAlbumArt(String song, String artist) throws UnsupportedEncodingException {
        String BASE_URL = "http://ts3.mm.bing.net/th?q=";
        String albumArtUrl = BASE_URL + song.replace(" ", "%20");
        if (artist != null) {
            albumArtUrl += "%20" + artist.replace(" ", "%20");
        }
        albumArtUrl += "+album+art";
        return albumArtUrl;
    }

    public static String getStoragePath(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString("prefStoragePath",
                Environment.getExternalStorageDirectory() + "/Saga");
    }

    public static void saveSongInfo(Context context, String filename, String content) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(content);
            outputStreamWriter.close();
            Log.d("File:", "saved");
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
