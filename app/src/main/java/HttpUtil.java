import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpUtil {
    public static String sendHttpRequest(String method, String url) throws IOException {
        String returnStr = null;
        try {
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod(method);
            StringBuilder result = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader((new BufferedReader(new InputStreamReader(connection.getInputStream()))))) {
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }
            }
            returnStr = result.toString();
        } catch (IOException e) {
            throw new IOException();
        }
        return returnStr;
    }
}
