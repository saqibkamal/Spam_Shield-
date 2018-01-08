package kamal.saqib.spamshield;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.Serializable;
import java.util.ArrayList;

public class single_user_msg extends AppCompatActivity implements Serializable {
    ArrayList<String> smsMessagesList = new ArrayList<>();
    ListView messages;
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_user_msg);

        Intent in=getIntent();

        messages = (ListView) findViewById(R.id.list);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        messages.setAdapter(arrayAdapter);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        ArrayList<Message> msgs = (ArrayList<Message>) args.getSerializable("ARRAYLIST");
        for(int i=0;i<msgs.size();i++)
        arrayAdapter.add(msgs.get(i).message);
    }
}
