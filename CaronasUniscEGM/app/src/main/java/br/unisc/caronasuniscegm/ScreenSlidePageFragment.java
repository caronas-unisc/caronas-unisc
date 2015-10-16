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
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.unisc.caronasuniscegm.adapters.DayOfTheWeekAdapter;
import br.unisc.caronasuniscegm.adapters.PeriodAdapter;

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
                        .inflate(R.layout.fragment_day, container, false);

                configureChooseDay( rootView );
                break;
            case 2:
                // Inflate the layout containing a title and body text.
                rootView = (ViewGroup) inflater
                        .inflate(R.layout.fragment_period, container, false);

                configureChoosePeriod(rootView);
                break;
            case 3:
                // Inflate the layout containing a title and body text.
                rootView = (ViewGroup) inflater
                        .inflate(R.layout.fragment_select_location, container, false);
                break;

            default:
                break;
        }
        return rootView;
    }

    private void configureChoosePlace(ViewGroup rootView) {

        mBtnAddPlace = (Button) rootView.findViewById( R.id.btnAddPlace );

        mBtnAddPlace.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), AddPlaceActivity.class);
                startActivityForResult(intent, PICK_LOCATION_REQUEST);
            }
        });


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

                ConfigureRideActivity configureRideActivity =
                        (ConfigureRideActivity)  getActivity();
                configureRideActivity.setLatitude(latitude);
                configureRideActivity.setLongitude(longitude);
                configureRideActivity.setAddress(address);

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
            mDaysList.add( getResources().getString(R.string.field_friday) );
        //}

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        //if( now.getTimeInMillis() < calendar.getTimeInMillis() ){
            mDaysList.add( getResources().getString(R.string.field_thursday) );
        //}

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        //if( now.getTimeInMillis() < calendar.getTimeInMillis() ){
            mDaysList.add( getResources().getString(R.string.field_wednesday) );
        //}

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        //if( now.getTimeInMillis() < calendar.getTimeInMillis() ){
            mDaysList.add( getResources().getString(R.string.field_tuesday) );
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

    public Double getLatitude(){ return this.latitude; }

}