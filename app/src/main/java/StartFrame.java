import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageProducer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class StartFrame extends JFrame implements ActionListener {
    private final JLabel labelToShowFilePath;
    private final JLabel labelForMsg;
    private final JTextField jTextField;
    ArrayList<Integer> roomIdsList;

    StartFrame() {
        //一番最初にROOMIDの一覧を取得する
        try {
            String result = HttpUtil.sendHttpRequest("GET", "https://felica-attendance-manager.azurewebsites.net/api/rooms/show");

            ObjectMapper mapper = new ObjectMapper();
            Integer[] roomIdsArray = mapper.readValue(result, Integer[].class);
            this.roomIdsList = new ArrayList<>(Arrays.asList(roomIdsArray));

            System.out.println("result:" + roomIdsArray[0]);
        } catch (Exception e) {
            JOptionPane.showInternalMessageDialog(this.getContentPane(), "データベースとの接続中にエラーが発生ため、ソフトウェアを閉じます。" + e.getMessage(),
                    "エラー", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }


        Image im = null;
        URL url = this.getClass().getResource("icon.png");
        try {
            im = this.createImage((ImageProducer) url.getContent());
            setIconImage(im);
        } catch (Exception ex) {
            ex.printStackTrace();
            im = null;
        }

        setTitle("Felica出席管理プログラム");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(200, 200, 800, 370);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Felica出席管理プログラム");
        label.setFont(new Font("msgothic.ttc", Font.PLAIN, 30));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);

        JLabel labelForExplanation = new JLabel("ルームを作成するか5桁の数字のROOMIDを入力してください");
        labelForExplanation.setFont(new Font("msgothic.ttc", Font.PLAIN, 20));
        labelForExplanation.setHorizontalAlignment(SwingConstants.CENTER);
        labelForExplanation.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(labelForExplanation);

        panel.add(new JLabel(" "));

        JLabel labelForChoice = new JLabel("ROOMIDを入力してください");
        labelForChoice.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(labelForChoice);

        this.jTextField = new JTextField("");
        this.jTextField.setFont(new Font("msgothic.ttc", Font.PLAIN, 30));
        this.jTextField.setPreferredSize(new Dimension(100, 40));
        this.jTextField.setHorizontalAlignment((int) Component.CENTER_ALIGNMENT);
        this.jTextField.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(this.jTextField);

        panel.add(new JLabel(" "));


        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout());

        this.labelToShowFilePath = new JLabel("ルームを作成したいときは右のボタンを押してください→");
        panel1.add(this.labelToShowFilePath);

        JButton btnToChooseFile = new JButton("作成");
        btnToChooseFile.addActionListener(this);
        btnToChooseFile.setActionCommand("make room");
        panel1.add(btnToChooseFile);

        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(panel1);

        panel.add(new JLabel(" "));

        this.labelForMsg = new JLabel(" ");
        this.labelForMsg.setFont(new Font("msgothic.ttc", Font.BOLD, 20));
        this.labelForMsg.setHorizontalAlignment(SwingConstants.CENTER);
        this.labelForMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.labelForMsg.setForeground(Color.RED);
        panel.add(this.labelForMsg);

        panel.add(new JLabel(" "));

        JButton button = new JButton("　　　　　決定　　　　　");
        button.addActionListener(this);
        button.setActionCommand("decide");
        panel.add(button);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(new JLabel(" "));

        getContentPane().add(panel);
    }

    public static void main(String[] args) {
        StartFrame start = new StartFrame();
        start.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "decide":
                if (this.jTextField.getText().equals("")) {
                    JOptionPane.showInternalMessageDialog(this.getContentPane(), "ROOMIDが空欄になっています", "エラー", JOptionPane.ERROR_MESSAGE);
                } else {
                    boolean isInt = checkInt(this.jTextField.getText());
                    if (isInt) {
                        int inputRoomId = Integer.parseInt(this.jTextField.getText());
                        if (this.roomIdsList.contains(inputRoomId)) {
                            //Mainインスタンスを生成する
                            Main main=new Main(inputRoomId);
                            main.setVisible(true);
                            dispose();
                        } else {
                            JOptionPane.showInternalMessageDialog(this.getContentPane(), "入力したROOMIDは存在しません。", "エラー", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showInternalMessageDialog(this.getContentPane(), "入力したROOMIDは数字ではありません。", "エラー", JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;


            case "make room":
                boolean isContinue = true;
                int newRoomId = 0;
                while (isContinue) {
                    Random random = new Random();
                    newRoomId = random.nextInt(89999) + 10000;
                    if (!this.roomIdsList.contains(newRoomId)) {
                        isContinue = false;
                    }
                }
                try {
                    HttpUtil.sendHttpRequest("POST", "https://felica-attendance-manager.azurewebsites.net/api/rooms/makeRoom?roomId=" + newRoomId);

                } catch (Exception ex) {
                    System.out.println("エラーがおこった");
                    //TODO:ここのエラー処理を実装
                    ex.printStackTrace();
                }
                this.jTextField.setText(String.valueOf(newRoomId));
                this.roomIdsList.add(newRoomId);
                break;
        }
    }

    public boolean checkInt(String text) {
        boolean isInt = true;

        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                isInt = false;
                break;
            }
        }
        return isInt;
    }
}

