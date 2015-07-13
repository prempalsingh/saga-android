package get.saga;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by prempal on 16/2/15.
 */
public class DownloadFragment extends Fragment {

    private static final String TAG = "DownloadFragment";
    private Tracker mTracker;
    private EditText mInput;
    private ProgressBar mProgress;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RequestQueue mQueue;
    private ImageLoader mImageLoader;
    private SharedPreferences sp;

    public DownloadFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = VolleySingleton.getInstance(getActivity()).
                getRequestQueue();

        mTracker = ((ApplicationWrapper) getActivity().getApplication()).getTracker(
                ApplicationWrapper.TrackerName.APP_TRACKER);

        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_download, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        mRecyclerView = (RecyclerView) view.findViewById(R.id.grid_view);

        GridLayoutManager glm = new GridLayoutManager(getActivity(), 2);
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        glm.setSpanCount(screenWidth / (getResources().getDimensionPixelOffset(R.dimen.column_width_main_recyclerview)));
        mRecyclerView.setLayoutManager(glm);

        mProgress = (ProgressBar) view.findViewById(R.id.progressBar);
        mInput = (EditText) view.findViewById(R.id.et_input);
        mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    processQuery(textView.getText().toString());
                    return true;
                }
                return false;
            }
        });

        ImageButton downloadBtn = (ImageButton) view.findViewById(R.id.btn_download);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processQuery(mInput.getText().toString());
            }
        });

        getCharts();

        Updater.checkForUpdates(getActivity(), false);
    }

    private void getCharts() {

        mImageLoader = VolleySingleton.getInstance(getActivity()).getImageLoader();
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

    private void processQuery(String query) {
        if (TextUtils.isEmpty(query))
            Toast.makeText(getActivity(), getString(R.string.enter_song), Toast.LENGTH_SHORT).show();
        else if (query.equalsIgnoreCase("whomadeyou"))
            Toast.makeText(getActivity(), "Prempal Singh", Toast.LENGTH_SHORT).show();
        else if (sp.getBoolean("prefSearchResults", true)) {
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            intent.putExtra("query", query);
            startActivity(intent);
            mInput.setText("");
        } else
            MusicDownloader.startDownload(getActivity(), query, null, new MusicDownloader.DownloaderListener() {
                @Override
                public void showProgressBar() {
                    mProgress.setVisibility(View.VISIBLE);
                }

                @Override
                public void hideProgressBar() {
                    mProgress.setVisibility(View.GONE);
                }

                @Override
                public void onSuccess() {
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Music Download")
                            .setAction("Click")
                            .build());
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
            viewHolder.songName.setSelected(true);
            viewHolder.artistName.setText(artistName);
            viewHolder.artistName.setSelected(true);
            String url = null;
            try {
                url = Utils.getAlbumArt(songName, artistName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            viewHolder.albumArt.setImageUrl(url, mImageLoader);
            viewHolder.albumArt.setResponseObserver(new NetworkImageView.ResponseObserver() {
                @Override
                public void onError() {

                }

                @Override
                public void onSuccess() {
                    Bitmap bitmap = ((BitmapDrawable) viewHolder.albumArt.getDrawable()).getBitmap();
                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                        @Override
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
                    MusicDownloader.startDownload(getActivity(), finalSongName, finalArtistName, new MusicDownloader.DownloaderListener() {
                        @Override
                        public void showProgressBar() {
                            mProgress.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void hideProgressBar() {
                            mProgress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onSuccess() {
                            mTracker.send(new HitBuilders.EventBuilder()
                                    .setCategory("Music Download")
                                    .setAction("Click")
                                    .build());
                        }
                    });
                }
            });
            viewHolder.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mProgress.setVisibility(View.VISIBLE);
                    String url = null;
                    try {
                        url = "http://rhythmsa.ga/api/sharable.php?q=" + URLEncoder.encode(finalSongName + " " + finalArtistName, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
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
                this.view = v;
                this.songName = (TextView) v.findViewById(R.id.song);
                this.artistName = (TextView) v.findViewById(R.id.artist);
                this.albumArt = (NetworkImageView) v.findViewById(R.id.album_art);
                this.songInfo = (LinearLayout) v.findViewById(R.id.songInfo);
            }
        }
    }
}
