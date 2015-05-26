package get.saga;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nononsenseapps.filepicker.FilePickerActivity;

/**
 * Created by prempal on 25/5/15.
 */
public class PrefFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private final String DEFAULT_PATH = Environment.getExternalStorageDirectory().getPath() + "/Saga";
    private final String TOS_URL = "http://getsa.ga/terms";
    private final String PRIVACY_URL = "http://getsa.ga/privacy";
    private final String FACEBOOK_URL = "https://www.facebook.com/sagafreemusic";

    private Preference storagePath;
    private Preference privacy;
    private Preference terms;
    private Preference facebook;
    private SharedPreferences sp;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        context = getActivity();

        storagePath = findPreference("prefStoragePath");
        storagePath.setOnPreferenceClickListener(this);
        privacy = findPreference("prefPrivacy");
        privacy.setOnPreferenceClickListener(this);
        terms = findPreference("prefTOS");
        terms.setOnPreferenceClickListener(this);
        facebook = findPreference("prefFacebook");
        facebook.setOnPreferenceClickListener(this);

        storagePath.setSummary(sp.getString("prefStoragePath", DEFAULT_PATH));
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            findPreference("prefAppVersion").setSummary(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == storagePath) {
            Intent i = new Intent(context, FilePickerActivity.class);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
            startActivityForResult(i, 69);
            return true;
        } else if (preference == facebook) {
            Utils.viewURL(context, FACEBOOK_URL);
        } else if (preference == privacy) {
            Utils.viewURL(context, PRIVACY_URL);
        } else if (preference == terms) {
            Utils.viewURL(context, TOS_URL);
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 69 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            Log.d("path", uri.getPath());
            storagePath.setSummary(uri.getPath());
            sp.edit().putString(storagePath.getKey(), uri.getPath()).apply();
        }
    }
}
