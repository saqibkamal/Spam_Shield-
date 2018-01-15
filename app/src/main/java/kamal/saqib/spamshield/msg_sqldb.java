package kamal.saqib.spamshield;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;


/**
 * Created by Saqib kamal on 10-01-2018.
 */

@Table(name = "msgs")
public class msg_sqldb extends Model {
    @Column(name = "msg_id")
    public String msg_id;

    @Column(name = "address")
    public String address;

    @Column(name = "date")
    public String date;

    @Column(name = "time")
    public String time;

    @Column(name = "type")
    public String type;

    @Column(name = "message")
    public String message;

    @Column(name = "timestamp")
    public Long timestamp;

    @Column(name = "spam")
    public String spam;

    public msg_sqldb() {
        super();
    }

    public msg_sqldb(String id, String address,String date,String time,String type,String message,String timestamp,String spam) {
        super();
        this.msg_id = id;
        this.address = address;
        this.date=date;
        this.time=time;
        this.type=type;
        this.message=message;
        this.timestamp=Long.decode(timestamp);
        this.spam=spam;
    }
}
