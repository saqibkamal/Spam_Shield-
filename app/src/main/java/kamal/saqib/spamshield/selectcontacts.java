package kamal.saqib.spamshield;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class selectcontacts extends AppCompatActivity {

    HashMap<String,ArrayList<Message>> all_msgs;
    HashMap<String,String> contacts;
    List<Pair> allnames;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectcontacts);
        lv=findViewById(R.id.listview);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        //all_msgs = (HashMap<String, ArrayList<Message>>) args.getSerializable("allmsgs");
        contacts=(HashMap<String,String>) args.getSerializable("allcntcts");



        allnames=new ArrayList<>();

        for (Map.Entry<String, String> entry : contacts.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            value = Character.toUpperCase(value.charAt(0)) + value.substring(1);
            allnames.add(new Pair(value,key));
            // ...
        }
        Collections.sort(allnames);

        lv.setAdapter(new CustomAdapter(this));



    }

    public class CustomAdapter extends BaseAdapter {
        Context context;
        private LayoutInflater inflater = null;

        public CustomAdapter(selectcontacts mainActivity) {
            // TODO Auto-generated constructor stub
            context = mainActivity;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return contacts.size();
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
            TextView name,number;
            ImageView img;

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            kamal.saqib.spamshield.selectcontacts.CustomAdapter.Holder holder = new kamal.saqib.spamshield.selectcontacts.CustomAdapter.Holder();
            View rowView;
            rowView = inflater.inflate(R.layout.contacts_list_view, null);

            holder.name = rowView.findViewById(R.id.name);
            holder.number = rowView.findViewById(R.id.phone_number);
            holder.img = rowView.findViewById(R.id.imageview);

            holder.name.setText(allnames.get(position).l);
            holder.number.setText(allnames.get(position).e);




            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent in = new Intent(getBaseContext(), single_user_msg.class);
                    Bundle args = new Bundle();
                    args.putSerializable("ARRAYLIST", new ArrayList<Message>());
                    args.putSerializable("phonenumber",allnames.get(position).e);
                    in.putExtra("BUNDLE", args);
                    startActivity(in);

                }
            });


            return rowView;
        }
    }

    private static class Pair implements Comparable<Pair>
    {
        private String l;
        private String e;

        public Pair(String key, String value) {
            this.l=key;
            this.e=value;
        }

        public int compareTo(Pair that) {
            return this.l.compareTo(that.l);
        }
    }
}
