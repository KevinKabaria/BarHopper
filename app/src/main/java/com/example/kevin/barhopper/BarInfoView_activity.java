package com.example.kevin.barhopper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.media.Rating;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BarInfoView_activity extends AppCompatActivity {
    private Place origP = null;
    File cacheDir = null;
    private static final int  MEGABYTE = 1024 * 1024;
    private Context context;



    private class DownloadInfo extends AsyncTask<Place, Integer, File> {

        @Override
        protected File doInBackground(Place... pA) {
            Place p = pA[0];
            try {
                JSONObject parent = new JSONObject();
                String address = (String) p.getAddress();

                String city = CalcFunction.extractCity(address);


                parent.put("text", p.getWebsiteUri().toString()+";"+"menu"+";"+city);
                URL url = new URL("https://us-central1-silent-wharf-151102.cloudfunctions.net/GetBarInfo");

                // The url of the pdf file
                String result = InternetConnect.sendPost(url, parent);
                System.out.println(result);


                File file;

                // If the result is a pdf, create file named pdfDownload
                String fileName = "pdfDownload";



                if (result.contains("barhopper")) {
                    System.out.println("image file from google!");
                    fileName = "imageFile";
                   // file = File.createTempFile(fileName, null);
                    //file = new File(context.getFilesDir(), fileName);
                }


                file = File.createTempFile(fileName, null);


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


            if (file == null) {
                return;
            }

            if (file.getName().contains("pdf")) {
                renderPdfFromFile(file);
            } else {
                displayImage(file);
            }


        }

        public void displayImage(File file) {
            ImageView description = (ImageView) findViewById(R.id.descriptionText);

            System.out.println(file.getPath());
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());


            System.out.println(bitmap.getByteCount());
            description.setImageBitmap(bitmap);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            final ArrayList<byte[]> images = new ArrayList<byte[]>();
            images.add(stream.toByteArray());


            description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MenuDisplay.class);

                    intent.putExtra("pics", images);
                    startActivity(intent);
                }
            });

        }


        public void renderPdfFromFile(File file) {
            ImageView description = (ImageView) findViewById(R.id.descriptionText);
            ParcelFileDescriptor fileDescriptor = null;


            try {

                fileDescriptor = ParcelFileDescriptor.open(
                        file, ParcelFileDescriptor.MODE_READ_ONLY);

                PdfRenderer pdfRenderer = null;
                pdfRenderer = new PdfRenderer(fileDescriptor);

                Bitmap[] bitmaps = new Bitmap[pdfRenderer.getPageCount()];
                final ArrayList<byte[]> images = new ArrayList<byte[]>();
                PdfRenderer.Page rendererPage = null;
                for (int i = 0; i < pdfRenderer.getPageCount(); i++) {
                    rendererPage = pdfRenderer.openPage(i);
                    int rendererPageWidth = rendererPage.getWidth();
                    int rendererPageHeight = rendererPage.getHeight();
                    Bitmap bitmap = Bitmap.createBitmap(
                            rendererPageWidth*2,
                            rendererPageHeight*2,
                            Bitmap.Config.ARGB_8888);
                    rendererPage.render(bitmap, null, null,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    bitmaps[i] = bitmap;

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    final byte[] bytes = stream.toByteArray();
                    images.add(bytes);
                    rendererPage.close();
                }

                description.setImageBitmap(bitmaps[0]);
                pdfRenderer.close();
                fileDescriptor.close();

                description.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, MenuDisplay.class);

                        intent.putExtra("pics", images);
                        startActivity(intent);
                    }
                });

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
        context = this;

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
