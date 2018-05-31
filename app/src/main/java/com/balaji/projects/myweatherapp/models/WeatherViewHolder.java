package com.balaji.projects.myweatherapp.models;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balaji.projects.myweatherapp.R;

public class WeatherViewHolder extends RecyclerView.ViewHolder {
    LinearLayout layTmpDetail;
    public TextView itemDate;
    public TextView itemTemperature;
    public TextView itemDescription;
    public TextView itemyWind;
    public TextView itemPressure;
    public TextView itemHumidity;
    public TextView itemIcon;
    public TextView readmore;
    public ImageView share;
    public View lineView;


    public WeatherViewHolder(View view) {
        super(view);
        this.itemDate = (TextView) view.findViewById(R.id.itemDate);
        this.readmore = (TextView) view.findViewById(R.id.readmore);
        this.layTmpDetail = (LinearLayout) view.findViewById(R.id.lay_tmp_detail);
        this.itemTemperature = (TextView) view.findViewById(R.id.itemTemperature);
        this.itemDescription = (TextView) view.findViewById(R.id.itemDescription);
        this.itemyWind = (TextView) view.findViewById(R.id.itemWind);
        this.itemPressure = (TextView) view.findViewById(R.id.itemPressure);
        this.itemHumidity = (TextView) view.findViewById(R.id.itemHumidity);
        this.itemIcon = (TextView) view.findViewById(R.id.itemIcon);
        this.share = (ImageView) view.findViewById(R.id.iv_share);
        this.lineView = view.findViewById(R.id.lineView);

        readmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layTmpDetail.setVisibility(View.VISIBLE);
            }
        });
    }
}