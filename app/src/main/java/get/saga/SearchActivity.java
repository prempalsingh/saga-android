package get.saga;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;


public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private RequestQueue mQueue;
    private ImageLoader mImageLoader;
    private ProgressBar mProgressBar;
    private ImageButton mClearButton;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        final EditText search = (EditText) findViewById(R.id.et_input);
        mProgressBar = (ProgressBar) findViewById(R.id.search_progress);
        mClearButton = (ImageButton) findViewById(R.id.btn_cross);
        mRecyclerView = (RecyclerView) findViewById(R.id.search_results);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mQueue = VolleySingleton.getInstance(this).getRequestQueue();
        mImageLoader = VolleySingleton.getInstance(this).getImageLoader();

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent i = getIntent();
        String query = i.getStringExtra("query");
        search.setText(query);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    if (textView.length() > 0) {
                        mClearButton.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        getSearchResults(textView.getText().toString());
                    } else {
                        Toast.makeText(getApplicationContext(), "Enter song name", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setText("");
                mClearButton.setVisibility(View.GONE);
            }
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0 && mClearButton.getVisibility() != View.VISIBLE) {
                    mClearButton.setVisibility(View.VISIBLE);
                }
            }
        });

        getSearchResults(query);
    }

    private void getSearchResults(final String query) {
        String url = "http://sagamusic.herokuapp.com/search.php";
        StringRequest request = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                try {
                    mAdapter = new SearchAdapter(new JSONArray(response));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mRecyclerView.setAdapter(mAdapter);
                mProgressBar.setVisibility(View.GONE);
                mClearButton.setVisibility(View.VISIBLE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Error connecting to the Internet", Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.GONE);
                mClearButton.setVisibility(View.VISIBLE);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("q", query);
                return params;
            }
        };
        request.setShouldCache(false);
        mQueue.add(request);
    }

    private class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

        private JSONArray mDataset;

        public SearchAdapter(JSONArray dataSet) {
            mDataset = dataSet;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            CardView v = (CardView) LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.list_search, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
            String songName = "unknown";
            String artistName = "unknown";
            try {
                songName = mDataset.getJSONObject(i).getString("track");
                artistName = mDataset.getJSONObject(i).getString("artist");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            viewHolder.songName.setText(songName);
            viewHolder.songName.setSelected(true);
            viewHolder.artistName.setText(artistName);
            viewHolder.artistName.setSelected(true);
            String url = Utils.getAlbumArt(songName, artistName);
            viewHolder.albumArt.setImageUrl(url, mImageLoader);
        }

        @Override
        public int getItemCount() {
            return mDataset.length();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView songName;
            TextView artistName;
            NetworkImageView albumArt;
            CardView view;

            public ViewHolder(CardView v) {
                super(v);
                this.view = v;
                this.songName = (TextView) v.findViewById(R.id.search_song);
                this.artistName = (TextView) v.findViewById(R.id.search_artist);
                this.albumArt = (NetworkImageView) v.findViewById(R.id.search_album_art);
            }
        }
    }
}
