package com.example.kevin.barhopper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import java.lang.reflect.Array;

/**
 * Created by kevin on 3/28/17.
 */

public class imageDisplayArrayAdapter extends BaseAdapter {
        private Context context;
        private Bitmap[] buttonInfo;

        public imageDisplayArrayAdapter(@NonNull Context context, Bitmap[] buttonInfo) {
            this.context = context;

            // Not deep copied
            this.buttonInfo = buttonInfo;

        }

        @Override
        public int getCount() {
            return buttonInfo.length;
        }

        @Override
        public Object getItem(int position) {
            return buttonInfo[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);




            View button = inflater.inflate(R.layout.menu_image_button, null);

            ImageView info = (ImageView) button.findViewById(R.id.menuButton);

            info.setImageBitmap(buttonInfo[position]);



            return button;
        }

        // When button is clicked, handle
        public void handleClick() {
            System.out.println("Name!: ");

        }

}
