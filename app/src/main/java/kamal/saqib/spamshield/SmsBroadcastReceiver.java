package kamal.saqib.spamshield;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;

import com.activeandroid.query.Select;
import com.activeandroid.query.Update;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by Saqib kamal on 07-01-2018.
 */

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";
    private String TAG=SmsBroadcastReceiver.class.getSimpleName();
    String spamon;

    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    Context ctx;


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();

        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        String smsBody="",address="",dateFromSms="",id="";
        Long date=0L;
        int x=0;
        ctx=context;



        sharedpreferences = context.getSharedPreferences("Mydata", Context.MODE_PRIVATE);
        editor=sharedpreferences.edit();

        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";
            Message message=new Message();
            for (int i = 0; i < sms.length; ++i) {
                String format = intentExtras.getString("format");
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i], format);

                smsBody = smsMessage.getMessageBody().toString()+"\n";
                address = smsMessage.getOriginatingAddress();
                date = smsMessage.getTimestampMillis();
                dateFromSms = simpleDateFormat.format(new Date(date));
                id=smsMessage.getDisplayMessageBody();

                Log.i("msg_detail",smsBody+" "+address+" "+date.toString());


                Random random =new Random();
                x=random.nextInt()+100000000;
                Log.i("x",String.valueOf(x));
              // message=new Message(String.valueOf(x),address,dateFromSms,"1",smsBody,date.toString(),"ham");

            }

            Log.i("NEW ","JSON CREATED");
            smsBody=smsBody.trim();
            sendJson json=new sendJson();
            json.execute(smsBody,String.valueOf(x),dateFromSms,address,date.toString());


           // MainActivity inst = MainActivity.instance();
            //inst.gotnewmessage(String.valueOf(x),address,dateFromSms,smsBody,date.toString());


        }

    }

    public void Notification(Context context, Message mg) {
        // Set Notification Title
        String strtitle = context.getString(R.string.notificationtitle);

        // Open NotificationView Class on Notification Click
        Intent intent = new Intent(context, NotificationView.class);
        // Send data to NotificationView Class
        intent.putExtra("title", mg.sender_address);
        intent.putExtra("text", mg.message);
        // Open NotificationView.java Activity
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Create Notification using NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context)
                // Set Icon
                .setSmallIcon(R.drawable.ic_message)
                // Set Ticker Message
                .setTicker(mg.message)
                // Set Title
                .setContentTitle(mg.sender_address)
                // Set Text
                .setContentText(mg.message)
                // Add an Action Button below Notification
                .addAction(R.drawable.bow_icon, "Action Button", pIntent)
                // Set PendingIntent into Notification
                .setContentIntent(pIntent)
                // Dismiss Notification
                .setAutoCancel(true)
                .setSound(Uri.parse("android.resource://" + "kamal.saqib.spamshield" + "/" + R.raw.notification));

        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        notificationmanager.notify(0, builder.build());

    }

    public void nextphase(Message mg){
        msg_sqldb msg_db = new msg_sqldb(mg.id,mg.sender_address,mg.date,mg.time,mg.type,mg.message,mg.timestamp,mg.spam);
        msg_db.save();
        Log.i("NEW ","msg received");
        updatecountdb(mg);
        MainActivity inst = MainActivity.instance();
        if(mg.spam.equals("ham"))
            Notification(ctx,mg);
        if(inst!=null ){

            //inst.addtocountdb(mg);
            inst.setcount();
            inst.updateInbox(mg);
            single_user_msg inst2=single_user_msg.instance();
            if(inst2!=null && inst2.phoneNo.equals(mg.sender_address)){
                inst2.msgs.add(0,mg);
                inst2.set();
            }
        }

    }
    public void updatecountdb(Message mg){
        String date=mg.date;
        if((new Select().from(msg_countdb.class).where("date = ?",date).execute()).size()==0){
            msg_countdb msgCountdb =new msg_countdb(date,1,mg.spam.equals("spam")?1:0);
            msgCountdb.save();
            Log.i("Update on old","Successful");
        }
        else{
            List<msg_countdb> msgCountdb=  new Select().from(msg_countdb.class).where("date = ?",date).execute();
            int x=0;
            int tot=msgCountdb.get(0).totalmsg;
            int spa=msgCountdb.get(0).spammsg;
            if(mg.spam.equals("spam")) {
                spa++;
            }

            new Update(msg_countdb.class)
                    .set("totalmsg = ?", tot+1)
                    .where("date = ?", date)
                    .execute();

            new Update(msg_countdb.class)
                    .set("spammsg = ?",spa)
                    .where("date = ?", date)
                    .execute();

            Log.i("Update in count db","Successful");
        }
    }

    public class sendJson extends AsyncTask<String ,Void,Void> {
        String msg,id,sender,date,timestamp,result;

        @Override
        protected Void doInBackground(String... strings) {



                msg = strings[0];
                id = strings[1];
                sender = strings[3];
                date = strings[2];
                timestamp = strings[4];

            spamon=sharedpreferences.getString("spamonoff","ham");
            if(spamon==null || spamon.equals("on")) {


                HttpClient httpclient;
                HttpResponse response = null;
                result = "";
                try {
                    Log.i("timestamp", timestamp);
                    httpclient = new DefaultHttpClient();

                    HttpPost post = new HttpPost("https://spamshield.herokuapp.com/predict");

                    JSONObject json = new JSONObject();
                    json.put("messagejson", msg);
                    StringEntity se;
                    se = new StringEntity(json.toString());
                    post.setEntity(se);

                    post.setHeader("Content-type", "application/json");
                    response = httpclient.execute(post);
                    Log.i("msgr", "result");

                } catch (Exception e) {
                    result = "error1";
                }

                try {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(
                            response.getEntity().getContent()));
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                        result = result + line;
                    }
                    Log.i("msgr", result);
                } catch (Exception e) {
                    result = "error2";
                }

                Log.i("msgr", result);
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i("Completeted","Chk ");
            String ans;
            if(result!=null && result.contains("spam")){
                ans="spam";
            }
            else
                ans="ham";
            Log.i("Result", String.valueOf(date.length()));
            Message mesg=new Message(id,sender,date,"1",msg,timestamp,ans);
            nextphase(mesg);


        }


    }


}
