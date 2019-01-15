package com.example.arago.translinklocator.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.arago.translinklocator.R;
import com.example.arago.translinklocator.adapters.BusRecyclerAdapter;
import com.example.arago.translinklocator.models.Bus;
import com.example.arago.translinklocator.models.BusStop;
import com.example.arago.translinklocator.models.UserLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.arago.translinklocator.Constants.MAPVIEW_BUNDLE_KEY;

public class BusListFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "BusListFragment";
    private RecyclerView myRecyclerView;

    private MapView mapView;

    private ArrayList<Bus> busArrayList = new ArrayList<>();
    private ArrayList<BusStop> busStop =  new ArrayList<>();
    private BusRecyclerAdapter busRecyclerAdapter;

    private GoogleMap googleMap;
    private LatLngBounds mapBoundary;
    private UserLocation userPosition;
    List<Marker> markerList = new ArrayList<Marker>();
    RequestQueue requestQueue;
    StringRequest objectRequest;


    public static double lat, lng;
    public static Boolean checker = false;
    public static BusListFragment newInstance(){return new BusListFragment();}


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(getContext());
        userPosition = new UserLocation();


        Bundle bundle = this.getArguments();
        if (getArguments() != null) {
            //get arguments from bundle
            lat = bundle.getDouble("Latitude");
            lng = bundle.getDouble("Longitude");
            busArrayList = getArguments().getParcelableArrayList("BusList");


            userPosition.setLatitude(lat);
            userPosition.setLongitude(lng);
            Log.d(TAG,"OnCreateView: Latitude: " + lat);


        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_list, container, false);
        myRecyclerView = view.findViewById(R.id.bus_list_recycler_view);
        mapView = view.findViewById(R.id.bus_list_map);


        //intialize the Bus List and Google Maps
        initBusListRecyclerView();
        initGoogleMap(savedInstanceState);

        return view;
    }

    private void getBusStopLocations(double lat, double lng){
        Log.d(TAG,"getBusStopLocations: called");
        //Returns stops near users' latitude/longitude coordinates, radius is 2000 meters
        String apiKey = getString(R.string.translink_api_key);
        final String url = "http://api.translink.ca/rttiapi/v1/stops?apikey=" + apiKey +
                "&lat=" + lat + "&long=" + lng + "&radius=2000";

        Log.d(TAG,"getBusStopLocations: URL: "+ url);
        objectRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = new JSONObject();

                             /* sample return
                                    {
                                  "StopNo": 61441,
                                  "Name": "SB BONSON RD FS HOFFMANN WAY",
                                  "BayNo": "N",
                                  "City": "PITT MEADOWS",
                                  "OnStreet": "BONSON RD",
                                  "AtStreet": "HOFFMANN WAY",
                                  "Latitude": 49.204363,
                                  "Longitude": -122.678366,
                                  "WheelchairAccess": 1,
                                  "Distance": 1987,
                                  "Routes": "719, 722"
                                }
                            */
                            int stopNo;
                            double stopDistance, stopLatitude, stopLongitude;
                            String stopRoutes, stopName;



                            //get each jsonobject
                            for(int x = 0; x < jsonArray.length(); x++){
                                jsonObject = jsonArray.getJSONObject(x);

                                stopNo = Integer.parseInt(jsonObject.getString("StopNo"));
                                stopDistance = Double.parseDouble(jsonObject.getString("Distance"));
                                stopLatitude = Double.parseDouble(jsonObject.getString("Latitude"));
                                stopLongitude = Double.parseDouble(jsonObject.getString("Longitude"));
                                stopRoutes = jsonObject.getString("Routes");
                                stopName = jsonObject.getString("Name");
                                //create an object of bus stop
                                busStop.add(new BusStop(stopNo,stopName,stopLatitude,stopLongitude,stopDistance,stopRoutes));
                            }
                            //Mark the latitude and longitude of bus stops
                            markBusStop(busStop);
                            Log.d(TAG,"Result: " +  jsonObject.toString());

                        } catch (JSONException e) {
                            System.out.println(e.getMessage());
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(),error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/JSON");
                params.put("api-key","21mn31dcULj0N9JLDzd7");
                return params;
            }
        };
        requestQueue.add(objectRequest);
    }

    //Mark Bus Stops
    public void markBusStop(ArrayList<BusStop> busStop){
        Marker marker;

        LatLng latLng;

        int size =  busStop.size();
        BusStop[] stop = new BusStop[size];
        for(int x = 0; x < size; x++){
            stop[x] = busStop.get(x);
        }

        //icon is from https://www.flaticon.com/authors/turkkub" title="turkkub">turkkub
        for(int x = 0; x < busStop.size(); x++){
            latLng = new LatLng(stop[x].getStopLatitude(),stop[x].getStopLongitude());
            if(!stop[x].getStopRoutes().equalsIgnoreCase("")){
                marker =  googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.red_icon_8)));
                marker.setTitle(String.valueOf(stop[x].getStopNo()) + ": " + stop[x].getStopName());
                marker.setSnippet(stop[x].getStopRoutes());

            }

        }

    }

    //Go to users current location and move camera
    private void setCameraView(double boundary){

        Log.d("SetCameraView","SetCameraView: " + "Latitude: " + userPosition.getLatitude());
        double bottomBoundary = userPosition.getLatitude() - boundary;
        double leftBoundary = userPosition.getLongitude() - boundary;
        double topBoundary = userPosition.getLatitude() + boundary;
        double rightBoundary = userPosition.getLongitude() + boundary;
        mapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mapBoundary,0));

    }

    //setGoogleMap
    private void initGoogleMap(Bundle savedInstanceState){
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);
    }
    private void initBusListRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        //PagerSnapHelper snapHelper = new PagerSnapHelper();
        busRecyclerAdapter = new BusRecyclerAdapter(busArrayList, mFragmentInterface);
       // snapHelper.attachToRecyclerView(myRecyclerView);
        myRecyclerView.setAdapter(busRecyclerAdapter);
        busRecyclerAdapter.notifyDataSetChanged();

        myRecyclerView.setLayoutManager(layoutManager);


    }
    //Communicator for RecyclerAdapter
    BusRecyclerAdapter.FragmentInterface mFragmentInterface = new BusRecyclerAdapter.FragmentInterface() {

        @Override
        public void respond(int busStopNo, String destination) {
            Log.d(TAG,"fragmentInterface clicked: busStopNo: " + busStopNo);
            if(!checker){//do a zoom out once
                setCameraView(0.02);
            }
            if(busStopNo == 0){
                Toast.makeText(getContext(),"Routes unavailable at this Bus Stop",Toast.LENGTH_SHORT).show();
            }
            //Find current Bus Location that stop at selected bus stop
            else{
                checker = true;
                //clear off previously marked locations
                reloadMarkerBusLocation();
                //get new and current bus locations
                getCurrentBusLocation(busStopNo, destination);

            }

        }
    };

    public void getCurrentBusLocation(int busStopNo, final String destination){
        Log.d(TAG,"getCurrentBusLocation: called");
        /*
        •	http://api.translink.ca/rttiapi/v1/buses?apikey=[apiKey]&routeNo=[routeNo]
- Returns all active buses serving route 701
         */

        String apiKey = getString(R.string.translink_api_key);
        final String url = "http://api.translink.ca/rttiapi/v1/buses?apikey=" + apiKey +
                "&routeNo=" + busStopNo;
        Log.d(TAG,"getCurrentBusLocations: URL: "+ url);
        objectRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {


                    @Override
                    public void onResponse(String response) {
                        try {

                            Log.d(TAG,"Response: " + response);
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = new JSONObject();

                            int vehicleNo = 0, busRouteNo = 0;
                            double busLatitude = 0, busLongitude = 0;
                            String busDirection = "", busDestination =  "", recordedTime = "";

                            ArrayList<Bus> busList = new ArrayList<>();
                            //get each jsonobject
                            for(int x = 0; x < jsonArray.length(); x++){
                                jsonObject = jsonArray.getJSONObject(x);
                                //get the distance of stops from request location
                                vehicleNo = Integer.parseInt(jsonObject.getString("VehicleNo"));
                                busRouteNo = Integer.parseInt(jsonObject.getString("RouteNo"));
                                busLatitude = Double.parseDouble(jsonObject.getString("Latitude"));
                                busLongitude = Double.parseDouble(jsonObject.getString("Longitude"));
                                busDirection = jsonObject.getString("Direction");
                                busDestination = jsonObject.getString("Destination");
                                recordedTime = jsonObject.getString("RecordedTime");
                                busList.add(new Bus(vehicleNo,busRouteNo,busLatitude,
                                        busLongitude,busDirection,busDestination,recordedTime));
                            }
                            //Mark The Bus Location

                            markBusCurrentLocation(busList,destination);

                        } catch (JSONException e) {
                            Log.d(TAG,"Response: " + e.getMessage());
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(),error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/JSON");
                params.put("api-key","21mn31dcULj0N9JLDzd7");
                return params;
            }
        };
        requestQueue.add(objectRequest);
    }

    private void markBusCurrentLocation(ArrayList<Bus> buses, String destination){

        Marker marker, stopMarker;

        int size =  buses.size();
        Bus[] bus = new Bus[size];
        for(int x = 0; x < size; x++){
            bus[x] = buses.get(x);
        }

        LatLng latLng;
        String busNo = "", name = "", time = "";
        for(int x = 0; x < size; x ++){
            //retrieve and set values
            latLng = new LatLng(bus[x].getBusLatitude(),bus[x].getBusLongitude());
            busNo = String.valueOf(bus[x].getBusRouteNo());
            name = bus[x].getBusDestination();
            time = bus[x].getRecordedTime();
            //stopmarker - add a mark on the clicked busstop
            if(destination.equalsIgnoreCase(name)){
                marker =  googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus32)));
                marker.setTitle(busNo + ": " + name);
                marker.setSnippet(time);
                markerList.add(marker);

            }
        }

        Log.d(TAG,"MARKER LIST LATLNG: " + size);
    }

    //clear of previously marked list
    private void reloadMarkerBusLocation(){

        for(int x = 0; x < markerList.size(); x++){
            markerList.get(x).setVisible(false);
        }

        markerList.clear();

    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }



    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        /*MarkerOptions markerOptions = new MarkerOptions();
        LatLng latLng = new LatLng(49.204363,-122.678366);
        map.addMarker(new MarkerOptions().position(latLng));*/
        map.setMyLocationEnabled(true);
        googleMap = map;
        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                setCameraView(0.009);
                getBusStopLocations(userPosition.getLatitude(),userPosition.getLongitude());

            }
        });

    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
