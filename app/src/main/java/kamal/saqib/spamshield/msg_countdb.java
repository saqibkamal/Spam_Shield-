package kamal.saqib.spamshield;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Saqib kamal on 15-01-2018.
 */
@Table(name = "message")
public class msg_countdb extends Model {

    @Column(name = "date")
    public String date;

    @Column(name = "totalmsg")
    public int totalmsg;

    @Column(name = "spammsg")
    public int spammsg;

    public msg_countdb(){
        super();
    }

    public msg_countdb(String date, int totalmsg,int spammsg) {
        super();
        this.date = date;
        this.totalmsg=totalmsg;
        this.spammsg=spammsg;
    }



}
