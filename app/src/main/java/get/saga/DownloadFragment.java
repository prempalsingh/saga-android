package get.saga;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by prempal on 16/2/15.
 */
public class DownloadFragment extends Fragment {

    private static final String TAG = "DownloadFragment";
    int mVersionCode = 0;
    String mChangelog = null;
    String mAPKUrl = null;
    Tracker mTracker;
    private EditText mInput;
    private ProgressBar mProgress;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RequestQueue mQueue;
    private ImageLoader mImageLoader;

    public DownloadFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = VolleySingleton.getInstance(getActivity()).
                getRequestQueue();
        try {
            mVersionCode = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mTracker = ((ApplicationWrapper) getActivity().getApplication()).getTracker(
                ApplicationWrapper.TrackerName.APP_TRACKER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_download, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.grid_view);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
            mRecyclerView.setLayoutManager(layoutManager);
        } else {
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
            mRecyclerView.setLayoutManager(layoutManager);
        }

        mProgress = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mInput = (EditText) rootView.findViewById(R.id.et_input);
        mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    startDownload(textView.getText().toString());
                }
                return false;
            }
        });
        ImageButton downloadBtn = (ImageButton) rootView.findViewById(R.id.btn_download);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDownload(mInput.getText().toString());
            }
        });

        getCharts();

        String updateUrl = "https://www.dropbox.com/s/bka9o3p43oki217/saga.json?raw=1";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, updateUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int updateVersionCode = response.getInt("versionCode");
                    if (updateVersionCode > mVersionCode && mVersionCode != 0) {
                        mAPKUrl = response.getString("apkUrl");
                        mChangelog = response.getString("changelog");
                        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                                .setTitle("New update available!")
                                .setMessage(mChangelog)
                                .setPositiveButton("Update now", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        File myFile = new File(Environment.getExternalStorageDirectory() + "/Saga/" + "update.apk");
                                        if (myFile.exists())
                                            myFile.delete();
                                        Uri uri = Uri.parse(mAPKUrl);
                                        DownloadManager dMgr = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                                        DownloadManager.Request dr = new DownloadManager.Request(uri);
                                        String filename = "update.apk";
                                        dr.setTitle("Saga - Free Music Update");
                                        dr.setDestinationInExternalPublicDir("/Saga/", filename);
                                        dMgr.enqueue(dr);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setCancelable(false)
                                .create();
                        dialog.show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("sd", error.toString());
            }
        });
        request.setShouldCache(false);
        mQueue.add(request);
        return rootView;
    }

    private void startDownload(final String input) {
        Log.d("htd", input);
        if (TextUtils.isEmpty(input))
            Toast.makeText(getActivity(), "Enter song name", Toast.LENGTH_SHORT).show();
        else if (input.equalsIgnoreCase("whomadeyou"))
            Toast.makeText(getActivity(), "Prempal Singh", Toast.LENGTH_SHORT).show();
        else {
            mProgress.setVisibility(View.VISIBLE);
            String url = "http://getsa.ga/request.php";
            StringRequest request = new StringRequest(Request.Method.POST,
                    url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d(TAG, response);
                    mProgress.setVisibility(View.GONE);
                    if (Patterns.WEB_URL.matcher(response).matches()) {
                        Uri uri = Uri.parse(response);
                        DownloadManager dMgr = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                        DownloadManager.Request dr = new DownloadManager.Request(uri);
                        String filename = uri.getQueryParameter("mp3").replace("_", " ");
                        filename = filename.replaceAll("(?i)\\b(official|lyrics|lyric|video|song)\\b", "");
                        filename = filename.trim().replaceAll(" +", " ");
                        Log.d(TAG, filename);
                        dr.setTitle(filename);
                        dr.setDestinationInExternalPublicDir("/Saga/", filename);
                        dMgr.enqueue(dr);
                        Toast.makeText(getActivity(), "Downloading...", Toast.LENGTH_SHORT).show();
                        getSongInfo(input, filename.substring(0, filename.length() - 4));
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Music Download")
                                .setAction("Click")
                                .build());
                    } else
                        Toast.makeText(getActivity(), "Nothing found, sorry. Try another query?", Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    Toast.makeText(getActivity(), "Error connecting to the Internet", Toast.LENGTH_SHORT).show();
                    mProgress.setVisibility(View.GONE);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("track", input + " lyrics");
                    return params;
                }
            };
            request.setShouldCache(false);
            mQueue.add(request);
        }

    }

    private void getCharts() {

        mImageLoader = VolleySingleton.getInstance(getActivity()).getImageLoader();

//        mImageLoader = new ImageLoader(mQueue,new DiskLruImageCache(getActivity(),
//                getActivity().getPackageCodePath()
//                , 1024*1024*30
//                , Bitmap.CompressFormat.PNG
//                , 100)
//        );
        String url = "http://boundbytech.com/saga/get_charts.php";
        JsonArrayRequest request = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        mAdapter = new ChartsAdapter(response);
                        mRecyclerView.setAdapter(mAdapter);
                        mProgress.setVisibility(View.GONE);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                mProgress.setVisibility(View.GONE);
            }
        });
        mQueue.add(request);

    }

    private void getSongInfo(final String input, final String filename) {
        String url = "http://rhythmsa.ga/2/everything_post.php";
        StringRequest request = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getActivity().openFileOutput(filename + ".txt", Context.MODE_PRIVATE));
                    outputStreamWriter.write(response);
                    outputStreamWriter.close();
                } catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("q", input);
                return params;
            }
        };
        request.setShouldCache(false);
        mQueue.add(request);
    }

    private class ChartsAdapter extends RecyclerView.Adapter<ChartsAdapter.ViewHolder> {

        private JSONArray mDataset;

        public ChartsAdapter(JSONArray dataSet) {
            mDataset = dataSet;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            LinearLayout v = (LinearLayout) LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.grid_chart, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
            String songName = null;
            String artistName = null;
            try {
                songName = mDataset.getJSONArray(i).getString(0);
                artistName = mDataset.getJSONArray(i).getString(1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            viewHolder.songName.setText(songName);
            viewHolder.songName.setSelected(true);
            viewHolder.artistName.setText(artistName);
            viewHolder.artistName.setSelected(true);
            String url = "http://ts3.mm.bing.net/th?q=" + songName.replace(" ", "%20") + "%20" + artistName.replace(" ", "%20") + "+album+art";
            viewHolder.albumArt.setImageUrl(url, mImageLoader);
            viewHolder.albumArt.setResponseObserver(new NetworkImageView.ResponseObserver() {
                @Override
                public void onError() {

                }

                @Override
                public void onSuccess() {
                    Bitmap bitmap = ((BitmapDrawable) viewHolder.albumArt.getDrawable()).getBitmap();
                    Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                        public void onGenerated(Palette palette) {
                            int bgColor = palette.getVibrantColor(R.color.white);
                            viewHolder.songInfo.setBackgroundColor(bgColor);
                            if (Utils.isColorDark(bgColor)) {
                                viewHolder.songName.setTextColor(getResources().getColor(R.color.white));
                                viewHolder.artistName.setTextColor(getResources().getColor(R.color.white));
                            } else {
                                viewHolder.songName.setTextColor(getResources().getColor(R.color.black));
                                viewHolder.artistName.setTextColor(getResources().getColor(R.color.black));
                            }
                        }
                    });
                }
            });
            final String finalSongName = songName;
            final String finalArtistName = artistName;
            viewHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startDownload(finalSongName + " " + finalArtistName);
                }
            });
            viewHolder.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mProgress.setVisibility(View.VISIBLE);
                    String url = "http://rhythmsa.ga/api/sharable.php?q=";
                    url = url + finalSongName.replace(" ", "+") + "+" + finalArtistName.replace(" ", "+");
                    StringRequest request = new StringRequest(Request.Method.GET,
                            url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mProgress.setVisibility(View.GONE);
                            Log.d(TAG, response);
                            if (Patterns.WEB_URL.matcher(response).matches()) {
                                Intent i = new Intent(Intent.ACTION_SEND);
                                i.setType("text/plain");
                                i.putExtra(Intent.EXTRA_TEXT, "Hey! Check out this amazing song - " + finalSongName + " by " + finalArtistName + ". " + response + "\nShared via Saga Music app - http://getsa.ga/apk");
                                try {
                                    startActivity(Intent.createChooser(i, "Share via"));
                                } catch (android.content.ActivityNotFoundException ex) {
                                    Toast.makeText(getActivity(), "No application available to share song", Toast.LENGTH_SHORT).show();
                                }
                                mTracker.send(new HitBuilders.EventBuilder()
                                        .setCategory("Music Share")
                                        .setAction("Click")
                                        .build());
                            } else
                                Toast.makeText(getActivity(), "Error in sharing", Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mProgress.setVisibility(View.GONE);
                            VolleyLog.d(TAG, "Error: " + error.getMessage());
                            Toast.makeText(getActivity(), "Error connecting to the Internet", Toast.LENGTH_SHORT).show();
                        }
                    });
                    request.setShouldCache(false);
                    mQueue.add(request);
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDataset.length();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView songName;
            TextView artistName;
            NetworkImageView albumArt;
            LinearLayout songInfo;
            View view;

            public ViewHolder(LinearLayout v) {
                super(v);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startDownload(songName.getText().toString() + " " + artistName.getText().toString());
                    }
                });
                this.view = v;
                this.songName = (TextView) v.findViewById(R.id.song);
                this.artistName = (TextView) v.findViewById(R.id.artist);
                this.albumArt = (NetworkImageView) v.findViewById(R.id.album_art);
                this.songInfo = (LinearLayout) v.findViewById(R.id.songInfo);
            }
        }
    }
}
