package transaction;

import java.util.Date;

public class Transaction {

    private String description;
    private Date timestamp;

    public Transaction(String description) {
        this.description = description;
        this.timestamp = new Date();
    }

    @Override
    public String toString() {
        return timestamp + " : " + description;
    }
}
