package com.example.kevin.barhopper;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import static android.graphics.Color.CYAN;
import static android.graphics.Color.RED;

public class SignUp extends AppCompatActivity {

    String ageVal = "";
    public static final int REQUEST_CODE_FOR_SMS=1;
    private String phoneNumber = "";

    SmsListener mReceiver = new SmsListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_sign_up);

        // Must Request SMS Permissions
        PermissionManager.check(this, Manifest.permission.RECEIVE_SMS, REQUEST_CODE_FOR_SMS);
        // Register App with SMS receiving
        getApplicationContext().registerReceiver(mReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));

        // Set up the siwtch
        Switch gender_select = (Switch) findViewById(R.id.gender);
        Drawable track = gender_select.getTrackDrawable();
        track.setColorFilter(CYAN, PorterDuff.Mode.ADD);

        // Set up age selection
        ArrayAdapter<CharSequence> ages = ArrayAdapter.createFromResource(this, R.array.age_groups, android.R.layout.simple_spinner_item);
        ages.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner age_groups = (Spinner) findViewById(R.id.age_select);

        age_groups.setAdapter(ages);

       // final String SOME_ACTION = "com.example.kevin.barhopper.SmsListener";
      //  IntentFilter intentFilter = new IntentFilter(SOME_ACTION);






    }

    /*
    age_groups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ageVal = age_groups.getSelectedItem().toString();
            }
        });
     */


    public void switchSlide(View view) {
        TextView textView = (TextView) findViewById(R.id.Signup_Here);
        Switch gender_select = (Switch) findViewById(R.id.gender);
        Drawable track = gender_select.getTrackDrawable();

        if (gender_select.isChecked()) {
            gender_select.setText("Female");
            track.setColorFilter(RED, PorterDuff.Mode.ADD);

        } else {
            gender_select.setText("Male");
            track.setColorFilter(CYAN, PorterDuff.Mode.ADD);
        }
    }

    public String collectInfo() {
        String parent = "";

        Switch gender_select = (Switch) findViewById(R.id.gender);
        String gender = (String) gender_select.getText();


        EditText phoneNum = (EditText) findViewById(R.id.phone_number);
        String phone = phoneNum.getText().toString();
        phoneNumber = phone;

        final Spinner age_groups = (Spinner) findViewById(R.id.age_select);
        ageVal = age_groups.getSelectedItem().toString();

        System.out.println("PHONE NUMBER: " + phone + " Gender: " + gender + " Age: " + ageVal);




        parent = (phone + ";" + gender + ";" + ageVal);

        return parent;
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode==REQUEST_CODE_FOR_SMS){//response for SMS permission request
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                //What to do if User allowed SMS permission
            }else{
                //What to do if user disallowed requested SMS permission
            }
        }
    }



    public void sendInfo(View view) {
        Button submitB = (Button) findViewById(R.id.submit);

        final String info = collectInfo();
        JSONObject parent = new JSONObject();
        try {
            parent.put("text",info);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final JSONObject infoParent = parent;
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL("https://us-central1-silent-wharf-151102.cloudfunctions.net/SMSer");
                    InternetConnect.sendPost(url, infoParent);

                    for (int i = 0; i < 120; i++) {
                        System.out.println("IN SEND");
                        if (mReceiver.receivedMessage != "") {

                            String message = mReceiver.receivedMessage;
                            System.out.println("IN SEND" + message);
                            if (message.substring(0, 5).equals("Bhop:")) {
                                String hash = message.substring(5);
                                JSONObject hashCollector = new JSONObject();

                                String compositePayload = hash + ";" + info;


                                hashCollector.put("text", compositePayload);


                                url = new URL("https://us-central1-silent-wharf-151102.cloudfunctions.net/HashResponseStore");

                                InternetConnect.sendPost(url, hashCollector);
                                mReceiver.receivedMessage = "";

                                // Save phone number to shared preferences for lookup
                               /* SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("number", phoneNumber);
                                editor.commit(); */
                               StoreDataLocally.writeToFile("store_data", phoneNumber, SignUp.this);

                                Intent intent = new Intent(SignUp.this, LaunchPageActivity.class);
                                startActivity(intent);
                               // System.out.println(sharedPreferences.getString("number", "NOT THERE"));

                                break;



                            } else {
                                mReceiver.receivedMessage = "";
                                SystemClock.sleep(500);
                            }

                        } else {
                            mReceiver.receivedMessage = "";
                            SystemClock.sleep(500);
                        }

                    }


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                /*
                try {

                    String data = URLEncoder.encode("something", "UTF-8") + "=" + URLEncoder.encode("Kevin", "UTF-8");


                    URL url = new URL("https://us-central1-silent-wharf-151102.cloudfunctions.net/SMSer");
                    URLConnection conn = url.openConnection();
                    conn.setDoOutput(true);

                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(data);

                    wr.flush();
                    wr.close();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            */
            }

        }).start();




    }
}
