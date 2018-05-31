package com.balaji.projects.myweatherapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.balaji.projects.myweatherapp.R;

/**
 * Created by Balaji on 30-May-18.
 */

public class LocationSpinnerAdapter extends ArrayAdapter {
    String[] city;

    public LocationSpinnerAdapter(Context context, int textViewResourceId, String[] objects) {
        super(context, textViewResourceId, objects);
        city = objects;
    }

    public View getCustomView(int position, View convertView,
                              ViewGroup parent) {

// Inflating the layout for the custom Spinner
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_spinner_item, parent, false);

// Declaring and Typecasting the textview in the inflated layout
        TextView tvCity = (TextView) layout.findViewById(R.id.tv_city);

// Setting the text using the array
        tvCity.setText(city[position]);
// Setting the color of the text
        tvCity.setTextColor(Color.rgb(75, 180, 225));
// Setting Special atrributes for 1st element
        if (position == 0) {
            // Setting the text Color
            tvCity.setTextColor(Color.WHITE);
        }
        return layout;
    }

    // It gets a View that displays in the drop down popup the data at the specified position
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    // It gets a View that displays the data at the specified position
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

}
