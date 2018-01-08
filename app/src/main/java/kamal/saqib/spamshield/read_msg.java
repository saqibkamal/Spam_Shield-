package kamal.saqib.spamshield;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;



public class read_msg extends AppCompatActivity implements Serializable {
    ArrayList<String> smsMessagesList = new ArrayList<>();
    ListView messages;
    ArrayAdapter arrayAdapter;
    HashMap<String,ArrayList<Message>> map;
    ArrayList<String> sendernumber,sendername;
    SimpleDateFormat simpleDateFormat;
    HashMap<String,String> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_msg);

        GetContact getContact=new GetContact();
        getContact.execute();


        map=new HashMap<>();

        simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        contacts=new HashMap<>();

        messages = (ListView) findViewById(R.id.msgs);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        messages.setAdapter(arrayAdapter);


        messages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String p=sendernumber.get(i);
                Intent in = new Intent(getBaseContext(), single_user_msg.class);
                //in.putExtra("msgs",map.get(p));
                Bundle args = new Bundle();
                args.putSerializable("ARRAYLIST",(Serializable)map.get(p));
                in.putExtra("BUNDLE",args);


                startActivity(in);

            }
        });




       refreshSmsInbox();


    }





    public void refreshSmsInbox() {
        arrayAdapter.clear();
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

        arrayAdapter.addAll(sendername);
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

}
