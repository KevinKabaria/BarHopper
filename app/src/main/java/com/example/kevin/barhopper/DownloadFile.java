package com.example.kevin.barhopper;

import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;

/**
 * Created by kevin on 3/28/17.
 */

public class DownloadFile extends AsyncTask<File, Void, Void> {

    @Override
    protected Void doInBackground(File... files) {
        File pdfFile = files[0];
        String fileUrl = "http://ivyinnprinceton.com/wp-content/uploads/2015/10/Menu-5-18-15-web.pdf";


        FileDownloader.downloadFile(fileUrl, pdfFile);
        return null;
    }
}
