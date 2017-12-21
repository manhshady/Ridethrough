package com.example.user.projectsepm;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SubmitActivity extends AppCompatActivity {

    private EditText numSpeed;
    private EditText txtLat;
    private EditText txtLon;
    private EditText txtDatetime;
    private Button btnSubmit;
    private String res;
    int speed = 0;
    double lat = 0.0;
    double lon = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);

        numSpeed = (EditText) findViewById(R.id.numSpeed);
        txtLat = (EditText) findViewById(R.id.inputLat);
        txtLon = (EditText) findViewById(R.id.inputLon);
        txtDatetime = (EditText) findViewById(R.id.txtDatetime);

        DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        String date = dateFormat.format(Calendar.getInstance().getTime());
        txtDatetime.setText(date);


        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speed = Integer.parseInt( numSpeed.getText().toString() );
                lat = Double.parseDouble(txtLat.getText().toString());
                lon = Double.parseDouble(txtLon.getText().toString());

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

    private class UpdateData extends AsyncTask<TrafficJamPoint, Void, Void> {

        @Override
        protected Void doInBackground(TrafficJamPoint... trafficJamPoints) {
            res = HttpHandler.doPost("http://13.229.132.227:3000/traffic-jams", trafficJamPoints[0]);
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
