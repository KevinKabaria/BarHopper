package com.example.kevin.barhopper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MenuDisplay extends AppCompatActivity {
    ArrayList<byte[]> pics = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_display);

        System.out.println("Opened Display");

        pics = (ArrayList<byte[]>) getIntent().getSerializableExtra("pics");
     //   pic = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        System.out.println("Creating UI");
        createUI();

    }

    public void createUI() {

        ListView listItem = (ListView) findViewById(R.id.list_item);
        System.out.println("Starting!");

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Starting Thread");

                // ImageView[] pics = new ImageView[1];
                Bitmap[] bPics = new Bitmap[pics.size()];


                for (int i = 0; i < pics.size(); i++) {

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;

                    Bitmap image = BitmapFactory.decodeByteArray(pics.get(i), 0, pics.get(i).length, options);


                    bPics[i] = image;


                    System.out.println(bPics[i].getConfig());

                    /*

                    int width = bPics[i].getWidth();
                    int height = bPics[i].getHeight();
                    int[] pixels = new int[width*height];
                    bPics[i].getPixels(pixels, 0, width, 0, 0, width, height);

                    Bitmap dest = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    dest.setPixels(pixels, 0, width, 0, 0, width, height);
                    bPics[i] = dest;
                    */

                }




                System.out.println("Done with Array!");
                final imageDisplayArrayAdapter adapter = new imageDisplayArrayAdapter(MenuDisplay.this, bPics);

                System.out.println("About to run on UI");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ListView listItem = (ListView) findViewById(R.id.list_item);
                        listItem.setAdapter(adapter);
                        System.out.println("DONE!");
                    }
                });

            }
        }).start();

    }
}
