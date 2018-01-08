package kamal.saqib.spamshield;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static android.content.ContentValues.TAG;


public class send_msg extends Activity implements OnClickListener {

    Button send,getcontact;
    EditText message,number;
    String msg,num;
    static final String TAG = send_msg.class.getSimpleName();
    static final int REQUEST_CODE_PICK_CONTACTS = 1;
    Uri uriContact;
    String contactID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_msg);

        send=(Button) findViewById(R.id.send);
        message=(EditText) findViewById(R.id.message);
        number=(EditText) findViewById(R.id.phone_number);
        getcontact=(Button) findViewById(R.id.getcontact);

        send.setOnClickListener(this);
        getcontact.setOnClickListener(this);







    }

    @Override
    public void onClick(View view) {
        if (view == send) {
            num = number.getText().toString();
             msg = message.getText().toString();
             sendMessage();
        }
        else if(view==getcontact){

            startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            Log.d(TAG, "Response: " + data.toString());
            uriContact = data.getData();

            retrieveContactName();
            retrieveContactNumber();


        }
    }

    public void retrieveContactNumber() {

        String contactNumber = null;

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        Log.d(TAG, "Contact ID: " + contactID);

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_HOME +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE +
                        ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK +
                        ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME +
                        ContactsContract.CommonDataKinds.Phone.TYPE_PAGER +
                        ContactsContract.CommonDataKinds.Phone.TYPE_OTHER +
                        ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK +
                        ContactsContract.CommonDataKinds.Phone.TYPE_CAR +
                        ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN +
                        ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX +
                        ContactsContract.CommonDataKinds.Phone.TYPE_RADIO +
                        ContactsContract.CommonDataKinds.Phone.TYPE_TELEX +
                        ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD +
                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE +
                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER +
                        ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MMS,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();

        Log.d(TAG, "Contact Phone Number: " + contactNumber);
        number.setText(contactNumber);
    }

    public void retrieveContactName() {

        String contactName = null;

        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {

            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.

            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();

        Log.d(TAG, "Contact Name: " + contactName);

    }

    public void sendMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                com.klinker.android.send_message.Settings sendSettings = new com.klinker.android.send_message.Settings();
                sendSettings.setUseSystemSending(true);
                com.klinker.android.send_message.Transaction transaction = new com.klinker.android.send_message.Transaction(send_msg.this, sendSettings);
                com.klinker.android.send_message.Message message = new com.klinker.android.send_message.Message(msg, num);
                transaction.sendNewMessage(message, com.klinker.android.send_message.Transaction.NO_THREAD_ID);
            }
        }).start();
    }











}
