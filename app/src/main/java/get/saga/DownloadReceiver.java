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
            }
        }
    }
}
