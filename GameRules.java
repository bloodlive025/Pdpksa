import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameRules {
    private int rows = 31, cols = 44;
    private String[][] tablero = new String[rows][cols];
    private String tableroLineas = "";
    private enum Estado {esperando, jugando, finalizado};
    private Estado estado = Estado.esperando;
    private Jugador jugador;
    private List<Enemigo> enemigos = new ArrayList<>();
    private Random random = new Random();
    private int playerRow; // Fixed row for player movement

    public GameRules() {
        playerRow = rows - 2; // Player starts near the bottom
        jugador = new Jugador("A", playerRow, cols / 2);
        inicializarTablero();
    }

    private void inicializarTablero() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                tablero[i][j] = "   ";
                tableroLineas += "   ";
            }
            tableroLineas += "\n";
        }
    }

    public void crearEnemigos(int cantidad) {
        enemigos.clear();
        int espacioHorizontal = cols / (cantidad + 1);

        for (int i = 0; i < cantidad; i++) {
            int tipoEnemigo = random.nextInt(3); // 0, 1, 2 - tres tipos de enemigos
            int posX = 3 + random.nextInt(2); // Filas iniciales 3-4
            int posY = (i + 1) * espacioHorizontal; // Distribuir horizontalmente
            enemigos.add(new Enemigo("E" + i, tipoEnemigo, posX, posY));
        }
    }

    public void setTableroLineas(String tableroLineas) {
        this.tableroLineas = tableroLineas;
    }

    public String getTableroLineas() {
        return tableroLineas;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public String[][] getTablero() {
        return tablero;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public List<Enemigo> getEnemigos() {
        return enemigos;
    }

    public int getPlayerRow() {
        return playerRow;
    }
}

class Jugador {
    private String name;
    private String nave = "^[=]=^";
    private int score;
    private int x, y;

    public Jugador(String name, int x, int y) {
        this.score = 0;
        this.name = name;
        this.x = x;
        this.y = y;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNave() {
        return nave;
    }

    public int getScore() {
        return score;
    }

    public void increaseScore(int points) {
        score += points;
    }
}

class Enemigo {
    private String name;
    private String[] diseños = {
            "/oo\\", // Pequeño - tipo 0
            "{<oo>}", // Mediano - tipo 1
            "={<(oo)>}=" // Grande - tipo 2
    };
    private int tipo; // 0: pequeño, 1: mediano, 2: grande
    private int puntos; // Puntos que da al ser eliminado
    private int x, y;
    private int velocidad; // Velocidad de movimiento

    public Enemigo(String name, int tipo, int x, int y) {
        this.name = name;
        this.tipo = tipo;
        this.x = x;
        this.y = y;

        // Asignar puntos según tamaño
        switch (tipo) {
            case 0: this.puntos = 10; this.velocidad = 3; break; // Pequeño: más puntos, más rápido
            case 1: this.puntos = 20; this.velocidad = 2; break; // Mediano
            case 2: this.puntos = 30; this.velocidad = 1; break; // Grande: menos puntos, más lento
        }
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

    public String getName() {
        return name;
    }

    public String getDiseño() {
        return diseños[tipo];
    }

    public int getTipo() {
        return tipo;
    }

    public int getPuntos() {
        return puntos;
    }

    public int getVelocidad() {
        return velocidad;
    }
}