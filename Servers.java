package aluguerservidores;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

/**
 * A class Servrs Representa os servidores que vamos alugar
 */
public class Servers extends Thread {

    String id;
    private String type;
    private float nominalPrice;
    private float actualPrice;
    private float minutes;
    private boolean occupied;
    private boolean boughtInAuction;
    private boolean firstTime;
    private String userEmail;

    public Servers(String type, float n_price, float i_price, String user) throws NoSuchAlgorithmException {
        this.id = this.createId();
        this.type = type;
        this.nominalPrice = n_price;
        this.actualPrice = i_price;
        this.minutes = 0;
        this.userEmail = user;
        this.occupied = true;
        this.boughtInAuction = false;
        this.firstTime = true;
    }

    public Servers(String type, float n_price) throws NoSuchAlgorithmException {
        this.id = this.createId();
        this.type = type;
        this.nominalPrice = n_price;
        this.actualPrice = 0;
        this.minutes = 0;
        this.occupied = false;
        this.userEmail = "";
        this.boughtInAuction = false;
        this.firstTime = true;
    }

    public synchronized void setMinutes(float m) {
        this.minutes = m;
    }

    public synchronized float getMin() {
        return this.minutes;
    }

    public synchronized float getCurrentTotal() {
        if (boughtInAuction) {
            return this.minutes * this.actualPrice;
        } else {
            return this.minutes * this.nominalPrice;
        }
    }

    public synchronized void setUserEmail(String x) {
        this.userEmail = x;
    }

    public synchronized String getUserEmail() {
        return this.userEmail;
    }

    public synchronized void incMinutes() {
        this.minutes++;
    }

    public float getMinutes() {
        return minutes;
    }

    public void setBoughtInAuction(boolean boughtInAuction) {
        this.boughtInAuction = boughtInAuction;
    }

    public boolean wasBoughtInAuction() {
        return boughtInAuction;
    }

    public synchronized void setType(String type) {
        this.type = type;
    }

    public synchronized String getType() {
        return this.type;
    }

    public synchronized String getIdServers() {
        return this.id;
    }

    public synchronized String createId() throws NoSuchAlgorithmException {
        Random r = new Random();
        String pk = "SLKNGLRKJBHTGLJRTHEBLE" + Integer.toString(r.nextInt(50000));
        String myHash = Base64.getEncoder().encodeToString(pk.getBytes());
        return myHash;
    }

    public synchronized float getNominalPrice() {
        return this.nominalPrice;
    }

    public synchronized void setNominalPrice(float x) {
        this.nominalPrice = x;
    }

    public synchronized float getActualPrice() {
        return this.actualPrice;
    }

    public synchronized void setActualPrice(float x) {
        this.actualPrice = x;
    }

    public synchronized void setOccupied(boolean x) {
        this.occupied = x;
    }

    public synchronized boolean isOccupied() {
        return this.occupied;
    }

    public synchronized Servers clone() {
        return this.clone();
    }

    public synchronized void reset() {
        this.nominalPrice = 0;
        this.actualPrice = 0;
        this.minutes = 0;
        this.occupied = false;
        this.userEmail = "";
        this.boughtInAuction = false;
    }

    public synchronized void standBy() {
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void startServer() {
        if (firstTime) {
            this.start();
            firstTime = false;
        } else {
            this.notify();
        }
    }

    public void run() {
        while (true) {
            while (isOccupied()) {
                try {
                    sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isOccupied()) {
                    incMinutes();
                }
            }
            standBy();
        }
    }
}
