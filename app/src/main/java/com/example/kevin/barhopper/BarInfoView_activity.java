package com.example.kevin.barhopper;

import android.content.Intent;
import android.media.Rating;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

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

    public void createUI(final Place p) {
        CharSequence titleS = p.getName();
        float ratingF = p.getRating();
        int priceI = p.getPriceLevel();
        CharSequence descriptionS = p.getAddress();

        TextView title = (TextView) findViewById(R.id.titleText);
        title.setText(p.getName());

        RatingBar rateBar = (RatingBar) findViewById(R.id.ratingBar);
        rateBar.setRating(ratingF);
        System.out.println("RATING!: " + ratingF);

        TextView price = (TextView) findViewById(R.id.priceText);
        String priceBuilder = "";
        System.out.println("Price!: " + priceI);

        for (int i = 0; i < priceI; i++) {
            priceBuilder += "$";
        }
        price.setText(priceBuilder);

        TextView description = (TextView) findViewById(R.id.descriptionText);
        description.setText(descriptionS);


        Button mapButton = (Button) findViewById(R.id.mapButton);
        mapButton.setText("Show me on map!");
        mapButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                bringMap(p);
            }
        });




    }

    public void bringMap(Place p) {
        String placeLat = Double.toString(p.getLatLng().latitude);
        String placeLong = Double.toString(p.getLatLng().longitude);

        // Create string for what we want google maps to do
        String place = "geo:"+placeLat+","+placeLong+"?q=" + p.getName() + ", " + p.getAddress();

        // Preprocess string for edge cases like & and US-1 that don't show up
        place = preprocName(place);

        System.out.println(place);
        Uri gmapsURI = Uri.parse(place);

        // check if some kind of mapping application is installed
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmapsURI);
        startActivity(mapIntent);
    }

    public String preprocName(String name) {
        String returnName = name;
        if (name.contains("&")) {
            returnName = name.replace("&","and");
        }

        if (name.contains("US-")) {
            returnName = name.replace("US-", "U.S. ");
        }

        return returnName;
    }
}
