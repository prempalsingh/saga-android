package get.saga;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.images.AndroidArtwork;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by prempal on 29/3/15.
 */
public class DownloadReceiver extends BroadcastReceiver {

    private String TAG = "Receiver";

    public DownloadReceiver() {

    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        DownloadManager dMgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Long downloadId = intent.getExtras().getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
        Cursor c = dMgr.query(new DownloadManager.Query().setFilterById(downloadId));
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                final String title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                Log.d("Receiver", "Title:" + title);
                if (title.equalsIgnoreCase("Saga - Free Music Update")) {
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/Saga/" + "update.apk")), "application/vnd.android.package-archive");
                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                  install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    context.startActivity(install);
                } else {
                    try {
                        TagOptionSingleton.getInstance().setAndroid(true);
                        final File file = new File(Environment.getExternalStorageDirectory() + "/Saga/" + title);
                        final AudioFile f = AudioFileIO.read(file);
                        final Tag tag = f.getTag();
                        String url = "http://ts3.mm.bing.net/th?q=" + title.substring(0, title.length() - 4).replace(" ", "%20") + "+album+art";
                        ImageRequest request = new ImageRequest(url,
                                new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap bitmap) {
                                        FileOutputStream out = null;
                                        try {
                                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                            String imageFileName = "JPEG_" + timeStamp + "_";
                                            File storageDir = Environment.getExternalStorageDirectory();
                                            File cover = File.createTempFile(
                                                    imageFileName, /* prefix */
                                                    ".jpg", /* suffix */
                                                    storageDir /* directory */
                                            );
                                            out = new FileOutputStream(cover);
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                            AndroidArtwork artwork = AndroidArtwork.createArtworkFromFile(cover);
                                            tag.setField(artwork);
                                            String json = readFromFile(context, title);
                                            JSONObject jsonObject = new JSONObject(json);
                                            if (jsonObject.getString("track") != null)
                                                tag.setField(FieldKey.TITLE, jsonObject.getString("track"));
                                            if (jsonObject.getString("artist") != null)
                                                tag.setField(FieldKey.ARTIST, jsonObject.getString("artist"));
                                            if (jsonObject.getString("artist") != null)
                                                tag.setField(FieldKey.ALBUM_ARTIST, jsonObject.getString("artist"));
                                            if (jsonObject.getString("release") != null)
                                                tag.setField(FieldKey.YEAR, jsonObject.getString("release"));
                                            if (jsonObject.getString("trackno") != null)
                                                tag.setField(FieldKey.TRACK, jsonObject.getString("trackno"));
                                            if (jsonObject.getString("album") != null)
                                                tag.setField(FieldKey.ALBUM, jsonObject.getString("album"));
                                            if (jsonObject.getString("genre") != null)
                                                tag.setField(FieldKey.GENRE, jsonObject.getString("genre"));
                                            tag.setField(FieldKey.COMMENT, "Downloaded from Saga");
                                            f.commit();
                                            Log.d(TAG, "AlbumArt deleted " + cover.delete());
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                Intent mediaScanIntent = new Intent(
                                                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                                Uri contentUri = Uri.fromFile(file);
                                                mediaScanIntent.setData(contentUri);
                                                context.sendBroadcast(mediaScanIntent);
                                            } else {
                                                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/Saga")));
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        } finally {
                                            try {
                                                if (out != null) {
                                                    out.close();
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }, 0, 0, null,
                                new Response.ErrorListener() {
                                    public void onErrorResponse(VolleyError error) {
                                        error.printStackTrace();
                                    }
                                });
                        request.setShouldCache(false);
                        VolleySingleton.getInstance(context).addToRequestQueue(request);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String readFromFile(Context context, String filename) {

        String ret = "";
        String file = filename.substring(0, filename.length() - 3) + "txt";

        try {
            InputStream inputStream = context.openFileInput(file);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        } finally {
            boolean deleted = context.deleteFile(file);
            Log.d(TAG, "Song info deleted: " + deleted);
        }

        return ret;
    }
}
