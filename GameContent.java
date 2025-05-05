import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class GameContent extends JPanel {
    private JLabel ipText;
    private JTextField ip;
    private JButton conectar;
    private JLabel jugador;
    private JTextArea mapa;

    public GameContent(){
        setLayout(new BorderLayout());
        contentTop();
        contentBot();
    }

    private void contentTop(){
        JPanel panelContent = new JPanel();
        panelContent.setLayout(new FlowLayout());

        JPanel panelContentLeft = new JPanel();
        panelContentLeft.setLayout(new FlowLayout());
        ipText = new JLabel("IP");
        ip = new JTextField(10);
        panelContentLeft.add(ipText);
        panelContentLeft.add(ip);
        panelContent.add(panelContentLeft);

        JPanel panelContentCenter = new JPanel();
        panelContentCenter.setLayout(new BorderLayout());
        conectar = new JButton("Conectar");
        panelContentCenter.add(conectar);
        panelContent.add(panelContentCenter);

        JPanel panelContentRight = new JPanel();
        panelContentRight.setLayout(new BorderLayout());
        jugador = new JLabel("Jugador");
        panelContentRight.add(jugador);
        panelContent.add(panelContentRight);
        panelContent.setBorder(new LineBorder(Color.black));

        add(panelContent, BorderLayout.NORTH);
    }

    private BufferedImage backgroundImage;

    private void fondo(){
        try {
            backgroundImage = ImageIO.read(getClass().getResource("/fondo.jpg"));
        } catch (IOException e) {
            System.out.println("no se encontro");
        }
    }
    private void contentBot(){
        fondo();
        JPanel panelContent = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
                }
            }
        };
        panelContent.setLayout(new BorderLayout());

        mapa = new JTextArea();
        mapa.setOpaque(false);
        mapa.setBackground(new Color(0, 0, 0, 0));
        mapa.setFont(new Font(Font.MONOSPACED, 0, 15));
        mapa.setEnabled(false);
        panelContent.add(mapa, BorderLayout.CENTER);
        add(panelContent, BorderLayout.CENTER);
    }

    public JLabel getIpText() {
        return ipText;
    }

    public JTextField getIp() {
        return ip;
    }

    public JButton getConectar() {
        return conectar;
    }

    public JLabel getJugador() {
        return jugador;
    }

    public JTextArea getMapa() {
        return mapa;
    }
}
