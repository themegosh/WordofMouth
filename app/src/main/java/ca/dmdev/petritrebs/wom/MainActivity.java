package ca.dmdev.petritrebs.wom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.Slider;
import com.gc.materialdesign.widgets.Dialog;
import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import ca.dmdev.petritrebs.wom.acccount.User;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        android.location.LocationListener{

    private static final String[] INITIAL_PERMS={
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.READ_CONTACTS
    };
    private static final int INITIAL_REQUEST=1337;
    private static final int LOCATION_REQUEST=INITIAL_REQUEST+3;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private static final float PANEL_ANCHORED = 0.7f;
    private static final float PANEL_EXPANDED = 1.0f;
    private static final int DEFAULT_PLACE_DISTANCE = 1000;
    private static final String TAG = MainActivity.class.getName();

    protected WordOfMouth wom;

    private SlidingUpPanelLayout slidingPanelLayout;
    private Toolbar toolbar;
    private FloatingActionButton fabAddLocation;
    private static SupportMapFragment mapFragment;
    private GoogleMap map;
    private LocationManager locationManager;
    private FragmentManager fragmentManager;
    private TextView lblPlaceTitle;
    private TextView lblScrollView;
    private ImageView imgToggleSlidingPanel;
    private Circle distanceCircle;
    private Location lastLocation;

    //slider view related
    private RelativeLayout viewDistanceSelector;
    private Slider sliderDistance;
    private ImageButton btnSliderCheck;

    //search view related
    private AutoCompleteTextView txtSearch;
    private MenuItem btnSearch;
    private MenuItem btnCloseSearch;

    protected GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wom = (WordOfMouth)getApplication();
        fragmentManager = getFragmentManager();

        //Initialize activity related resources, the order here is important
        initializePermissions();
        initializeLocation();
        initializeToolbar();
        initializeDrawerLayout();
        initializeSlidingPanel();
        initializeFab();
        initializeNavPanel();
        initializeMap();
        initializePlacesApi();
        initializeDistanceSlider();
    }
    @Override
    protected void onPause() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
        super.onPause();
    }
    @Override
    public void onStop(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
        super.onStop();
    }
    @Override
    protected void onResume() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(bestProvider, 2000, 0, this);  // time in miliseconds, distance in meters (Distance drains battry life) originally set to 20000 and 0

        }
        super.onResume();
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (slidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {
            slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (slidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED && slidingPanelLayout.getAnchorPoint() == PANEL_ANCHORED){
            slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
        } else if (slidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            //prevent the user from going back to the login activity
            //super.onBackPressed(); //this should be commented out
            Intent logout = new Intent();
            logout.putExtra("backPressed", true);
            setResult(Activity.RESULT_OK, logout);
            finish();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);


        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.btnCloseSearch) {
            txtSearch.setVisibility(View.GONE);
            btnCloseSearch.setVisible(false);
            btnSearch.setVisible(true);
            txtSearch.setText("");
            //hide keyboard
            InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(txtSearch.getWindowToken(), 0);
            return true;
        }
        else if (id == R.id.btnSearch) {
            txtSearch.setVisibility(View.VISIBLE);
            btnCloseSearch.setVisible(true);
            btnSearch.setVisible(false);
            txtSearch.requestFocus();
            //force show keyboard
            InputMethodManager inputMethodManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInputFromWindow(txtSearch.getWindowToken(), InputMethodManager.SHOW_FORCED, 0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_toggle_anchor) {
            if (slidingPanelLayout != null) {
                if (slidingPanelLayout.getAnchorPoint() == PANEL_EXPANDED) {
                    slidingPanelLayout.setAnchorPoint(PANEL_ANCHORED);
                    slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                    item.setTitle("Enable Anchor");
                } else {
                    slidingPanelLayout.setAnchorPoint(PANEL_EXPANDED);
                    slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    item.setTitle("Disable Anchor");
                }
            }
        } else if (id == R.id.nav_logout) {

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            Intent logout = new Intent();
                            logout.putExtra("logout", true);
                            setResult(Activity.RESULT_OK, logout);
                            finish();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener)
                    .show();


        }

        else if (id == R.id.nav_toggle_proximity){
            if (viewDistanceSelector.getVisibility() == View.GONE){
                viewDistanceSelector.setVisibility(View.VISIBLE);
            } else {
                viewDistanceSelector.setVisibility(View.GONE);
                if (distanceCircle != null)
                    distanceCircle.remove();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    centerMapOnMyLocation();
                    Log.d(TAG, "onRequestPermissionsResult: Allowed");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d(TAG, "onRequestPermissionsResult: Denied");

                    Dialog dialog = new Dialog(MainActivity.this, "Title", "Message");
                    dialog.show();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }
        map.setTrafficEnabled(false);
        map.setIndoorEnabled(true);
        map.setBuildingsEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG, "Map clicked! Lat: " + latLng.latitude + " Lng: " + latLng.longitude);
            }
        });

        centerMapOnMyLocation();


    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        btnSearch = menu.findItem(R.id.btnSearch);
        btnCloseSearch = menu.findItem(R.id.btnCloseSearch);
        return true;
    }
    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    @Override
    public void onProviderEnabled(String provider) {

    }
    @Override
    public void onProviderDisabled(String provider) {

    }

    private void initializePermissions(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }

    }
    private void initializeLocation(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(bestProvider, 5, 5, this);  // time in miliseconds, distance in meters (Distance drains battry life) originally set to 20000 and 0

        }
    }
    private void initializeSlidingPanel(){
        imgToggleSlidingPanel = (ImageView) findViewById(R.id.imgToggleSlidingPanel);
        slidingPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingPanelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                //Log.i(TAG, "onPanelSlide, offset " + slideOffset);

                //move the FAB based on sliding bar's offset
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fabAddLocation.getLayoutParams();
                params.bottomMargin = 200 + (int)(((0-1)*slideOffset) * 150);
                fabAddLocation.setLayoutParams(params);

            }

            @Override
            public void onPanelExpanded(View panel) {
                Log.i(TAG, "onPanelExpanded");
                imgToggleSlidingPanel.setImageResource(R.drawable.ic_keyboard_arrow_down_black_48dp);
            }

            @Override
            public void onPanelCollapsed(View panel) {
                Log.i(TAG, "onPanelCollapsed");
                imgToggleSlidingPanel.setImageResource(R.drawable.ic_keyboard_arrow_up_black_48dp);
            }

            @Override
            public void onPanelAnchored(View panel) {
                Log.i(TAG, "onPanelAnchored");
                imgToggleSlidingPanel.setImageResource(R.drawable.ic_keyboard_arrow_down_black_48dp);
            }

            @Override
            public void onPanelHidden(View panel) {
                Log.i(TAG, "onPanelHidden");
                imgToggleSlidingPanel.setImageResource(R.drawable.ic_keyboard_arrow_up_black_48dp);
            }
        });
        slidingPanelLayout.setAnchorPoint(PANEL_ANCHORED);
        slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }
    private void initializeToolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        txtSearch = (AutoCompleteTextView)
                findViewById(R.id.txtSearch);
        txtSearch.setOnItemClickListener(mAutocompleteClickListener);
    }
    private void initializeDrawerLayout(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }
    private void initializeFab(){
        fabAddLocation = (FloatingActionButton) findViewById(R.id.fab_save);
        fabAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent myIntentA1A2 = new Intent(MainActivity.this,
                            AddLocation.class);

                    startActivityForResult(myIntentA1A2, 1);
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void initializeNavPanel(){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeaderView = navigationView.getHeaderView(0); //navigationView.inflateHeaderView(R.layout.nav_header_main);

        ImageView profilePic = (ImageView) navHeaderView.findViewById(R.id.imageView);
        TextView profileName = (TextView) navHeaderView.findViewById(R.id.profileName);


        profileName.setText(User.getInstance().getFirstName() + " " + User.getInstance().getLastName());
        //profileName.setText("Firstname Lastname");
        Picasso.with(getApplicationContext()).load(User.getInstance().getPicUrl()).into(profilePic);

        Log.d(TAG, "picUrl: " + User.getInstance().getPicUrl());

        Log.d(TAG, "initializeNavPanel() User(): " + User.getInstance().getFirstName() + " " + User.getInstance().getLastName());
    }
    private void initializeMap() {

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }
    private void initializePlacesApi(){
        if (lastLocation != null) {
            LatLngBounds latLngBounds = convertCenterAndRadiusToBounds(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), DEFAULT_PLACE_DISTANCE);

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, 0 /* clientId */, this)
                    .addApi(Places.GEO_DATA_API)
                    .build();

            mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, latLngBounds,
                    null);
            txtSearch.setAdapter(mAdapter);

            lblPlaceTitle = (TextView) findViewById(R.id.lblPlaceTitle);
            lblScrollView = (TextView) findViewById(R.id.lblScrollView);
        }
    }
    private void initializeDistanceSlider(){
        viewDistanceSelector = (RelativeLayout) findViewById(R.id.viewDistanceSelector);

        btnSliderCheck = (ImageButton) findViewById(R.id.btnConfirmDistance);
        btnSliderCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (distanceCircle != null)
                    distanceCircle.remove();


                viewDistanceSelector.setVisibility(View.GONE);

            }
        });

        sliderDistance = (Slider) findViewById(R.id.sliderDistance);
        sliderDistance.setOnValueChangedListener(
                new Slider.OnValueChangedListener() {
                    @Override
                    public void onValueChanged(int i) {
                        if (distanceCircle != null) {
                            distanceCircle.setRadius(i);
                        } else {
                            if (lastLocation == null) {
                                if (ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                                        == PackageManager.PERMISSION_GRANTED) {
                                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                                    Criteria criteria = new Criteria();
                                    String bestProvider = locationManager.getBestProvider(criteria, true);
                                    lastLocation = locationManager.getLastKnownLocation(bestProvider);
                                }
                            }

                            if (lastLocation != null) {
                                CircleOptions distanceCircleOptions = new CircleOptions()
                                        .center(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                                        .radius(i)
                                        .strokeColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimaryDark))
                                        .fillColor(R.color.colorPrimaryDark);
                                distanceCircle = map.addCircle(distanceCircleOptions);
                            }
                        }

                        if (mAdapter != null && lastLocation != null){
                            mAdapter.setBounds(convertCenterAndRadiusToBounds(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), i));
                        }
                    }
                }
        );
        sliderDistance.setValue(DEFAULT_PLACE_DISTANCE); //default 1000m?

    }
    private LatLngBounds convertCenterAndRadiusToBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }
    private void centerMapOnMyLocation(){
        if (lastLocation != null)
        {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))      // Sets the center of the map to location user
                .zoom(14)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to north
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }
    }

    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
     * String...)
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            Log.i(TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final com.google.android.gms.location.places.Place place = places.get(0);

            //remove the keyboard from the screen
            txtSearch.clearFocus();
            //hide keyboard
            InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(txtSearch.getWindowToken(), 0);

            // Format details of the place for display and show it in a TextView.
            lblPlaceTitle.setText("Name: "+ place.getName());
            //formatPlaceDetails(getResources(), place.getName(), place.getId(), place.getAddress(), place.getPhoneNumber(),                    place.getWebsiteUri()));
            String website = "";
            if (place.getWebsiteUri() != null)
                website = place.getWebsiteUri().toString();
            lblScrollView.setText("ID: " + place.getId() +
                    "\nAddress: " + place.getAddress() +
                    "\nLat/Lang: " + place.getLatLng().toString() +
                    "\nPhone: " + place.getPhoneNumber() +
                    "\nWebsite: " + website);

            //anchor the panel
            //slidingPanelLayout.setAnchorPoint(PANEL_ANCHORED);
            slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

            //move the map
            CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(place.getLatLng().latitude, place.getLatLng().longitude))      // Sets the center of the map to location user
                .zoom(14)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to north
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            //add a marker
            map.addMarker(new MarkerOptions()
                    .position(place.getLatLng())
                    .title(place.getName().toString()));

            // Display the third party attributions if set.
            /*final CharSequence thirdPartyAttribution = places.getAttributions();
            if (thirdPartyAttribution == null) {
                lblPlaceAddress.setVisibility(View.GONE);
            } else {
                lblPlaceAddress.setVisibility(View.VISIBLE);
                lblPlaceAddress.setText(Html.fromHtml(thirdPartyAttribution.toString()));
            }*/

            Log.i(TAG, "Place details received: " + place.getName());

            places.release();
        }
    };

    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed( ConnectionResult connectionResult) {

        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}
