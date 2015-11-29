package br.unisc.caronasuniscegm.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import br.unisc.caronasuniscegm.AgendaActivity;
import br.unisc.caronasuniscegm.R;
import br.unisc.caronasuniscegm.UpdateRideActivity;
import br.unisc.caronasuniscegm.rest.RideIntention;
import br.unisc.caronasuniscegm.utils.CalendarUtils;

/**
 * Created by MateusFelipe on 17/10/2015.
 */
public class AgendaAdapter extends RecyclerView.Adapter<AgendaAdapter.ViewHolder> {

    private List<RideIntention> data = null;
    private Context mContext;


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView txtDate;
        public TextView txtAvailabilityType;
        public TextView txtDataPosition;

        public ImageView iconGiveReceiveRide;

        public LinearLayout layoutAvaiablePlacesInCar;

        public IMyViewHolderClicks mListener;

        public ViewHolder(View v, IMyViewHolderClicks listener) {
            super(v);
            txtDate = (TextView) v.findViewById(R.id.txt_date);
            txtAvailabilityType = (TextView) v.findViewById(R.id.txt_availability_type);
            txtDataPosition = (TextView) v.findViewById(R.id.txt_data_position);

            mListener = listener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.initUpdateRideActivity(v);
        }

        public static interface IMyViewHolderClicks {
            public void initUpdateRideActivity(View v);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AgendaAdapter(List<RideIntention> data, Context mContext) {
        this.data = data;
        this.mContext = mContext;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AgendaAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_agenda_item_row, parent, false);
        // set the view's size, margins, paddings and layout parameters

        AgendaAdapter.ViewHolder vh = new ViewHolder(v, new AgendaAdapter.ViewHolder.IMyViewHolderClicks() {
            @Override
            public void initUpdateRideActivity(View v) {
                int position = Integer.valueOf(((TextView) v.findViewById(R.id.txt_data_position)).getText().toString());
                RideIntention rideIntention = data.get(position);

                ((AgendaActivity) mContext).startUpdateRideActivity(rideIntention);
            }
        });
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RideIntention rideIntention = getData().get(position);

        String dayOfWeek = CalendarUtils.dateToDayOfTheWeek(mContext, rideIntention.getDate());
        String period = rideIntention.getPeriod();

        switch (period) {
            case "morning":
                period = mContext.getString(R.string.field_morning);
                break;

            case "afternoon":
                period = mContext.getString(R.string.field_afternoon);
                break;

            case "night":
                period = mContext.getString(R.string.field_night);
                break;
        }

        String dateAndPeriod = mContext.getString(R.string.date_and_period, dayOfWeek,
                period.toLowerCase());

        holder.txtDataPosition.setText(position + "");
        holder.txtDate.setText(dateAndPeriod);

        String type = rideIntention.getAvailabilityType();
        int typeStringId = (type.equals("receive")) ? R.string.receive_ride : R.string.give_ride;

        holder.txtAvailabilityType.setText(mContext.getString(typeStringId).toLowerCase());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return data.size();
    }

    public List<RideIntention> getData() { return data; }

    public void setData(List<RideIntention> data) {
        this.data = data;
    }

    public void updateDataList(List<RideIntention> newlist) {
        data.clear();
        data.addAll(newlist);
        this.notifyDataSetChanged();
    }
}