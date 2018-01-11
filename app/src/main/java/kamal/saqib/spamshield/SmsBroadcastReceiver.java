package kamal.saqib.spamshield;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by Saqib kamal on 07-01-2018.
 */

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";
    private String TAG=SmsBroadcastReceiver.class.getSimpleName();


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();

        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";
            Message message=new Message();
            for (int i = 0; i < sms.length; ++i) {
                String format = intentExtras.getString("format");
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i], format);

                String smsBody = smsMessage.getMessageBody().toString()+"\n";
                String address = smsMessage.getOriginatingAddress();
                Long date = smsMessage.getTimestampMillis();
                String dateFromSms = simpleDateFormat.format(new Date(date));
                String id=smsMessage.getDisplayMessageBody();

                Log.i("msg_detail",smsBody+" "+address+" "+date.toString());


                Random random =new Random();
                int x=random.nextInt()+100000000;
                Log.i("x",String.valueOf(x));
               message=new Message(String.valueOf(x),address,dateFromSms,"1",smsBody,date.toString());

            }
            MainActivity inst = MainActivity.instance();
            //MainActivity inst=new MainActivity();
            if(inst!=null)
            inst.addnewmsgtodb(message);
            else{
                MainActivity mainActivity=new MainActivity();
                mainActivity.addtodb(message);
            }


        }
    }
}
