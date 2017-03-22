package com.example.kevin.barhopper;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by kevin on 3/20/17.
 */

public class InternetConnect {
    public static String sendPost(URL url, JSONObject parent) {
        StringBuilder builder = new StringBuilder();
        try {

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoOutput(true);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");


            // OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            OutputStream os = conn.getOutputStream();
            os.write(parent.toString().getBytes("UTF-8"));
            os.flush();
            os.close();


            InputStream in = new BufferedInputStream(conn.getInputStream());

            for (int c; (c = in.read()) >= 0; )
                builder.append((char) c);

            in.close();
            conn.disconnect();



        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}
