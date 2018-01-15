package kamal.saqib.spamshield;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class single_user_msg extends AppCompatActivity implements Serializable,View.OnClickListener {
    ArrayList<String> smsMessagesList = new ArrayList<>();
    ListView messages;
    ArrayAdapter arrayAdapter;
    EditText msg_box;
    ImageView sendButton;
    String phoneNo;
    SimpleDateFormat simpleDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_user_msg);

        Intent in=getIntent();

        messages = (ListView) findViewById(R.id.list);
        msg_box=(EditText) findViewById(R.id.msg_box);
        sendButton=(ImageView) findViewById(R.id.send);
        simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        messages.setAdapter(arrayAdapter);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        ArrayList<Message> msgs = (ArrayList<Message>) args.getSerializable("ARRAYLIST");
        for(int i=0;i<msgs.size();i++) {
            arrayAdapter.add(msgs.get(i).message+msgs.get(i).spam+msgs.get(i).type);
            phoneNo = msgs.get(i).sender_address;
        }
        sendButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == sendButton) {
            String msg = msg_box.getText().toString();
            if (msg.length() > 0)
                sendSMS(msg);
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
                            Toast.makeText(getBaseContext(), "SMS sent",
                                    Toast.LENGTH_SHORT).show();

                            arrayAdapter.insert(message,0);
                            msg_box.setText("");


                            Long tsLong = System.currentTimeMillis();
                            String ts = tsLong.toString();
                            String dateFromSms = simpleDateFormat.format(new Date(tsLong));
                            Random random =new Random();
                            int x=random.nextInt()+100000000;
                            Log.i("xyz",String.valueOf(x));
                            Message mess=new Message(String.valueOf(x),phoneNo,dateFromSms,"2",message,ts,"ham");
                            MainActivity inst = MainActivity.instance();
                            inst.addsendsmstodb(mess);

                            Toast.makeText(getApplicationContext(), "SMS lundo!",
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
}
