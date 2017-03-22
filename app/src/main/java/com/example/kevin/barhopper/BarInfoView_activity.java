package com.example.kevin.barhopper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.location.places.Place;

public class BarInfoView_activity extends AppCompatActivity {
    private Place origP = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_info_view_activity);

        // Get Place info of where the click originated from
        origP = (Place) getIntent().getParcelableExtra("com.example.kevin.barhopper.ListViewActivity.PLACE");
        System.out.println(origP.getName());

        createUI(origP);
    }

    public void createUI(Place p) {

    }
}
