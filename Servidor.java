import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Servidor {
    TCPServer mTcpServer;
    GameGUI gameGUI;
    GameRules gameRules;
    private Timer enemigosTimer;
    private Timer disparosTimer;
    private boolean gameRunning = false;
    private Random random = new Random();
    private List<Disparo> disparos = new ArrayList<>();

    public static void main(String[] args) {
        Servidor objser = new Servidor();
        objser.crear();
    }

    void crear() {
        gameRules = new GameRules();
        gameGUI = new GameGUI(gameRules);
        gameGUI.getGameContent().getConectar().setText("Iniciar");
        gameGUI.getGameContent().getIp().setVisible(false);
        gameGUI.getGameContent().getJugador().setText("Numero de jugadores: 0");
        gameGUI.getGameContent().getIpText().setVisible(false);
        iniciar();
    }

    void iniciar() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        gameGUI.start();
                        mTcpServer = new TCPServer(
                                new TCPServer.OnMessageReceived() {
                                    @Override
                                    public void messageReceived(String message) {
                                        synchronized (this) {
                                            ServidorRecibe(message);
                                        }
                                    }
                                }
                        );
                        mTcpServer.run(gameGUI);
                    }
                }
        ).start();

        gameGUI.getGameContent().getConectar().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (mTcpServer.getNrcli() != 0 && !gameRunning) {
                    gameRunning = true;
                    gameGUI.getGameContent().getConectar().setEnabled(false);
                    gameGUI.getGameContent().getMapa().setEnabled(true);

                    // Crear enemigos iniciales (entre 5-10)
                    int cantidadEnemigos = 5 + random.nextInt(6);
                    gameRules.crearEnemigos(cantidadEnemigos);

                    // Iniciar movimiento de enemigos
                    iniciarMovimientoEnemigos();

                    // Iniciar procesamiento de disparos
                    iniciarProcesadoDisparos();

                    recargarTablero();
                    enviarTablero();
                }
            }
        });
    }

    private void iniciarMovimientoEnemigos() {
        enemigosTimer = new Timer();
        enemigosTimer.scheduleAtFixedRate(new TimerTask() {
            int direccion = 1; // 1 derecha, -1 izquierda
            int contadorMovimientos = 0;

            @Override
            public void run() {
                List<Enemigo> enemigos = gameRules.getEnemigos();
                boolean bajarNivel = false;

                // Verificar si algún enemigo llegó al borde
                for (Enemigo enemigo : enemigos) {
                    if ((enemigo.getY() >= gameRules.getCols() - 2 && direccion == 1) ||
                            (enemigo.getY() <= 1 && direccion == -1)) {
                        bajarNivel = true;
                        break;
                    }
                }

                // Mover enemigos
                for (Enemigo enemigo : enemigos) {
                    if (bajarNivel) {
                        // Si toca borde, bajar una fila y cambiar dirección
                        enemigo.setX(enemigo.getX() + 1);
                        direccion *= -1;
                    } else if (contadorMovimientos % enemigo.getVelocidad() == 0) {
                        // Mover horizontalmente según velocidad
                        enemigo.setY(enemigo.getY() + direccion);
                    }

                    // Verificar si algún enemigo llegó a la fila del jugador
                    if (enemigo.getX() >= gameRules.getPlayerRow()) {
                        // Fin del juego
                        gameOver();
                        return;
                    }
                }

                contadorMovimientos++;

                // Si no quedan enemigos, crear una nueva oleada
                if (enemigos.isEmpty()) {
                    int nuevaCantidad = 5 + random.nextInt(6);
                    gameRules.crearEnemigos(nuevaCantidad);
                }

                // Disparo enemigo aleatorio
                if (random.nextInt(10) == 0 && !enemigos.isEmpty()) {
                    Enemigo disparador = enemigos.get(random.nextInt(enemigos.size()));
                    disparos.add(new Disparo(disparador.getX() + 1, disparador.getY(), 1, "e")); // disparo enemigo hacia abajo
                }

                recargarTablero();
                enviarTablero();
            }
        }, 1000, 300); // Actualizar cada 300ms
    }

    private void iniciarProcesadoDisparos() {
        disparosTimer = new Timer();
        disparosTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!gameRunning) return;

                String[][] tablero = gameRules.getTablero();
                List<Enemigo> enemigos = gameRules.getEnemigos();
                List<Jugador> jugadores = mTcpServer.getJugadores();

                // Procesar movimiento de disparos
                Iterator<Disparo> disparoIterator = disparos.iterator();
                while (disparoIterator.hasNext()) {
                    Disparo disparo = disparoIterator.next();

                    // Limpiar posición anterior
                    if (disparo.getX() >= 0 && disparo.getX() < gameRules.getRows() &&
                            disparo.getY() >= 0 && disparo.getY() < gameRules.getCols()) {
                        tablero[disparo.getX()][disparo.getY()] = "   ";
                    }

                    // Mover disparo
                    disparo.setX(disparo.getX() - disparo.getDireccion());

                    // Verificar si salió de la pantalla
                    if (disparo.getX() < 0 || disparo.getX() >= gameRules.getRows()) {
                        disparoIterator.remove();
                        continue;
                    }

                    // Colisión con enemigos (disparos jugador)
                    if (disparo.getTipo().equals("j")) {
                        boolean impacto = false;
                        Iterator<Enemigo> enemigoIterator = enemigos.iterator();
                        while (enemigoIterator.hasNext()) {
                            Enemigo enemigo = enemigoIterator.next();
                            if (disparo.getX() == enemigo.getX() && disparo.getY() == enemigo.getY()) {
                                // Impacto con enemigo
                                enemigoIterator.remove();

                                // Aumentar puntuación del jugador que disparó
                                for (Jugador jugador : jugadores) {
                                    if (jugador.getName().equals(disparo.getJugadorId())) {
                                        jugador.increaseScore(enemigo.getPuntos());
                                        break;
                                    }
                                }

                                disparoIterator.remove();
                                impacto = true;
                                break;
                            }
                        }
                        if (impacto) continue;
                    }

                    // Colisión con jugadores (disparos enemigos)
                    if (disparo.getTipo().equals("e")) {
                        Iterator<Jugador> jugadorIterator = jugadores.iterator();
                        while (jugadorIterator.hasNext()) {
                            Jugador jugador = jugadorIterator.next();
                            if (disparo.getX() == jugador.getX() && disparo.getY() == jugador.getY()) {
                                // Impacto con jugador
                                jugadorIterator.remove();
                                mTcpServer.setNrcli(mTcpServer.getNrcli() - 1);
                                disparoIterator.remove();

                                // Si no quedan jugadores, terminar juego
                                if (jugadores.isEmpty()) {
                                    gameOver();
                                }
                                break;
                            }
                        }
                    }

                    // Dibujar disparo en nueva posición
                    if (disparo.getX() >= 0 && disparo.getX() < gameRules.getRows() &&
                            disparo.getY() >= 0 && disparo.getY() < gameRules.getCols()) {
                        tablero[disparo.getX()][disparo.getY()] = disparo.getTipo().equals("j") ? " ↑ " : " ↓ ";
                    }
                }

                recargarTablero();
                enviarTablero();
            }
        }, 500, 100); // Actualizar cada 100ms
    }

    private void gameOver() {
        if (enemigosTimer != null) {
            enemigosTimer.cancel();
        }
        if (disparosTimer != null) {
            disparosTimer.cancel();
        }
        gameRunning = false;

        // Mostrar mensaje de fin de juego
        String[][] tablero = gameRules.getTablero();
        for (int i = 0; i < gameRules.getRows(); i++) {
            for (int j = 0; j < gameRules.getCols(); j++) {
                tablero[i][j] = "   ";
            }
        }

        String[] gameOver = {
                " _____ _____ _____ _____    _____ _____ _____ _____ ",
                "|   __|  _  |     |   __|  |     |  |  |   __| __  |",
                "|  |  |     | | | |   __|  |  |  |  |  |   __|    -|",
                "|_____|__|__|_|_|_|_____|  |_____|\\___/|_____|__|__|"
        };

        int startRow = gameRules.getRows() / 2 - gameOver.length / 2;
        int startCol = gameRules.getCols() / 2 - gameOver[0].length() / 2;

        for (int i = 0; i < gameOver.length; i++) {
            if (startRow + i >= 0 && startRow + i < gameRules.getRows()) {
                char[] line = gameOver[i].toCharArray();
                for (int j = 0; j < line.length; j++) {
                    if (startCol + j >= 0 && startCol + j < gameRules.getCols()) {
                        tablero[startRow + i][startCol + j] = " " + line[j] + " ";
                    }
                }
            }
        }

        recargarTablero();
        enviarTablero();
    }

    private void recargarTablero() {
        String[][] tablero = gameRules.getTablero();
        String tableroLineas = "";

        // Limpiar tablero
        for (int i = 0; i < gameRules.getRows(); i++) {
            for (int j = 0; j < gameRules.getCols(); j++) {
                tablero[i][j] = "   ";
            }
        }

        // Dibujar jugadores
        if (mTcpServer.getJugadores().size() > 0) {
            for (Jugador jugador : mTcpServer.getJugadores()) {
                int x = jugador.getX();
                int y = jugador.getY();

                if (x >= 0 && x < gameRules.getRows() && y >= 0 && y < gameRules.getCols()) {
                    tablero[x][y] = jugador.getNave();
                }
            }
        }

        // Dibujar enemigos
        for (Enemigo enemigo : gameRules.getEnemigos()) {
            int x = enemigo.getX();
            int y = enemigo.getY();

            if (x >= 0 && x < gameRules.getRows() && y >= 0 && y < gameRules.getCols()) {
                tablero[x][y] = enemigo.getDiseño();
            }
        }

        // Dibujar disparos
        for (Disparo disparo : disparos) {
            int x = disparo.getX();
            int y = disparo.getY();

            if (x >= 0 && x < gameRules.getRows() && y >= 0 && y < gameRules.getCols()) {
                tablero[x][y] = disparo.getTipo().equals("j") ? " ↑ " : " ↓ ";
            }
        }

        // Generar string del tablero
        for (int i = 0; i < gameRules.getRows(); i++) {
            for (int j = 0; j < gameRules.getCols(); j++) {
                tableroLineas += tablero[i][j];
            }
            tableroLineas += "\n";
        }

        gameRules.setTableroLineas(tableroLineas);
        gameGUI.getGameContent().getMapa().setText(tableroLineas);
    }

    private void enviarTablero() {
        String tab = gameRules.getTableroLineas();
        String[] lines = tab.split("\n");
        String tabLine = "";
        for (String line : lines) {
            tabLine += line + "_";
        }

        // Añadir puntuación de cada jugador al mensaje
        for (int i = 0; i < mTcpServer.getJugadores().size(); i++) {
            Jugador jugador = mTcpServer.getJugadores().get(i);
            String mensaje = jugador.getName() + "|" + jugador.getX() + "," + jugador.getY() + "|" + tabLine + "|" + jugador.getScore();
            mTcpServer.getClients()[i + 1].sendMessage(mensaje);
        }
    }

    void ServidorRecibe(String llego) {
        String[] datos = llego.split("\\|");

        // Disparos
        if (datos.length == 2 && datos[1].equals("d")) {
            for (Jugador jugador : mTcpServer.getJugadores()) {
                if (jugador.getName().equals(datos[0])) {
                    // Crear un nuevo disparo desde la posición del jugador
                    disparos.add(new Disparo(jugador.getX() - 1, jugador.getY(), 1, "j", jugador.getName()));
                    break;
                }
            }

            recargarTablero();
            enviarTablero();
            return;
        }

        // Movimiento
        if (datos.length >= 3) {
            int[] pn = Arrays.stream(datos[1].split(",")).mapToInt(Integer::parseInt).toArray();
            int[] pa = Arrays.stream(datos[2].split(",")).mapToInt(Integer::parseInt).toArray();

            // Actualizar posición del jugador
            for (Jugador jugador : mTcpServer.getJugadores()) {
                if (jugador.getName().equals(datos[0])) {
                    // Limitar movimiento a la fila del jugador (horizontal solamente)
                    jugador.setX(gameRules.getPlayerRow());
                    jugador.setY(pn[1]);
                    break;
                }
            }

            recargarTablero();
            enviarTablero();
        }
    }

    void ServidorEnvia(String envia) {
        if (envia != null) {
            if (mTcpServer != null) {
                mTcpServer.sendMessageTCPServerRango(envia);
            }
        }
    }
}

class Disparo {
    private int x;
    private int y;
    private int direccion; // 1 = hacia arriba, -1 = hacia abajo
    private String tipo; // "j" = jugador, "e" = enemigo
    private String jugadorId; // id del jugador que disparó (para contabilizar puntos)

    public Disparo(int x, int y, int direccion, String tipo) {
        this.x = x;
        this.y = y;
        this.direccion = direccion;
        this.tipo = tipo;
    }

    public Disparo(int x, int y, int direccion, String tipo, String jugadorId) {
        this(x, y, direccion, tipo);
        this.jugadorId = jugadorId;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getDireccion() {
        return direccion;
    }

    public String getTipo() {
        return tipo;
    }

    public String getJugadorId() {
        return jugadorId;
    }
}