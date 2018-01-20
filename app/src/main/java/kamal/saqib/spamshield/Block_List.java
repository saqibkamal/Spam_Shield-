package kamal.saqib.spamshield;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

public class Block_List extends AppCompatActivity {

    ArrayList<String> number;
    ListView ls;
    final CharSequence options[] = new CharSequence[]{"Remove From Block List"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block__list);

        number=new ArrayList<>();
        ls=findViewById(R.id.listview);


        showActionBar();


        List<BlockListsql> blockListsqls=new Select("*").from(BlockListsql.class).execute();

        for(BlockListsql x:blockListsqls){
            number.add(x.number);
        }

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,number);
        ls.setAdapter(adapter);

        ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int pi, long l) {
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(Block_List.this);
                builder.setTitle("Choose an option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0)
                        {
                            new android.support.v7.app.AlertDialog.Builder(Block_List.this).setTitle("Delete").
                                    setMessage("Are You Sure You Want to Remove this number from blocklist").
                                    setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Log.i("YES","delete function reached");
                                            new Delete().from(BlockListsql.class).where("number = ?",number.get(pi))
                                                    .execute();

                                            number.remove(pi);
                                            ArrayAdapter<String> adapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,number);
                                            ls.setAdapter(adapter);

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
            }
        });


    }

    private void showActionBar(){
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar_analytics,null);
        final ActionBar bar = getSupportActionBar();

        //to set name of the action at the middle
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        TextView textviewTitle = (TextView) v.findViewById(R.id.tv_title);
        textviewTitle.setText("Blocked List");
        ///////
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(false);
        bar.setDisplayShowCustomEnabled(true);
        bar.setDisplayShowTitleEnabled(false);
        bar.setCustomView(v, params);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
