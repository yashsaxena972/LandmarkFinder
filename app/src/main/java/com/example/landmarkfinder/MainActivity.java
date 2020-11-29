package com.example.landmarkfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText latitudeEditText;
    private EditText longitudeEditText;
    private Button findButton;
    private Spinner dropDown;
    String landmark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeEditText = (EditText)findViewById(R.id.lat_edit_text);
        longitudeEditText = (EditText)findViewById(R.id.long_edit_text);
        findButton = (Button)findViewById(R.id.find_button);
        dropDown = findViewById(R.id.spinner);

        landmark = "restaurant";
        //create a list of items for the spinner.
        String[] items = new String[]{"restaurant", "school", "store","all"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        dropDown.setAdapter(adapter);
        dropDown.setOnItemSelectedListener(this);

        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(latitudeEditText.getText().toString().matches("") || longitudeEditText.getText().toString().matches("")){
                    Toast.makeText(getApplicationContext(),"Value(s) missing",Toast.LENGTH_SHORT).show();
                    return;
                }

                double latitude = Double.parseDouble(latitudeEditText.getText().toString());
                double longitude = Double.parseDouble(longitudeEditText.getText().toString());
                LatLng location = new LatLng(latitude, longitude);

                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("Latlng", location);
                intent.putExtra("landmark",landmark);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                landmark = "restaurant";
                break;
            case 1:
                landmark = "school";
                Log.d("myTag",landmark);
                break;
            case 2:
                landmark = "store";
                break;
            case 3:
                landmark = "all";
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}