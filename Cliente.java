import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Scanner;

class Cliente {
    public int sum[] = new int[40];
    TCPClient mTcpClient;
    Scanner sc;
    GameGUI gameGUI;
    GameRules gameRules;
    private long lastShotTime = 0;
    private static final long SHOT_COOLDOWN = 300; // milisegundos entre disparos

    public static void main(String[] args) {
        Cliente objcli = new Cliente();
        objcli.start();
    }

    void start() {
        gameRules = new GameRules();
        gameGUI = new GameGUI(gameRules);
        gameGUI.getGameContent().getMapa().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                keyAreaPressed(e);
            }
        });
        String ip = gameGUI.getGameContent().getIp().getText();
        gameGUI.getGameContent().getConectar().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                iniciar(ip);
            }
        });
    }

    void iniciar(String ip) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        gameGUI.start();
                        mTcpClient = new TCPClient(ip,
                                new TCPClient.OnMessageReceived() {
                                    @Override
                                    public void messageReceived(String message) {
                                        ClienteRecibe(message);
                                    }
                                }
                        );
                        mTcpClient.run(gameGUI);
                    }
                }
        ).start();
    }

    void ClienteRecibe(String llego) {
        if (llego.contains("|")) {
            String[] datos = llego.split("\\|");
            gameGUI.getGameContent().getJugador().setText("Jugador: " + datos[0] + " | Puntos: " +
                    (datos.length > 3 ? datos[3] : "0"));
            Jugador jugador = gameRules.getJugador();
            jugador.setName(datos[0]);
            String[] posicion = datos[1].split(",");
            jugador.setX(Integer.parseInt(posicion[0]));
            jugador.setY(Integer.parseInt(posicion[1]));

            String[] lines = datos[2].split("_");
            String mapa = "";
            for (String line : lines) {
                mapa += line + "\n";
            }
            gameGUI.getGameContent().getMapa().setText(mapa);
            gameGUI.getGameContent().getMapa().setEnabled(true);
            gameRules.setTableroLineas(mapa);
        } else {
            gameGUI.getGameContent().getMapa().setEnabled(false);
        }
    }

    private void keyAreaPressed(KeyEvent e) {
        Jugador jugador = gameRules.getJugador();
        int x = jugador.getX(), y = jugador.getY();
        int playerRow = gameRules.getPlayerRow();
        int cols = gameRules.getCols();
        String msg = "";

        // Solo permitir movimiento horizontal (izquierda/derecha)
        if (e.getKeyChar() == 'd') {
            y = (y + 1 < cols ? y + 1 : y);
        }
        if (e.getKeyChar() == 'a') {
            y = (y - 1 >= 0 ? y - 1 : y);
        }

        // Disparar con la tecla de espacio
        if (e.getKeyChar() == ' ') {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastShotTime > SHOT_COOLDOWN) {
                lastShotTime = currentTime;
                ClienteEnvia(jugador.getName() + "|d");
                return;
            }
        }

        if (e.getKeyChar() == 'p') {
            ClienteEnvia(jugador.getName() + "|x");
            return;
        }

        msg = jugador.getName() + "|" + String.valueOf(x) + "," + String.valueOf(y) + "|" +
                String.valueOf(jugador.getX()) + "," + String.valueOf(jugador.getY());
        jugador.setX(x);
        jugador.setY(y);
        ClienteEnvia(msg);
    }

    void ClienteEnvia(String envia) {
        if (mTcpClient != null) {
            mTcpClient.sendMessage(envia);
        }
    }
}