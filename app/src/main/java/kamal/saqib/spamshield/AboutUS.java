package kamal.saqib.spamshield;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutUS extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar_analytics,null);
        final ActionBar bar = getSupportActionBar();
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        TextView textviewTitle = (TextView) v.findViewById(R.id.tv_title);
        textviewTitle.setText("About us");

        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(false);
        bar.setDisplayShowCustomEnabled(true);
        bar.setDisplayShowTitleEnabled(false);
        bar.setCustomView(v,params);

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.main_logo_s)
                .setDescription("Spam Shield++ : As the name suggests, it is utilitarian app designed with the main motive " +
                        "to block any kind of futile spams that crowd your inbox on a day to day basis."+"With its intuitive ML algorithm " +
                        "it is able to shield any spam sms and learn from its mistakes to increase filter accuracy." +
                        "Developed by a team of 4 developer friends with a love for code, it is based upon a great structure with a GUI to " +
                        "to provide not only a great User Interface but also a greater User Experience. Cheers!!")
                .addGroup("Developers")
                .addItem(new Element().setTitle("UI/UX Developer   : Aayush Shrestha (saayush97@gmail.com)"))
                .addItem(new Element().setTitle("Android Developer : Saqib Kamal (saqib.kamal01@gmail.com)"))
                .addItem(new Element().setTitle("Algorithm Designer : Vedant Nepal (vedantlfc@gmail.com)"))
                .addItem(new Element().setTitle("Backend Developer : Sandeep Bhandari (sandeep135@gmail.com)"))


                .addItem(new Element().setTitle("Version 1.0"))
                .addGroup("Connect with us")
                .addEmail("spamshield2k18@gmail.com")
                .addWebsite("http://github.com/saqibkamal")
                .addGitHub("github.com/saqibkamal")
                .addItem(getCopyRightsElement())
                .create();

        setContentView(aboutPage);
    
    }

    Element getCopyRightsElement() {
        Element copyRightsElement = new Element();
        final String copyrights = String.format(getString(R.string.copy_right), Calendar.getInstance().get(Calendar.YEAR));
        copyRightsElement.setTitle(copyrights);
        copyRightsElement.setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color);
        copyRightsElement.setIconNightTint(android.R.color.white);
        copyRightsElement.setGravity(Gravity.CENTER);
        copyRightsElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), copyrights, Toast.LENGTH_SHORT).show();
            }
        });
        return copyRightsElement;
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
