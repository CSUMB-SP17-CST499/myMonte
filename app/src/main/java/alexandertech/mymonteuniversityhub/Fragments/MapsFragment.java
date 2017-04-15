package alexandertech.mymonteuniversityhub.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kishan.askpermission.AskPermission;
import com.kishan.askpermission.ErrorCallback;
import com.kishan.askpermission.PermissionCallback;
import com.kishan.askpermission.PermissionInterface;
import alexandertech.mymonteuniversityhub.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import static alexandertech.mymonteuniversityhub.Activities.MainActivity.prefs;
import static alexandertech.mymonteuniversityhub.Activities.MainActivity.sharedPrefs;


public class MapsFragment extends Fragment implements PermissionCallback, ErrorCallback,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final int REQUEST_LOCATION = 20;
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private Location mLastLocation;
    private MapView mMapView;
    LatLng currentParkingLocation;
    @BindView(R.id.showDirection) FloatingActionButton showDirectionFAB;
    @BindView(R.id.saveFAB)FloatingActionButton saveFAB;
    @BindView(R.id.GPSFAB)FloatingActionButton GPSFAB;

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflator.inflate(R.layout.fragment_maps, container, false);
        ButterKnife.bind(this, view);
        mMapView = (MapView) view.findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        Log.d("OnCreate", "Inside onCreate");
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        return view;
    }

    @OnClick(R.id.saveFAB)
    public void onSaveFAB(){
        final LocationManager manager = (LocationManager)
                getActivity().getSystemService (Context.LOCATION_SERVICE );

        if (!manager.isProviderEnabled( LocationManager.GPS_PROVIDER) ) {
            Snackbar.make(getView(), "You must obtain your location first!",
                    Snackbar.LENGTH_LONG).show();
        }
        else if(currentParkingLocation == null){
            Snackbar.make(getView(), "No location captured.",Snackbar.LENGTH_LONG).show();
        }
        else if(currentParkingLocation!= null){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String longitude = String.valueOf(currentParkingLocation.longitude);
                    String latitude = String.valueOf(currentParkingLocation.latitude);
                    prefs.putString("location", latitude + "," + longitude);
                    prefs.apply();
                    Snackbar.make(getView(), "Parking Location saved!", Snackbar.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Snackbar.make(getView(), "Canceled saving the parking location",
                            Snackbar.LENGTH_SHORT).show();

                }
            });
            builder.setMessage(R.string.dialog_message)
                    .setTitle(R.string.dialog_title);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @OnClick(R.id.showDirection)
    public void onShowDirection(){
        if (sharedPrefs.contains("location")) {
            String[] locationArray = sharedPrefs.getString("location", "").split(",");
            Double retrievedLat = Double.parseDouble(locationArray[0]);
            Double retrievedLong = Double.parseDouble(locationArray[1]);
            Toast.makeText(getActivity(), retrievedLat + " and "
                    + retrievedLong, Toast.LENGTH_SHORT).show();

            LatLng latlng = new LatLng(retrievedLat, retrievedLong);
            // locationList.add(1,latlng);//adding parking location
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latlng);
            markerOptions.title("Last Parked Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mGoogleMap.addMarker(markerOptions);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 18));

//            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
//                    Uri.parse("http://maps.google.com/maps?saddr=36.653875,-121.801811&daddr=36.652414, -121.796707"));
//            startActivity(intent);
        } else {
            Snackbar.make(getView(), "No saved Location", Snackbar.LENGTH_LONG).show();
        }

    }

    @OnClick(R.id.GPSFAB)
    public void onGPSFAB(){
        askPermissions();
    }

    private void askPermissions(){
        Log.d("On AskPermissions ", " permissions method");
        new AskPermission.Builder(this).setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .setCallback(this)
                .setErrorCallback(this)
                .request(REQUEST_LOCATION);
    }
    @Override
    public void onShowRationalDialog(final PermissionInterface permissionInterface, int requestCode) {
        Log.d("Onshowrational", "showing the dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("We need location permissions to get your location");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               permissionInterface.onDialogShown();
            }
        });
        builder.setNegativeButton("Cancel",null);
        builder.show();
    }

    @Override
    public void onShowSettings(PermissionInterface permissionInterface, int requestCode) {

    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        Log.d("Permissions granted", "On permissions granted method");
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        final PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()){
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        Log.d("SettingRequest", "inside Success");
                        getUserLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.d("SettingRequest", "inside resolution required");
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            Log.d("SettingsRequest", "Inside resolution required- try statement");
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                            //getUserLocation();
                        }catch (IntentSender.SendIntentException e){
                            //ignore this error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });

    }

    @Override
    public void onPermissionsDenied(int requestCode) {
        Log.d("Permissions denied", "On permissions denied method");
        Snackbar.make(getView(), "Permissions denied",Snackbar.LENGTH_SHORT).show();
    }


    //Call back method for when map is ready to be used.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MapsFragment", "Inside onMapReady");
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        LatLng csumb = new LatLng(36.653758, -121.798056);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(csumb,17));
    }

    public void getUserLocation(){
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("getUserLocation", "inside the function and permission not granted");
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        //Determining the availability of location data on the device.
        LocationAvailability locationAvailability =
                LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);
        if (null != locationAvailability && locationAvailability.isLocationAvailable()) {
            //Gives the most recent location current available.
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            //If we were able to retrieve the recent location, move the camera to the users current location.
            if (mLastLocation != null) {
                LatLng currentLocation =
                        new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                currentParkingLocation = currentLocation;
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
            }
        }
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    public void settingsRequest(){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        final PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()){
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d("SettingRequest", "inside Success");
                        getUserLocation();
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.d("SettingRequest", "inside resolution required");

                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            Log.d("SettingsRequest", "Inside resolution required- try statement");

                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                            //getUserLocation();
                        }catch (IntentSender.SendIntentException e){
                            //ignore this error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }


    @Override
    public void onResume() {
        Log.d("MapsFragment", "Inside onResume");

        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onDestroy() {
        Log.d("MapsFragment", "Inside onDestroy");
        super.onDestroy();
        if (mMapView != null)
            mMapView.onDestroy();
    }

    @Override
    public void onStart() {
        Log.d("MapsFragment", "Inside onStart");
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        Log.d("MapsFragment", "Inside onStop");

        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("MapsFragment", "Inside onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Snackbar.make(getView(), "Connection was suspended",Snackbar.LENGTH_LONG).show();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Snackbar.make(getView(), "Connection failed, try again", Snackbar.LENGTH_LONG).show();
    }
}
