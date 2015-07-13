package get.saga;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nononsenseapps.filepicker.FilePickerActivity;

/**
 * Created by prempal on 25/5/15.
 */
public class PrefFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private final String TOS_URL = "http://sagaone.com/terms";
    private final String PRIVACY_URL = "http://sagaone.com/privacy";
    private final String ABOUT_URL = "http://sagaone.com/about";
    private final String FACEBOOK_URL = "https://www.facebook.com/sagafreemusic";
    private final String GOOGLE_PLUS_URL = "https://plus.google.com/communities/104099181842319544700";
    private final String MADE_WITH_LOVE_URL = "http://madewithlove.org.in";

    private Preference storagePath;
    private Preference update;
    private Preference privacy;
    private Preference terms;
    private Preference facebook;
    private Preference googlePlus;
    private Preference about;
    private Preference madeWithLove;
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
        update = findPreference("prefUpdate");
        update.setOnPreferenceClickListener(this);
        privacy = findPreference("prefPrivacy");
        privacy.setOnPreferenceClickListener(this);
        terms = findPreference("prefTOS");
        terms.setOnPreferenceClickListener(this);
        facebook = findPreference("prefFacebook");
        facebook.setOnPreferenceClickListener(this);
        googlePlus = findPreference("prefGooglePlus");
        googlePlus.setOnPreferenceClickListener(this);
        madeWithLove = findPreference("prefMadeWithLove");
        madeWithLove.setOnPreferenceClickListener(this);
        about = findPreference("prefAboutApp");
        about.setOnPreferenceClickListener(this);

        storagePath.setSummary(Utils.getStoragePath(getActivity()));
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            findPreference("prefAppVersion").setSummary(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == update) {
            Updater.checkForUpdates(getActivity(), true);
        } else if (preference == storagePath) {
            Intent i = new Intent(context, FilePickerActivity.class);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
            startActivityForResult(i, 69);
            return true;
        } else if (preference == facebook) {
            Utils.viewURL(context, FACEBOOK_URL);
        } else if (preference == googlePlus) {
            Utils.viewURL(context, GOOGLE_PLUS_URL);
        } else if (preference == privacy) {
            Utils.viewURL(context, PRIVACY_URL);
        } else if (preference == terms) {
            Utils.viewURL(context, TOS_URL);
        } else if (preference == about) {
            Utils.viewURL(context, ABOUT_URL);
        } else if (preference == madeWithLove) {
            Utils.viewURL(context, MADE_WITH_LOVE_URL);
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
