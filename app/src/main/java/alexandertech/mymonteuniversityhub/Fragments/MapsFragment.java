package alexandertech.mymonteuniversityhub.Fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import alexandertech.mymonteuniversityhub.Activities.MainActivity;
import alexandertech.mymonteuniversityhub.R;

import static android.content.Context.MODE_PRIVATE;


public class MapsFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    Button saveUserLocationB;
    Button retrieveUserLocationB;
    GoogleMap mGoogleMap;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Location savedLocation;
    Marker mCurrLocationMarker;
    Marker lastParkedMarker;
    private LocationSettingsRequest.Builder builder;
    private PendingResult<LocationSettingsResult> result;
    MapView mMapView;
    Boolean isFragVisible = false;
    SharedPreferences sharedPreference;
    public static final String IS_AGREED = "Agree";
    public static final String PARKING_LOCATION = "";
    private boolean isAgreed = false;
    private LocationListener mLocateListener;
    double latitude, longitude;
    double lastLatitude, lastLongitude;
    Location mLocation = null;


    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container,
                             Bundle savedInstanceState){
        View v = inflator.inflate(R.layout.fragment_maps, container, false);
        mMapView = (MapView) v.findViewById(R.id.googleMap);
        saveUserLocationB = (Button)v.findViewById(R.id.saveLocationButton);
        retrieveUserLocationB = (Button)v.findViewById(R.id.retrieveLocation);
        sharedPreference = getActivity().getSharedPreferences(MainActivity.MYPREFERENCE, MODE_PRIVATE);
        isAgreed = sharedPreference.getBoolean(IS_AGREED , false);

        if(!isAgreed){
            showDialog();
        }

        saveUserLocationB.setOnClickListener(this);
        retrieveUserLocationB.setOnClickListener(this);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        checkLocationPermission();
        return v;
    }

    public void showDialog(){

        new AlertDialog.Builder(getActivity())
                .setTitle("Location permission needed")
                .setMessage("This app needs your location.," +
                        " Press ok to share your location.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //prompt the user once the explanation has been shown
                        SharedPreferences.Editor editor = sharedPreference.edit();
                        editor.putBoolean(IS_AGREED,true);
                        editor.commit();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            isFragVisible = true;
        }else
            isFragVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MapsFragment", "Inside onResume");
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        Log.d("MapsFragment", "Inside setUpMapIfNeeded");

        if (mGoogleMap == null) {
            mMapView.getMapAsync(this);
        }
    }

    //This method gets called when the activity's view is obstructed.
    @Override
    public void onPause() {
        super.onPause();
        Log.d("MapsFragment", "Inside onPause");

        mMapView.onPause();
        //Stop location updates when Activity is no longer active
        if(mGoogleApiClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,mLocateListener);
        }
    }

    //Call back method for when map is ready to be used.
    @Override
    public void onMapReady(GoogleMap googleMap){
        Log.d("MapsFragment", "Inside onMapReady");

        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        //Checking if Google Play Services is within the Phone
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //Checking if we have access to location
            if(ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                //Location Permission already granted
                buildGoogleApiClient();
                //Enables or disables the my-location layer
                mGoogleMap.setMyLocationEnabled(true);
            }else{
                //Request location permission
                if(isFragVisible){
                    checkLocationPermission();
                }
                //checkLocationPermission();
            }
        }
        else{
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }
    }


    private void buildGoogleApiClient() {
        Log.d("MapsFragment", "Inside buildGoogleApiClient");

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    //After calling connect(), this method will be invoked asynch when the
    //connect request has successfully completed.
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("MapsFragment", "Inside onConnected");


        mLocateListener  = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(mLocation == null){
                    mLocation = location;
                    latitude =location.getLatitude();
                    longitude =location.getLongitude();
                    Toast.makeText(getActivity(), "Location Changed\nLatitude:" +latitude + "\nLongitude:" + longitude, Toast.LENGTH_SHORT).show();
                    goToLocation(latitude,longitude, 19);
                }

            }
        };
        //Next I need to set up my locationRequest
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(2);

        //to register your request
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, mLocateListener);
       /* mLocationRequest =  new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }*/
    }
    private void goToLocation(double lat, double lng, float zoom) {
        LatLng latLng = new LatLng(lat,lng);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

        //LatLng latLng = new LatLng(lat, lng);//represents a location on the map.
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mGoogleMap.animateCamera(cameraUpdate);// moveCamera() or animateCamera() are used to change how map looks
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    /*@Override
    public void onLocationChanged(Location location) {
        Log.d("MapsFragment", "Inside onLocationChanged");


        if(mLastLocation == null) {
            mLastLocation = location;
            mGoogleMap.clear();
            //Place current location marker
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
            //Move map Camera
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomBy(14));

        }
        mLastLocation = location;
        if(mCurrLocationMarker != null){
            mGoogleMap.clear();
            mCurrLocationMarker.remove();
        }

        Toast.makeText(getActivity(), "Location Changed", Toast.LENGTH_LONG).show();

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

        //Move map Camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomBy(17));

        //Optionally, stop location updates if only current location is needed
        if(mGoogleApiClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        }
    }
    */


    private void checkLocationPermission() {
        Log.d("MapsFragment", "Inside checkLocationPermission");

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            //Should we show an explanation?
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)){

                //Show an explanation to the user *asyncly* -- dont block
                //this thread waiting for the user's response! After the user
                //sees the explanation try again to request the permission
                new AlertDialog.Builder(getActivity())
                        .setTitle("Location permission needed")
                        .setMessage("This app needs the Location permission," +
                                " please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                //prompt the user once the explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                //NO explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[]
            , int[] grantResults){
        Log.d("MapsFragment", "Inside onRequestPermissionsResult");

        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_LOCATION:{
                //If request is cancelled, the results arrays are empty
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Permission was granted do the location-related task you need to do.
                    if(ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED){

                        if(mGoogleApiClient == null){
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }
                }
                else{
                    //permissions were denied.Disable the functionality that depends on this permission
                    Toast.makeText(getActivity(), "Permission denied",Toast.LENGTH_LONG).show();
                }
                return;
            }//Other case lines to check for other permissions this app might request
        }

    }



    public void goToCurrentLocation(){

    }


    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.saveLocationButton:
                //Toast.makeText(getActivity(), "Pressed",Toast.LENGTH_LONG).show();
                Toast.makeText(getActivity(), String.valueOf(mLocation),Toast.LENGTH_LONG).show();
                SharedPreferences.Editor editor = sharedPreference.edit();
                String parking  = mLocation.getLatitude() + "," + mLocation.getLatitude();
                editor.putString(PARKING_LOCATION,parking);
                editor.commit();
                //savedLocation = mLocation;
                break;
            case R.id.retrieveLocation:
               // Toast.makeText(getActivity(), String.valueOf(savedLocation), Toast.LENGTH_LONG).show();
                //Place last location marker
                if(sharedPreference.contains(PARKING_LOCATION)){
                    String[] locationArray = sharedPreference.getString(PARKING_LOCATION,"").split(",");
                    Double retrievedLat = Double.parseDouble(locationArray[0]);
                    Double retrievedLong = Double.parseDouble(locationArray[1]);

                    LatLng latlng = new LatLng(retrievedLat,retrievedLong);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latlng);
                    markerOptions.title("Last Parked Position");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    lastParkedMarker = mGoogleMap.addMarker(markerOptions);
                }
                else{
                    Toast.makeText(getActivity(),"You have not saved your location", Toast.LENGTH_LONG).show();

                }
               /*if(savedLocation != null){
                    LatLng latLng = new LatLng(savedLocation.getLatitude(),savedLocation.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Last Parked Position");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    lastParkedMarker = mGoogleMap.addMarker(markerOptions);
                }else{
                    Toast.makeText(getActivity(),"You have not saved your location", Toast.LENGTH_LONG).show();

                }
                */

        }

    }


}
