package kamal.saqib.spamshield;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Spam_msgs extends AppCompatActivity {

    HashMap<String,ArrayList<Message>> smap_for_db;
    ArrayList<String> smsgsndrs;
    HashMap<String,Message> s_fmsg;
    HashMap<String,String> contacts_for_db;

    ListView ls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spam_msgs);

        ls=findViewById(R.id.list);

        readfromdatabase();
        readcontactsfromdatabase();
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

        smap_for_db=new HashMap<>();
        smsgsndrs=new ArrayList<>();
        s_fmsg=new HashMap<>();

        ActiveAndroid.beginTransaction();
        try {

            List<msg_sqldb> msg_from_db = new Select("*").from(msg_sqldb.class)
                    .orderBy("timestamp DESC ").execute();

            for (msg_sqldb msgSqldb : msg_from_db) {
                String id = msgSqldb.msg_id;
                String add = msgSqldb.address;
                String dt = msgSqldb.date;
                String tm = msgSqldb.time;
                String tp = msgSqldb.type;
                String mm = msgSqldb.message;
                Long tmstmp = msgSqldb.timestamp;
                String sp = msgSqldb.spam;

                String msg_sender = add;
                if (sp.equals("spam")) {
                    Log.i("spam", msgSqldb.message);
                    if (smsgsndrs.contains(msg_sender)) {
                        smap_for_db.get(msg_sender).add(new Message(id,add,dt+" "+tm,tp,mm,String.valueOf(tmstmp),sp));
                    } else {
                        smsgsndrs.add(msg_sender);
                        ArrayList<Message> temp = new ArrayList<>();
                        temp.add(new Message(id, add, dt + " " + tm, tp, mm, String.valueOf(tmstmp), sp));
                        smap_for_db.put(msg_sender, temp);
                        s_fmsg.put(add, new Message(id, add, dt + " " + tm, tp, mm, String.valueOf(tmstmp), sp));
                    }
                }
            }


            ls.setAdapter(new Spam_msgs.CustomAdapter(this));
            ActiveAndroid.setTransactionSuccessful();

        }
        finally {
            ActiveAndroid.endTransaction();
        }
    }

    public class CustomAdapter extends BaseAdapter {
        ArrayList<String> result;
        Context context;
        private LayoutInflater inflater = null;

        public CustomAdapter(Spam_msgs mainActivity) {
            // TODO Auto-generated constructor stub
            result = smsgsndrs;
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

            kamal.saqib.spamshield.Spam_msgs.CustomAdapter.Holder holder = new kamal.saqib.spamshield.Spam_msgs.CustomAdapter.Holder();
            View rowView;
            rowView = inflater.inflate(R.layout.front_page_listview, null);

            holder.tv = rowView.findViewById(R.id.phone_number);
            holder.fmsg = rowView.findViewById(R.id.fmsgs);
            holder.img = rowView.findViewById(R.id.imageview);

            holder.tv.setText(result.get(position));

            holder.fmsg.setText(s_fmsg.get(result.get(position)).message);

            String ph_no = result.get(position);
            if (contacts_for_db.containsKey(ph_no))
                holder.tv.setText(contacts_for_db.get(ph_no));




            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String p = smsgsndrs.get(position);
                    Intent in = new Intent(getBaseContext(), single_user_msg.class);
                    Bundle args = new Bundle();
                    args.putSerializable("ARRAYLIST", smap_for_db.get(p));
                    args.putSerializable("phonenumber",smap_for_db.get(p).get(0).sender_address);
                    in.putExtra("BUNDLE", args);
                    startActivity(in);

                }
            });


            return rowView;
        }
    }

}
