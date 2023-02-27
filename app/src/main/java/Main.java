import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.advanced.AdvancedPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.ImageProducer;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main extends JFrame implements ActionListener {

    public JTextField idmField;
    public JLabel labelForInfo;
    public JLabel labelForMsg;
    private String temIdm = "";
    //出席管理データの次に入力する列の番号を保持
    private int count = 0;
    private final int roomId;

    Main(int roomId) {
        this.roomId = roomId;


        //GUIのデザイン
        setTitle("入退出管理システム");
        Toolkit toolkit = getToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        Insets screenInsets = toolkit.getScreenInsets(getGraphicsConfiguration());

        int width = screenSize.width - (screenInsets.left + screenInsets.right);
        int height = screenSize.height - (screenInsets.top + screenInsets.bottom);

        setBounds(0, 0, width, height);

        Image im = null;
        URL url = this.getClass().getResource("icon.png");
        try {
            im = this.createImage((ImageProducer) url.getContent());
            setIconImage(im);
        } catch (Exception ex) {
            System.out.println("Resource Error!");
            im = null;
        }

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        GridLayout gridLayout = new GridLayout();
        gridLayout.setHgap(10);
        gridLayout.setVgap(20);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel(" "));

        JLabel label = new JLabel("ICカードをカードリーダーにタッチしてください。");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);

        panel.add(new JLabel(" "));

        JPanel panelOfFlowLayout = new JPanel();
        panelOfFlowLayout.setLayout(new FlowLayout());

        JLabel label2 = new JLabel("IDM:");
        label2.setFont(new Font("msgothic.ttc", Font.PLAIN, 30));
        panelOfFlowLayout.add(label2);

        this.idmField = new JTextField("");
        this.idmField.addActionListener(this);
        this.idmField.setActionCommand("Idm number input");
        this.idmField.setFont(new Font("msgothic.ttc", Font.PLAIN, 30));
        this.idmField.setPreferredSize(new Dimension(400, 40));
        panelOfFlowLayout.add(this.idmField);

        panel.add(panelOfFlowLayout);

        panel.add(new JLabel(""));

        JPanel panelForLabelForMsg = new JPanel();
        panelForLabelForMsg.setLayout(new FlowLayout());

        this.labelForInfo = new JLabel(" ");
        this.labelForInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.labelForInfo.setHorizontalAlignment(SwingConstants.CENTER);
        this.labelForInfo.setFont(new Font("UDDigiKyokashoN-B.ttc", Font.BOLD, 80));
        panelForLabelForMsg.add(this.labelForInfo);


        this.labelForMsg = new JLabel(" ");
        this.labelForMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.labelForMsg.setHorizontalAlignment(SwingConstants.CENTER);
        this.labelForMsg.setFont(new Font("UDDigiKyokashoN-B.ttc", Font.BOLD, 80));
        panelForLabelForMsg.add(this.labelForMsg);

        panel.add(panelForLabelForMsg);

        getContentPane().add(panel);

        FelicaReader felicaReader;

        try {
            felicaReader = new FelicaReader(this);
            Thread thread = new Thread(felicaReader);
            thread.start();

        } catch (NoFoundReaderException noFoundReaderException) {
            JOptionPane.showMessageDialog(this, noFoundReaderException, "注意", JOptionPane.ERROR_MESSAGE);
        }

    }

    public void process(String idm) {
        this.labelForMsg.setText("");

        String musicFilePath = null;
        this.temIdm = idm;
        musicFilePath = "celebrationMusic.mp3";

        //ここでアクセスして出席か退出を取得
        String result;
        try {
            result = HttpUtil.sendHttpRequest("POST", "https://felica-attendance-manager.azurewebsites.net/api/rooms/" + this.roomId + "/update?idm=" + idm);
        } catch (IOException e) {
            e.printStackTrace();

            JOptionPane.showInternalMessageDialog(this.getContentPane(), "リクエストを送信中に予期せぬエラーが発生しました。");
            return;
        }

        //名前が登録されているかを確認
        String getName = "";
        try {
            getName = HttpUtil.sendHttpRequest("GET", "https://felica-attendance-manager.azurewebsites.net/api/name/get?idm=" + idm);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            JOptionPane.showInternalMessageDialog(this.getContentPane(), "リクエストを送信中に予期せぬエラーが発生しました。");
            return;
        }
        if (getName.equals("")) {
            getName = idm;
        }

        this.labelForMsg.setText(getName);
        boolean isEntry;
        if (result.equals("attend")) {
            isEntry = true;
            this.labelForMsg.setText(this.labelForMsg.getText()+"が入室しました");
        } else if (result.equals("exit")) {
            isEntry = false;
            this.labelForMsg.setText(this.labelForMsg.getText()+"が退出しました");
        } else {
            JOptionPane.showInternalMessageDialog(this.getContentPane(), "データの解析中にエラーが発生しました。");
            return;
        }

        try (InputStream is = this.getClass().getResourceAsStream(musicFilePath)) {
            play(is);
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        } catch (JavaLayerException javaLayerException) {
            System.out.println("unsupportedAudioException");
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

    public void play(InputStream mp3file) throws JavaLayerException {

        AudioDevice device = FactoryRegistry.systemRegistry().createAudioDevice();
        // create an MP3 player
        AdvancedPlayer player = new AdvancedPlayer(mp3file, device);
        player.play();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Idm number input":
                if (!this.temIdm.equals(this.idmField.getText())) {
                    process(this.idmField.getText());
                }
        }
    }

   }