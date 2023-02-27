import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import javazoom.jl.decoder.JavaLayerException;

import javax.swing.*;
import java.io.*;


public class FelicaReader implements Runnable {
    final public static short WILDCARD = (short) 0xFE00;        // ワイルドカード
    final public static short SUICA = 0x03;
    final private Main main;
    Pointer pasoriHandle;
    Pointer felicaHandle;
    String studentNum;
    String idm;

    public FelicaReader(Main main) throws NoFoundReaderException {
        this.main = main;
        this.pasoriHandle = FelicaLib.INSTANCE.pasori_open(null);
        if (this.pasoriHandle == null) {
            throw new NoFoundReaderException("felicalib.dllを開けません");
        }
        if (FelicaLib.INSTANCE.pasori_init(this.pasoriHandle) != 0) {
            throw new NoFoundReaderException("PaSoRiに接続できません");
        }
    }

    public String getIdm(short systemCode) throws FelicaException {
        String returnIdm;
        FelicaLib.INSTANCE.felica_free(this.felicaHandle);
        this.felicaHandle = FelicaLib.INSTANCE.felica_polling(this.pasoriHandle, systemCode, (byte) 0, (byte) 0);
        if (this.felicaHandle == Pointer.NULL) {
            throw new FelicaException("カード読み取り失敗");
        }
        byte[] buf = new byte[8];
        FelicaLib.INSTANCE.felica_getidm(this.felicaHandle, buf);

        StringBuilder stringBuilder = new StringBuilder();

        for (byte b : buf) {
            String hex = Integer.toHexString(b);
            if (hex.length() == 1) {
                // 1文字の場合前に0を付加
                stringBuilder.append("0");
            }
            if (hex.length() > 2) {
                // 3文字以上の場合は最後の2文字のみ使用
                hex = hex.substring(hex.length() - 2);
            }
            stringBuilder.append(hex);
        }
        //DEBUG:ここでの値をcheck
        System.out.println("IDM:" + stringBuilder.toString());
        return stringBuilder.toString();
    }

    public void close() {
        if (felicaHandle != Pointer.NULL) {
            FelicaLib.INSTANCE.felica_free(felicaHandle);
        }
        if (pasoriHandle != Pointer.NULL) {
            FelicaLib.INSTANCE.pasori_close(pasoriHandle);
        }
    }

    @Override
    public void run() {
        while (true) {
            // Felicaカードの読み取りループ
            try {
                this.idm = getIdm(FelicaReader.WILDCARD);

                if (!this.idm.equals("00000000000")) {
                    if (!this.main.idmField.getText().equals(this.idm)) {
                        this.main.idmField.setText(this.idm);
                        this.main.process(this.idm);
                    }
                } else {
                    try (InputStream is = new FileInputStream(new File("badMusic.mp3"))) {
                        this.main.play(is);
                    } catch (FileNotFoundException fileNotFoundException) {
                        System.out.println("fileNotFoundException");
                    } catch (JavaLayerException javaLayerException) {
                        System.out.println("unsupportedAudioException");
                    } catch (IOException ioexception) {
                        ioexception.printStackTrace();
                    }
                    JOptionPane.showMessageDialog(this.main.getContentPane(), "ICカードではないか、正しく読み取りができていません。", "エラー", JOptionPane.ERROR_MESSAGE);
                }


            } catch (FelicaException e) {
                this.studentNum = "";
            }
            try {
                Thread.sleep(500); // 0.5秒おきに読み取り
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public interface FelicaLib extends Library {
        FelicaLib INSTANCE = (FelicaLib) Native.loadLibrary("felicalib",
                FelicaLib.class);

        Pointer pasori_open(String dummy);

        int pasori_init(Pointer pasoriHandle);

        void pasori_close(Pointer pasoriHandle);

        Pointer felica_polling(Pointer pasoriHandle, short systemCode, byte rfu, byte time_slot);

        void felica_free(Pointer felicaHandle);

        void felica_getidm(Pointer felicaHandle, byte[] data);

        void felica_getpmm(Pointer felicaHandle, byte[] data);

        int felica_read_without_encryption02(Pointer felicaHandle, int serviceCode, int mode, byte addr, byte[] data);
    }
}





