package get.saga;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by prempal on 7/6/15.
 */
public class MusicDownloader {

    public static void startDownload(final Context context, final String songName, final String artistName, final DownloaderListener listener) {
        listener.showProgressBar();
        String url = "http://162.243.144.151/new_api.php?q=" + songName.replace(" ", "%20");
        if (artistName != null) {
            url += "&r=" + artistName.replace(" ", "%20");
        }
        StringRequest request = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                if (response.length() != 0) {
                    try {
                        final String[] fileName = new String[1];
                        final JSONObject jsonObject = new JSONObject(response);
                        String BASE_URL = "http://YouTubeInMP3.com/fetch/?video=http://www.youtube.com/watch?v=";
                        final DownloadManager dMgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

                        final String urlString = BASE_URL + jsonObject.getString("id");
                        final URL url = new URL(urlString);
                        new AsyncTask<Void, Void, String>() {
                            @Override
                            protected String doInBackground(Void... voids) {
                                try {
                                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                                    urlConnection.setInstanceFollowRedirects(true);
                                    return urlConnection.getContentType();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                return null;
                            }

                            @Override
                            protected void onPostExecute(String result) {
                                Log.d("Content type: ", result);
                                if ("audio/mpeg".equals(result)) {
                                    listener.hideProgressBar();
                                    Uri uri = Uri.parse(urlString);
                                    DownloadManager.Request dr = new DownloadManager.Request(uri);

                                    if (artistName == null) {
                                        try {
                                            fileName[0] = jsonObject.getString("title");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        fileName[0].replaceAll("(?i)\\b(official|lyrics|lyric|video|song)\\b", "");
                                        fileName[0].trim().replaceAll(" +", " ");
                                    } else {
                                        fileName[0] = songName;
                                    }

                                    fileName[0] += ".mp3";
                                    dr.setTitle(fileName[0]);
                                    dr.setDestinationUri(Uri.fromFile(new File(Utils.getStoragePath(context) + "/" + fileName[0])));
                                    dMgr.enqueue(dr);
                                    Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show();
                                    listener.onSuccess();
                                    getSongInfo(context, fileName[0].substring(0, fileName[0].length() - 4), songName, artistName);
                                } else {
                                    listener.hideProgressBar();
                                    Toast.makeText(context, "Nothing found, sorry. Try again later", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    listener.hideProgressBar();
                    Toast.makeText(context, "Nothing found, sorry. Try again later", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, context.getString(R.string.error_connect), Toast.LENGTH_SHORT).show();
                listener.hideProgressBar();
            }
        });
        request.setShouldCache(false);
        VolleySingleton.getInstance(context).getRequestQueue().add(request);
    }

    private static void getSongInfo(final Context context, final String filename, final String songName, final String artistName) {

        String url = "http://162.243.144.151/everything.php?q=";
        url += artistName == null ? filename.replace(" ", "%20") : songName.replace(" ", "%20");

        StringRequest request = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                if (artistName != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        jsonObject.put("track", songName);
                        jsonObject.put("artist", artistName);
                        Utils.saveSongInfo(context, filename, jsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Utils.saveSongInfo(context, filename, response);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Music download", "Error: " + error.toString());
                if (artistName != null) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("track", songName);
                        jsonObject.put("artist", artistName);
                        Utils.saveSongInfo(context, filename, jsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
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
