import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GameGUI {
    private final int HEIGHT=700, WIDTH=1200;
    private JFrame ventana;
    private GameRules gameRules;
    private GameContent gameContent;
    public GameGUI(GameRules gameRules){
        ventana = new JFrame();
        this.gameRules = gameRules;
        gameContent = new GameContent();
        ventana.setTitle("Battle Chopper");
        ventana.setUndecorated(true);
        ventana.pack();
        ventana.setSize(WIDTH, HEIGHT);
        ventana.setContentPane(gameContent);
        ventana.setLocationRelativeTo(null);
        ventana.setResizable(false);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setVisible(true);
    }

    public void start(){
        gameContent.getMapa().setText(gameRules.getTableroLineas());
    }

    public GameContent getGameContent() {
        return gameContent;
    }

    public static void main(String[] args) {
        new GameGUI(new GameRules()).start();
    }
}
