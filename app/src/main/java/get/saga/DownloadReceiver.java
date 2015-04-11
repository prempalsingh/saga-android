package get.saga;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by prempal on 29/3/15.
 */
public class DownloadReceiver extends BroadcastReceiver {

    public DownloadReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        DownloadManager dMgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Long downloadId = intent.getExtras().getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
        Cursor c = dMgr.query(new DownloadManager.Query().setFilterById(downloadId));
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                String title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                Log.d("Receiver", "Title:" + title);
                if(title.equalsIgnoreCase("Saga - Free Music Update")){
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/Saga/" + "update.apk")), "application/vnd.android.package-archive");
                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                  install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    context.startActivity(install);
                }
//                else{
//                    try{
//                        TagOptionSingleton.getInstance().setAndroid(true);
//                        final AudioFile f = AudioFileIO.read(new File(Environment.getExternalStorageDirectory() + "/Saga/" + title));
//                        final Tag tag = f.getTag();
//                        String url = "http://ts3.mm.bing.net/th?q=" + title.replace(" ","%20") + "+album+art";
//                        ImageRequest request = new ImageRequest(url,
//                                new Response.Listener<Bitmap>() {
//                                    @Override
//                                    public void onResponse(Bitmap bitmap) {
//                                        FileOutputStream out = null;
//                                        try {
//                                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//                                            String imageFileName = "JPEG_" + timeStamp + "_";
//                                            File storageDir = Environment.getExternalStorageDirectory();
//                                            File cover = File.createTempFile(
//                                                    imageFileName, /* prefix */
//                                                    ".jpg", /* suffix */
//                                                    storageDir /* directory */
//                                            );
//                                            out = new FileOutputStream(cover);
//                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//                                            AndroidArtwork artwork = AndroidArtwork.createArtworkFromFile(cover);
//                                            tag.addField(artwork);
//                                            f.commit();
//                                            boolean deleted = false; //cover.delete();
//                                            Log.d("Receiver",tag.toString() + deleted);
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        } finally {
//                                            try {
//                                                if (out != null) {
//                                                    out.close();
//                                                }
//                                            } catch (IOException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                    }
//                                }, 0, 0, null,
//                                new Response.ErrorListener() {
//                                    public void onErrorResponse(VolleyError error) {
//                                        error.printStackTrace();
//                                    }
//                                });
//                        request.setShouldCache(false);
//                        VolleySingleton.getInstance(context).addToRequestQueue(request);
//                    }
//                    catch(Exception e){
//                        e.printStackTrace();
//                    }
//                }
            }
        }
    }
}
