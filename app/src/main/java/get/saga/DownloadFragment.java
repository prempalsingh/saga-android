package get.saga;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by prempal on 16/2/15.
 */
public class DownloadFragment extends Fragment {

    EditText mInput;
    ProgressBar mProgress;
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
        //new GetCharts().execute();

        return rootView;
    }

    private void startDownload(){
        RequestQueue queue = Volley.newRequestQueue(getActivity());
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
        queue.add(request);
    }

    private class GetCharts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            URL url = null;
            String jsonStr = null;
            BufferedReader reader = null;
            try {
                url = new URL("http://boundbytech.com/saga/get_charts.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String inputLine = "";
                while ((inputLine = reader.readLine()) != null) {
                    result.append(inputLine);
                }
                jsonStr = result.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try{
                if(reader!=null)
                    reader.close();
                } catch (IOException e) {
                e.printStackTrace();
                }
            }
            Log.d("Response: ", "> " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonStr);

                    for(int i=0; i<jsonArray.length(); i++){
                        JSONArray chart = jsonArray.getJSONArray(i);
                        Log.d("Chart",chart.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("Charts", "Couldn't get any data from the url");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

    }
}
