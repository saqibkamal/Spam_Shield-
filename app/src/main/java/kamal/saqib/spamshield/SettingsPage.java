package kamal.saqib.spamshield;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsPage extends AppCompatActivity {

    Switch notification,message_preview,sound;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);

        notification=(Switch) findViewById(R.id.one);
        message_preview=findViewById(R.id.two);
        sound =findViewById(R.id.three);


        showActionBar();

        sharedpreferences = getSharedPreferences("Mydata", Context.MODE_PRIVATE);
        editor=sharedpreferences.edit();

        String ntf=sharedpreferences.getString("notification",null);
        String msgprv=sharedpreferences.getString("messagepreview",null);
        final String sond=sharedpreferences.getString("sound",null);

        if(ntf==null || ntf.equals("on")){
            notification.setChecked(true);
        }
        else
            notification.setChecked(false);

        if(msgprv==null || msgprv.equals("on")){
            message_preview.setChecked(true);
        }
        else
            message_preview.setChecked(false);

        if(sond==null || sond.equals("on")){
            sound.setChecked(true);
        }
        else
            sound.setChecked(false);

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(notification.isChecked()){
                    editor.remove("notification");
                    editor.commit();
                    editor.putString("notification","on");
                    editor.commit();
                }
                else{
                    editor.remove("notification");
                    editor.commit();
                    editor.putString("notification","off");
                    editor.commit();
                }
            }
        });

        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sound.isChecked()){
                    editor.remove("sound");
                    editor.commit();
                    editor.putString("sound","on");
                    editor.commit();
                }
                else{
                    editor.remove("sound");
                    editor.commit();
                    editor.putString("sound","off");
                    editor.commit();
                }
            }
        });

        message_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(message_preview.isChecked()){
                    editor.remove("messagepreview");
                    editor.commit();
                    editor.putString("messagepreview","on");
                    editor.commit();
                }
                else{
                    editor.remove("messagepreview");
                    editor.commit();
                    editor.putString("messagepreview","off");
                    editor.commit();
                }
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
        textviewTitle.setText("Settings");
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
