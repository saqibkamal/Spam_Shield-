package kamal.saqib.spamshield;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Saqib kamal on 15-01-2018.
 */
@Table(name = "contacts")
public class cntcts_sqldb extends Model {

    @Column(name = "name")
    public String name;

    @Column(name = "number")
    public String number;

    public cntcts_sqldb(){
        super();
    }
    public cntcts_sqldb(String name, String number) {
        super();
        this.name = name;
        this.number = number;
    }
}
