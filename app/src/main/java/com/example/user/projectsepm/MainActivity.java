package com.example.user.projectsepm;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener,
        LocationListener, ResultCallback<Status>,
        GoogleMap.OnMyLocationButtonClickListener {

    private GoogleApiClient mGoogleApiClient;

    private TextView txtSpeed;
//    private ListView lvData;
    private Button btnMap;
    private Button btnForm;

    private int speed;
    private TrafficJamPoint trafficJamPoint;
    private List<Marker> geoList;
    //private HashMap<Marker, Circle> markerAndCircle;
//    private List<HashMap<String, String>> data;
//    private HashMap<String,String> detail;
    private int activityType = 99;
    private TextView txtActivity;
    private String res;

    private MyBroadcastReceiver myBroadcastReceiver;
    private MyBroadcastReceiver_Update myBroadcastReceiver_Update;

    private MapFragment mapFragment;
    private GoogleMap map;
    private Location lastLocation;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 500.0f; // in meters
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;
    private Circle geoFenceLimits;
    private static final int REFRESH_JAM_INTERVAL = 60 * 1000;
    private android.os.Handler mHandler;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";
    private String url = "http://13.229.132.227:3000/trafficJams";

    private MyLocationService mService;
    private boolean mBound;
    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent( context, MainActivity.class );
        intent.putExtra( NOTIFICATION_MSG, msg );
        return intent;
    }

            @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
//        txtSpeed = (TextView) findViewById(R.id.numSpeed);
////        lvData = (ListView) findViewById(R.id.lvData);
//        txtActivity = (TextView) findViewById(R.id.txtActivity);
//        data = new ArrayList<>();
//        detail = new HashMap<>();

        //markerAndCircle = new HashMap<>();
        geoList = new ArrayList<>();
        mHandler = new Handler();
//         if(isConnectedToServer(url, 3000)) {
//             startRepeatingLoadTask();
//         }

//        btnMap = (Button) findViewById(R.id.btnMap);
//        btnMap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
//                startActivity(intent);
//
//            }
//        });
//        btnForm = (Button) findViewById(R.id.btnForm);
//        btnForm.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, SubmitActivity.class);
//                startActivity(intent);
//            }
//        });

        //Start MyIntentService
        Intent intentMyIntentService = new Intent(this, ActivityRecognizedService.class);
        //intentMyIntentService.putExtra(ActivityRecognizedService.EXTRA_KEY_IN, msgToIntentService);
        startService(intentMyIntentService);

//        Intent intentMyLocationService = new Intent(this, MyLocationService.class);
//        startService(intentMyLocationService);
//        bindService(intentMyLocationService, mConnection,Context.BIND_AUTO_CREATE);


        myBroadcastReceiver = new MyBroadcastReceiver();
        myBroadcastReceiver_Update = new MyBroadcastReceiver_Update();

        //register BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(ActivityRecognizedService.ACTION_MyIntentService);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(myBroadcastReceiver, intentFilter);

        IntentFilter intentFilter_update = new IntentFilter(ActivityRecognizedService.ACTION_MyUpdate);
        intentFilter_update.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(myBroadcastReceiver_Update, intentFilter_update);

        if( checkPlayServices()) {
            buildGoogleApiClient();
//           mGoogleApiClient.connect();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            initGMaps();
        }


    }

    //Repeatedly request for jam points
    Runnable m_statusChecker = new Runnable() {
        @Override
        public void run() {
            if (geoList.size() > 0){
                for (int i = 0; i < geoList.size() ; i++) {
                    clearGeofence(geoList.get(i));
                }

            }
           // Log.w("geoListB4", geoList.toString());

            new LoadJamPoints().execute();
           // Log.w("geoListAfter", geoList.toString());
            mHandler.postDelayed(m_statusChecker, REFRESH_JAM_INTERVAL);
        }

    };

    void startRepeatingLoadTask()
    {
        m_statusChecker.run();
    }

    void stopRepeatingLoadTask()
    {
        mHandler.removeCallbacks(m_statusChecker);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        //stop location updates when Activity is no longer active
//        if (mGoogleApiClient != null) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//        }
//    }

    @Override
    protected void onStop() {
        super.onStop();

    }

//    private ServiceConnection mConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder service) {
//            MyLocationService.LocalBinder binder = (MyLocationService.LocalBinder) service;
//            mService = binder.getService();
//            mBound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            mBound = false;
//        }
//    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
//        if(mBound) {
//            unbindService(mConnection);
//            mBound = false;
//        }
        unregisterReceiver(myBroadcastReceiver);
        unregisterReceiver(myBroadcastReceiver_Update);
        stopRepeatingLoadTask();
    }

    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getBaseContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();
        //mGoogleApiClient.connect();

    }
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        Intent intent = new Intent(this, ActivityRecognizedService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 1000, pendingIntent);
        getLastKnownLocation();
        //recoverGeofenceMarker();
        //if(mGoogleApiClient.isConnected()){

        startRepeatingLoadTask();

        //}



    }

            @Override
    public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "onMapReady()");
                map = googleMap;
                //map.setOnMapClickListener(this);
                map.setOnMarkerClickListener(this);
                map.setTrafficEnabled(true);
                if(checkPermission())
                map.setMyLocationEnabled(true);
                map.setOnMyLocationButtonClickListener(this);
                map.getUiSettings().setMyLocationButtonEnabled(true);
//                getLastKnownLocation();
//                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude()
//                        , lastLocation.getLongitude()), 12f);
//                map.animateCamera(cameraUpdate);

    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick("+latLng +")");
        markerForGeofence(latLng);
    }

     @Override
     public boolean onMarkerClick(Marker marker) {
         Log.d(TAG, "onMarkerClickListener: " + marker.getPosition() );
        return false;
     }


    @Override
    public boolean onMyLocationButtonClick() {
        getLastKnownLocation();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude()
                , lastLocation.getLongitude()), 12f);
        map.animateCamera(cameraUpdate);
        return false;
    }

    // Initialize GoogleMaps
     private void initGMaps(){
         mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
         mapFragment.getMapAsync(this);
     }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
     @Override
     public void onLocationChanged(Location location) {
         if (location == null ){
            //txtSpeed.setText("00 km/h");
            } else  {
                //Diaplay the moving speed
                Log.e("Speed",location.getSpeed() + "" );
                speed =(int) ((location.getSpeed()*3600)/1000);
                //txtSpeed.setText(speed + " km/h");

                if(activityType == DetectedActivity.IN_VEHICLE && speed < 6) { //Start record user's data only when they are driving
                    DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                    String date = dateFormat.format(Calendar.getInstance().getTime());

//                    detail = new HashMap<>();
//                    detail.put("Speed", "" + speed);
//                    detail.put("lat", "" + location.getLat());
//                    detail.put("lon", "" + location.getLon());
//                    detail.put("Time", "" + date);
//                    if (data.size() == 0) {
//                        data.add(detail);
//                    } else {
//                        data.set(0, detail);
//                    }
                    trafficJamPoint = new TrafficJamPoint(speed, date, location.getLatitude(), location.getLongitude());
                    Log.e("Jam point", trafficJamPoint.toString());
                    //Log.e("Data", data.toString());
//                    ArrayAdapter<HashMap<String, String>> arrayAdapter = new ArrayAdapter<HashMap<String, String>>(MainActivity.this, android.R.layout.simple_list_item_1, data);
//                    lvData.setAdapter(arrayAdapter);
                    new UpdateData().execute(trafficJamPoint);
                    //writeActualLocation(location);
                }
            }
     }



    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            activityType = intent.getIntExtra(ActivityRecognizedService.EXTRA_KEY_OUT, 99);
            Log.e("Activity type","" +  activityType);

        }
    }

    public class MyBroadcastReceiver_Update extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            activityType = intent.getIntExtra(ActivityRecognizedService.EXTRA_KEY_UPDATE, 100);
            Log.e("Activity type","" + activityType);
            //txtActivity.setText("Activity type: " + activityType);

        }
    }

    private class UpdateData extends AsyncTask<TrafficJamPoint, Void, Void>{

        @Override
        protected Void doInBackground(TrafficJamPoint... trafficJamPoints) {
            res = HttpHandler.doPost("http://13.229.132.227:3000/trafficJams", trafficJamPoints[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (res.equals("")){

                Toast.makeText(MainActivity.this, "Cannot connect to the server", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(MainActivity.this, res, Toast.LENGTH_SHORT).show();

            }
        }
    }
            // Get last known location
            private void getLastKnownLocation() {
                Log.d(TAG, "getLastKnownLocation()");
                if ( checkPermission() ) {
                    lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if ( lastLocation != null ) {
                        Log.i(TAG, "LasKnown location. " +
                                "Long: " + lastLocation.getLongitude() +
                                " | Lat: " + lastLocation.getLatitude());
                        //writeLastLocation();
                        startLocationUpdates();
                    } else {
                        Log.w(TAG, "No location retrieved yet");
                        startLocationUpdates();
                    }
                }
                else askPermission();
            }
            private LocationRequest locationRequest;
            // Defined in mili seconds.
            // This number in extremely low, and should be used only for debug
            private final int UPDATE_INTERVAL =  60*1000;
            private final int FASTEST_INTERVAL = 30*1000;

            // Start location Updates
            private void startLocationUpdates(){
                Log.i(TAG, "startLocationUpdates()");
                locationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(UPDATE_INTERVAL)
                        .setFastestInterval(FASTEST_INTERVAL);

                if ( checkPermission() )
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            }

            // Check for permission to access Location
            private boolean checkPermission() {
                Log.d(TAG, "checkPermission()");
                // Ask for permission if it wasn't granted yet
                return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED );
            }

            // Asks for permission
            private final int REQ_PERMISSION = 999;
            private void askPermission() {
                Log.d(TAG, "askPermission()");
                ActivityCompat.requestPermissions(
                        this,
                        new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION },
                        REQ_PERMISSION
                );
            }

            // Verify user's response of the permission requested
            @Override
            public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                Log.d(TAG, "onRequestPermissionsResult()");
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                switch ( requestCode ) {
                    case REQ_PERMISSION: {
                        if ( grantResults.length > 0
                                && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                            // Permission granted
                            getLastKnownLocation();

                        } else {
                            // Permission denied
                            permissionsDenied();
                        }
                        break;
                    }
                }
            }

            // App cannot work without the permissions
            private void permissionsDenied() {
                Log.w(TAG, "permissionsDenied()");
            }

            // Write location coordinates on UI
            private LatLng writeActualLocation(Location location) {
                //markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                return new LatLng(location.getLatitude(), location.getLongitude());
            }

            private void writeLastLocation() {
                writeActualLocation(lastLocation);
            }


            // Create a Location Marker
            private Marker locationMarker;
            private void markerLocation(LatLng latLng) {
                Log.i(TAG, "markerLocation("+latLng+")");
                String title = getCompleteAddressString(latLng.latitude, latLng.longitude);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                if ( map!=null ) {
                    // Remove the anterior marker
                    if ( locationMarker != null )
                        locationMarker.remove();
                    locationMarker = map.addMarker(markerOptions);
                    float zoom = 14f;
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
                    map.animateCamera(cameraUpdate);
                }
            }

            private Marker geoFenceMarker;
            // Create a marker for the geofence creation
            private void markerForGeofence(LatLng latLng) {
                Log.i(TAG, "markerForGeofence(" + latLng + ")");
                String title = getCompleteAddressString(latLng.latitude, latLng.longitude);
                // Define marker options
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        .title(title);
                if (map != null) {
                    // Remove last geoFenceMarker
//                    if (geoFenceMarker != null)
//                        geoFenceMarker.remove();

                    geoFenceMarker = map.addMarker(markerOptions);
                    geoList.add(geoFenceMarker);
                    startGeofence();
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

            // Create a Geofence
            private Geofence createGeofence( LatLng latLng, float radius ) {
                Log.d(TAG, "createGeofence");
                return new Geofence.Builder()
                        .setRequestId(GEOFENCE_REQ_ID)
                        .setCircularRegion( latLng.latitude, latLng.longitude, radius)
                        .setExpirationDuration( GEO_DURATION )
                        .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                                | Geofence.GEOFENCE_TRANSITION_EXIT )
                        .build();
            }

            // Create a Geofence Request
            private GeofencingRequest createGeofenceRequest( Geofence geofence ) {
                Log.d(TAG, "createGeofenceRequest");
                return new GeofencingRequest.Builder()
                        .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                        .addGeofence( geofence )
                        .build();
            }

            private PendingIntent createGeofencePendingIntent() {
                Log.d(TAG, "createGeofencePendingIntent");
                if ( geoFencePendingIntent != null )
                    return geoFencePendingIntent;

                Intent intent = new Intent( this, GeofenceTrasitionService.class);
                return PendingIntent.getService(
                        this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
            }

            // Add the created GeofenceRequest to the device's monitoring list
            private void addGeofence(GeofencingRequest request) {
                Log.d(TAG, "addGeofence");
                if (checkPermission())
                    LocationServices.GeofencingApi.addGeofences(
                            mGoogleApiClient,
                            request,
                            createGeofencePendingIntent()
                    ).setResultCallback(this);
            }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if ( status.isSuccess() ) {
            saveGeofence();
            //drawGeofence();
        } else {
            // inform about fail
        }
    }


    // Start Geofence creation process
    private void startGeofence() {
        Log.i(TAG, "startGeofence()");
        if( geoFenceMarker != null ) {
            Geofence geofence = createGeofence( geoFenceMarker.getPosition(), GEOFENCE_RADIUS );
            GeofencingRequest geofenceRequest = createGeofenceRequest( geofence );
            addGeofence( geofenceRequest );
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.main_menu, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
            case R.id.report: {
                Intent intent = new Intent(MainActivity.this, SubmitActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");

//        if ( geoFenceLimits != null )
//            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center( geoFenceMarker.getPosition())
                .strokeColor(Color.RED)
                .strokeWidth(2)
                .fillColor( 0x15f45642 )
                .radius( GEOFENCE_RADIUS );
        geoFenceLimits = map.addCircle( circleOptions );

    }

    // Saving GeoFence marker with prefs mng
    private final String KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE";
    private final String KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE";

    private void saveGeofence() {
        Log.d(TAG, "saveGeofence()");
        SharedPreferences sharedPref = getPreferences( Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putLong( KEY_GEOFENCE_LAT, Double.doubleToRawLongBits( geoFenceMarker.getPosition().latitude ));
        editor.putLong( KEY_GEOFENCE_LON, Double.doubleToRawLongBits( geoFenceMarker.getPosition().longitude ));
        editor.apply();
    }

    // Recovering last Geofence marker
    private void recoverGeofenceMarker() {
        Log.d(TAG, "recoverGeofenceMarker");
        SharedPreferences sharedPref = getPreferences( Context.MODE_PRIVATE );

        if ( sharedPref.contains( KEY_GEOFENCE_LAT ) && sharedPref.contains( KEY_GEOFENCE_LON )) {
            double lat = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LAT, -1 ));
            double lon = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LON, -1 ));
            LatLng latLng = new LatLng( lat, lon );
            markerForGeofence(latLng);
            drawGeofence();
        }
    }

    // Clear Geofence
    private void clearGeofence(final Marker geoMarker) {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                createGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if ( status.isSuccess() ) {
                    // remove drawing
//                    removeGeofenceDraw(geoMarker, circle);
                    if ( geoMarker != null)
                        geoMarker.remove();
                }
            }
        });
    }

    private void removeGeofenceDraw(Marker marker,Circle circle) {
        Log.d(TAG, "removeGeofenceDraw()");
        if ( marker != null)
            marker.remove();
        if ( circle != null )
            circle.remove();
    }

    private String getResquest;
    private class LoadJamPoints extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            if (isConnectedToServer(url, 3000)){
                getResquest = HttpHandler.getJSONFromUrl(url);
                Log.e("getRequest", getResquest);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //geoList.clear();
            Gson gson = new Gson();
            Type listType = new TypeToken<List<TrafficJamPoint>>(){}.getType();
            List<TrafficJamPoint> jamPointList = gson.fromJson(getResquest, listType);
            if (jamPointList!= null) {
                Log.e("jamPointList", jamPointList.toString());

                //List<Map<String, Double>> data = new ArrayList<>();
                for (TrafficJamPoint trafficJamPoint : jamPointList) {
//                Map<String, Double > element = new HashMap<>();
//                element.put("lat", trafficJamPoint.getLat());
//                element.put("lon", trafficJamPoint.getLon());
//                data.add(element);

                    LatLng trafficJam = new LatLng(trafficJamPoint.getLat(), trafficJamPoint.getLon());
//                for (int i = 0; i < geoList.size(); i++) {
//                    if(geoList.get(i).getPosition() == trafficJam){
//
//                    }else {
                    markerForGeofence(trafficJam);

                    startGeofence();
                    //drawGeofence();
//                    }
//                }

                }
            }
//            Log.e("Received data", data.toString());
            //TODO: implement to display geoMarkers on map base on the data

        }


    }
    public boolean isConnectedToServer(String url, int timeout) {
        try{
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            connection.setConnectTimeout(timeout);
            connection.connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
