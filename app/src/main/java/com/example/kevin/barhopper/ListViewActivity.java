package com.example.kevin.barhopper;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class ListViewActivity extends AppCompatActivity implements OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private double curLat = -1;
    private double curLong = -1;

    private double lastLat = 0;
    private double lastLong = 0;

    private double locLat = -1;
    private double locLong = -1;

    private static final int LOCATION_REQUEST=5;
    private static final int BACKGROUND_POLLMS=800;
    private static final int GPS_POLL_RATE=30000;
    private static final int GPS_MIN_MOVE=5;

    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    // Amount of buttons we would like to display
    private int maxSize = 1000;

    // Swipe to refresh
    private SwipeRefreshLayout swipeContainer;

    // The autocomplete bar
    private PlaceAutocompleteFragment autocompleteFragment;
    private String phoneNum;



    // Create data structure to store butotns
    public class ListViewButton {
        private String name;
        private String distance;
        private Place p;

        public ListViewButton(String name, String distance, Place p) {
            this.name = name;
            this.distance = distance;
            this.p = p;
        }

        public String getName() {
            return this.name;
        }

        public String getDistance() {
            return this.distance;
        }

        public Place getPlace() {
            return this.p;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        // Get current identity
        phoneNum = StoreDataLocally.readFromFile("store_data", ListViewActivity.this).toString();
        // make it usable

        // Handles our swipe to refresh
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handleSwipeRefresh();
            }
        });

        PermissionManager.check(this, android.Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_REQUEST);
        maintainGPS();

        // Create new google API client. API key stored in manifest
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        recurringCordUpdate();

        // Set initial display to be home
        curLat = locLat;
        curLong = locLong;


    }

    // When placed a place, sets current search coords there.
    public void setCurDisplayCoords(Place place) {
        curLat = place.getLatLng().latitude;
        curLong = place.getLatLng().longitude;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                System.out.println("Place: " + place.getName());

                setCurDisplayCoords(place);

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                System.out.println(status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }



    public void recurringCordUpdate() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                while(true) {
                    getAreaInfo();
                    try {
                        Thread.sleep(BACKGROUND_POLLMS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void handleCurLocation(Location location) {
        locLat = location.getLatitude();
        locLong = location.getLongitude();
        System.out.println("CURRENT LOCATION! " + locLat + " " + locLong);



        // Contact Google Cloud Function, store current positioning
        // Format: phone num;lat;long
        final String postData = phoneNum + ";" + Double.toString(locLat) + ";" + Double.toString(locLong);

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    JSONObject parent = new JSONObject();
                    parent.put("text", postData);
                    URL url = new URL("https://us-central1-silent-wharf-151102.cloudfunctions.net/RegisterLocation");
                    String response = InternetConnect.sendPost(url, parent);
                    System.out.println(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    // Called when swipe-pulldown
    // We just update curLat/Long to locLat
    public void handleSwipeRefresh() {
        System.out.println("CURRENT LOCATION! " + locLat + " " + locLong);
        curLat = locLat;
        curLong = locLong;

        // Gotta end the refreshing
        swipeContainer.setRefreshing(false);
     //   autocompleteFragment.setText("");


    }


    // Receive GPS updates:
    @SuppressWarnings("MissingPermission")
    public void maintainGPS() {

        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //Called when a new location is found by network location providers
                handleCurLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

        };

        System.out.println("Establishing..");
        //noinspection MissingPermission

        Location lastLocation;

        PermissionManager.check(this, android.Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_REQUEST);
        if ((lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) == null) {
            lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (lastLocation != null) {
            handleCurLocation(lastLocation);
        }
        // 10k ms, a min movement of 5 meters.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_POLL_RATE, GPS_MIN_MOVE, locationListener);


        System.out.println("Established Link!");


    }



    // Provide a function to update interface from a separate thread.
    // Accepts an ArrayList of Places
    public void updateResults(final ArrayList<Place> estab) {

        // Set amount of buttons to max size if more.
        int length = estab.size();

        // Create an offset for the first button being Search
        length = length + 1;


        ListViewButton[] buttonInfo = new ListViewButton[length];



        for (int i = 1; i < length; i++) {
            // Retrieve name information for respective place.

            int estabOffset = i-1;

            Place p = estab.get(estabOffset);

            CharSequence placeName = p.getName();

            // Place lat and long
            double toLat = p.getLatLng().latitude;
            double toLong = p.getLatLng().longitude;

            // Retrieve shortest distance to place (straight path)
            double distance = CalcFunction.calculateDistanceInMile(curLat, curLong, toLat, toLong);

            // While accuracy is imprecise, set to vague wording
            String distanceTo = "<5 miles";

            if (distance < 1.0) {
                distanceTo = "<1 Mile";
            }

            buttonInfo[i] = new ListViewButton(placeName.toString(), distanceTo, p);
        }


        ListView linearLayout = (ListView) findViewById(R.id.vertListing);
        System.out.println(buttonInfo.length);
        final listbuttonArrayAdapter adapter = new listbuttonArrayAdapter(this, buttonInfo);


        linearLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("CLICKED!");
                System.out.println(parent.findViewById(R.id.bigText));

            }
        });

        linearLayout.setAdapter(adapter);

        System.out.println("display!");

    }

    // Passes in current location, receives information about venues in area
    // Sends that venue information in form of Place ArrayList in order to create UI
    final public void getAreaInfo() {
        // Have to perform Network Ops on another thread
        // But design is that it is called from another thread

        // Check if an unecessary redundant call.
        if (((lastLat == curLat) && (lastLong == curLong)) ||
                ((curLat == -1) || (curLong == -1))) {
            return;
        }

        lastLat = curLat;
        lastLong = curLong;


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

        System.out.println("LAT: " + sLat + " " + "LONG: " + sLong);

        if (response == null || response.equals("")) {
            System.out.println("No venues in area");
            return;
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


}
