package kamal.saqib.spamshield;

import android.Manifest;
import android.animation.Animator;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.ebanx.swipebtn.OnStateChangeListener;
import com.ebanx.swipebtn.SwipeButton;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import dmax.dialog.SpotsDialog;


import static kamal.saqib.spamshield.R.id.txt_msg1;
import static kamal.saqib.spamshield.R.id.txt_spam_count;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private DrawerLayout mDrawer;
    private  android.support.v7.app.ActionBarDrawerToggle toggle;

    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;
    private static final int SEND_SMS_PERMISSIONS_REQUEST = 2;
    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 3;

    private FloatingActionButton fab;
    private RelativeLayout layoutMain;
    private RelativeLayout layoutContent;
    private  boolean isOpen=false;
    TextView spamcount;


    ListView lv;
    AlertDialog alertDialog;
    Button button;

    HashMap<String,ArrayList<Message>> map_for_asyntask,smap_for_db,hmap_for_db;
    ArrayList<String> msgids_for_asynctask,smsgsndrs,sfmsgs,hfmsgs,hmsgsndrs;
    HashMap<String,Message> s_fmsg,h_fmsg;
    SimpleDateFormat simpleDateFormat;
    HashMap<String,String> contacts_for_db,contacts_for_asynctask;
    private static MainActivity inst;

    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_first);

        //getAllPermission();

        mDrawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        toggle = new android.support.v7.app.ActionBarDrawerToggle(this,mDrawer,R.string.open,R.string.close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        button=findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Spam_msgs.class));
            }
        });

        /*ImageView searchImage = findViewById(R.id.img_search_icon);
        searchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(),"Search",Toast.LENGTH_SHORT).show();
            }
        });*/

        final String myPackageName = getPackageName();
        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
            Intent intent =
                    new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                    myPackageName);
            startActivity(intent);
        }
        else {
            Toast.makeText(getApplicationContext(),"Default Messaging App",Toast.LENGTH_LONG).show();
        }




        sharedpreferences = getSharedPreferences("Mydata", Context.MODE_PRIVATE);
        editor=sharedpreferences.edit();

        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
            editor.putString("firsttime",null);
            editor.commit();

        }



        layoutMain=findViewById(R.id.layoutMain);
        //layoutContent=findViewById(R.id.layoutContent);
        fab=findViewById(R.id.big_button);
        setcount();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent in=new Intent(getApplicationContext(),selectcontacts.class);
                Bundle args = new Bundle();
                //args.putSerializable("allmsgs", hmap_for_db);
                args.putSerializable("allcntcts",contacts_for_db);
                in.putExtra("BUNDLE", args);

                //viewMenu();
                startActivity(in);
            }
        });

        TextView t1 =findViewById(txt_spam_count);
        Typeface myFont1 = Typeface.createFromAsset(getAssets(),"Fonts/Nexa_Bold.otf");
        t1.setTypeface(myFont1);

        TextView t2 =findViewById(txt_msg1);
        Typeface myFont2 = Typeface.createFromAsset(getAssets(),"Fonts/Nexa_Bold.otf");
        t2.setTypeface(myFont2);

        lv = findViewById(R.id.lv_msg);


        alertDialog = new SpotsDialog(this);
        alertDialog.show();

        showActionBar();

        hmap_for_db=new HashMap<>();
        smap_for_db=new HashMap<>();
        smsgsndrs=new ArrayList<>();
        hmsgsndrs=new ArrayList<>();
        sfmsgs=new ArrayList<>();
        hfmsgs=new ArrayList<>();
        s_fmsg=new HashMap<>();
        h_fmsg=new HashMap<>();

        GetContact getContact = new GetContact();
        getContact.execute();

        String x=sharedpreferences.getString("firsttime",null);
       // Log.i("VALUE OF X IS ",x);
        if(x==null){
            editor.putString("firsttime","no");
            editor.commit();
            Log.i("First","time");
            GetAllMessages getAllMessages = new GetAllMessages();
            getAllMessages.execute();
            editor.remove("spamonoff");
            editor.commit();
            editor.putString("spamonoff","off");
            editor.commit();
        }


        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

        readcontactsfromdatabase();
        readfromdatabase();

        SwipeButton enableButton = findViewById(R.id.swipe_btn);


        enableButton.setOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(boolean active) {
                if(active==true) {
                    Toast.makeText(MainActivity.this, "Spam shield is on!", Toast.LENGTH_SHORT).show();
                    editor.remove("spamonoff");
                    editor.commit();
                    editor.putString("spamonoff","on");
                    editor.commit();
                }
                else {
                    Toast.makeText(MainActivity.this, "Spam shield is off!", Toast.LENGTH_SHORT).show();
                    editor.remove("spamonoff");
                    editor.commit();
                    editor.putString("spamonoff","off");
                    editor.commit();
                }
            }
        });


        


    }

    private void showActionBar(){
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.screen1,null);
        final ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(false);
        bar.setDisplayShowCustomEnabled(true);
        bar.setDisplayShowTitleEnabled(false);
        bar.setCustomView(v);

    }

    public void readcontactsfromdatabase(){
        contacts_for_db=new HashMap<>();
        ActiveAndroid.beginTransaction();
        try{
            List<cntcts_sqldb> cntctsSqldbs = new Select("*")
                    .from(cntcts_sqldb.class)
                    .execute();
            for(cntcts_sqldb ct:cntctsSqldbs){
                contacts_for_db.put(ct.number,ct.name);

            }
            ActiveAndroid.setTransactionSuccessful();
        }
        finally {
            ActiveAndroid.endTransaction();
        }

    }


    public void readfromdatabase(){
        hmap_for_db=new HashMap<>();
        smap_for_db=new HashMap<>();
        smsgsndrs=new ArrayList<>();
        hmsgsndrs=new ArrayList<>();
        sfmsgs=new ArrayList<>();
        hfmsgs=new ArrayList<>();
        s_fmsg=new HashMap<>();
        h_fmsg=new HashMap<>();

        ActiveAndroid.beginTransaction();
        try {

            List<msg_sqldb> msg_from_db = new Select("*").from(msg_sqldb.class)
                    .orderBy("timestamp DESC ").execute();

            for (msg_sqldb msgSqldb : msg_from_db){
                String id=msgSqldb.msg_id;
                String add=msgSqldb.address;
                String dt=msgSqldb.date;
                String tm=msgSqldb.time;
                String tp=msgSqldb.type;
                String mm=msgSqldb.message;
                Long tmstmp=msgSqldb.timestamp;
                String sp=msgSqldb.spam;

                String msg_sender=add;
                if(sp.equals("spam")){
                    Log.i("spam",msgSqldb.message);
                    if(smsgsndrs.contains(msg_sender)){

                        smap_for_db.get(msg_sender).add(new Message(id,add,dt+" "+tm,tp,mm,String.valueOf(tmstmp),sp));
                        //s_fmsg.remove(add);
                        //s_fmsg.put(add,new Message(id,add,dt+" "+tm,tp,mm,String.valueOf(tmstmp),sp));
                    }
                    else{
                        smsgsndrs.add(msg_sender);
                        ArrayList<Message> temp=new ArrayList<>();
                        temp.add(new Message(id,add,dt+" "+tm,tp,mm,String.valueOf(tmstmp),sp));
                        smap_for_db.put(msg_sender,temp);
                        s_fmsg.put(add,new Message(id,add,dt+" "+tm,tp,mm,String.valueOf(tmstmp),sp));
                        sfmsgs.add(mm);
                    }
                }
                else{
                    if(hmsgsndrs.contains(msg_sender)){
                        //Log.i("repeated",msg_sender);
                        //h_fmsg.remove(add);
                        //h_fmsg.put(add,new Message(id,add,dt+" "+tm,tp,mm,String.valueOf(tmstmp),sp));
                        hmap_for_db.get(msg_sender).add(new Message(id,add,dt+" "+tm,tp,mm,String.valueOf(tmstmp),sp));
                    }
                    else{
                        //Log.i("not repeated",msg_sender);
                        hmsgsndrs.add(msg_sender);
                        ArrayList<Message> temp=new ArrayList<>();
                        temp.add(new Message(id,add,dt+" "+tm,tp,mm,String.valueOf(tmstmp),sp));
                        hmap_for_db.put(msg_sender,temp);
                        h_fmsg.put(add,new Message(id,add,dt+" "+tm,tp,mm,String.valueOf(tmstmp),sp));
                        hfmsgs.add(mm);
                    }
                }
            }
            if (hmap_for_db.size() != 0 || smap_for_db.size() != 0)
                alertDialog.dismiss();
            lv.setAdapter(new CustomAdapter(this));
            ActiveAndroid.setTransactionSuccessful();

        }
        finally {
            ActiveAndroid.endTransaction();
        }
    }

    public  void updateInbox(Message message) {

        if (message.spam.equals("spam")) {
            if (smsgsndrs.contains(message.sender_address)) {
                s_fmsg.remove(message.sender_address);
                s_fmsg.put(message.sender_address,message);
                smap_for_db.get(message.sender_address).add(message);
            } else {
                smsgsndrs.add(message.sender_address);
                ArrayList<Message> temp = new ArrayList<>();
                temp.add(message);
                smap_for_db.put(message.sender_address, temp);
                s_fmsg.put(message.sender_address,message);
                sfmsgs.add(message.message);
            }
        } else {
            if (hmsgsndrs.contains(message.sender_address)) {
                h_fmsg.remove(message.sender_address);
                h_fmsg.put(message.sender_address,message);
                hmap_for_db.get(message.sender_address).add(message);
            } else {
                hmsgsndrs.add(message.sender_address);
                ArrayList<Message> temp = new ArrayList<>();
                temp.add(message);
                hmap_for_db.put(message.sender_address, temp);
                h_fmsg.put(message.sender_address,message);
                hfmsgs.add(message.message);
            }
        }
        lv.setAdapter(new CustomAdapter(this));
    }

    public void addtocountdb(Message message){
        String date=message.date;
        if((new Select().from(msg_countdb.class).where("date = ?",date).execute()).size()==0){
            msg_countdb msgCountdb =new msg_countdb(date,1,message.spam.equals("spam")?1:0);
            msgCountdb.save();
            Log.i("Update on old","Successful");

        }
        else{
            List<msg_countdb> msgCountdb=  new Select().from(msg_countdb.class).where("date = ?",date).execute();
            int x=0;
            int tot=msgCountdb.get(0).totalmsg;
            int spa=msgCountdb.get(0).spammsg;
            if(message.spam.equals("spam")) {
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

            Log.i("Update on new","Successful");
        }
        setcount();
    }

    public void addnewmsgtodb(Message mg){
        if(mg.spam.equals("ham"))
            shownotification(mg);
        addtocountdb(mg);

        if ((new Select()
                .from(msg_sqldb.class)
                .where("address = ?", mg.sender_address)
                .where("timestamp = ?",mg.timestamp)
                .where("message = ?",mg.message)
                .execute()).size() == 0) {
            msg_sqldb msg_db = new msg_sqldb(mg.id,mg.sender_address,mg.date,mg.time,mg.type,mg.message,mg.timestamp,mg.spam);
            msg_db.save();
            Log.i("NEW ","msg received");
            updateInbox(mg);
        }
    }

    public void addsendsmstodb(Message mg){
        msg_sqldb msgSqldb=new msg_sqldb(mg.id,mg.sender_address,mg.date,mg.time,mg.type,mg.message,mg.timestamp,mg.spam);
        msgSqldb.save();
    }

    public void gotnewmessage(String id,String sender,String date,String msg,String timestamp){


       // sendJson json=new sendJson();
        //json.execute(msg,id,date,sender,timestamp);
    }

    public void shownotification(Message message) {
        //PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), 0);
        // Resources r = getResources();
        if (message.spam.equals("ham")) {
            String snd = message.sender_address;
            if (contacts_for_db.containsKey(message.sender_address))
                snd = contacts_for_db.get(message.sender_address);

            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker("New Message")
                    .setSmallIcon(android.R.drawable.stat_notify_sync)
                    .setContentTitle(snd)
                    .setContentText(message.message)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_MAX)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, notification);
        }
    }

    public void viewMenu(){
        if(!isOpen){
            int x=layoutContent.getRight();
            int y=layoutContent.getBottom();

            int startRadius=0;
            int endRadius=(int) Math.hypot(layoutMain.getWidth(),layoutMain.getHeight());
            //fab.setBackgroundTintList(ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),android.R.color.holo_blue_light,null)));
            Animation clock = AnimationUtils.loadAnimation(this, R.anim.clock);
            Animation aclock = AnimationUtils.loadAnimation(this, R.anim.aclock);
            fab.startAnimation(clock);
            Animator anim= ViewAnimationUtils.createCircularReveal(layoutMain,x,y,startRadius,endRadius);
            anim.start();
            startActivity(new Intent(getApplicationContext(),send_msg.class));
            fab.startAnimation(aclock);
        }
        else{
            int x=layoutContent.getRight();
            int y=layoutContent.getBottom();

            int startRadius=Math.max(layoutMain.getWidth(),layoutMain.getHeight());
            int endRadius=0;
            //fab.setBackgroundTintList(ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),android.R.color.white,null)));
            Animation aclock = AnimationUtils.loadAnimation(this, R.anim.aclock);
            fab.startAnimation(aclock);
            Animator anim= ViewAnimationUtils.createCircularReveal(layoutContent,x,y,startRadius,endRadius);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    //layoutContent.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            anim.start();
            isOpen=false;
        }
    }

    public void set(){
        if(alertDialog.isShowing())
            alertDialog.dismiss();
        lv.setAdapter(new CustomAdapter(this));
    }

    public void setcount(){
        spamcount=findViewById(R.id.txt_spam_count);

        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        Long t=System.currentTimeMillis();
        String date=simpleDateFormat.format(new Date(t));

        date=date.substring(0,10);

        List<msg_countdb> msgCountdbs = new Select("*").from(msg_countdb.class).
                where("date = ?",date).execute();

        if(msgCountdbs==null || msgCountdbs.size()==0){

            spamcount.setText("0");
        }
        else{


            spamcount.setText(String.valueOf(msgCountdbs.get(0).spammsg));
        }


    }


    @Override
    public void onClick(View view) {

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

        switch (requestCode) {
            case READ_SMS_PERMISSIONS_REQUEST: {
                if (grantResults.length == 1 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_SHORT).show();
                    // getPermissionToSendSMS();


                } else {
                    Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            break;



            case SEND_SMS_PERMISSIONS_REQUEST: {
                if (grantResults.length == 1 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Send SMS permission granted", Toast.LENGTH_SHORT).show();
                    //getPermissionToReadContacts();


                } else {
                    Toast.makeText(this, "Send SMS permission denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            break;



            case READ_CONTACTS_PERMISSIONS_REQUEST: {
                if (grantResults.length == 1 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Read Contacts permission granted", Toast.LENGTH_SHORT).show();


                } else {
                    Toast.makeText(this, "Read Contacts permission denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    public class GetContact extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            contacts_for_asynctask=new HashMap<>();

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

                            contacts_for_asynctask.put(phoneNo,name);
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
            UpdateDatabasecontacts updateDatabasecontacts=new UpdateDatabasecontacts();
            updateDatabasecontacts.execute();

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
                msg=msg.trim();
                String dateFromSms = simpleDateFormat.format(new Date(tt));


                msgids_for_asynctask.add(id);
                Message message=new Message(id,sndrnmbr,dateFromSms,type,msg,date,"ham");


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
            //sendOldMessage sendOldMessage=new sendOldMessage();
            //sendOldMessage.execute();
            UpdateDatabaseMessages updateDatabaseMessages=new UpdateDatabaseMessages();
            updateDatabaseMessages.execute();

        }
    }

    public class UpdateDatabaseMessages extends AsyncTask<String,Void,Void>{
        int flag1=0;

        @Override
        protected Void doInBackground(String... strings) {
            ActiveAndroid.beginTransaction();
            try {


                for (ArrayList<Message> tmpmsg : map_for_asyntask.values()) {
                    for (int j = 0; j < tmpmsg.size(); j++) {
                        Message mg = tmpmsg.get(j);
                        String tmp_id = mg.id;
                        if ((new Select().from(msg_sqldb.class).where("address = ?", mg.sender_address)
                                .where("timestamp = ?",mg.timestamp).where("message = ?",mg.message)
                                .execute()).size() == 0) {

                            msg_sqldb msgSqldb = new msg_sqldb(tmp_id, mg.sender_address, mg.date, mg.time, mg.type, mg.message, mg.timestamp, mg.spam);
                            msgSqldb.save();
                            flag1=1;

                           /* String id=msgSqldb.msg_id;
                            String add=msgSqldb.address;
                            String dt=msgSqldb.date;
                            String tm=msgSqldb.time;
                            String tp=msgSqldb.type;
                            String mm=msgSqldb.message;
                            Long tmstmp=msgSqldb.timestamp;
                            String sp=msgSqldb.spam;
                            String msg_sender=add;
                            if(sp.equals("spam")){
                                Log.i("spam",msgSqldb.message);
                                if(smsgsndrs.contains(msg_sender)){
                                    smap_for_db.get(msg_sender).add(new Message(id,add,dt+" "+tm,tp,mm,String.valueOf(tmstmp),sp));
                                }
                                else{
                                    smsgsndrs.add(msg_sender);
                                    ArrayList<Message> temp=new ArrayList<>();
                                    temp.add(new Message(id,add,dt+" "+tm,tp,mm,String.valueOf(tmstmp),sp));
                                    smap_for_db.put(msg_sender,temp);
                                    sfmsgs.add(mm);
                                }
                            }
                            else{
                                if(hmsgsndrs.contains(msg_sender)){
                                    hmap_for_db.get(msg_sender).add(new Message(id,add,dt+" "+tm,tp,mm,String.valueOf(tmstmp),sp));
                                }
                                else{
                                    hmsgsndrs.add(msg_sender);
                                    ArrayList<Message> temp=new ArrayList<>();
                                    temp.add(new Message(id,add,dt+" "+tm,tp,mm,String.valueOf(tmstmp),sp));
                                    hmap_for_db.put(msg_sender,temp);
                                    hfmsgs.add(mm);
                                }
                            }*/
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
            //set();
            if(flag1==1)
                readfromdatabase();
        }
    }

    public class UpdateDatabasecontacts extends AsyncTask<String,Void,Void>{
        int flag=0;

        @Override
        protected Void doInBackground(String... strings) {
            ActiveAndroid.beginTransaction();
            try {

                for (String phn : contacts_for_asynctask.keySet()) {
                    String nm= contacts_for_asynctask.get(phn);
                    if ((new Select().from(cntcts_sqldb.class).where("name = ?", nm).
                            where("number = ?", phn).execute()).size() == 0) {

                        cntcts_sqldb cntctsSqldb=new cntcts_sqldb(nm,phn);
                        cntctsSqldb.save();
                        flag=1;

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
            Log.i("Completeted","Database phone Updation");
            if(flag==1)
                readcontactsfromdatabase();
        }
    }

    public class CustomAdapter extends BaseAdapter {
        ArrayList<String> result;
        Context context;
        private LayoutInflater inflater = null;

        public CustomAdapter(MainActivity mainActivity) {
            // TODO Auto-generated constructor stub
            result = hmsgsndrs;
            context = mainActivity;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        public class Holder {
            TextView tv, fmsg;
            ImageView img;

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            kamal.saqib.spamshield.MainActivity.CustomAdapter.Holder holder = new kamal.saqib.spamshield.MainActivity.CustomAdapter.Holder();
            View rowView;
            rowView = inflater.inflate(R.layout.front_page_listview, null);

            holder.tv = rowView.findViewById(R.id.phone_number);
            holder.fmsg = rowView.findViewById(R.id.fmsgs);
            holder.img = rowView.findViewById(R.id.imageview);

            holder.tv.setText(result.get(position));
            holder.fmsg.setText(hfmsgs.get(position));

           holder.fmsg.setText(h_fmsg.get(result.get(position)).message);

            String ph_no = result.get(position);
            if (contacts_for_db.containsKey(ph_no))
                holder.tv.setText(contacts_for_db.get(ph_no));


           /*final SwipeMenuCreator creator;
           // creator = new SwipeMenuCreator() {
                @Override
                public void create(SwipeMenu menu) {
                    // create "open" item
                    SwipeMenuItem deleteItem = new SwipeMenuItem(
                            getApplicationContext());
                    // set item background
                    deleteItem.setBackground(new ColorDrawable(Color.rgb(148, 143, 143)));
                    // set item width
                    deleteItem.setWidth(150);
                    deleteItem.setIcon(R.drawable.ic_bin);
                    // add to menu
                    menu.addMenuItem(deleteItem);
                    // create "delete" item
                    SwipeMenuItem blockItem = new SwipeMenuItem(
                            getApplicationContext());
                    // set item background
                    blockItem.setBackground(new ColorDrawable(Color.rgb(252,
                            93, 93)));
                    // set item width
                    blockItem.setWidth(150);
                    // set a icon
                    blockItem.setIcon(R.drawable.ic_block);
                    // add to menu
                    menu.addMenuItem(blockItem);
                }
            };
// set creator
          /*  lv.setMenuCreator(creator);
            lv.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                    switch (index) {
                        case 0:
                            Toast.makeText(getApplicationContext(),"Delete",Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            Toast.makeText(getApplicationContext(),"Block",Toast.LENGTH_SHORT).show();
                            break;
                    }
                    // false : close the menu; true : not close the menu
                    return true;
                }
            });*/

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String p = hmsgsndrs.get(position);
                    Intent in = new Intent(getBaseContext(), single_user_msg.class);
                    Bundle args = new Bundle();
                    args.putSerializable("ARRAYLIST", hmap_for_db.get(p));
                    args.putSerializable("phonenumber",hmap_for_db.get(p).get(0).sender_address);
                    in.putExtra("BUNDLE", args);
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

   /* public class sendJson extends  AsyncTask<String ,Void,Void>{
        String msg,id,sender,date,timestamp,result;

        @Override
        protected Void doInBackground(String... strings) {

            msg=strings[0];
            id=strings[1];
            sender=strings[3];
            date=strings[2];
            timestamp=strings[4];


            HttpClient httpclient;
            HttpResponse response = null;
            result = "";
            try{
                httpclient = new DefaultHttpClient();

                HttpPost post = new HttpPost("https://spamshield.herokuapp.com/predict");

                JSONObject json = new JSONObject();
                json.put("messagejson", msg);
                StringEntity se;
                se = new StringEntity(json.toString());
                post.setEntity(se);

                post.setHeader("Content-type", "application/json");
                response = httpclient.execute(post);
                Log.i("msgr","result");

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
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i("Completeted","Chk ");
            String ans;
            if(result.contains("spam")){
                ans="spam";
            }
            else
                ans="ham";
            Log.i("Result", String.valueOf(date.length()));
            Message mesg=new Message(id,sender,date,"1",msg,timestamp,ans);
            addnewmsgtodb(mesg);

        }


    }
*/
    public class sendOldMessage extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... strings) {
            for(String messages:map_for_asyntask.keySet()){
                for(Message message:map_for_asyntask.get(messages)){
                    HttpClient httpclient;
                    HttpResponse response = null;
                    String result = "";
                    try{
                        httpclient = new DefaultHttpClient();

                        HttpPost post = new HttpPost("https://spamshield.herokuapp.com/predict");

                        JSONObject json = new JSONObject();
                        json.put("messagejson", message.message);
                        StringEntity se;
                        se = new StringEntity(json.toString());
                        post.setEntity(se);

                        post.setHeader("Content-type", "application/json");
                        response = httpclient.execute(post);
                        Log.i("msgr","result");

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

                    if(result.contains("spam"))
                        message.spam="spam";
                    else
                        message.spam="ham";
                }
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i("All msgs send to","server");
            UpdateDatabaseMessages updateDatabaseMessages=new UpdateDatabaseMessages();
            updateDatabaseMessages.execute();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // MainActivity mainActivity=new MainActivity();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_items, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.blocked){

            Toast.makeText(getApplicationContext(), "yolo", Toast.LENGTH_SHORT).show();

        } else if (id==R.id.settings){
            Intent i = new Intent(this, MyPreferencesActivity.class);
            startActivity(i);


        }
//        else if (id==android.R.id.home){
//
//            finish();
//
//        }
        return super.onOptionsItemSelected(item);
    }




}