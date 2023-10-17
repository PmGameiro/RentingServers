/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aluguerservidores;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author C, J, A, I
 */
public class Client {

    private String address;
    private int port;
    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
    private Scanner read;
    private boolean work;

    public Client() throws IOException {

        this.address = "127.0.0.1";
        this.port = 12345;
        this.socket = new Socket(this.address, this.port);
        this.read = new Scanner(System.in);
        this.work = true;
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void getInputsDoThings() throws IOException {

        new Thread(() -> {
            try {
                String respostaSocket = new String();
                while (!respostaSocket.equals("Bye!")) {
                    respostaSocket = input.readLine();
                    if (respostaSocket.equals("exit")) {
                        closeSocket();
                        break;
                    }

                    if (respostaSocket != null && !respostaSocket.equals("")) {
                        System.out.println(respostaSocket);
                    }
                }
            } catch (IOException ex) {
            }
        }).start();

        while (work) {

            String clientText = read.nextLine();

            try {
                output.write(clientText);
                output.newLine();
                output.flush();
            } catch (IOException e) {
                System.out.println("O programa terminou com sucesso");
            }
        }
    }

    private void closeSocket() throws IOException {
        this.socket.shutdownInput();
        this.socket.shutdownOutput();
        this.socket.close();
        this.work = false;
    }
}
