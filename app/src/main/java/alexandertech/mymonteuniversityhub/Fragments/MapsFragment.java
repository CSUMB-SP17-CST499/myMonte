package alexandertech.mymonteuniversityhub.Fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
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
import com.google.android.gms.location.LocationAvailability;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import alexandertech.mymonteuniversityhub.Activities.MainActivity;
import alexandertech.mymonteuniversityhub.Classes.DataParser;
import alexandertech.mymonteuniversityhub.R;

import static alexandertech.mymonteuniversityhub.Activities.MainActivity.prefs;
import static alexandertech.mymonteuniversityhub.Activities.MainActivity.sharedPrefs;
import static android.content.Context.MODE_PRIVATE;


public class MapsFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Location mLastLocation;
    private MapView mMapView;
    Button saveParkingLocation;
    Button lastParkingLocation;
    LatLng currentParkingLocation;
    String serverKey = "AIzaSyCSNFes9NZKUDrslyRG8Z-9qQ9X37df1Os";
    Polyline line;
    private ArrayList<LatLng> locationList;


    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflator.inflate(R.layout.fragment_maps, container, false);
        mMapView = (MapView) view.findViewById(R.id.mapview);
        saveParkingLocation = (Button) view.findViewById(R.id.saveLocationButton);
        saveParkingLocation.setOnClickListener(this);
        lastParkingLocation = (Button) view.findViewById(R.id.lastParkingLocationButton);
        lastParkingLocation.setOnClickListener(this);

        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //checkLocationPermission();
        return view;
    }

    //Call back method for when map is ready to be used.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MapsFragment", "Inside onMapReady");
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng csumb = new LatLng(36.6524204, -121.79731630000003);
        mGoogleMap.addMarker(new MarkerOptions().position(csumb).title("Marker in CSUMB"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(csumb, 18));
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

    }

    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        //Enables the blue dot and gps button that when tapped centers the map on user location.
        mGoogleMap.setMyLocationEnabled(true);
        //Determining the availability of location data on the device.
        LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);
        if (null != locationAvailability && locationAvailability.isLocationAvailable()) {
            //Gives the most recent location current available.
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            //If we were able to retrieve the recent location, move the camera to the users current location.
            if (mLastLocation != null) {
                LatLng currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                currentParkingLocation = currentLocation;
                // locationList.add(0,currentLocation);//adding current location in arraylist
                //placeMarkerOnMap(currentLocation);
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
            }
        }
    }

    protected void placeMarkerOnMap(LatLng location) {
        //Creates obj and sets the users current location as the position for marker.
        MarkerOptions markerOptions = new MarkerOptions().position(location);
        mGoogleMap.addMarker(markerOptions);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveLocationButton:
                if (currentParkingLocation == null) {
                    Toast.makeText(getActivity(), "Turn on GPS and press GPS button to set your location", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), currentParkingLocation.toString(), Toast.LENGTH_SHORT).show();
                    String longitude = String.valueOf(currentParkingLocation.longitude);
                    String latitude = String.valueOf(currentParkingLocation.latitude);
                    prefs.putString("location", latitude + "," + longitude);
                    prefs.apply();
                    Toast.makeText(getActivity(), "Saved!", Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.lastParkingLocationButton:
                Toast.makeText(getActivity(), "Works", Toast.LENGTH_LONG).show();

                if (sharedPrefs.contains("location")) {
                    String[] locationArray = sharedPrefs.getString("location", "").split(",");
                    Double retrievedLat = Double.parseDouble(locationArray[0]);
                    Double retrievedLong = Double.parseDouble(locationArray[1]);
                    Toast.makeText(getActivity(), retrievedLat + " and " + retrievedLong, Toast.LENGTH_SHORT).show();

                    LatLng latlng = new LatLng(retrievedLat, retrievedLong);
                    // locationList.add(1,latlng);//adding parking location
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latlng);
                    markerOptions.title("Last Parked Position");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    mGoogleMap.addMarker(markerOptions);
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 18));

                } else {
                    Toast.makeText(getActivity(), "No saved location", Toast.LENGTH_SHORT).show();
                }
        }
    }

    /*private void build_retrofit_and_get_response(String type){
        String url = "https://maps.googleapis.com/maps/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitMaps service = retrofit.create(RetrofitMaps.class);
        Call<Example> call = service.getDistanceDuration("metric", mLastLocation.getLatitude() + "," + mLastLocation.getLongitude(), currentParkingLocation.latitude + "," + currentParkingLocation.longitude, type);
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Response<Example> response, Retrofit retrofit) {
                for(int i = 0; i < response.body().getRoutes().size();i++){
                    //String distance = response.body().getRoutes().get(i).getLegs().get(i).getDistance().getText();
                   // String time = response.body().getRoutes().get(i).getLegs().get(i).getDuration().getText();
                    //ShowDistanceDuration.setText("Distance:" + distance + ", Duration:" + time);
                    String encodedString = response.body().getRoutes().get(0).getOverviewPolyline().getPoints();
                    List<LatLng> list = decodePoly(encodedString);
                    line = mGoogleMap.addPolyline(new PolylineOptions()
                            .addAll(list)
                            .width(20)
                            .color(Color.RED)
                            .geodesic(true)
                    );
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });


    }*/

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mMapView != null)
            mMapView.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
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
        setUpMap();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mGoogleMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }

    }
}
