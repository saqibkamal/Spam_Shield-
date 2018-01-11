package kamal.saqib.spamshield;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AndroidException;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.query.Select;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;
    private static final int SEND_SMS_PERMISSIONS_REQUEST = 2;
    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 3;
    Button send,read;
    ListView lv;

    HashMap<String,ArrayList<Message>> map_for_asyntask,map_for_db;
    ArrayList<String> msgids_for_asynctask,msgids_for_db,msgsndrs,fmsgs;
    SimpleDateFormat simpleDateFormat;
    HashMap<String,String> contacts;
    private static MainActivity inst;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            getAllPermission();
        } finally {

            send = (Button) findViewById(R.id.send);
            read = (Button) findViewById(R.id.read);


            send.setOnClickListener(this);
            read.setOnClickListener(this);

            lv = (ListView) findViewById(R.id.listview);

            sendJson("HELLO");

            GetContact getContact = new GetContact();
            getContact.execute();

            GetAllMessages getAllMessages = new GetAllMessages();
            getAllMessages.execute();


            simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            contacts = new HashMap<>();

            readfromdatabase();

        }
    }


    public void readfromdatabase(){
        map_for_db=new HashMap<>();
        msgsndrs=new ArrayList<>();
        fmsgs=new ArrayList<>();
        ActiveAndroid.beginTransaction();
        try{

            List<msg_sqldb> msg_from_db = new Select("*")
                    .from(msg_sqldb.class)
                    .orderBy("timestamp DESC").execute();

           for( msg_sqldb msgSqldb:msg_from_db){
               String id=msgSqldb.msg_id;
               String add=msgSqldb.address;
               String dt=msgSqldb.date;
               String tm=msgSqldb.time;
               String tp=msgSqldb.type;
               String mm=msgSqldb.message;
               Long tmstmp=msgSqldb.timestamp;

               String msg_sender=add;

               if(msgsndrs.contains(msg_sender)){
                   map_for_db.get(msg_sender).add(new Message(id,add,dt+" "+tm,tp,mm,String.valueOf(tmstmp)));
               }
               else{
                   msgsndrs.add(msg_sender);
                   ArrayList<Message> temp=new ArrayList<>();
                   temp.add(new Message(id,add,dt+" "+tm,tp,mm,String.valueOf(tmstmp)));
                   map_for_db.put(msg_sender,temp);
                   fmsgs.add(mm);
               }
           }
            lv.setAdapter(new CustomAdapter(this));
            ActiveAndroid.setTransactionSuccessful();

        }
        finally {
            ActiveAndroid.endTransaction();
        }

    }

    public  void updateInbox(Message message){

        if(msgsndrs.contains(message.sender_address)){
            map_for_db.get(message.sender_address).add(message);
        }
        else{
            msgsndrs.add(message.sender_address);
            ArrayList<Message> temp=new ArrayList<>();
            temp.add(message);
            map_for_db.put(message.sender_address,temp);
            fmsgs.add(message.message);
        }
        lv.setAdapter(new CustomAdapter(this));
    }

    public void addnewmsgtodb(Message mg){
        if ((new Select()
                .from(msg_sqldb.class)
                .where("msg_id = ?", mg.id)
                .execute()).size() == 0) {
            msg_sqldb msg_db = new msg_sqldb(mg.id,mg.sender_address,mg.date,mg.time,mg.type,mg.message,mg.timestamp);
            msg_db.save();
            Log.i("NEW ","msg received");
            updateInbox(mg);
        }
    }
    public void addtodb(Message mg){
       msg_sqldb msgSqldb=new msg_sqldb(mg.id,mg.sender_address,mg.date,mg.time,mg.type,mg.message,mg.timestamp);
       msgSqldb.save();
    }





    public void read_msg(){
       // Intent i = new Intent(getBaseContext(), read_msg.class);
        //startActivity(i);
    }
    public void send_msg(){
        Intent i = new Intent(getBaseContext(), send_msg.class);
        startActivity(i);
    }


    @Override
    public void onClick(View view) {
        if(view==read){
            read_msg();
        }
        else if(view==send){
            send_msg();
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void  getAllPermission(){
       getPermissionToReadSMS();
       getPermissionToReadContacts();
        getPermissionToSendSMS();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToReadSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_SMS)) {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_SMS},
                    READ_SMS_PERMISSIONS_REQUEST);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToSendSMS(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS)
                !=PackageManager.PERMISSION_GRANTED){
            if(shouldShowRequestPermissionRationale(
                    Manifest.permission.SEND_SMS
            )){
                Toast.makeText(this,"Please allow Permission!",Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.SEND_SMS},
                    SEND_SMS_PERMISSIONS_REQUEST);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToReadContacts(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS)
                !=PackageManager.PERMISSION_GRANTED){
            if(shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_CONTACTS
            )){
                Toast.makeText(this,"Please allow Permission!",Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.SEND_SMS},
                    READ_CONTACTS_PERMISSIONS_REQUEST);
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_SHORT).show();
               // getPermissionToSendSMS();


            } else {
                Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }

        }
        else if (requestCode == SEND_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Send SMS permission granted", Toast.LENGTH_SHORT).show();
                //getPermissionToReadContacts();


            } else {
                Toast.makeText(this, "Send SMS permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }

        }

        else if (requestCode == READ_CONTACTS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read Contacts permission granted", Toast.LENGTH_SHORT).show();


            } else {
                Toast.makeText(this, "Read Contacts permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }

        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    public class GetContact extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            contacts=new HashMap<>();

            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);

            if ((cur != null ? cur.getCount() : 0) > 0) {
                while (cur != null && cur.moveToNext()) {
                    String id = cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(
                            ContactsContract.Contacts.DISPLAY_NAME));

                    if (cur.getInt(cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));

                            contacts.put(phoneNo,name);

                        }
                        pCur.close();
                    }
                }
            }
            if(cur!=null){
                cur.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i("Completetd","yes");

        }
    }

    public class GetAllMessages extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... strings) {
            map_for_asyntask=new HashMap<>();

            msgids_for_asynctask=new ArrayList<>();

            ContentResolver contentResolver = getContentResolver();
            Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms"), null, null, null, null);
            int indexBody = smsInboxCursor.getColumnIndex("body");
            int indexAddress = smsInboxCursor.getColumnIndex("address");
            int dateColumn = smsInboxCursor.getColumnIndex("date");
            int typeColumn=smsInboxCursor.getColumnIndex("type");
            int typeid=smsInboxCursor.getColumnIndex("_id");
            Log.i("COLUMNS", Arrays.toString(smsInboxCursor.getColumnNames()));

            if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return null;

            do {
                String sndrnmbr=smsInboxCursor.getString(indexAddress);
                String msg=smsInboxCursor.getString(indexBody);
                String date=smsInboxCursor.getString(dateColumn);
                String type=smsInboxCursor.getString(typeColumn);
                String id=smsInboxCursor.getString(typeid);
                Long tt= Long.valueOf(date);
                String dateFromSms = simpleDateFormat.format(new Date(tt));


                msgids_for_asynctask.add(id);
                Message message=new Message(id,sndrnmbr,dateFromSms,type,msg,date);


                if(map_for_asyntask.get(sndrnmbr)!=null)
                    map_for_asyntask.get(sndrnmbr).add(message);
                else{
                    ArrayList<Message> te=new ArrayList<>();
                    te.add(message);
                    map_for_asyntask.put(sndrnmbr,te);
                }

            } while (smsInboxCursor.moveToNext());


            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i("Completeted","Mesage Updation");
           UpdateDatabaseMessages updateDatabaseMessages=new UpdateDatabaseMessages();
           updateDatabaseMessages.execute();

        }
    }

    public class UpdateDatabaseMessages extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... strings) {
            ActiveAndroid.beginTransaction();
            try {
                for (ArrayList<Message> tmpmsg : map_for_asyntask.values()) {
                    for (int j = 0; j < tmpmsg.size(); j++) {
                        Message mg = tmpmsg.get(j);
                        String tmp_id = mg.id;
                        if ((new Select()
                                .from(msg_sqldb.class)
                                .where("msg_id = ?", tmp_id)
                                .execute()).size() == 0) {
                            msg_sqldb msg_db = new msg_sqldb(tmp_id,mg.sender_address,mg.date,mg.time,mg.type,mg.message,mg.timestamp);
                            msg_db.save();
                        }
                    }
                }
                ActiveAndroid.setTransactionSuccessful();
            }
            finally{
                ActiveAndroid.endTransaction();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i("Completeted","Database Updation");
            readfromdatabase();
        }
    }

    public class CustomAdapter extends BaseAdapter {
        ArrayList<String> result;
        Context context;
        int [] imageId;
        private LayoutInflater inflater=null;
        public CustomAdapter(MainActivity mainActivity) {
            // TODO Auto-generated constructor stub
            result=msgsndrs;
            context=mainActivity;


            inflater = ( LayoutInflater )context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return result.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public class Holder
        {
            TextView tv,fmsg;
            ImageView img;

        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            kamal.saqib.spamshield.MainActivity.CustomAdapter.Holder holder= new kamal.saqib.spamshield.MainActivity.CustomAdapter.Holder();
            View rowView;
            rowView = inflater.inflate(R.layout.front_page_listview, null);
            holder.tv=(TextView) rowView.findViewById(R.id.phone_number);
            holder.fmsg=(TextView) rowView.findViewById(R.id.fmsgs);
            holder.img=(ImageView) rowView.findViewById(R.id.imageview);
           holder.tv.setText(result.get(position));
           holder.fmsg.setText(fmsgs.get(position));
           String ph_no=result.get(position);
           if(contacts.containsKey(ph_no))
               holder.tv.setText(contacts.get(ph_no));
            //holder.img.setImageResource(imageId[position]);


            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  String p=msgsndrs.get(position);
                    Intent in = new Intent(getBaseContext(), single_user_msg.class);
                   Bundle args = new Bundle();
                   args.putSerializable("ARRAYLIST",(Serializable)map_for_db.get(p));
                   in.putExtra("BUNDLE",args);


                    startActivity(in);
                }
            });
            return rowView;
        }

    }

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    protected void sendJson(final String msg) {
        Thread t = new Thread() {

            public void run() {
                Looper.prepare();
                HttpClient httpclient;
                HttpGet request;
                HttpResponse response = null;
                String result = "";
                try{
                    httpclient = new DefaultHttpClient();
                    request = new HttpGet("http://10.0.2.2/init.php");
                    HttpPost post = new HttpPost("http://10.0.2.2/init.php");
                    String data= URLEncoder.encode("message","UTF-8")+"="+URLEncoder.encode(msg,"UTF-8");




                    post.setEntity(new StringEntity(data));

                    response = httpclient.execute(post);

                   // response = httpclient.execute(request);
                } catch (Exception e){
                    result = "error1";
                }

                try{
                    BufferedReader rd = new BufferedReader(new InputStreamReader(
                            response.getEntity().getContent()));
                    String line="";
                    while((line = rd.readLine()) != null){
                        result = result + line;
                    }
                    Log.i("msgr",result);
                } catch(Exception e){
                    result = "error2";
                }

                Log.i("msgr",result);
                Looper.loop(); //Loop in the message queue
            }
        };

        t.start();

    }


}
