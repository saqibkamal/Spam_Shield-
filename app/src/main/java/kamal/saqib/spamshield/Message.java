package kamal.saqib.spamshield;



import java.io.Serializable;


/**
 * Created by Saqib kamal on 08-01-2018.
 */

public class Message implements Serializable {
    public String sender_address,date,time,type,message,sender_name;



    public Message(String a, String b, String c, String d, String e){
        this.sender_address=a;
        this.date=b.substring(0,10);
        this.time=b.substring(11);
        this.type=c;
        this.message=d;
        this.sender_name=e;



    }
}
