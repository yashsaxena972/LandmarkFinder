package com.example.landmarkfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, OnTaskCompleted {

    private GoogleMap mMap;
    GoogleApiClient client;
    private LatLng location;
    private Button findButton;
    private LinearLayout linearLayout;
    private TextView averageRatingTextView;
    private TextView averageNoOfRatingsTextView;
    private TextView pincodeTextView;
    private List<LatLng> sourcePolyPoints;
    Gradient gradient;
    private double ratingSum = 0;
    private long noOfRatingsSum = 0;
    double rating;
    private long noOfRatings;
    String landmark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        location = getIntent().getExtras().getParcelable("Latlng");
        landmark = getIntent().getExtras().getString("landmark");
        findButton = (Button)findViewById(R.id.getNearbyButton);
        linearLayout = (LinearLayout) findViewById(R.id.text_layout);
        averageRatingTextView = (TextView)findViewById(R.id.averageRatingTextView);
        averageNoOfRatingsTextView = (TextView)findViewById(R.id.averageNoOfReviewsTextView);
        pincodeTextView = (TextView)findViewById(R.id.pincode_text_view);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            Address pin = addresses.get(0);
            pincodeTextView.setVisibility(View.VISIBLE);
            pincodeTextView.setText("Locality: " + pin.getLocality() + "\nAddress: " + pin.getAddressLine(0) + "\nPincode: " + pin.getPostalCode());

        } catch (IOException e) {
            e.printStackTrace();
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pincodeTextView.setVisibility(View.GONE);
                findButton.setVisibility(View.GONE);
                findNearby(location);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MapsActivity.this,MainActivity.class);
        startActivity(intent);
    }

    public void findNearby(LatLng location){

        Log.d("tag1",landmark);

        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?")
                .append("location="+location.latitude+","+location.longitude)
                .append("&radius="+1000)
                .append("&key="+getResources().getString(R.string.google_places_key));

        if(landmark != "all"){
            stringBuilder.append("&type="+landmark);
        }

        String url = stringBuilder.toString();

        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;

        GetNearbyPlaces getNearbyPlaces = new GetNearbyPlaces(this);
        getNearbyPlaces.execute(dataTransfer);

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

        // Create the gradient.
        int[] colors = {
                Color.rgb(102, 225, 0), // green
                Color.rgb(255, 0, 0)    // red
        };

        float[] startPoints = {
                0.2f, 1f
        };

        gradient = new Gradient(colors, startPoints);

        // List containing lat longs
        sourcePolyPoints = new ArrayList<>();

        client =  new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        client.connect();

        // Add a marker in Sydney and move the camera
        // LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(location).title("Marker in Sydney"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,15));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    // This method is called after API request to Nearby Places and receives JSONObject from GetNearbyPlaces java class
    @Override
    public void onTaskCompleted(JSONObject jsonObject) throws JSONException {
        linearLayout.setVisibility(View.VISIBLE);
        JSONArray resultsArray = jsonObject.getJSONArray("results");
        StringBuilder names = new StringBuilder();


        // Parsing JSON data to extract required parameters
        for(int i=0; i<resultsArray.length(); i++) {
            JSONObject object = resultsArray.getJSONObject(i);
            JSONObject locationObj = object.getJSONObject("geometry").getJSONObject("location");
            //JSONObject ratingObj = object.getJSONObject("rating");

            String name = object.getString("name");
            double latitude = locationObj.getDouble("lat");
            double longitude = locationObj.getDouble("lng");

            if(object.has("rating")){
                rating = object.getDouble("rating");
                noOfRatings = object.getLong("user_ratings_total");
            }
            else{
                rating = 0;
                noOfRatings = 0;
            }

            ratingSum += rating;
            noOfRatingsSum += noOfRatings;

            names.append(name+",");
            sourcePolyPoints.add(new LatLng(latitude, longitude));

        }

        double averageRating = ratingSum/(resultsArray.length());
        long averageNoOfRatings = noOfRatingsSum/(resultsArray.length());
        String str = String.format("%.2f", averageRating);


        // Create the tile provider.
        HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                .data(sourcePolyPoints)
                .gradient(gradient)
                .build();

        TileOverlay tileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));


        averageRatingTextView.setText(str);
        averageNoOfRatingsTextView.setText(Long.toString(averageNoOfRatings));
    }
}