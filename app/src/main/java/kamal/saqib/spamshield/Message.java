package kamal.saqib.spamshield;



import android.view.Display;

import com.activeandroid.Model;

import java.io.Serializable;


/**
 * Created by Saqib kamal on 08-01-2018.
 */

public class Message implements Serializable {
    public String id,sender_address,date,time,type,message,timestamp,spam;

    public Message(){
        this.id=null;
        this.sender_address=null;
        this.date=null;
        this.time=null;
        this.type=null;
        this.message=null;
        this.timestamp=null;
        this.spam="ham";
    }

    public Message(String i,String a, String b, String c, String d,String e,String spam){
        this.id=i;
        this.sender_address=a;
        this.date=b.substring(0,10);
        this.time=b.substring(11);
        this.type=c;
        this.message=d;
        this.timestamp=e;
        this.spam=spam;




    }
}
