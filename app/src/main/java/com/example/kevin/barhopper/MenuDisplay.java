package com.example.kevin.barhopper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

        pics = (ArrayList<byte[]>) getIntent().getSerializableExtra("pics");
     //   pic = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        createUI();

    }

    public void createUI() {
       // ImageView[] pics = new ImageView[1];
        Bitmap[] bPics = new Bitmap[pics.size()];


        for (int i = 0; i < pics.size(); i++) {
            bPics[i] = BitmapFactory.decodeByteArray(pics.get(i), 0, pics.get(i).length);
        }


        imageDisplayArrayAdapter adapter = new imageDisplayArrayAdapter(this, bPics);

        ListView listItem = (ListView) findViewById(R.id.list_item);
        listItem.setAdapter(adapter);
    }
}
