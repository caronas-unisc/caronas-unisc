package br.unisc.caronasuniscegm;

/**
 * Created by MateusFelipe on 11/10/2015.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.unisc.caronasuniscegm.adapters.DayOfTheWeekAdapter;
import br.unisc.caronasuniscegm.adapters.PeriodAdapter;
import br.unisc.caronasuniscegm.rest.RideIntention;

public class ScreenSlidePageFragment extends Fragment {
    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "page";

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mPageNumber;

    private ListView mDaysListview;
    private DayOfTheWeekAdapter mDayOfTheWeekAdapter;
    private List<String> mDaysList;

    private ListView mPeriodListview;
    private PeriodAdapter mPeriodAdapter;
    private List<String> mPeriodList;

    private Button mBtnAddPlace;

    private Double latitude;
    private Double longitude;
    private String address;

    static final int PICK_LOCATION_REQUEST = 1;  // The request code
    private List<String> selectedDays;

    private TextView vlAddress;
    private TextView vlLatitude;
    private TextView vlLongitude;

    private LinearLayout layoutPlaceInCar;
    private EditText textPlacesInCar;
    private String availabilityType;


    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static ScreenSlidePageFragment create(int pageNumber) {
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ScreenSlidePageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
        mDaysList = new ArrayList<String>();
        mPeriodList = new ArrayList<String>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = null;
        switch (mPageNumber){
            case 0:
                // Inflate the layout containing a title and body text.
                rootView = (ViewGroup) inflater
                        .inflate(R.layout.fragment_choose_places, container, false);

                configureChoosePlace(rootView);
                break;
            case 1:
                // Inflate the layout containing a title and body text.
                rootView = (ViewGroup) inflater
                        .inflate(R.layout.fragment_give_receive_ride, container, false);

                configureChooseGiveReceiveRide(rootView);
                break;
            case 2:
                // Inflate the layout containing a title and body text.
                rootView = (ViewGroup) inflater
                        .inflate(R.layout.fragment_day, container, false);

                configureChooseDay(rootView);
                break;
            case 3:
                // Inflate the layout containing a title and body text.
                rootView = (ViewGroup) inflater
                        .inflate(R.layout.fragment_period, container, false);

                configureChoosePeriod(rootView);
                break;

            default:
                break;
        }
        return rootView;
    }

    private void configureChooseGiveReceiveRide(ViewGroup rootView) {

        layoutPlaceInCar = (LinearLayout) rootView.findViewById(R.id.layout_places_in_car);
        textPlacesInCar = (EditText) rootView.findViewById(R.id.text_places_in_car);

        RadioGroup rg = (RadioGroup) rootView.findViewById(R.id.radio_group_give_receive_ride);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch(checkedId)
                {
                    case R.id.radio_give_ride:
                        availabilityType = RideIntention.AVAILABILITY_TYPE_GIVE;
                        layoutPlaceInCar.setVisibility(View.VISIBLE);
                        break;
                    case R.id.radio_receive_ride:
                        availabilityType = RideIntention.AVAILABILITY_TYPE_RECEIVE;
                        layoutPlaceInCar.setVisibility(View.INVISIBLE);
                        textPlacesInCar.setText("");
                        break;
                }
            }
        });

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_give_ride:
                if (checked)
                    availabilityType = ((RadioButton) view).getText().toString();
                    layoutPlaceInCar.setVisibility(View.VISIBLE);
                    break;
            case R.id.radio_receive_ride:
                if (checked)
                    availabilityType = ((RadioButton) view).getText().toString();
                    layoutPlaceInCar.setVisibility(View.INVISIBLE);
                    textPlacesInCar.setText("");
                break;
        }
    }

    private void configureChoosePlace(ViewGroup rootView) {

        mBtnAddPlace = (Button) rootView.findViewById( R.id.btnAddPlace );

        mBtnAddPlace.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), AddPlaceActivity.class);
                startActivityForResult(intent, PICK_LOCATION_REQUEST);
            }
        });

        vlAddress = (TextView) rootView.findViewById(R.id.vl_address);
        vlLatitude = (TextView) rootView.findViewById(R.id.vl_latitude);
        vlLongitude = (TextView) rootView.findViewById(R.id.vl_longitude);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_LOCATION_REQUEST) {
            // Make sure the request was successful
            if (resultCode == getActivity().RESULT_OK) {
                latitude = data.getExtras().getDouble("latitude");
                longitude = data.getExtras().getDouble("longitude");
                address = data.getExtras().getString("address");

                vlAddress.setText(address);
                vlLatitude.setText(latitude + "");
                vlLongitude.setText(longitude+ "");
            }
        }
    }

    private void configureChoosePeriod(ViewGroup rootView) {
        mPeriodListview = (ListView) rootView.findViewById(R.id.listview_choose_period);

        mPeriodList.add( getResources().getString(R.string.field_morning) );
        mPeriodList.add( getResources().getString(R.string.field_afternoon) );
        mPeriodList.add( getResources().getString(R.string.field_night) );

        mPeriodAdapter = new PeriodAdapter(getActivity().getApplicationContext(), getActivity().getLayoutInflater(),
                R.layout.fragment_period_item_row, mPeriodList);

        mPeriodListview.setAdapter(mPeriodAdapter);
    }

    private void configureChooseDay(ViewGroup rootView) {

        mDaysListview = (ListView) rootView.findViewById(R.id.listview_choose_day);

        Calendar now = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        now.setTime(date);
        calendar.setTime(date);

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        //if( now.getTimeInMillis() < calendar.getTimeInMillis() ){
            mDaysList.add(getResources().getString(R.string.field_friday));
        //}

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        //if( now.getTimeInMillis() < calendar.getTimeInMillis() ){
            mDaysList.add(getResources().getString(R.string.field_thursday));
        //}

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        //if( now.getTimeInMillis() < calendar.getTimeInMillis() ){
            mDaysList.add(getResources().getString(R.string.field_wednesday));
        //}

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        //if( now.getTimeInMillis() < calendar.getTimeInMillis() ){
            mDaysList.add(getResources().getString(R.string.field_tuesday));
        //}

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        //if( now.getTimeInMillis() < calendar.getTimeInMillis() ){
            mDaysList.add( getResources().getString(R.string.field_monday) );
        //}

        mDayOfTheWeekAdapter = new DayOfTheWeekAdapter(getActivity().getApplicationContext(), getActivity().getLayoutInflater(),
                R.layout.fragment_day_item_row, mDaysList);

        mDaysListview.setAdapter(mDayOfTheWeekAdapter);
    }

    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() {
        return mPageNumber;
    }

    public List<String> getSelectedPeriods() {

        List<String> names = new ArrayList<String>();
        for (int i=0;i<mPeriodAdapter.getCount();i++){
            if( mPeriodAdapter.checkedPeriods[i] ){
                names.add( mPeriodList.get(i) );
            }
        }

        return names;
    }

    public List<String> getSelectedDays() {
        List<String> dates = new ArrayList<String>();
        for (int i=0;i<mDayOfTheWeekAdapter.getCount();i++){
            if( mDayOfTheWeekAdapter.checkedDays[i] ){

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();
                calendar.setTime(date);

                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                if( mDaysList.get(i) == getResources().getString(R.string.field_monday) ){
                    dates.add(dateFormat.format(calendar.getTime()));
                }

                calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                if( mDaysList.get(i) == getResources().getString(R.string.field_tuesday) ){
                    dates.add(dateFormat.format(calendar.getTime()));
                }

                calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                if( mDaysList.get(i) == getResources().getString(R.string.field_wednesday) ){
                    dates.add(dateFormat.format(calendar.getTime()));
                }
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                if( mDaysList.get(i) == getResources().getString(R.string.field_thursday) ){
                    dates.add(dateFormat.format(calendar.getTime()));
                }
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                if( mDaysList.get(i) == getResources().getString(R.string.field_friday) ){
                    dates.add(dateFormat.format(calendar.getTime()));
                }

            }
        }

        return dates;
    }

    public Double getLatitude(){ return this.latitude; }

    Double getLongitude(){ return this.longitude; }

    String getAddress(){ return this.address; }

    public int getPlacesInCar() {
        if (textPlacesInCar.getText().toString().isEmpty()) {
            return 0;
        } else {
            return Integer.valueOf(textPlacesInCar.getText().toString());
        }
    }

    public String getAvailabilityType() { return availabilityType; }
}