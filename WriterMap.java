/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aluguerservidores;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * Classs Utilizada para Guardar os BufferedWriters dos clientes
 * para que seja possivel establecer a comunicação do servidor
 * com o cliente
 */
public class WriterMap {

    private HashMap<String, BufferedWriter> writers;

    public WriterMap() {
        this.writers = new HashMap<>();
    }

    public synchronized void add(String s, BufferedWriter b) {
        this.writers.put(s, b);
    }

    public synchronized void remove(String s) {
        this.writers.remove(s);
    }

    public synchronized void writeMessage(String id, String message) {
        if (this.writers.get(id) != null) {
            try {
                this.writers.get(id).write(message);
                this.writers.get(id).newLine();
                this.writers.get(id).flush();
            } catch (IOException ex) {
            }
        }
    }
}
