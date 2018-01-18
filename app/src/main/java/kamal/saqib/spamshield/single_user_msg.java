package kamal.saqib.spamshield;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.ahmadrosid.lib.MessageView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static java.lang.Long.valueOf;

public class single_user_msg extends AppCompatActivity implements Serializable,View.OnClickListener {
    ArrayList<String> smsMessagesList = new ArrayList<>();
    ListView messages;
    ArrayAdapter arrayAdapter;
    EditText msg_box;
    ImageView sendButton;
    ArrayList<Message> msgs;
    String phoneNo;
    SimpleDateFormat simpleDateFormat;
    final CharSequence options[] = new CharSequence[]{"Delete"};
    ProgressDialog progressDialog;
    private static single_user_msg inst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_user_msg);

        Intent in=getIntent();

        messages = (ListView) findViewById(R.id.list);

        msg_box=(EditText) findViewById(R.id.msg_box);
        sendButton=(ImageView) findViewById(R.id.send);
        simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
       // arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsMessagesList);
      //  messages.setAdapter(arrayAdapter);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
       msgs = (ArrayList<Message>) args.getSerializable("ARRAYLIST");
       phoneNo=(String) args.getSerializable("phonenumber");
       // for(int i=0;i<msgs.size();i++) {
//            arrayAdapter.add(msgs.get(i).message+msgs.get(i).spam+msgs.get(i).type);
            //phoneNo = msgs.get(i).sender_address;
        //}
        sendButton.setOnClickListener(this);
        set();

    }

    @Override
    public void onClick(View view) {
        if (view == sendButton) {
            String msg = msg_box.getText().toString();
            if (msg.length() > 0) {
                msg = msg.trim();
                sendSMS(msg);
            }
        }
    }

    private void sendSMS(final String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        final PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        final PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);



        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case RESULT_OK:


                        //arrayAdapter.insert(message,0);

                        msg_box.setText("");


                        Long tsLong = System.currentTimeMillis();
                        String ts = tsLong.toString();
                        String dateFromSms = simpleDateFormat.format(new Date(tsLong));
                        Random random =new Random();
                        int x=random.nextInt()+100000000;
                        Log.i("xyz",String.valueOf(x));
                        Message mess=new Message(String.valueOf(x),phoneNo,dateFromSms,"2",message,ts,"ham");
                        msgs.add(0,mess);
                        MainActivity inst = MainActivity.instance();
                        inst.addsendsmstodb(mess);

                        Toast.makeText(getApplicationContext(), "SMS Sent!",
                                Toast.LENGTH_LONG).show();


                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));


        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, sentPI, deliveredPI);


        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faileed, please try again later!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
            e.getCause();

        }


    }

    public class CustomAdapter extends BaseAdapter {
        ArrayList<String> result;
        Context context;
        int [] imageId;
        private LayoutInflater inflater=null;
        public CustomAdapter(single_user_msg mainActivity) {
            // TODO Auto-generated constructor stub

            context=mainActivity;


            inflater = ( LayoutInflater )context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return msgs.size();
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
            MessageView tv;

        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            kamal.saqib.spamshield.single_user_msg.CustomAdapter.Holder holder= new kamal.saqib.spamshield.single_user_msg.CustomAdapter.Holder();
            View rowView;
            rowView = inflater.inflate(R.layout.single_msg_lsview, null);
            holder.tv=(MessageView)rowView.findViewById(R.id.message_view);

            if(msgs.get(position).type.equals("1")){
                holder.tv.setLeft();
                holder.tv.setTitleMessages(msgs.get(position).date);
                holder.tv.setDecsMessages(msgs.get(position).message);
            }
            else{
                holder.tv.setRight();
                holder.tv.setTitleMessages(msgs.get(position).date);
                holder.tv.setDecsMessages(msgs.get(position).message);
            }
            //holder.img.setImageResource(imageId[position]);

            holder.tv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(single_user_msg.this);
                    builder.setTitle("Choose an option");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(i==0){
                                new android.support.v7.app.AlertDialog.Builder(single_user_msg.this).setTitle("Delete").
                                        setMessage("Are You Sure You Want to delete the image").
                                        setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Log.i("YES","delete function reached");
                                                progressDialog = new ProgressDialog(single_user_msg.this);
                                                progressDialog.setTitle("Deleting...");
                                                progressDialog.show();
                                                progressDialog.setCanceledOnTouchOutside(false);
                                                Message mg=msgs.get(position);
                                                msgs.remove(msgs.get(position));
                                                Deletemessage deletemessage=new Deletemessage();

                                                deletemessage.execute(mg.message,mg.sender_address,mg.timestamp);

                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Log.i("NOT","DELETED");
                                            }
                                        }).show();
                            }
                        }
                    });
                    builder.show();


                   // deleteSMS(msgs.get(position).message,phoneNo,msgs.get(position).timestamp);
                    return true;
                }
            });



            return rowView;
        }

    }
    public void set(){
        messages.setAdapter(new single_user_msg.CustomAdapter(this));
    }



    public class Deletemessage extends AsyncTask<String,Void,Void> {
        int flag1=0;

        @Override
        protected Void doInBackground(String... strings) {
            String message=strings[0];
            String number=strings[1];
            String timestamp=strings[2];
            try {
                Context context=getApplicationContext();

                Uri uriSms = Uri.parse("content://sms");
                Cursor c = context.getContentResolver().query(uriSms,
                        new String[] { "_id", "thread_id", "address",
                                "person", "date", "body" }, null, null, null);

                if (c != null && c.moveToFirst()) {
                    do {
                        long id = c.getLong(0);
                        long threadId = c.getLong(1);
                        String address = c.getString(2);
                        String body = c.getString(5);
                        String date=c.getString(4);

                        if (message.equals(body) && address.equals(number) && date.equals(timestamp)) {
                            //mLogger.logInfo("Deleting SMS with id: " + threadId);
                            context.getContentResolver().delete(
                                    Uri.parse("content://sms/" + id), null, null);
                            Log.i("DELETED ","FROM PHONE");
                            break;
                        }
                    } while (c.moveToNext());
                }


            } catch (Exception e) {
                // mLogger.logError("Could not delete SMS from inbox: " + e.getMessage());
            }



            new Delete().from(msg_sqldb.class).where("address = ?",number)
                    .where("timestamp = ?",timestamp).where("message = ?",message).execute();


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity inst = MainActivity.instance();
                    inst.readfromdatabase();
                }
            });



            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i("Completeted","Database Updation");
            progressDialog.dismiss();
            set();

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    public static single_user_msg instance() {
        return inst;
    }


}