package aluguerservidores;

public class AluguerServidores {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
        System.out.println("Aluguer de Servidores");
        Client client = new Client();
        client.getInputsDoThings();
        }catch(Exception e){
            System.out.println("Erro!");
        }
    }

}
