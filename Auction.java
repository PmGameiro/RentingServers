package aluguerservidores;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author quim
 */
public class Auction extends Thread {

    private WriterMap writers;
    private ArrayList<String> participants;
    private Catalogue catalogue;
    private String type;
    private float highestBid;
    private final float serverPrice;
    private String currentHighestBidder;
    //1 - acabou o tempo; 2 - preço >= do que o nominal; 3 - falta de servidores
    private int finished;
    AuctionManager manager;

    public Auction(Catalogue c, String type, AuctionManager manager, WriterMap writers) {
        this.writers = writers;
        this.participants = new ArrayList<>();
        this.catalogue = c;
        this.serverPrice = this.catalogue.getNominalPrice(type);
        this.finished = 0;
        this.type = type;
        this.highestBid = 0;
        this.manager = manager;
        this.currentHighestBidder = "";
    }

    public synchronized void addParticipant(String s) {
        this.participants.add(s);
    }

    public synchronized void removeParticipant(String s) {
        this.participants.remove(s);
    }

    public synchronized void sendMessage(String s, String user){
        this.writers.writeMessage(user, s);
    }

    public synchronized void sendGeneralMessage(String s) {
        for (String user : participants) {
            sendMessage(s, user);
        }
    }

    public synchronized void sendWinnerMessage(){
        for(String user : participants){
            if(user.equals(currentHighestBidder))
                sendMessage("Ganhou o leilão!", user);
            else
                sendMessage("Acabou o tempo. O vencedor é " + currentHighestBidder + "\n", user);
        }
    }

    public synchronized void sendBidMessage() {
        sendGeneralMessage("Oferta mais alta: " + this.highestBid + "\nMaior licitador: " + getCurrentHighestBidder() + "\nFaça uma proposta: ");
    }

    public synchronized float getHighestBid(){
        return highestBid;
    }

    public String getCurrentHighestBidder() {
        return currentHighestBidder;
    }

    public synchronized void terminate(int cause) {
        this.finished = cause;
        notify();
    }

    public synchronized boolean isFinished(){
        return finished > 0;
    }

    public synchronized void endAuction() {
        Servers server;
        switch (this.finished) {
            case 1:
                server = catalogue.getAvailableServer(type);
                if (server != null && !currentHighestBidder.equals("")) {
                    server.setOccupied(true);
                    server.setUserEmail(currentHighestBidder);
                    server.setBoughtInAuction(true);
                    server.setActualPrice(highestBid);
                    server.startServer();
                    this.sendWinnerMessage();
                } else {
                    this.sendGeneralMessage("Deu-se o caso improvável de o último servidor disponível este tipo ter sido requisitado no último minuto...\n");
                }
                break;
            case 2:
                server = catalogue.getAvailableServer(type);
                if (server != null) {
                    server.setOccupied(true);
                    server.setUserEmail(currentHighestBidder);
                    server.setBoughtInAuction(true);
                    server.setActualPrice(serverPrice);
                    server.startServer();
                    this.sendWinnerMessage();
                } else {
                    this.sendGeneralMessage("Lamentamos, mas o leilão foi cancelado dado que todos os servidores disponíveis deste tipo foram requisitados.\n");
                }
                break;
            case 3:
                this.sendGeneralMessage("Lamentamos, mas o leilão foi cancelado dado que todos os servidores disponíveis deste tipo foram requisitados.\n");
                break;
            default:
                break;
        }
        this.manager.removeAuction(type);
        try{
            this.join();
        } catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    public synchronized void bid(String user, float price) {
        if (price >= this.serverPrice) {
            this.currentHighestBidder = user;
            this.terminate(2);
        }
        if (price > this.highestBid) {
            this.highestBid = price;
            this.currentHighestBidder = user;
            this.notify();
        }
    }

    public synchronized void standBy() throws InterruptedException {
        wait();
    }

    public void run() {
        while (!isFinished()) {
            try {
                sendBidMessage();
                standBy();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        endAuction();
    }
}