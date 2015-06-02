package get.saga;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by prempal on 29/5/15.
 */
public class Updater {

    public static void checkForUpdates(final Context context,boolean visibility){
        final int versionCode = getVersionCode(context);
        final ProgressDialog progressDialog = new ProgressDialog(context);
        String updateUrl = "https://www.dropbox.com/s/bka9o3p43oki217/saga.json?raw=1";
        if(visibility){
            progressDialog.setTitle(context.getString(R.string.update));
            progressDialog.setMessage(context.getString(R.string.update_checking));
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, updateUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int updateVersionCode = response.getInt("versionCode");
                    if (updateVersionCode > versionCode && versionCode != 0) {
                        if(progressDialog.isShowing()){
                            progressDialog.cancel();
                        }
                        final String apkUrl = response.getString("apkUrl");
                        String changelog = response.getString("changelog");
                        AlertDialog dialog = new AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.new_update))
                                .setMessage(changelog)
                                .setPositiveButton(context.getString(R.string.update_now), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        File myFile = new File(Utils.getStoragePath(context),"update.apk");
                                        if (myFile.exists())
                                            myFile.delete();
                                        Uri uri = Uri.parse(apkUrl);
                                        DownloadManager dMgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                                        DownloadManager.Request dr = new DownloadManager.Request(uri);
                                        String filename = "update.apk";
                                        dr.setTitle(context.getString(R.string.app_name) + " " + context.getString(R.string.update));
                                        dr.setDestinationUri(Uri.fromFile(new File(Utils.getStoragePath(context) + "/" + filename)));
//                                        dr.setDestinationInExternalPublicDir("/Saga/", filename);
                                        dMgr.enqueue(dr);
                                    }
                                })
                                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setCancelable(false)
                                .create();
                        dialog.show();
                    }
                    else{
                        if(progressDialog.isShowing()){
                            progressDialog.cancel();
                            Toast.makeText(context,context.getString(R.string.no_update),Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(progressDialog.isShowing()){
                    progressDialog.cancel();
                    Toast.makeText(context, context.getString(R.string.error_connect), Toast.LENGTH_SHORT).show();
                }
            }
        });
        request.setShouldCache(false);
        VolleySingleton.getInstance(context).getRequestQueue().add(request);
    }

    public static int getVersionCode(Context context){
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }
}