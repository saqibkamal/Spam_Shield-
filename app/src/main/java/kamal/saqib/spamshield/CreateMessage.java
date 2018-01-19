package kamal.saqib.spamshield;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;

public class CreateMessage extends AppCompatActivity {

    EditText edtxtCreateMsg;
    Button send;
    ImageView addContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_message);

        showActionBar();

        edtxtCreateMsg=findViewById(R.id.edtxt_msg_bar);
        addContact=findViewById(R.id.ic_add_contact);
        send=findViewById(R.id.bt_send);

        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),selectcontacts.class));
                finish();

            }
        });


    }

    private void showActionBar(){
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar_createmsg_screen,null);
        final ActionBar bar = getSupportActionBar();
        //bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(false);
        bar.setDisplayShowCustomEnabled(true);
        bar.setDisplayShowTitleEnabled(false);
        bar.setCustomView(v);


        final EditText editText=findViewById(R.id.edtxt_search_bar);

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                if (i == KeyEvent.KEYCODE_ENTER) {

                    String num=editText.getText().toString();
                    Intent in = new Intent(getBaseContext(), single_user_msg.class);
                    Bundle args = new Bundle();
                    args.putSerializable("ARRAYLIST", new ArrayList<Message>());
                    args.putSerializable("phonenumber",num);
                    args.putSerializable("name",null);
                    in.putExtra("BUNDLE", args);
                    startActivity(in);
                    finish();

                    return true;
                }
                return false;
            }
        });
    }
    ////////
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
