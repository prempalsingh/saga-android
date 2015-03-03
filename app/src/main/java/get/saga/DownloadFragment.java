package get.saga;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
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

    private EditText mInput;
    private ProgressBar mProgress;
    private RequestQueue mQueue;
    private static final String TAG = "DownloadFragment";

    public DownloadFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_download, container, false);
        mProgress = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mInput = (EditText) rootView.findViewById(R.id.et_input);
        mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i== EditorInfo.IME_ACTION_DONE){
                    startDownload();
                }
                return false;
            }
        });
        ImageButton downloadBtn = (ImageButton) rootView.findViewById(R.id.btn_download);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(mInput.getText()))
                    Toast.makeText(getActivity(),"Enter song name",Toast.LENGTH_SHORT).show();
                else if(mInput.getText().toString().equalsIgnoreCase("whomadeyou"))
                    Toast.makeText(getActivity(),"Prempal Singh",Toast.LENGTH_SHORT).show();
                else
                    startDownload();
            }
        });
        mQueue = Volley.newRequestQueue(getActivity());
        getCharts();

        return rootView;
    }

    private void startDownload(){
        mProgress.setVisibility(View.VISIBLE);
        final String input = mInput.getText().toString();
        String url = "http://getsa.ga/request.php";
        StringRequest request = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
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

    private void getCharts(){
        String url = "http://boundbytech.com/saga/get_charts.php";
        JsonArrayRequest request = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for(int i=0; i<response.length(); i++){
                            JSONArray chart = null;
                            try {
                                chart = response.getJSONArray(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d("Chart",chart.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });
        mQueue.add(request);
    }
}
