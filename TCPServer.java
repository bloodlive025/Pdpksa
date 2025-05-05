import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class TCPServer {
    private String message;

    private int nrcli = 0;//Numero de clientes
    private int nrcliIniciar = 0;
    char primero = 65;
    char primerNombre = 65;
    private List<Jugador> jugadores;
    private int y = 0; // Posición horizontal de la nave

    public static final int SERVERPORT = 4444;
    private OnMessageReceived messageListener = null;
    private boolean running = false;
    TCPServerThread[] sendclis = new TCPServerThread[10];

    PrintWriter mOut;
    BufferedReader in;

    ServerSocket serverSocket;

    //el constructor pide una interface OnMessageReceived
    public TCPServer(OnMessageReceived messageListener) {
        jugadores = new ArrayList<>();
        this.messageListener = messageListener;
    }

    public OnMessageReceived getMessageListener(){
        return this.messageListener;
    }

    public void sendMessageTCPServer(String message){
        for (int i = 1; i <= nrcli; i++) {
            sendclis[i].sendMessage(message);
            System.out.println("ENVIANDO A JUGADOR " + (i));
        }
    }
    public void sendMessageTCPServerRango(String message){
        Jugador jugador;
        String temp = message;
        int j = 0;
        // A B
        for (int i = 0; i < nrcliIniciar; i++) {
            if (j<nrcli){
                jugador = jugadores.get(j);
                if (primerNombre == jugador.getName().charAt(0)){
                    message = jugador.getName() + "|" + jugador.getX() + "," + jugador.getY() + "|" + temp;
                    sendclis[i+1].sendMessage(message);
                    j++;
                    primerNombre++;
                }
                else {
                    sendclis[i+1].sendMessage("moriste");
                    primerNombre++;
                }
            }
            else {
                sendclis[i+1].sendMessage("moriste");
            }
        }
        primerNombre = 65;
    }
    public void run(GameGUI gui){
        running = true;
        Random random = new Random();
        try{
            System.out.println("TCP Server"+"S : Connecting...");
            serverSocket = new ServerSocket(SERVERPORT);

            while(running){
                Socket client = serverSocket.accept();
                System.out.println("TCP Server"+"S: Receiving...");
                nrcli++;
                nrcliIniciar++;
                System.out.println("Engendrado " + nrcli);
                sendclis[nrcli] = new TCPServerThread(client,this,nrcli,sendclis);
                Thread t = new Thread(sendclis[nrcli]);
                t.start();

                // MODIFICACIÓN: Posicionar al jugador en la parte inferior de la pantalla
                // Fijar el valor de X a la última fila (rows-1)
                int bottomRow = 30; // Última fila (gameRules.getRows()-1)

                // Calcular una posición horizontal aleatoria
                y = random.nextInt(44); // Rango de 0 a cols-1

                // Crear jugador en la posición inferior
                jugadores.add(new Jugador(String.valueOf(primero++), bottomRow, y));
                gui.getGameContent().getJugador().setText("Numero de jugadores: "+nrcli);

                System.out.println("Nuevo conectado:"+ nrcli+" jugadores conectados");
                for (Jugador jugador: jugadores){
                    System.out.println(jugador.getName());
                }
            }

        }catch( Exception e){
            System.out.println("Error"+e.getMessage());
        }finally{

        }
    }

    public int getNrcli() {
        return nrcli;
    }

    public void setNrcli(int nrcli) {
        this.nrcli = nrcli;
    }

    public  TCPServerThread[] getClients(){
        return sendclis;
    }

    public List<Jugador> getJugadores() {
        return jugadores;
    }

    public  interface OnMessageReceived {
        public void messageReceived(String message);
    }
}