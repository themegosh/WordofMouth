package ca.dmdev.test.wom;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
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

import ca.dmdev.test.wom.acccount.User;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String[] INITIAL_PERMS={
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.READ_CONTACTS
    };
    private static final int INITIAL_REQUEST=1337;
    private static final int LOCATION_REQUEST=INITIAL_REQUEST+3;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private static final float PANEL_ANCHORED = 0.7f;
    private static final float PANEL_EXPANDED = 1.0f;
    public static final int DEFAULT_PLACE_DISTANCE = 1000;
    private static final String TAG = MainActivity.class.getName();

    protected WordOfMouth wom;

    private SlidingUpPanelLayout slidingPanelLayout;
    private Toolbar toolbar;
    private FloatingActionButton fabAddLocation;
    private GoogleMap map;
    private FragmentManager fragmentManager;
    private TextView lblPlaceTitle;
    private Circle distanceCircle;

    //slider view related
    private RelativeLayout viewDistanceSelector;
    private Slider sliderDistance;
    private ImageButton btnSliderCheck;

    //search view related
    private AutoCompleteTextView txtSearch;
    private MenuItem btnSearch;
    private MenuItem btnCloseSearch;

    private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    protected GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wom = (WordOfMouth)getApplication();
        fragmentManager = getFragmentManager();

        //Initialize activity related resources, the order here is important
        initializeToolbar();
        initializeDrawerLayout();
        initializeSlidingPanel();
        initializeFab();
        initializeNavPanel();
        initializePermissions();
        initializeMap();
        initializePlacesApi();
        initializeDistanceSlider();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    public void onStop(){
        super.onStop();
    }
    @Override
    protected void onResume() {
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

            slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

            //hide keyboard
            InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(txtSearch.getWindowToken(), 0);

            //hide the bottom panel

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    wom.initializeLocation();
                    centerMapOnMyLocation();
                    Log.d(TAG, "onRequestPermissionsResult: Allowed");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d(TAG, "onRequestPermissionsResult: Denied");

                    Dialog dialog = new Dialog(MainActivity.this, "Title", "Message");
                    dialog.show();

                }
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

        //comply with google's TOS and don't hide the logo
        map.setPadding(0,0,0,(int)getResources().getDimension(R.dimen.sliding_toolbar_height));

        centerMapOnMyLocation();


    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        btnSearch = menu.findItem(R.id.btnSearch);
        btnCloseSearch = menu.findItem(R.id.btnCloseSearch);
        return true;
    }



    private void initializeSlidingPanel(){
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
            }

            @Override
            public void onPanelCollapsed(View panel) {
                Log.i(TAG, "onPanelCollapsed");

                //comply with google's TOS and don't hide the logo
                map.setPadding(0,0,0,(int)getResources().getDimension(R.dimen.sliding_toolbar_height));
            }


            @Override
            public void onPanelAnchored(View panel) {
                Log.i(TAG, "onPanelAnchored");
                map.setPadding(0, (int)(PANEL_ANCHORED * 750), 0, (int)(PANEL_ANCHORED * 950));
            }

            @Override
            public void onPanelHidden(View panel) {
                Log.i(TAG, "onPanelHidden");
            }
        });
        slidingPanelLayout.setAnchorPoint(PANEL_ANCHORED);
        //slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        ImageButton btnPhonePlace = (ImageButton) findViewById(R.id.btnPhonePlace);
        btnPhonePlace.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (wom.getSelectedPlace() != null) {
                            if (wom.getSelectedPlace().getPhone() != null) {
                                String uri = "tel:" + wom.getSelectedPlace().getPhone();
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse(uri));
                                startActivity(intent);
                            }
                        }
                    }
                }
        );

        ImageButton btnWebPlace = (ImageButton) findViewById(R.id.btnWebPlace);
        btnWebPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wom.getSelectedPlace().getUrl() != null) {
                    Log.d(TAG, "-------- WEBSITE ----------- " +  wom.getSelectedPlace().getUrl());
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(wom.getSelectedPlace().getUrl());
                    startActivity(i);
                }
            }
        });


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
    private void initializePlacesApi(){
        if (wom.getLastLocation() != null) {
            Location loc = wom.getLastLocation();
            LatLngBounds latLngBounds = convertCenterAndRadiusToBounds(new LatLng(loc.getLatitude(), loc.getLongitude()), DEFAULT_PLACE_DISTANCE);

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, 0 /* clientId */, this)
                    .addApi(Places.GEO_DATA_API)
                    .build();

            placeAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, latLngBounds,
                    null);
            txtSearch.setAdapter(placeAutocompleteAdapter);

            //lblPlaceTitle = (TextView) findViewById(R.id.lblPlaceTitle);
        }
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
                    Intent reviewActivityIntent = new Intent(MainActivity.this,
                            AddReviewActivity.class);

                    startActivityForResult(reviewActivityIntent, 1);
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

        String name = User.getInstance().getFirstName() + " " + User.getInstance().getLastName();
        profileName.setText(name);
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
                            if (wom.getLastLocation() != null) {
                                Location loc = wom.getLastLocation();
                                CircleOptions distanceCircleOptions = new CircleOptions()
                                        .center(new LatLng(loc.getLatitude(), loc.getLongitude()))
                                        .radius(i)
                                        .strokeColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimaryDark))
                                        .fillColor(R.color.colorPrimaryDark);
                                distanceCircle = map.addCircle(distanceCircleOptions);
                            }
                        }

                        if (placeAutocompleteAdapter != null && wom.getLastLocation() != null){
                            Location loc = wom.getLastLocation();
                            placeAutocompleteAdapter.setBounds(convertCenterAndRadiusToBounds(new LatLng(loc.getLatitude(), loc.getLongitude()), i));
                        }
                    }
                }
        );
        sliderDistance.setValue(DEFAULT_PLACE_DISTANCE); //default 1000m?

    }

    private void centerMapOnMyLocation(){
        if (wom.getLastLocation() != null)
        {
            Location loc = wom.getLastLocation();
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(loc.getLatitude(), loc.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(loc.getLatitude(), loc.getLongitude()))      // Sets the center of the map to location user
                .zoom(14)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to north
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }
    }


    private LatLngBounds convertCenterAndRadiusToBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
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
            final AutocompletePrediction item = placeAutocompleteAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            //final CharSequence primaryText = item.getPrimaryText(null);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
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

            CharSequence placeName = place.getName();
            //lblPlaceTitle.setText(placeName);
            //formatPlaceDetails(getResources(), place.getName(), place.getId(), place.getAddress(), place.getPhoneNumber(),place.getWebsiteUri()));
            String website = "";
            if (place.getWebsiteUri() != null)
                website = place.getWebsiteUri().toString();


            TextView locationDescription = (TextView) findViewById(R.id.loc_desc);

            wom.setSelectedPlace(new PlaceLocation(place));

            locationDescription.setText("ID: " + place.getId() +
                    "\nAddress: " + place.getAddress() +
                    "\nLat/Lang: " + place.getLatLng().toString() +
                    "\nPhone: " + place.getPhoneNumber() +
                    "\nWebsite: " + website);



            //anchor the panel
            //slidingPanelLayout.setAnchorPoint(PANEL_ANCHORED);
            slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

            updatePlacePhoto(place.getId());

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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    private void updatePlacePhoto(String placeId) {

        final ImageView mImageView = (ImageView)findViewById(R.id.loc_img);

        // Create a new AsyncTask that displays the bitmap and attribution once loaded.
        new PlacesPhotoTask(mImageView.getWidth(), mImageView.getHeight(), mGoogleApiClient) {
            @Override
            protected void onPreExecute() {
                // Display a temporary image to show while bitmap is loading.
                mImageView.setImageResource(R.drawable.placeholder);
            }

            @Override
            protected void onPostExecute(AttributedPhoto attributedPhoto) {
                if (attributedPhoto != null) {
                    // Photo has been loaded, display it.
                    mImageView.setImageBitmap(attributedPhoto.bitmap);

                    // Display the attribution as HTML content if set.
                    /*if (attributedPhoto.attribution == null) {
                        mText.setVisibility(View.GONE);
                    } else {
                        mText.setVisibility(View.VISIBLE);
                        mText.setText(Html.fromHtml(attributedPhoto.attribution.toString()));
                    }*/

                }
            }
        }.execute(placeId);
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
