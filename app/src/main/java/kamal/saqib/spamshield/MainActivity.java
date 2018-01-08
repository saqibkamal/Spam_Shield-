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
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;
    private static final int SEND_SMS_PERMISSIONS_REQUEST = 2;
    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 3;
    Button send,read;
    ListView lv;


    HashMap<String,ArrayList<Message>> map;
    ArrayList<String> sendernumber,sendername;
    SimpleDateFormat simpleDateFormat;
    HashMap<String,String> contacts;
    private static MainActivity inst;




    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getAllPermission();


        send=(Button) findViewById(R.id.send);
        read=(Button) findViewById(R.id.read);


        send.setOnClickListener(this);
        read.setOnClickListener(this);


        lv=(ListView) findViewById(R.id.listview);


        GetContact getContact= new GetContact();
        getContact.execute();


        map=new HashMap<>();

        simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        contacts=new HashMap<>();







        refreshSmsInbox();



    }

    public void refreshSmsInbox() {
        sendernumber = new ArrayList<>();
        sendername=new ArrayList<>();

        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        int dateColumn = smsInboxCursor.getColumnIndex("date");
        int typeColumn=smsInboxCursor.getColumnIndex("type");
        Log.i("COLUMNS", Arrays.toString(smsInboxCursor.getColumnNames()));



        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;


        do {
            String sndrnmbr=smsInboxCursor.getString(indexAddress);
            String msg=smsInboxCursor.getString(indexBody);
            String date=smsInboxCursor.getString(dateColumn);
            String type=smsInboxCursor.getString(typeColumn);
            Long tt= Long.valueOf(date);
            String dateFromSms = simpleDateFormat.format(new Date(tt));



            if(!sendernumber.contains(sndrnmbr)) {
                String sndrname=sndrnmbr;
                if(contacts.containsKey(sndrnmbr))
                    sndrname=contacts.get(sndrnmbr);

                sendernumber.add(sndrnmbr);
                sendername.add(sndrname);

                Message message=new Message(sndrnmbr,dateFromSms,type,msg,sndrname);

                ArrayList<Message> te=new ArrayList<>();
                te.add(message);
                map.put(sndrnmbr,te);
            }
            else{
                String sndrname=map.get(sndrnmbr).get(0).sender_name;
                Message message=new Message(sndrnmbr,dateFromSms,type,msg,sndrname);
                map.get(sndrnmbr).add(message);
            }
        } while (smsInboxCursor.moveToNext());



        lv.setAdapter(new CustomAdapter(this));
    }

    public  void updateInbox(Message message){
        if(sendernumber.contains(message.sender_name))
            message.sender_name=map.get(message.sender_address).get(0).sender_name;
        lv.setAdapter(new CustomAdapter(this));
    }



    public void read_msg(){
        Intent i = new Intent(getBaseContext(), read_msg.class);
        startActivity(i);
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



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_SHORT).show();


            } else {
                Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }

        }
        else if (requestCode == SEND_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Send SMS permission granted", Toast.LENGTH_SHORT).show();


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
            refreshSmsInbox();
        }
    }

    public class CustomAdapter extends BaseAdapter {
        ArrayList<String> result;
        Context context;
        int [] imageId;
        private LayoutInflater inflater=null;
        public CustomAdapter(MainActivity mainActivity) {
            // TODO Auto-generated constructor stub
            result=sendernumber;
            context=mainActivity;
            imageId=new int[sendername.size()];
            for(int i=0;i<sendername.size();i++)
                imageId[i]=R.drawable.ic_alert;
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
            holder.img.setImageResource(imageId[position]);
            holder.fmsg.setText(map.get(result.get(position)).get(0).message);
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String p=sendername.get(position);
                    Intent in = new Intent(getBaseContext(), single_user_msg.class);
                    Bundle args = new Bundle();
                    args.putSerializable("ARRAYLIST",(Serializable)map.get(p));
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


}
