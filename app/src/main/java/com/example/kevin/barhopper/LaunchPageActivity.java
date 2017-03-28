package com.example.kevin.barhopper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class LaunchPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkUser();
        setContentView(R.layout.activity_launch_page);

    }

    private void switchToSignup() {
        Intent intent = new Intent(LaunchPageActivity.this, SignUp.class);
        startActivity(intent);
    }

    private void goToMain(String gender, String age) {
        Intent intent = new Intent(LaunchPageActivity.this, ListViewActivity.class);
        startActivity(intent);
    }

    private void checkUser() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject parent = new JSONObject();

                String phoneNum = StoreDataLocally.readFromFile("store_data", LaunchPageActivity.this);
                System.out.println("FORM SHARED PHONE" + phoneNum);
                if (phoneNum.equals("NONE")) {
                    switchToSignup();
                } else {
                    try {
                        parent.put("text", phoneNum);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        URL url = new URL("https://us-central1-silent-wharf-151102.cloudfunctions.net/Confirmuser");

                        // Resp in format: Gender;Age
                        String resp = InternetConnect.sendPost(url, parent);
                        System.out.println("RESPONSE FOR USER: " + resp);

                        // Not in DATABASE!
                        if (resp.contains("no such entity")) {
                            switchToSignup();
                            return;
                        }

                        int gendDivider = resp.indexOf(";");
                        String gender = resp.substring(0, gendDivider);
                        String age = resp.substring(gendDivider+1);
                        goToMain(gender, age);


                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }
}
