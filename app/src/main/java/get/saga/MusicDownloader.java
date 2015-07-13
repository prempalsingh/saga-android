package get.saga;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by prempal on 7/6/15.
 */
public class MusicDownloader {

    public static void startDownload(final Context context, final String songName, final String artistName, final DownloaderListener listener) {
        listener.showProgressBar();
        String url = "http://getsa.ga/request.php";
        StringRequest request = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                listener.hideProgressBar();
                if (Patterns.WEB_URL.matcher(response).matches()) {
                    Uri uri = Uri.parse(response);
                    DownloadManager dMgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request dr = new DownloadManager.Request(uri);
                    String fileName = songName;
                    if (fileName == null) {
                        fileName = uri.getQueryParameter("mp3").replace("_", " ");
                        fileName = fileName.replaceAll("(?i)\\b(official|lyrics|lyric|video|song)\\b", "");
                        fileName = fileName.trim().replaceAll(" +", " ");
                        dr.setTitle(fileName);
                    } else {
                        fileName = fileName + ".mp3";
                    }
                    dr.setTitle(fileName);
                    dr.setDestinationUri(Uri.fromFile(new File(Utils.getStoragePath(context) + "/" + fileName)));
//                    dr.setDestinationInExternalPublicDir("/Saga/", filename);
                    dMgr.enqueue(dr);
                    Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show();
                    listener.onSuccess();
                    getSongInfo(context, fileName.substring(0, fileName.length() - 4), artistName);
                } else
                    Toast.makeText(context, "Nothing found, sorry. Try another query?", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, context.getString(R.string.error_connect), Toast.LENGTH_SHORT).show();
                listener.hideProgressBar();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                String query = null;
                if (artistName != null)
                    query = songName + " " + artistName;
                else
                    query = songName;
                params.put("track", query + " lyrics");
                return params;
            }
        };
        request.setShouldCache(false);
        VolleySingleton.getInstance(context).getRequestQueue().add(request);
    }

    private static void getSongInfo(final Context context, final String filename, final String artistName) {
        String url = "http://rhythmsa.ga/2/everything.php?q=" + filename.replace(" ", "%20");
        StringRequest request = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                if (artistName != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        jsonObject.put("artist", artistName);
                        response = jsonObject.toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("Test", "chal raha hai");
                Utils.saveSongInfo(context, filename + ".txt", response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Music download", "Error: " + error.toString());
            }
        });
        request.setShouldCache(false);
        VolleySingleton.getInstance(context).getRequestQueue().add(request);
    }

    interface DownloaderListener {
        void showProgressBar();

        void hideProgressBar();

        void onSuccess();
    }

}
