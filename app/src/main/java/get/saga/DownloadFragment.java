package get.saga;

import android.app.DownloadManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
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
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by prempal on 16/2/15.
 */
public class DownloadFragment extends Fragment {

    private static final String TAG = "DownloadFragment";

    private EditText mInput;
    private ProgressBar mProgress;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RequestQueue mQueue;
    private ImageLoader mImageLoader;

    public DownloadFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_download, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.grid_view);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
            mRecyclerView.setLayoutManager(layoutManager);
        }else {
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
            mRecyclerView.setLayoutManager(layoutManager);
        }

        mProgress = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mInput = (EditText) rootView.findViewById(R.id.et_input);
        mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i== EditorInfo.IME_ACTION_DONE){
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
        mQueue = Volley.newRequestQueue(getActivity());
        getCharts();

        return rootView;
    }

    private void startDownload(final String input){
        if(TextUtils.isEmpty(input))
            Toast.makeText(getActivity(),"Enter song name",Toast.LENGTH_SHORT).show();
        else if(input.equalsIgnoreCase("whomadeyou"))
            Toast.makeText(getActivity(),"Prempal Singh",Toast.LENGTH_SHORT).show();
        else{
            mProgress.setVisibility(View.VISIBLE);
            String url = "http://getsa.ga/request.php";
            StringRequest request = new StringRequest(Request.Method.POST,
                    url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d(TAG, response);
                    mProgress.setVisibility(View.GONE);
                    if(Patterns.WEB_URL.matcher(response).matches()){
                        Uri uri = Uri.parse(response);
                        DownloadManager dMgr = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                        DownloadManager.Request dr = new DownloadManager.Request(uri);
                        dr.setDestinationInExternalPublicDir("/saga/", uri.getQueryParameter("mp3"));
                        dMgr.enqueue(dr);
                        Toast.makeText(getActivity(),"Downloading...",Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(getActivity(),"Nothing found, sorry. Try another query?",Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    Toast.makeText(getActivity(),"Error connecting to the Internet",Toast.LENGTH_SHORT).show();
                    mProgress.setVisibility(View.GONE);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("track", input);
                    return params;
                }
            };
            mQueue.add(request);
        }

    }

    private void getCharts(){
        String url = "http://boundbytech.com/saga/get_charts.php";
        JsonArrayRequest request = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        mAdapter = new ChartsAdapter(response);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });
        mQueue.add(request);
        mImageLoader = new ImageLoader(mQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(100);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
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
            viewHolder.artistName.setText(artistName);
            String url = "http://ts3.mm.bing.net/th?q=" + songName.replace(" ","%20") + "%20" + artistName.replace(" ","%20") + "album+art";
            Log.d("jdf",url);
            viewHolder.albumArt.setImageUrl(url, mImageLoader);
            viewHolder.albumArt.setResponseObserver(new NetworkImageView.ResponseObserver() {
                @Override
                public void onError() {

                }

                @Override
                public void onSuccess() {
                    Bitmap bitmap = ((BitmapDrawable)viewHolder.albumArt.getDrawable()).getBitmap();
                    Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                        public void onGenerated(Palette palette) {
                            viewHolder.songInfo.setBackgroundColor(palette.getLightVibrantColor(R.color.white));
                            viewHolder.songName.setTextColor(palette.getDarkVibrantColor(R.color.white));
                            viewHolder.artistName.setTextColor(palette.getDarkVibrantColor(R.color.white));
                        }
                    });
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

            public ViewHolder(LinearLayout v) {
                super(v);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startDownload(songName.toString() + artistName.toString());
                    }
                });
                this.songName = (TextView) v.findViewById(R.id.song);
                this.artistName = (TextView) v.findViewById(R.id.artist);
                this.albumArt = (NetworkImageView) v.findViewById(R.id.album_art);
                this.songInfo = (LinearLayout) v.findViewById(R.id.songInfo);
            }
        }
    }
}
