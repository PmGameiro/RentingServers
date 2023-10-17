package aluguerservidores;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe servidor que recebe os clientes que sao criados pela AluguerServidores
 */
public class Server {

    private final ServerSocket serverSocket;
    private ArrayList<ServerThread> clients;
    private AccountsMap accounts;
    private EmailList loggedIn;
    private Catalogue catalogue;
    private MyQueue queue;
    private WriterMap writers;
    private AuctionManager auctionManager;
    private Lock accountsLock;

    public Server() throws IOException, NoSuchAlgorithmException {
        this.serverSocket = new ServerSocket(12345);
        this.clients = new ArrayList<>();
        this.accounts = new AccountsMap();
        this.accountsLock = new ReentrantLock();
        this.loggedIn = new EmailList();
        this.catalogue = new Catalogue();
        this.writers = new WriterMap();
        this.auctionManager = new AuctionManager(catalogue, writers);
        this.queue = new MyQueue(catalogue, writers);
    }

    public static void main(String[] args) throws Exception{
        try {
            Server servidor = new Server();
            servidor.getInput();
        } catch (Exception e){
            throw new Exception(e);
        }
    }

    private void getInput() throws Exception {
        auctionManager.start();
        queue.start();
        while (true) {
            try {
                Socket clSocket = serverSocket.accept();
                ServerThread st = new ServerThread(clSocket);
                this.clients.add(st);
                st.start();
            } catch (Exception e) {
                throw new Exception(e);
            }
        }
    }

    private class ServerThread extends Thread {

        private BufferedWriter output;
        private BufferedReader input;
        public String myEmail;

        private ServerThread(Socket clSocket) {
            try {
                input = new BufferedReader(new InputStreamReader(clSocket.getInputStream()));
                output = new BufferedWriter(new OutputStreamWriter(clSocket.getOutputStream()));
                myEmail = "";
            } catch (IOException ex) {
            }
        }

        private void sendMessage(String textInput) {
            try {
                output.write(textInput);
                output.newLine();
                output.flush();
            } catch (IOException ex) {
                System.out.println("Error send Message!");
            }
        }

        private int startMenu() throws IOException {
            this.sendMessage("\n1 - Login \n2 - Registar \nquit para sair");
            String answer = input.readLine();
            if (answer.equalsIgnoreCase("quit")) {
                return -1;
            } else if (answer.equals("2")) {
                return signupPrompt();
            } else if (answer.equals("1")) {
                return loginPrompt();
            } else {
                return 0;
            }
        }

        private int mainPage() throws IOException, InterruptedException {
            ArrayList<Servers> catalogue_list = catalogue.makeServerList();
            int status = 0;
            String answer;
            while (status == 0) {
                this.sendMessage("\n1 - Listar Catálogo \n2 - Reservar servidor \n3 - Meus Servidores \n4 - Libertar servidor \n5 - Log Out");
                answer = input.readLine();
                switch (answer) {
                    case "1":
                        this.sendMessage(this.listCatalogue());
                        break;
                    case "2":
                        this.sendMessage(this.request_Server());
                        break;
                    case "3":
                        this.sendMessage(this.listMyServers());
                        break;
                    case "4":
                        this.sendMessage(this.liberateServer());
                        break;
                    case "5":
                        loggedIn.removeEmail(myEmail);
                        writers.remove(myEmail);
                        status = 1;
                        break;
                    default:
                        break;
                }
            }
            return 0;
        }

        private String listCatalogue(){
            StringBuilder sb = new StringBuilder();
            sb.append("\tServidores livres:\n").append(listFreeServers());
            sb.append("\tServidores Ocupados:\n").append(listOccupiedServers());
            return sb.toString();
        }

        private String request_Server() throws IOException{
            this.sendMessage(listFreeServers());
            this.sendMessage("\n1 - Reservar servidor pelo preço nominal \n2 - Propor oferta de preço em leilão");
            String answer = input.readLine();
            String response = "";
            switch (answer) {
                case "1":
                    response = this.rentServer();
                    break;
                case "2":
                    response = this.serverAuction();
                    break;
                default:
                    break;
            }
            return response;
        }

        private String rentServer() throws IOException {
            ArrayList<String> typeList = catalogue.getTypes();
            StringBuilder sb = new StringBuilder();
            int i = 1;
            for (String type : typeList) {
                sb.append(i).append(" - Alugar servidor to tipo ").append(type).append("\n");
                i++;
            }
            this.sendMessage(sb.toString());

            String answer = input.readLine();
            int n;

            try {
                n = Integer.parseInt(answer);
            } catch (Exception e) {
                return "Comando inválido";
            }

            if (n > typeList.size() || n < 0) {
                return "Comando inválido";
            }
            String tipo = typeList.get(n - 1);
            Servers requested = catalogue.getAvailableServer(tipo);
            if (requested == null) {
                requested = catalogue.findOccupiedAuctionedServerOfType(tipo);
                if (requested != null) {
                    String oldUserMail = requested.getUserEmail();
                    Account oldUser = accounts.getAccount(oldUserMail);
                    requested.reset();
                    requested.setOccupied(true);
                    requested.setUserEmail(myEmail);
                    requested.startServer();
                    writers.writeMessage(oldUserMail, "Lamentamos, mas a reserva que obteve em leilão, cuja identificação é: " + requested.getIdServers()
                            + " teve de ser cedida para uma reserva pelo preço nominal, por falta de disponibilidade de servidores.");
                    return "Este é o identificador da reserva: " + requested.getIdServers() + "\n";
                } else {
                    
                 this.sendMessage( "Neste momento não há servidores disponíveis do tipo pretendido\nPretende ficar na Fila de Espera?\n1 - Sim\n2 - Não");
                    answer = input.readLine();
                    try {
                        n = Integer.parseInt(answer);
                    } catch (Exception e) {
                        return "Comando inválido";
                    }
                    switch(n){
                        case 1:
                            queue.addQueue(tipo, myEmail, output);
                            return "Está na Fila de Espera!\n";
                        default:
                            return "";
                    }                  
                }
            } else {
                requested.setOccupied(true);
                requested.setUserEmail(myEmail);
                requested.startServer();
                return "Este é o identificador da reserva: " + requested.getIdServers() + "\n";
            }
        }

        private String serverAuction() throws IOException {
            ArrayList<String> typeList = catalogue.getTypes();
            String serverType;
            StringBuilder sb = new StringBuilder();
            int i = 1;
            for (String type : typeList) {
                sb.append(i).append(" - Propor oferta para servidor to tipo ");
                sb.append(type).append("\n");
                i++;
            }
            this.sendMessage(sb.toString());
            String answer = input.readLine();
            int n;

            try {
                n = Integer.parseInt(answer);
            } catch (Exception e) {
                return "Comando inválido\n";
            }

            if (n > typeList.size() || n < 0) {
                return "Comando inválido\n";
            }

            serverType = typeList.get(n - 1);

            Auction auction = auctionManager.joinAuction(myEmail, serverType);

            while (!auction.isFinished()) {
                answer = input.readLine();
                if (answer.equals("quit")) {
                    if (auction.getCurrentHighestBidder().equals(myEmail)) {
                        sendMessage("Não pode sair do leilão dado que é o atual maior licitador");
                    } else {
                        auction.removeParticipant(myEmail);
                        return "Saiu do leilão";
                    }
                } else if (!auction.isFinished()) {
                    try {
                        n = Integer.parseInt(answer);
                        auction.bid(myEmail, n);
                    } catch (Exception e) {
                        sendMessage("Comando inválido\n");
                    }
                }
            }
            return ("\n");
        }

        private String listMyServers() {
            ArrayList<Servers> catalogue_list = catalogue.makeServerList();
            StringBuilder sb = new StringBuilder();

            float price;
            for (Servers server : catalogue_list) {
                if (server.getUserEmail().equals(myEmail)) {
                    if (server.wasBoughtInAuction())
                        price = server.getActualPrice();
                    else
                        price = server.getNominalPrice();
                    sb.append("Id da Reserva: ").append(server.getIdServers());
                    sb.append(" \n\t Tipo: ").append(server.getType());
                    sb.append(" \n\t Preço Por hora:").append(price);
                    sb.append("\nHoras ativo: ").append(server.getMinutes());
                    sb.append("\nTotal a pagar: ").append(server.getCurrentTotal());
                    sb.append("\n\n");
                }
            }
            return sb.toString();
        }

        private String listFreeServers() {
            StringBuilder sb = new StringBuilder();
            ArrayList<Servers> catalogue_list = catalogue.makeServerList();
            sb.append("Servidores livres:\n");
            ArrayList<String> typeList = catalogue.getTypes();
            for (String type : typeList) {

                sb.append("\t").append(type).append(": ").append(catalogue.nFreeServers(type));
                sb.append("\n\t Preço nominal: ").append(catalogue.getNominalPrice(type));
                sb.append("\n");
            }
            return sb.toString();
        }

        private String listOccupiedServers() {
            StringBuilder sb = new StringBuilder();
            sb.append("Servidores Ocupados:\n");
            ArrayList<String> typeList = catalogue.getTypes();

            for (String type : typeList) {
                sb.append("\t").append(type).append(": ");
                sb.append(catalogue.getNominalPrice(type)).append("\n");
            }
            return sb.toString();
        }




        private String liberateServer() throws IOException{
            float total_pay = 0;
            this.sendMessage("Por favor indique o identificador da reserva!\n");
            String answer = input.readLine();
            String u_email = myEmail;
            StringBuilder sb = new StringBuilder();

            if (catalogue.containsKey(answer)) {
                Servers s_requested = catalogue.getServer(answer);
                if (s_requested.getUserEmail().equals(u_email)) {
                    sb.append("O servidor foi libertado com sucesso! Teria de pagar " );
                    sb.append(s_requested.getCurrentTotal());
                    sb.append(" mas desta vez fica por conta da casa ;D \n");
                    s_requested.reset();
                    queue.signal();
                } else {
                    sb.append("O servidor mencionado já não está associado a si!\n");
                }
            } else {
                sb.append("A referência é inválida\n");
            }
            return sb.toString();
        }

        private int loginPrompt() {
            try {
                boolean set = false;
                boolean valid = false;
                int tries = 3;
                String email;
                String password;
                while (!set) {
                    this.sendMessage("E-mail: ");
                    email = input.readLine();
                    if (email.equalsIgnoreCase("quit")) {
                        return 0;
                    } else if (!accounts.isAccountEmail(email)) {
                        this.sendMessage("E-mail inválido\n");
                    } else if (loggedIn.containsEmail(email)) {
                        this.sendMessage("Um utilizador com esse e-mail já efetuou log-in\n");
                    } else {
                        while (!valid) {
                            this.sendMessage("Password: ");
                            password = input.readLine();
                            if (!accounts.isValidPassword(email, password)) {
                                tries--;
                                if (tries == 0) {
                                    this.sendMessage("Terceira tentativa falhada\n");
                                    return 0;
                                } else {
                                    this.sendMessage("Password Inválida. Tem mais " + tries + " tentativas\n");
                                }
                            } else {
                                loggedIn.addEmail(email);
                                writers.add(this.myEmail, this.output);
                                set = true;
                                valid = true;
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            return 1;
        }

        private int signupPrompt() {
            try {
                boolean set = false;
                String email;
                String password;
                String answer = "";
                while (!set) {
                    this.sendMessage("E-mail: ");
                    email = input.readLine();
                    if (accounts.isAccountEmail(email)) {
                        this.sendMessage("Já existe uma conta com o e-mail indicado.\nPretende introduzir novo e-mail? s/n");
                        while (!answer.equals("s") && !answer.equals("n")) {
                            answer = input.readLine();
                            if (answer.equals("n")) {
                                return 0;
                            }
                        }
                    } else if (email == null || email.equals(""))
                    ; else {
                        this.sendMessage("Password: ");
                        password = input.readLine();
                        Account conta = new Account(email, password);
                        myEmail = email;
                        accounts.addAccount(conta);
                        set = true;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            return 0;
        }

        @Override
        public void run() {
            try {
                int phase = 0;
                while (phase != -1) {
                    if (phase == 0) {
                        phase = this.startMenu();
                    }
                    if (phase == 1) {
                        phase = this.mainPage();
                    }
                }
                sendMessage("exit");
                this.join();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}
