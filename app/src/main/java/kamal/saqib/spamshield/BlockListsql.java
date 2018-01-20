package kamal.saqib.spamshield;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Saqib kamal on 20-01-2018.
 */

@Table(name = "blocklist")
public class BlockListsql extends Model {

    @Column(name = "number")
    public String number;


    public BlockListsql(){
        super();
    }

    public BlockListsql(String number){
        this.number=number;
    }
}
