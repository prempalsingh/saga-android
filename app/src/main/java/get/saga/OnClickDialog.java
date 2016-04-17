package get.saga;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Sandeep on 17-Apr-16.
 */
public class OnClickDialog extends DialogFragment {
    private static final String KEY_URL = "URL";
    private static final String KEY_ARTIST = "ARTIST";
    private static final String KEY_TITLE = "TITLE";
    private String url;
    private String arts;
    private String title;
    VolleySingleton mVolley;
    ImageLoader mImageLoader;
    RequestQueue mQueue;
    private Tracker mTracker;

    public static OnClickDialog getInstance(String url, String arti, String title) {
        OnClickDialog mOnClick = new OnClickDialog();
        Bundle mBundle = new Bundle();
        mBundle.putString(KEY_URL, url);
        mBundle.putString(KEY_ARTIST, arti);
        mBundle.putString(KEY_TITLE, title);
        mOnClick.setArguments(mBundle);
        return mOnClick;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((ApplicationWrapper) getActivity().getApplication()).getTracker(
                ApplicationWrapper.TrackerName.APP_TRACKER);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            url = bundle.getString(KEY_URL);
            arts = bundle.getString(KEY_ARTIST);
            title = bundle.getString(KEY_TITLE);
        }
        mVolley = VolleySingleton.getInstance(getActivity());
        mImageLoader = mVolley.getImageLoader();
        mQueue = mVolley.getRequestQueue();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View mView = inflater.inflate(R.layout.dialog_on_click, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        final ProgressBar mProgress = (ProgressBar) mView.findViewById(R.id.progress_doc);
        mImageLoader.get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                ((ImageView) mView.findViewById(R.id.album_iv_doc)).setImageBitmap(response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                //should get a cache hit
            }
        });
        ((TextView) mView.findViewById(R.id.artist_value_tv_doc)).setText(arts);
        ((TextView) mView.findViewById(R.id.track_value_tv_doc)).setText(title);
        ImageButton mDownload = (ImageButton) mView.findViewById(R.id.download_btn_doc);
        mDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicDownloader.startDownload(getActivity(), title, arts, new MusicDownloader.DownloaderListener() {
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
                        dismiss();
                    }
                });
            }
        });
        ImageButton mShare = (ImageButton) mView.findViewById(R.id.share_btn_doc);
        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.setVisibility(View.VISIBLE);
                String url = null;
                try {
                    url = "http://rhythmsa.ga/api/sharable.php?q=" + URLEncoder.encode(title + " " + arts, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                StringRequest request = new StringRequest(Request.Method.GET,
                        url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mProgress.setVisibility(View.GONE);
                        Log.d("kthenks", response);
                        if (Patterns.WEB_URL.matcher(response).matches()) {
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType("text/plain");
                            i.putExtra(Intent.EXTRA_TEXT, "Hey! Check out this amazing song - " + title + " by " + arts + ". " + response + "\nShared via Saga Music app - http://getsa.ga/apk");
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

                        dismiss();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mProgress.setVisibility(View.GONE);
                        VolleyLog.d("kthenks", "Error: " + error.getMessage());
                        Toast.makeText(getActivity(), "Error connecting to the Internet", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                });
                request.setShouldCache(false);
                mQueue.add(request);
            }
        });
        return mView;
    }
}
