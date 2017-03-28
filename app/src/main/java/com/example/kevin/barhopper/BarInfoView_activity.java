package com.example.kevin.barhopper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.media.Rating;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class BarInfoView_activity extends AppCompatActivity {
    private Place origP = null;
    File cacheDir = null;
    private static final int  MEGABYTE = 1024 * 1024;



    private class DownloadInfo extends AsyncTask<Place, Integer, File> {

        @Override
        protected File doInBackground(Place... pA) {
            Place p = pA[0];
            try {
                JSONObject parent = new JSONObject();
                System.out.println("LOCALE: " + p.getLocale());
                parent.put("text", p.getWebsiteUri().toString()+";"+"menu"+";"+p.getLocale());
                URL url = new URL("https://us-central1-silent-wharf-151102.cloudfunctions.net/GetBarInfo");
                String response = InternetConnect.sendPost(url, parent);
                System.out.println(response);

                String result = response;
                File file = File.createTempFile("testtest", null);

                url = new URL(result);
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                //urlConnection.setRequestMethod("GET");
                //urlConnection.setDoOutput(true);
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                int totalSize = urlConnection.getContentLength();

                byte[] buffer = new byte[MEGABYTE];
                int bufferLength = 0;
                while((bufferLength = inputStream.read(buffer))>0 ){
                    fileOutputStream.write(buffer, 0, bufferLength);
                }
                fileOutputStream.close();


                return file;
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(File file) {



            ImageView description = (ImageView) findViewById(R.id.descriptionText);
            ParcelFileDescriptor fileDescriptor = null;
            try {
                fileDescriptor = ParcelFileDescriptor.open(
                        file, ParcelFileDescriptor.MODE_READ_ONLY);

                PdfRenderer pdfRenderer = null;
                pdfRenderer = new PdfRenderer(fileDescriptor);

                PdfRenderer.Page rendererPage = pdfRenderer.openPage(0);
                int rendererPageWidth = rendererPage.getWidth();
                int rendererPageHeight = rendererPage.getHeight();
                Bitmap bitmap = Bitmap.createBitmap(
                        rendererPageWidth,
                        rendererPageHeight,
                        Bitmap.Config.ARGB_8888);
                rendererPage.render(bitmap, null, null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                description.setImageBitmap(bitmap);
                rendererPage.close();
                pdfRenderer.close();
                fileDescriptor.close();



            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_info_view_activity);

        // Get Place info of where the click originated from
        origP = (Place) getIntent().getParcelableExtra("com.example.kevin.barhopper.ListViewActivity.PLACE");
        System.out.println(origP.getName());
        cacheDir = getCacheDir();

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
      //  price.setText(priceBuilder);
        price.setText(p.getWebsiteUri().toString());
      //  final TextView description = (TextView) findViewById(R.id.descriptionText);
      //  description.setText(descriptionS);


        Button mapButton = (Button) findViewById(R.id.mapButton);
        mapButton.setText("Show me on map!");
        mapButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                bringMap(p);
            }
        });

        new DownloadInfo().execute(p);

    }

    public void setDescription(String text) {
        //final TextView description = (TextView) findViewById(R.id.descriptionText);
        //description.setText(text);

    }

    public void getMenu(final String barURL, final Place p) {

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
