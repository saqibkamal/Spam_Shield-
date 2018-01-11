package kamal.saqib.spamshield;

import android.app.Application;
import android.content.Intent;

import com.activeandroid.ActiveAndroid;


/**
 * Created by Saqib kamal on 10-01-2018.
 */

public class Activity extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, MyService.class));
        ActiveAndroid.initialize(this);

    }
}
