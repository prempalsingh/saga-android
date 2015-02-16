package get.saga;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import get.saga.ui.BootstrapButton;
import get.saga.ui.BootstrapEditText;

/**
 * Created by prempal on 16/2/15.
 */
public class DownloadFragment extends Fragment {

    BootstrapEditText mInput;

    public DownloadFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mInput = (BootstrapEditText) rootView.findViewById(R.id.et_input);
        BootstrapButton downloadBtn = (BootstrapButton) rootView.findViewById(R.id.btn_download);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(mInput.getText()))
                    Toast.makeText(getActivity(),"Enter song name",Toast.LENGTH_SHORT).show();
                else
                    new PostQuery().execute();
            }
        });

        return rootView;
    }

    private class PostQuery extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            String input = mInput.getText().toString();
            DownloadManager dMgr = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://getsa.ga/request.php");

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("track", input));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String response = httpclient.execute(httppost, responseHandler);
                Log.d("Response", response);
                Uri uri = Uri.parse(response);
                DownloadManager.Request dr = new DownloadManager.Request(uri);
                dMgr.enqueue(dr);

            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
