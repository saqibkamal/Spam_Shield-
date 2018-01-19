package kamal.saqib.spamshield;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;


//import me.philio.preferencecompatextended.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref);
    }

}