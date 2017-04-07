package com.example.kevin.barhopper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

/**
 * Created by kevin on 3/25/17.
 */

public class listbuttonArrayAdapter extends BaseAdapter {
    private Context context;
    private ListViewActivity.ListViewButton[] buttonInfo;

    public listbuttonArrayAdapter(@NonNull Context context, ListViewActivity.ListViewButton[] buttonInfo) {
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

        position--;

        // Add search button to top
        if (position == -1) {
            View button = inflater.inflate(R.layout.search_button, null);
            button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        Intent intent =
                                new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                        .build((Activity) context);
                        ((Activity) context).startActivityForResult(intent, ListViewActivity.PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException e) {
                        // TODO: Handle the error.
                    } catch (GooglePlayServicesNotAvailableException e) {
                        // TODO: Handle the error.
                    }
                }
            });

            return button;
        }

        View button = inflater.inflate(R.layout.listview_button_layout, parent, false);

        final ListViewActivity.ListViewButton info = buttonInfo[position+1];

        TextView textView = (TextView) button.findViewById(R.id.bigText);
        textView.setText(info.getName());

        TextView textView2 = (TextView)button.findViewById(R.id.smallText);
        textView2.setText(info.getDistance());

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("test");
                handleClick(info.getPlace());
            }
        });

        return button;
    }

    // When button is clicked, handle
    public void handleClick(Place p) {
        System.out.println("Name!: " + p.getName());

        // Create new intent and activate it.
        // Pass in Place p via Parcelable
        Intent intent = new Intent(this.context, BarInfoView_activity.class);
        intent.putExtra("com.example.kevin.barhopper.ListViewActivity.PLACE", (Parcelable) p);
        context.startActivity(intent);
    }


}
