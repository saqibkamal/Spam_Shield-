package kamal.saqib.spamshield;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.telephony.SmsMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Saqib kamal on 07-01-2018.
 */

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();

        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";
            Message message=new Message(null,null,null,null,null);
            for (int i = 0; i < sms.length; ++i) {
                String format = intentExtras.getString("format");
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i], format);

                String smsBody = smsMessage.getMessageBody().toString();
                String address = smsMessage.getOriginatingAddress();
                Long date = smsMessage.getTimestampMillis();
                String dateFromSms = simpleDateFormat.format(new Date(date));

                smsMessageStr += "SMS From: " + address + "\n";
                smsMessageStr += smsBody + "\n";

                message=new Message(address,dateFromSms,"1",smsBody,address);
            }

            MainActivity inst = MainActivity.instance();
            inst.updateInbox(message);
        }
    }
}
