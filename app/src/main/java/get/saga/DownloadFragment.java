package get.saga;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by prempal on 16/2/15.
 */
public class DownloadFragment extends Fragment {

    EditText mInput;

    public DownloadFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mInput = (EditText) rootView.findViewById(R.id.et_input);
        Button downloadBtn = (Button) rootView.findViewById(R.id.btn_download);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PostQuery().execute();
            }
        });

        return rootView;
    }

    private class PostQuery extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            String input = mInput.getText().toString();
            BufferedReader reader = null;
            DownloadManager dMgr = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

            try
            {
                URL url = new URL("http://getsa.ga/response.php");
                URLConnection urlConnection = url.openConnection();
                urlConnection.setDoOutput(true);
                OutputStreamWriter streamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                streamWriter.write(input);
                streamWriter.flush();

                // Get the server response
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String response = reader.readLine();
                Log.d("Response",response);
                Uri uri = Uri.parse(response);
                DownloadManager.Request dr = new DownloadManager.Request(uri);
                dMgr.enqueue(dr);

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

            return null;
        }
    }
}
