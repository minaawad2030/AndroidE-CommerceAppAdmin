package com.minassoftware.morkosmedicalsuppliesserverside;

import android.Manifest;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.minassoftware.morkosmedicalsuppliesserverside.Model.Common;
import com.minassoftware.morkosmedicalsuppliesserverside.Model.DirectionJSONParser;
import com.minassoftware.morkosmedicalsuppliesserverside.Remote.IGeoCoordinates;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST=1000;
    private final static int LOCATION_PERMISSION_REQUEST=1001;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocalRequest;

    private static int UPDATE_INTERVAL=1000;
    private static int FATEST_INTERVAL=5000;
    private static int DISPLACEMENT=10;


    private IGeoCoordinates mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);

        mService= Common.getGeoCodeService();

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            requestRuntimePermission();

        }else{
            if(checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        displayLocation();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        displayLocation();

    }

    private void displayLocation() {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            requestRuntimePermission();

        }
        else
            {
            mLastLocation= LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mLastLocation!=null){

                double latiude=mLastLocation.getLatitude();
                double longitude=mLastLocation.getLongitude();

                LatLng yourLocation=new LatLng(latiude,longitude);
                mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));

                drawRoute(yourLocation,Common.CurrentRequest.getAddress());


            }else{
            //  Toast.makeText(this, "Location error", Toast.LENGTH_LONG).show();
                Log.d("DEBUG","Couldnot get the location");
            }
        }
    }

    private void drawRoute(final LatLng yourLocation, String address) {

        mService.getGeoCode(address).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                try {
                    JSONObject jsonObj=new JSONObject(response.body().toString());

                    String lat=((JSONArray)jsonObj.get("results"))
                            .getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                            .get("lat").toString();

                    String lng=((JSONArray)jsonObj.get("results"))
                            .getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                            .get("lng").toString();

                    //Toast.makeText(TrackingOrder.this, lat+","+lng, Toast.LENGTH_SHORT).show();
                    LatLng orderLocation=new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));

                    Bitmap bitmap =BitmapFactory.decodeResource(getResources(),R.drawable.deliverybox);
                    //bitmap=bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    bitmap=Common.scaleBitmap(bitmap,70,70);

                    MarkerOptions markerOptions=new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                            .title("Order of "+Common.CurrentRequest.getPhone())
                            .position(orderLocation);
                    mMap.addMarker(markerOptions);

                    //Draw Route
                    //LatLng tempLocation=yourLocation;
                    mService.getDirections(yourLocation.latitude+","+yourLocation.longitude,
                                            orderLocation.latitude+","+orderLocation.longitude)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    new ParserTask().execute(response.body().toString());
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable throwable) {

                                }
                            });


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable throwable) {

            }
        });
    }



    private void createLocationRequest() {

        mLocalRequest=new LocationRequest();
        mLocalRequest.setInterval(UPDATE_INTERVAL);
        mLocalRequest.setFastestInterval(FATEST_INTERVAL);
        mLocalRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocalRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();



    }

    private boolean checkPlayServices() {

        int resultCode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode!= ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RESOLUTION_REQUEST).show();

            }
            else{
                Toast.makeText(this, "This device isn't supperted", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void requestRuntimePermission() {
        ActivityCompat.requestPermissions(this,new String[]
                {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                },LOCATION_PERMISSION_REQUEST);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if(checkPlayServices()){
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
                break;
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;


    }



    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient!=null){
            mGoogleApiClient.connect();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;

        }
        //Maybe an error here
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocalRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jObject;
            List<List<HashMap<String ,String>>> routes=null;
            try{
                jObject=new JSONObject(strings[0]);
                DirectionJSONParser parser=new DirectionJSONParser();
                routes=parser.parse(jObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String,String>>> lists) {

            ArrayList points=null;
            PolylineOptions line=null;

            for (int i=0;i<lists.size();i++){

                points=new ArrayList();
                line=new PolylineOptions();

                List<HashMap<String,String>> path=lists.get(i);

                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point=path.get(j);
                    double lat=Double.parseDouble(point.get("lat"));
                    double lng=Double.parseDouble(point.get("lng"));

                    LatLng position=new LatLng(lat,lng);
                    points.add(position);

                }
                line.addAll(points);
                line.width(12);
                line.color(Color.BLUE);
                line.geodesic(true);

            }
            mMap.addPolyline(line);
        }
    }
}
