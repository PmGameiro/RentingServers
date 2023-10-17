package aluguerservidores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;

public class EmailList {

    private ArrayList<String> list;

    public EmailList() {
        this.list = new ArrayList<>();
    }

    public synchronized boolean containsEmail(String email) {
        return this.list.contains(email);
    }
    

    public synchronized boolean addEmail(String email) {
        return this.list.add(email);
    }
    
    public synchronized void removeEmail(String email) {
         this.list.remove(email);
    }
}
