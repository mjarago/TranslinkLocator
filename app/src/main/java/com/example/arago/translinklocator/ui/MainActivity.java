package com.example.arago.translinklocator.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.arago.translinklocator.R;
import com.example.arago.translinklocator.fragments.BusListFragment;
import com.example.arago.translinklocator.models.Bus;
import com.example.arago.translinklocator.models.SaveArrayList;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.arago.translinklocator.Constants.ERROR_DIALOG_REQUEST;
import static com.example.arago.translinklocator.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.arago.translinklocator.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;


/*
    Created by Mark Arago. email: markjosepharago@gmail.com
    Explicitly asking for permission and maps tutorial all goes to Coding with Mitch on youtube.


 */

public class MainActivity extends AppCompatActivity implements android.location.LocationListener{
    private static final String TAG = "MainActivity";

    private boolean locationPermissionGranted = false;
    
    //APi Call
    RequestQueue requestQueue;
    StringRequest objectRequest;
    private ArrayList<Bus> busArrayList;
    SaveArrayList saveArrayList;


    ProgressBar progressBar;

    //Var for getting locations
    public LocationManager locationManager;
    public Criteria criteria;
    public String bestProvider;
    double latitude, longitude;
    ArrayList<String> uniqueRoutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialize variables
        requestQueue = Volley.newRequestQueue(this);
        busArrayList = new ArrayList<Bus>();
        saveArrayList = new SaveArrayList();
        uniqueRoutes = new ArrayList<>();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);



    }

    //Call Map and the List of Buses
    private void initBusListAndMaps() {
        progressBar.setVisibility(View.GONE);
        Log.d(TAG, "getBusListAndMaps: called");

        BusListFragment busFragment = BusListFragment.newInstance();
        Bundle bundle = new Bundle();
        //Add bus list, latitude and longitude as arguemnts
        bundle.putParcelableArrayList("BusList", saveArrayList.getBusArrayList());
        bundle.putDouble("Latitude", latitude);
        bundle.putDouble("Longitude", longitude);
        busFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bus_container, busFragment, getString(R.string.fragment_bus_list));

        transaction.commit();

    }
    //http://api.translink.ca/rttiapi/v1/stops?apikey=[APIKey]&lat=[latitude]&long=[longitude]
    //Returns stops near users latitude/longitude coordinates.

    private void getBusStop(final double lat, final double lng) {
        String apiKey = getString(R.string.translink_api_key);
        Log.d(TAG, "getBusList: called");
        //Returns stops near latitude/longitude coordinates, radius is 1000 meters

        final String url = "http://api.translink.ca/rttiapi/v1/stops?apikey=" + apiKey +
                "&lat=" + lat + "&long=" + lng + "&radius=2000";

        Log.d("getBusStop: URL: ", url);
        objectRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d(TAG,"getBusStop: + onResponse: " + response);
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = new JSONObject();
                            String  stopRoutes = "";

                            ArrayList<String> routes = new ArrayList<>();
                            routes.clear();
                            //get each jsonobject
                            for (int x = 0; x < jsonArray.length(); x++) {
                                jsonObject = jsonArray.getJSONObject(x);
                                //get stoproutes or bus numbers (i.e. 701, 791, 169,.. etc.)
                                stopRoutes = jsonObject.getString("Routes");
                                //Clean spaces from routes. Translink returns empty spaces sometimes
                                stopRoutes = stopRoutes.replace(" ","");
                                //Add all route numbers to routes and split string returns
                                routes.addAll(Arrays.asList(stopRoutes.split(",")));

                            }
                            //remove null and empty spaces
                            routes.removeAll(Arrays.asList(""," ",null));
                            //convert routes to unique only
                            Set setOfRoutes = new HashSet<String>(routes);
                            //convert set to arraylist

                            uniqueRoutes.addAll(setOfRoutes);
                            Log.d(TAG,"getBusStop: Routes: " + uniqueRoutes.toString());
                            //if routes are found request for bus details
                            getBusInfo(uniqueRoutes);

                        } catch (JSONException e) {
                            System.out.println("Error Response: " + e.getMessage());
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"getBusInfo: Error: " + error.getMessage());

                        //if no bus stops found an error will be returned
                        //make an empty array to load up list and map
                        ArrayList<Bus> arrayList = new ArrayList<>();
                        Toast.makeText(MainActivity.this,"No Active Bus/Bus Stops near you",Toast.LENGTH_LONG).show();
                        saveArrayList(arrayList);
                        //Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {//translates api return to json type
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/JSON");
                params.put("api-key", "21mn31dcULj0N9JLDzd7");
                return params;
            }
        };
        requestQueue.add(objectRequest);

    }

    //Get Bus info such as bus name/ route number and direction
    private void getBusInfo(ArrayList<String> routeList){
        Log.d(TAG,"getBusInfo" + routeList.toString());
        String apiKey = getString(R.string.translink_api_key);
        //http://api.translink.ca/rttiapi/v1/buses?apikey=[apiKey]&routeNo=[routeNo]
        //return bus details
        busArrayList.clear();
        for(int x = 0; x < routeList.size(); x++){
            final String routeNo;
            //clear route number
            routeNo = routeList.get(x).trim();
            final String url = "http://api.translink.ca/rttiapi/v1/buses?apikey=" + apiKey +
                    "&routeNo=" + routeNo;

            Log.d("getBusInfo URL: ", url);
            objectRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                JSONObject jsonObject = new JSONObject();



                                int busRouteNo = 0;
                                String busDestination = "", busDirection = "";
                                List<String> items;
                                ArrayList<String> destination = new ArrayList<>();
                                ArrayList<Integer> busNo = new ArrayList<>();
                                ArrayList<String> direction = new ArrayList<>();

                                //get each jsonobject
                                for (int x = 0; x < jsonArray.length(); x++) {
                                    jsonObject = jsonArray.getJSONObject(x);
                                    //int stopNo, String stopName, double stopLatitude,
                                    // double stopLongitude, double stopDistance, String stopRoutes

                                    //busID = Integer.parseInt(jsonObject.getString("VehicleNo"));
                                    busRouteNo = Integer.parseInt(jsonObject.getString("RouteNo"));
                                    busDestination = jsonObject.getString("Destination");
                                    busDirection = jsonObject.getString("Direction");

                                    destination.add(busDestination);
                                    busNo.add(busRouteNo);
                                    direction.add(busDirection);


                                }
                                //Make unique destination or bus names since this request returns multiple buses at a time.
                                Set setOfDestination = new HashSet<String>(destination);
                                ArrayList uniqueDestination = new ArrayList<>(setOfDestination);
                                for(int x = 0; x < uniqueDestination.size(); x++){
                                    Log.d(TAG,"getBusInfo: routeNo: " + routeNo + " destination: " + uniqueDestination.get(x));
                                    busArrayList.add( new Bus(Integer.parseInt(routeNo),uniqueDestination.get(x).toString()));
                                }

                                Log.d(TAG,"Destination:  " +  uniqueDestination.toString());
                                Log.d(TAG,"Bus Array Size:  " +  busArrayList.size());
                                saveArrayList(busArrayList);

                            } catch (JSONException e) {
                                System.out.println(e.getMessage());
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG,"getBusInfo: Error: " + error.getMessage());

                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/JSON");
                    params.put("api-key", "21mn31dcULj0N9JLDzd7");
                    return params;
                }
            };
            requestQueue.add(objectRequest);

        }


    }
    //Just passing off busArrayList to initBusListandMaps
    private void saveArrayList(ArrayList<Bus> arrayList) {
        Log.d(TAG, "saveArrayList : called");
        saveArrayList.setBusArrayList(arrayList);
        initBusListAndMaps();
    }

    //get user's location
    private void getLastLocation(){
        Log.d(TAG, "getLastLocation : called");
        progressBar.setVisibility(View.VISIBLE);
        //check if permission is granted.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        else{
            locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true));
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                Log.e("TAG", "GPS is on");
                //convert latitude and longitude to 5 decimal places. (Translink only accepts up to five)
                latitude = (double) Math.round(location.getLatitude() * 100000d) / 100000d;
                longitude = (double) Math.round(location.getLongitude() * 100000d) / 100000d;

               getBusStop(latitude, longitude);
                Log.d(TAG,"getLastLocation: Latitude: " + latitude);
            }
            else{
                //if location is null request for update on on LocationChanged()
                locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
            }
        }


    }

    //check if services are available
    private boolean checkMapServices() {
        if (isServicesOK()) {
            Log.d(TAG,"checkMapServices: isMapsEnabled");
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }
    //Credits to coding with mitch
    private boolean isServicesOK() {

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
    //check if mops is enabled if not call buildalert messages
    private boolean isMapsEnabled() {
        Log.d(TAG, "isMapsEnabled: called");
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        Log.d(TAG, "buildAlertMessageNoGPS: called");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to enable GPS to use the application?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called");

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (locationPermissionGranted) {
                    //if permission granted retrieve user's location
                    getLastLocation();
                } else {
                    getLocationPermission();
                }
            }
        }

    }

    private void getLocationPermission() {
        Log.d(TAG,"getLocationPermission");
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            Log.d(TAG,"getLocationPermission Called True");
            //getLastKnownLocation();
            //getBusListAndMaps();
            getLastLocation();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Log.d(TAG,"YES onRequestPermissionResult");
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (checkMapServices()) {
            if (locationPermissionGranted) {
                //getLastKnownLocation();
                //getBusListAndMaps();
                getLastLocation();
            } else {
                getLocationPermission();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //Hey, a non null location! Sweet!
        Log.d(TAG,"onLocationChanged");
        //remove location callback:
        locationManager.removeUpdates(this);

        //open the map:
        latitude = (double) Math.round(location.getLatitude() * 100000d) / 100000d;
        longitude = (double) Math.round(location.getLongitude() * 100000d) / 100000d;
        getLastLocation();

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
