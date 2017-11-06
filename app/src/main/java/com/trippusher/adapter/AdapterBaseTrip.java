package com.trippusher.adapter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trippusher.AppStatus;
import com.trippusher.R;
import com.trippusher.activity.ActivityTripDetail;
import com.trippusher.classes.TripItemList;

import java.util.List;

/**
 * Created by Desktop-KS on 7/26/2017.
 */
public class AdapterBaseTrip extends RecyclerView.Adapter<AdapterBaseTrip.MyViewHolder> {
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    private List<TripItemList> mModelList;

    public AdapterBaseTrip(List<TripItemList> modelList) {
        mModelList = modelList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_triplisting, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder ViewHolder, int position) {

        final TripItemList model = mModelList.get(position);
        ViewHolder.Image.setImageResource(R.drawable.airlineimage);
        ViewHolder.base_airport.setText(model.base_airport);
        ViewHolder.airline_title.setText(model.airline_title);
        ViewHolder.date.setText(model.start_date);
        ViewHolder.hours.setText(model.hours);
        ViewHolder.gift.setText(model.gift);
        ViewHolder.LayRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppStatus.getInstance(view.getContext()).isOnline()) {
                    Intent intent = new Intent(view.getContext(), ActivityTripDetail.class);
                    /*prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                    editor = prefs.edit();
                    editor.putString("postTripId", String.valueOf(model.post_trip_id));
                    editor.commit();*/
                    intent.putExtra("postTripId", String.valueOf(model.post_trip_id));
                    view.getContext().startActivity(intent);
                } else {
                    Toast.makeText(view.getContext(), "Please check network connection and try again", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return mModelList == null ? 0 : mModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private View view;
        public LinearLayout LayRow;
        public LinearLayout LayExpired;
        public ImageView Image;
        public TextView base_airport;
        public TextView airline_title;
        public TextView date;
        public TextView hours;
        public TextView gift;

        private MyViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            base_airport = (TextView) itemView.findViewById(R.id.txtbaseairport);
            LayRow = (LinearLayout) itemView.findViewById(R.id.llrow);
            LayExpired = (LinearLayout) itemView.findViewById(R.id.LayExpired);
            Image = (ImageView) itemView.findViewById(R.id.img);
            base_airport = (TextView) itemView.findViewById(R.id.txtbaseairport);
            airline_title = (TextView) itemView.findViewById(R.id.txtairlinetitle);
            date = (TextView) itemView.findViewById(R.id.txtstartdate);
            hours = (TextView) itemView.findViewById(R.id.txthours);
            gift = (TextView) itemView.findViewById(R.id.txtgift);
        }
    }
}