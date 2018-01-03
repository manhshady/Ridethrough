package com.example.user.projectsepm;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.example.user.projectsepm.ReportActivity.REQUEST_LOCATION;

public class SubmitActivity extends AppCompatActivity {

    private EditText numSpeed;
    private EditText txtLat;
    private EditText txtLon;
    private EditText txtAddress;
    private EditText txtDatetime;
    private Button btnUseCurrentLocation;
    private Button btnSubmit;
    private String res;
    private LocationManager locationManager;
    int speed = 0;
    double lat = 0.0;
    double lon = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);

//        numSpeed = (EditText) findViewById(R.id.numSpeed);
//        txtLat = (EditText) findViewById(R.id.inputLat);
//        txtLon = (EditText) findViewById(R.id.inputLon);
        txtDatetime = (EditText) findViewById(R.id.txtDatetime);
        txtAddress = (EditText) findViewById(R.id.address);


        btnUseCurrentLocation = (Button) findViewById(R.id.btnUseCurrentLocation);
        btnUseCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get longtitude and latitude
                locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                getLocation();
                txtAddress.setText(getCompleteAddressString(lat, lon));
            }
        });
        DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        String date = dateFormat.format(Calendar.getInstance().getTime());
        txtDatetime.setText(date);


        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                speed = Integer.parseInt( numSpeed.getText().toString() );
//                lat = Double.parseDouble(txtLat.getText().toString());
//                lon = Double.parseDouble(txtLon.getText().toString());
                LatLng latLngFromAddress = getLocationFromAddress(SubmitActivity.this, txtAddress.getText() + "");
                lat = latLngFromAddress.latitude;
                lon = latLngFromAddress.longitude;
                TrafficJamPoint trafficJamPoint = new TrafficJamPoint(speed, txtDatetime.getText() +"", lat, lon);
                new UpdateData().execute(trafficJamPoint);
                Toast.makeText(SubmitActivity.this, trafficJamPoint.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void getLocation(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        }else {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null){
                lat = location.getLatitude();
                lon = location.getLongitude();
            }
        }
    }
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0;  i <= returnedAddress.getMaxAddressLineIndex() ; i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("Loction address", "" + strReturnedAddress.toString());
            } else {
                Log.w("Loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("Loction address", "Canont get Address!");
        }
        return strAdd;
    }

    public LatLng getLocationFromAddress(Context context,String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    private class UpdateData extends AsyncTask<TrafficJamPoint, Void, Void> {

        @Override
        protected Void doInBackground(TrafficJamPoint... trafficJamPoints) {
            res = HttpHandler.doPost("http://13.229.132.227:3000/trafficJams", trafficJamPoints[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (res.equals("")){

                Toast.makeText(SubmitActivity.this, "Cannot connect to the server", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
