package aluguerservidores;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MyQueue extends Thread {

    private HashMap<String, Queue<String>> mapQueue;
    private WriterMap writers;
    Catalogue catalogue;

    public MyQueue(Catalogue c, WriterMap w) {
        this.mapQueue = new HashMap<>();
        this.writers = w;
        this.catalogue = c;
        List<String> l = catalogue.getTypes();
        for (String tipo : l) {
            Queue<String> q = new LinkedList<>();
            mapQueue.put(tipo, q);
        }
    }

    public synchronized void addQueue(String tipo, String user, BufferedWriter b) {
        if (mapQueue.containsKey(tipo)) {
            mapQueue.get(tipo).add(user);
        }
    }

    public synchronized String getNext(String tipo) {
        return mapQueue.get(tipo).remove();
    }

    public synchronized int size(String tipo) {
        return mapQueue.get(tipo).size();
    }

    public synchronized void addNewTipo(String tipo) {
        if (!this.mapQueue.containsKey(tipo)) {
            Queue<String> q = new LinkedList<>();
            mapQueue.put(tipo, q);
        }
    }


    public synchronized void AllocateFreeServers() {
        ArrayList<String> types = catalogue.getTypes();
        String user = null;
        for (String type : types) {
            int n = catalogue.nFreeServers(type);
            for (int i = 0; i < n; i++) {
                Servers server = catalogue.getAvailableServer(type);
                if (server != null) {
                    server.setOccupied(true);
                    try {
                        user = getNext(type);
                    } catch (Exception e) {}
                    if (user != null) {
                        server.setUserEmail(user);
                        server.startServer();
                        writers.writeMessage(user, "Quem espera sempre alcança! Um servidor foi libertado e reservado para si. Este é o identificador da reserva: " + server.getIdServers() + "\n");
                    } else {
                        server.setOccupied(false);
                    }
                }
            }
        }
    }

    /*public synchronized void AllocateFreeServers(){
            System.out.println("ta tudo");
        }*/
    public synchronized void standBy() throws InterruptedException {
        this.wait();
    }

    public synchronized void signal() {
        this.notify();
    }

    public void run() {
        while (true) {
            try {
                standBy();
                AllocateFreeServers();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
