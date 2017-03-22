package com.example.kevin.barhopper;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
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

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class ListViewActivity extends AppCompatActivity implements OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private double curLat = 40.750568;
    private double curLong = -73.993519;

    // Amount of buttons we would like to display
    private int maxSize = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        // Create new google API client. API key stored in manifest
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


    // Create a series of buttons representing bars in vincinity
    // Assigns buttons IDs from 0 to num-1, where num is amount of buttons to be displayed
    public void createUI(int num) {
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.vertical_layout, null, false);

        for (int i = 0; i < num; i++) {
            // Creates button as a linear layout from XML template
            LinearLayout button = (LinearLayout)  inflater.inflate(R.layout.listview_button_layout, linearLayout, false);

            // Assigns incremental ID to button
            // Possible issue: uniqueness
            button.setId(i);

            // Add to master layout
            linearLayout.addView(button);
        }

        // Create parameters for master layout.
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);

        // Add master layout to view-screen
        this.addContentView(linearLayout, layoutParams);

    }

    // When button is clicked, handle
    public void handleClick(int id, Place p) {
        System.out.println("Name!: " + p.getName());

        // Create new intent and activate it.
        // Pass in Place p via Parcelable
        Intent intent = new Intent(this, BarInfoView_activity.class);
        intent.putExtra("com.example.kevin.barhopper.ListViewActivity.PLACE", (Parcelable) p);
        startActivity(intent);
    }


    // When provided a LinearLayout and ID of child, return matching child.
    // If no children with matching ID found, return NULL.
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
    // Accepts an ArrayList of Places
    public void updateResults(final ArrayList<Place> estab) {
        // Set amount of buttons to max size if more.
        int length = estab.size();
        if (length > maxSize) {
            length = maxSize;
        }

        // Initialize buttons for UI
        createUI(length);

        // Iterate through received Place values, getting information for button
        for (int i = 0; i < length; i++) {
            LinearLayout button = (LinearLayout) findViewById(i);

            // Retrieve name information for respective place.
            CharSequence placeName = estab.get(i).getName();

            // Place lat and long
            double toLat = estab.get(i).getLatLng().latitude;
            double toLong = estab.get(i).getLatLng().longitude;

            // Retrieve shortest distance to place (straight path)
            double distance = UsefulMathCalculations.calculateDistanceInMile(curLat, curLong, toLat, toLong);

            // While accuracy is imprecise, set to vague wording
            String distanceTo = "<5 miles";

            if (distance < 1.0) {
                distanceTo = "<1 Mile";
            }
            // Set values of the Big and Small texts for each button
            TextView textView = (TextView) getChildOfLayout(button, R.id.bigText);
            textView.setText(placeName);

            TextView textView2 = (TextView) getChildOfLayout(button, R.id.smallText);
            textView2.setText(distanceTo);


            // For each button, send onClick characteristics
            final int finalI = i;
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    handleClick(finalI, estab.get(finalI));
                }
            });
        }
    }

    // Passes in current location, receives information about venues in area
    // Sends that venue information in form of Place ArrayList in order to create UI
    public void getAreaInfo() {
        // Have to perform Network Ops on another thread
        new Thread(new Runnable(
        ) {
            @Override
            public void run() {
                String sLat = Double.toString(curLat);
                String sLong = Double.toString(curLong);

                // Format for Google Cloud Function Call
                String latLng = sLat + "," + sLong;

                String response = "";

                // Contact Google Cloud Function, receive string of IDs
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

                // Split by new line delimiter into array of ids
                String[] ids = response.split("\n");

                // Create array list of places, convert each ID to Place info
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

                // Build UI and perform actions off of place info
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateResults(places);
                    }
                });
             }
        }).start();
    }
}
