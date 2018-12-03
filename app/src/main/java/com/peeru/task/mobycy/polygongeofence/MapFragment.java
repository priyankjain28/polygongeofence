package com.peeru.task.mobycy.polygongeofence;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Created by Priyank Jain on 12-11-2018.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback{

    private static final String TAG = "NewTagFragment";
    private static final int REQUEST_PERMISSION_LOCATION = 34839;
    private static final String GEOFENCE_ID = TAG;
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Circle mCircle;
    private Polygon mPolygon;
    private ImageView markerIcon;
    private static final int LOCATION_REQUEST_CODE = 101;
    LatLng gpsLatLng;
    Location gpsLocation;
    SupportMapFragment mapFragment;
    Button nextButton, saveButton;
    LatLng mLatLng;
    ImageView gpsButton;
    protected LocationRequest locationRequest;
    int REQUEST_CHECK_SETTINGS = 500;
    private List<Marker> mMarkers = new ArrayList<>();
    private List<LatLng> mLatLngList = new ArrayList<>();
    private Handler handler = new Handler();
    private Boolean flag = true;
    private View view;
    private FusedLocationProviderClient mFusedLocationClient;
    private static String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, null, false);
        checkPermit(getContext(), permissions, 1000);

        mFusedLocationClient = getFusedLocationProviderClient(getContext());

        return view;
    }

    public void showAlertMessage(String msg) {
        LayoutInflater inflater = getLayoutInflater();
        View layouttoast = inflater.inflate(R.layout.toastcustom, null, false);
        Toast toast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        int[] values = new int[2];
        view.getLocationOnScreen(values);
        int x = values[0];
        int y = values[1];
        toast.setView(layouttoast);
        //toast.getView().setBackgroundResource(R.color.colorPrimary);
        toast.setGravity(Gravity.TOP | Gravity.CENTER, x, y);
        toast.show();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        gpsButton = view.findViewById(R.id.gps_button);
        markerIcon = view.findViewById(R.id.icon_marker);
        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapReload();
            }
        });
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void mapReload() {
        checkLocationandAddToMap();
    }


    public void onStart() {
        super.onStart();
        googleApiClient.connect();

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        //TO Map Call
    }

    public void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationandAddToMap();
    }

    @Override
    public void onConnected(Bundle bundle) {
        //Utility.checkPermissions(getContext());
        checkLocationService();
        mapFragment = (SupportMapFragment)
                this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void checkLocationService() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        googleApiClient,
                        builder.build()
                );
        result.setResultCallback(this);
    }
/*
    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();

        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // NO need to show the dialog;
                break;

            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                //  Location settings are not satisfied. Show the user a dialog
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().

                    status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);

                } catch (IntentSender.SendIntentException e) {

                    //failed to show
                }
                break;

            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are unavailable so not possible to show any dialog now
                break;
        }


    }*/


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    public void checkLocationandAddToMap() {
        //Checking if the user has granted the permission
        callGpsToAccessFineLocation();

    }

    private void plotMapOnLocationBasis() {
        mLatLng = new LatLng(gpsLatLng.latitude, gpsLatLng.longitude);
        final MarkerOptions marker = new MarkerOptions().position(mLatLng).title("You are Here");// LatLng : "+ gpsLatLng.latitude+","+gpsLatLng.longitude);
        //addMarkerOnMap(marker);
        markerIcon.setVisibility(View.VISIBLE);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        //mMap.setMinZoomPreference(16.0f);


        //mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        float zoomLevel = 10.0f; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, zoomLevel));

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                //float[] distance = new float[2];
                LatLng latlng = mMap.getCameraPosition().target;
                mLatLng = new LatLng(latlng.latitude, latlng.longitude);
                if (PolyUtil.containsLocation(mLatLng, mLatLngList, false)) {
                    mLatLng = new LatLng(latlng.latitude, latlng.longitude);
                }else{
                    showAlertMessage("Please select location in given Geofence");
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
                }
                //Location.distanceBetween(latlng.latitude, latlng.longitude,mCircle.getCenter().latitude, mCircle.getCenter().longitude, distance);
                /*if (distance[0] <= mCircle.getRadius()) {
                    //callGpsToAccessFineLocation();
                    //addCircleOnGpsBasis();
                    mLatLng = new LatLng(latlng.latitude, latlng.longitude);
                } else {
                    // flag = false;
                    // Toast.makeText(getContext(),"Please select location in given Geofence",Toast.LENGTH_LONG).show();
                    showAlertMessage("Please select location in given Geofence");
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
                }*/
            }
        });
    }

    public boolean checkPermit(Context context, String[] permissions, int requestCode) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(getContext(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions((Activity) context, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), requestCode);
            return false;
        }
        return true;
    }
    private void callGpsToAccessFineLocation() {

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            //Requesting the Location permission
            //Utility.checkPermissions(getContext());
            callGpsToAccessFineLocation();
            return;
        }
        getFusedLocationProviderClient(getContext()).requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }


    public void onLocationChanged(Location location) {
        // New location has now been determined

        // You can now create a LatLng Object for use with maps
        gpsLocation = location;
        mMap.clear();
        //saveButtonEnableDisable(true);
        addCircleOnGpsBasis();
        plotMapOnLocationBasis();
    }



    private void addCircleOnGpsBasis() {
        gpsLatLng = new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude());
        //drawMarkerWithCircle(gpsLatLng);
        drawMarkerWithPolygon();
        //MarkerOptions are used to create a new Marker.You can specify location, title etc with MarkerOptions
    }


    public Bitmap getMarkerIcon() {
        int height = 100;
        int width = 74;
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.mipmap.marker2);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        return smallMarker;
    }



    private void drawMarkerWithCircle(LatLng position) {
        double radiusInMeters = 25.0;
        int strokeColor = 0xf56C98F; //Green outline
        int shadeColor = 0x4456C98F; //opaque Green fill

        CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        mCircle = mMap.addCircle(circleOptions);
    }

    private void drawMarkerWithPolygon(){
        mLatLngList = dummyPoints();
        if (mLatLngList != null) {
            for (LatLng latLng : mLatLngList) {
                mMarkers.add(mMap.addMarker(new MarkerOptions().position(latLng).draggable(true)));
            }
            redraw();
        }
    }
    private List<LatLng> dummyPoints() {
        List<LatLng> points = new ArrayList<LatLng>();
        points.add(new LatLng( 28.444247, 77.036774));
        points.add(new LatLng(28.414344, 77.080014));
        points.add(new LatLng(28.436954, 77.122606));
        points.add(new LatLng(28.468362, 77.108214));
        points.add(new LatLng(28.477139, 77.068723));
      /*  points.add(new LatLng(25.902550, 74.656180));
        points.add(new LatLng(25.902277, 74.662052));
        points.add(new LatLng(25.907267, 74.663136));
        points.add(new LatLng(25.909236, 74.656645));*/
        return points;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mMap != null) {
            mMap.clear();
        }
    }
    private static ArrayList<LatLng> convertMarkersToLatlng(List<Marker> markers) {
        ArrayList<LatLng> points = new ArrayList<>();
        for (Marker marker : markers) {
            points.add(marker.getPosition());
        }
        return points;
    }

    private void redraw() {
        if (mMarkers.size() <= 2) {
            return;
        }
        if (mPolygon != null) {
            mPolygon.remove();
        }
        mPolygon = mMap.addPolygon(new PolygonOptions().addAll(convertMarkersToLatlng(mMarkers))
                .strokeColor(Color.RED).fillColor(0x4456C98F));

        if (mCircle != null) {
            mCircle.remove();
        }

    }
    private void refreshGeofences() {
        removeGeofences();
        List<Geofence> geofences = new ArrayList<>();
        Geofence geofence = new Geofence.Builder()
                .setRequestId(GEOFENCE_ID)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setCircularRegion(mCircle.getCenter().latitude, mCircle.getCenter().longitude,
                        (float) mCircle.getRadius())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setLoiteringDelay(1000)
                .build();
        geofences.add(geofence);
        addGeofences(geofences);
    }

    private void addGeofences(List<Geofence> geofenceList) {
        if (geofenceList == null) {
            throw new IllegalArgumentException("Argument 'geofenceList' cannot be null.");
        }

        if (!googleApiClient.isConnected()) {
            Log.d(TAG, "Google API client is not connected!");
            return;
        }

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    createGeofencingRequest(geofenceList),
                    GeofenceTransitionIntentService.newPendingIntent(getContext(), convertMarkersToLatlng(mMarkers))
            ).setResultCallback(this);
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_LOCATION);
        }
    }

    private void removeGeofences() {
        Log.d(TAG, "removeGeofences()");
        if (googleApiClient == null) {
            Log.d(TAG, "Failed to remove geofence because Google API client is null!");
            return;
        }
        List<String> geofences = new ArrayList<>();
        geofences.add(GEOFENCE_ID);
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, geofences);
    }

    private static GeofencingRequest createGeofencingRequest(List<Geofence> geofenceList) {
        if (geofenceList == null) {
            throw new IllegalArgumentException("Argument 'geofenceList' cannot be null.");
        }
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofences(geofenceList)
                .build();
    }


    @Override
    public void onResult(@NonNull Result result) {

    }
}
