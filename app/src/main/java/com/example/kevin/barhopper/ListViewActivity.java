package com.example.kevin.barhopper;

import android.app.ActionBar;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.vision.text.Text;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class ListViewActivity extends AppCompatActivity implements OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private double curLat = 40.298069;
    private double curLong = -74.678407;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        getAreaInfo();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    // Create a series of buttons
    public void createUI(int num) {
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.vertical_layout, null, false);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
       // linearLayout.setId(R.id.layout_1);


        for (int i = 0; i < num; i++) {

            LinearLayout button = (LinearLayout)  inflater.inflate(R.layout.listview_button_layout, linearLayout, false);
            button.setId(i);

         //   TextView textView = (TextView) inflater.inflate(R.layout.text_layout, linearLayout, false);
         //   textView.setId(i);

            // button.addView(text);

           // TextView textView = (TextView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.text_layout, linearLayout, false);

         //   textView.setText("TESTESTESTSTETEST");
         //   textView.setVisibility(View.VISIBLE);
           // button.setLayoutParams(R.layout.listview_button_layout);
            linearLayout.addView(button);
         //   linearLayout.addView(textView);

        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        //this.addContentView(linearLayout);
        this.addContentView(linearLayout, params);

    }

    public void handleClick(int id, Place p) {
        System.out.println("Name!: " + p.getName());

    }

    public View getChildOfLayout(LinearLayout layout, int ID) {
        int n = layout.getChildCount();
        for (int i = 0; i < n; i++) {
            View child = layout.getChildAt(i);
            if (child.getId() == ID) {
                return child;
            }
        }

        return null;
    }


    // Provide a function to update interface from a separate thread.
    public void updateResults(final ArrayList<Place> estab) {
        int maxSize = 9;
        int length = estab.size();
        if (length > maxSize) {
            length = maxSize;
        }
        createUI(length);

        for (int i = 0; i < length; i++) {
            LinearLayout display1 = (LinearLayout) findViewById(i);

            // Retrieve name information for respective place.
            CharSequence placeName = estab.get(i).getName();

            // Place lat and long
            double toLat = estab.get(i).getLatLng().latitude;
            double toLong = estab.get(i).getLatLng().longitude;

            // Retrieve shortest distance to place (straight path)
            double distance = UsefulMathCalculations.calculateDistanceInMile(curLat, curLong, toLat, toLong);

           // String distanceTo = Double.toString(distance);
            String distanceTo = "<5 miles";

            if (distance < 1.0) {
                distanceTo = "<1 Mile";
            }

           // String buttonText = "<h1><span style=\"font-size: 12pt;\"><strong>" +
                  //  placeName +"</strong></span></h1><h5><span style=\"font-size: 8pt;\">&lt;"
                   // +distanceTo + "</span></h5>";

            String buttonText = placeName + "\n" + distanceTo;



            TextView textView = (TextView) getChildOfLayout(display1, R.id.big1);
            textView.setText(placeName);

            TextView textView2 = (TextView) getChildOfLayout(display1, R.id.small);
            textView2.setText(distanceTo);



         //   display1.setText(buttonText);
/*
            TextView text1 = (TextView) display1.
            text1.setText(placeName);

            TextView text2 = (TextView) display1.getChildAt(1);
            text2.setText(distanceTo);

          */
            System.out.println("BUTTON: "+ i);

            final int finalI = i;

            display1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    handleClick(finalI, estab.get(finalI));
                }
            });

        }
        System.out.println("DONE WITH NAMING!");


    }

    public void getAreaInfo() {
        new Thread(new Runnable(


        ) {
            @Override
            public void run() {

                String sLat = Double.toString(curLat);
                String sLong = Double.toString(curLong);

                String latLng = sLat + "," + sLong;


                String response = "";

                try {
                    JSONObject parent = new JSONObject();
                    parent.put("text", latLng);
                    URL url = new URL("https://us-central1-silent-wharf-151102.cloudfunctions.net/SearchLocations");
                    response = InternetConnect.sendPost(url, parent);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                String[] ids = response.split("\n");

              //  for (String s : ids) {
                //    System.out.println("responseID: " + s);
                //}



                final ArrayList<Place> places = new ArrayList<Place>();
                for (String id : ids) {

                    PendingResult<PlaceBuffer> result =
                            Places.GeoDataApi.getPlaceById(mGoogleApiClient, id);

                    // Blocking Call!
                    PlaceBuffer place = result.await();

                    // The place object is in spot 0
                    // Freeze so release won't remove
                    // Increased memory usage, but allows for caching
                    places.add(place.get(0).freeze());

                    // Release buffer so no memory leak
                    place.release();

                }





                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateResults(places);
                    }
                });







            /*
                LatLng southWest = new LatLng(40.315, -74.68935);
                LatLng northEast = new LatLng(40.389636, -74.583778);



                LatLngBounds princeton = new LatLngBounds(southWest, northEast);
                AutocompleteFilter filter = new AutocompleteFilter.Builder()
                        .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                        .build();

                // Need to release when done
                PendingResult<AutocompletePredictionBuffer> result =
                        Places.GeoDataApi.getAutocompletePredictions(
                                mGoogleApiClient,
                                "Newark Airport",
                                princeton,
                                filter);

                // Blocks until task complete
                AutocompletePredictionBuffer places = result.await();
                for (AutocompletePrediction p : places) {
                    System.out.println(p.getFullText(null));
                    System.out.println(p.getPlaceTypes());
                }
                // Release
                places.release();
                */

                System.out.println("DONE!");







            }
        }).start();
    }
}
