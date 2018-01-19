package kamal.saqib.spamshield;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Analytics extends AppCompatActivity {

    private LineChart mChart;
    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    ArrayList<Integer> msgcount;
    int totalmsg,spammsg;
    float accuracy;

    String mnth[]={"","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sept","Oct","Nov","Dec"};

    int currentmnth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analytics_activity);

        showActionBar();

        mChart=findViewById(R.id.line_chart);
        tv1=findViewById(R.id.tv_total_msg_count);
        tv2= findViewById(R.id.tv_spam_msg_count);
        tv3= findViewById(R.id.accuracy);



        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(false);

        msgcount=new ArrayList<>(31);
       // Log.i("Array size",String.valueOf(msgcount.size()));
        for(int i=0;i<31;i++)
            msgcount.add(0);

        totalmsg=0;
        spammsg=0;

        List<msg_countdb> msgCountdbs = new Select("*").from(msg_countdb.class).execute();
        Log.i("Read","from db1");

        for(msg_countdb mg:msgCountdbs){
            String dt=mg.date;
            int p=mg.spammsg;
            int q=mg.totalmsg;

            totalmsg+=q;
            spammsg+=p;

            Log.i("spammsg",Integer.toString(p));
            Log.i("spammsg",Integer.toString(q));

            String day=dt.substring(0,2);
            String mnth=dt.substring(3,5);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

            Long t=System.currentTimeMillis();
            String date=simpleDateFormat.format(new Date(t));

             String currmnth=date.substring(3,5);

             currentmnth=((currmnth.charAt(0)-'0')*10)+(currmnth.charAt(1)-'0');


            int dy=((day.charAt(0)-'0')*10)+(day.charAt(1)-'0');


            if(mnth.equals(currmnth)){
                msgcount.set(dy,p);
            }

        }



        /*mChart.getAxisRight().setEnabled(false);
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);*/

        ArrayList<Entry> yValues=new ArrayList<>(31);

        for(int i=0;i<31;i++){
            yValues.add(new Entry(i,msgcount.get(i).floatValue()));
        }

        accuracy=(spammsg*100)/totalmsg;
        Log.i("percentage",String.valueOf(accuracy));

        Log.i("Percentage",Integer.toString(Math.round(accuracy)));

      /*  yValues.add(new Entry(0,10f));
        yValues.add(new Entry(1,60f));
        yValues.add(new Entry(2,30f));
        yValues.add(new Entry(3,50f));
        yValues.add(new Entry(4,100f));
        yValues.add(new Entry(5,20f));
        yValues.add(new Entry(6,30f));
        yValues.add(new Entry(7,10f));
        yValues.add(new Entry(8,60f));
        yValues.add(new Entry(9,30f));
        yValues.add(new Entry(10,50f));
        yValues.add(new Entry(11,100f));
        yValues.add(new Entry(12,10f));
        yValues.add(new Entry(13,60f));
        yValues.add(new Entry(14,30f));
        yValues.add(new Entry(15,50f));
        yValues.add(new Entry(16,100f));
        yValues.add(new Entry(17,20f));
        yValues.add(new Entry(18,30f));
        yValues.add(new Entry(19,10f));
        yValues.add(new Entry(20,60f));
        yValues.add(new Entry(21,30f));
        yValues.add(new Entry(22,50f));
        yValues.add(new Entry(23,100f));
        yValues.add(new Entry(24,100f));
        yValues.add(new Entry(25,20f));
        yValues.add(new Entry(26,30f));
        yValues.add(new Entry(27,10f));
        yValues.add(new Entry(28,60f));
        yValues.add(new Entry(29,30f));
        yValues.add(new Entry(30,50f));*/

        LineDataSet set1 = new LineDataSet(yValues,  mnth[currentmnth]+" Analytics");

        set1.setFillAlpha(110);
        set1.setLineWidth(2f);
        set1.setValueTextSize(10f);
        set1.setValueTextColor(Color.GRAY);
        ArrayList<ILineDataSet> dataSets=new ArrayList<>();
        dataSets.add(set1);
        LineData data = new LineData(dataSets);
        mChart.setData(data);
        mChart.setVisibleXRangeMaximum(6);
        mChart.moveViewToX(3);


        showTextAnimation();
        showCircularProgressAnimation();
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
        textviewTitle.setText("Spam Analytics");
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

    private void showTextAnimation() {
        ValueAnimator animator1 = ValueAnimator.ofInt(0, totalmsg);
        ValueAnimator animator2 = ValueAnimator.ofInt(0, spammsg);
        animator1.setDuration(1500);
        animator2.setDuration(1500);
        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                tv1.setText(animation.getAnimatedValue().toString());
            }
        });

        animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                tv2.setText(animation.getAnimatedValue().toString());
            }
        });
        animator1.start();
        animator2.start();
    }

    private void showCircularProgressAnimation(){

        int x=Math.round(accuracy);
        Log.i("value",Integer.toString(x));
        CircularProgressBar circularProgressBar = (CircularProgressBar)findViewById(R.id.filter_circle);
        circularProgressBar.setColor(ContextCompat.getColor(this, R.color.progressbarfg));
        circularProgressBar.setBackgroundColor(ContextCompat.getColor(this, R.color.progressbarbg));
        circularProgressBar.setProgressBarWidth(25);
        circularProgressBar.setBackgroundProgressBarWidth(5);
        int animationDuration = 1500;
        circularProgressBar.setProgressWithAnimation(x, animationDuration);

        ValueAnimator animator3 = ValueAnimator.ofInt(0,x);
        animator3.setDuration(1000);
        animator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                tv3.setText(String.valueOf(accuracy)+"%");
            }
        });
        animator3.start();
    }
}
