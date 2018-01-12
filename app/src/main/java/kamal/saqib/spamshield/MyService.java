package kamal.saqib.spamshield;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;

public class MyService extends Service {
    public MyService() {

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.


        throw new UnsupportedOperationException("Not yet implemented");
    }


}
