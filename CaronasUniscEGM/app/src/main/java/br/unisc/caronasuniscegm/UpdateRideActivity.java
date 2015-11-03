package br.unisc.caronasuniscegm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.unisc.caronasuniscegm.rest.RideIntention;
import br.unisc.caronasuniscegm.utils.CalendarUtils;

/**
 * Created by mfelipe on 03/11/2015.
 */
public class UpdateRideActivity extends AppCompatActivity {

    private Spinner mSpinnerDayOfTheWeek;
    private Spinner mSpinnerPeriod;
    private Spinner mSpinnerAvailabilityType;

    private EditText mTextPlacesInCar;
    private TextView mTextStartingLocationAddress;

    private List<String> mPeriodList;
    private List<String> mDaysList;
    private List<String> mAvailabilityTypeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_ride);

        mPeriodList = new ArrayList<String>();
        mPeriodList.add(getResources().getString(R.string.field_morning));
        mPeriodList.add(getResources().getString(R.string.field_afternoon));
        mPeriodList.add( getResources().getString(R.string.field_night) );

        mDaysList = CalendarUtils.getUpcommingDaysOfTheWeek(getApplicationContext());

        mAvailabilityTypeList = new ArrayList<String>();
        mAvailabilityTypeList.add( RideIntention.AVAILABILITY_TYPE_GIVE );
        mAvailabilityTypeList.add(RideIntention.AVAILABILITY_TYPE_RECEIVE);

        mSpinnerDayOfTheWeek = (Spinner) findViewById(R.id.spinner_day_of_the_week);
        ArrayAdapter<String> arrayAdapterDays = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                mDaysList );
        arrayAdapterDays.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerDayOfTheWeek.setAdapter(arrayAdapterDays);

        mSpinnerPeriod = (Spinner) findViewById(R.id.spinner_period);
        ArrayAdapter<String> arrayAdapterPeriod = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                mPeriodList );
        arrayAdapterPeriod.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        mSpinnerPeriod.setAdapter(arrayAdapterPeriod);

        mSpinnerAvailabilityType = (Spinner) findViewById(R.id.spinner_availability_type);
        ArrayAdapter<String> arrayAdapterAvailabilityType = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                mAvailabilityTypeList );
        arrayAdapterAvailabilityType.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        mSpinnerAvailabilityType.setAdapter(arrayAdapterAvailabilityType);

        mTextPlacesInCar = (EditText) findViewById(R.id.txt_places_in_car);
        mTextStartingLocationAddress = (TextView) findViewById(R.id.txt_starting_location_address);
    }
}
