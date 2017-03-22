package com.example.kevin.barhopper;

import android.media.Rating;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    public void createUI(Place p) {
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


    }
}
