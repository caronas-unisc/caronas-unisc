package br.unisc.caronasuniscegm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import br.unisc.caronasuniscegm.utils.TokenUtils;
import br.unisc.caronasuniscegm.rest.ApiEndpoints;
import br.unisc.caronasuniscegm.rest.RideIntention;

/**
 * Created by MateusFelipe on 11/10/2015.
 */
public class ConfigureRideActivity extends AppCompatActivity {
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 4;

    private ProgressDialog pd;
    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;

    private RideIntention rideIntention;

    private List<String> selectedPeriodList;
    private List<String> selectedDayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_slide);

        rideIntention = new RideIntention();

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit(4);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When changing pages, reset the action bar actions since they are dependent
                // on which page is currently active. An alternative approach is to have each
                // fragment expose actions itself (rather than the activity exposing actions),
                // but for simplicity, the activity provides the actions in this sample.
                invalidateOptionsMenu();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_screen_slide, menu);
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_previous).setEnabled(mPager.getCurrentItem() > 0);

        // Add either a "next" or "finish" button to the action bar, depending on which page
        // is currently selected.
        MenuItem item = menu.findItem( R.id.action_next );
        if(mPager.getCurrentItem() == mPagerAdapter.getCount() - 1){
            item.setTitle(R.string.action_finish);
        }else{
            item.setTitle(R.string.action_next);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate "up" the demo structure to the launchpad activity.
                // See http://developer.android.com/design/patterns/navigation.html for more.
                NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                return true;

            case R.id.action_previous:
                // Go to the previous step in the wizard. If there is no previous step,
                // setCurrentItem will do nothing.
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                return true;

            case R.id.action_next:

                // Finish, send data to web service
                if( mPager.getCurrentItem() == mPagerAdapter.getCount() - 1 ){
                    addRideIntention();
                }else {
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addRideIntention() {

        ScreenSlidePageFragment page0 = (ScreenSlidePageFragment) mPagerAdapter.getRegisteredFragment(0);
        ScreenSlidePageFragment page1 = (ScreenSlidePageFragment) mPagerAdapter.getRegisteredFragment(1);
        ScreenSlidePageFragment page2 = (ScreenSlidePageFragment) mPagerAdapter.getRegisteredFragment(2);
        ScreenSlidePageFragment page3 = (ScreenSlidePageFragment) mPagerAdapter.getRegisteredFragment(3);

        rideIntention.setStartingLocationLatitude(page0.getLatitude());
        rideIntention.setStartingLocationLongitude(page0.getLongitude());
        rideIntention.setStartingLocationAddress(page0.getAddress());
        rideIntention.setAvailabilityType(page1.getGiveReceiveRide());

        if( rideIntention.getAvailabilityType() == RideIntention.AVAIBILITY_TYPE_GIVE ) {
            rideIntention.setAvailablePlacesInCar(page1.getPlacesInCar());
        }

        this.selectedDayList = page2.getSelectedDays();
        this.selectedPeriodList = page3.getSelectedPeriods();

        // Monta objeto JSON
        JSONObject requestJson = new JSONObject();
        JSONObject rideIntentionJson = new JSONObject();


        final String token = TokenUtils.getToken(this.getApplicationContext());

        try {
            rideIntentionJson.put("availability_type", rideIntention.getAvailabilityType() );
            if( rideIntention.getAvailabilityType().equals(RideIntention.AVAIBILITY_TYPE_GIVE) ){
                rideIntentionJson.put("available_places_in_car", rideIntention.getAvailablePlacesInCar());
            }
            rideIntentionJson.put("starting_location_address", rideIntention.getStartingLocationAddress());
            rideIntentionJson.put("starting_location_latitude", rideIntention.getStartingLocationLatitude());
            rideIntentionJson.put("starting_location_longitude", rideIntention.getStartingLocationLongitude());
            requestJson.put("ride_availability", rideIntentionJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Resposta de sucesso
        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject responseJson) {
                finish();
            }
        };

        // Resposta de erro
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                hideProgressDialog();
                volleyError.printStackTrace();

            }
        };

        for( String date : selectedDayList ){

            for( String period : selectedPeriodList ){

                // Envia requisição
                showProgressDialog();

                String url = ApiEndpoints.RIDE_AVAIABILITIES + "/" + date + "/" + period.toLowerCase();
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, requestJson,
                        successListener, errorListener){
                    @Override
                    public HashMap<String, String> getHeaders() {
                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put("Authentication-Token", token);
                        return params;
                    }
                };

                request.setRetryPolicy(new DefaultRetryPolicy(ApiEndpoints.TIMEOUT,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                RequestQueue queue = Volley.newRequestQueue(this);
                queue.add(request);

            }
        }
    }



    /**
     * A simple pager adapter that represents 5 {@link ScreenSlidePageFragment} objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ScreenSlidePageFragment.create(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }

    }

    private void showProgressDialog() {
        String message = getResources().getString(R.string.please_wait);
        pd = ProgressDialog.show(this, "", message, false);
    }

    private void hideProgressDialog() {
        pd.dismiss();
        pd = null;
    }
}
