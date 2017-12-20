package com.example.user.projectsepm;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by user on 6/12/2017.
 */

public class HttpHandler {
    public static String doPost(String urlStr, /*List<HashMap<String, String>> dataMap*/ TrafficJamPoint trafficJamPoint){

        try {

            URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //Connect
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.connect();


//            List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
//            for (HashMap<String, String> data: dataMap) {
//                JSONObject jsonObj = new JSONObject(data);
//                jsonObjects.add(jsonObj);
//            }
//            JSONArray jsonArray = new JSONArray(jsonObjects);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("speed", trafficJamPoint.getSpeed());
            jsonObject.put("lat", trafficJamPoint.getLatitude());
            jsonObject.put("lon", trafficJamPoint.getLongitude());
            jsonObject.put("datetime", trafficJamPoint.getDatetime());


            //Write
            OutputStream outputStream = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            //Log.e("jsonArray", jsonArray.toString());
            //writer.write(jsonArray.toString());
            Log.e("jsonObject", jsonObject.toString());
            writer.write(jsonObject.toString());
            writer.close();
            outputStream.close();

            int status = conn.getResponseCode();
            InputStream inputStream;
            //Read
            if (status >= 400 && status <= 499){
                throw new Exception("Bad authentication status: "+ status);
            }else {
                inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                return builder.toString();
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";

    }

    public static String getJSONFromUrl(String url)  {

        HttpURLConnection httpURLConnection ;
        BufferedReader bufferedReader;
        StringBuilder stringBuilder;
        String line;
        String jsonString = null;
        try {
            URL u = new URL(url);
            httpURLConnection = (HttpURLConnection) u.openConnection();
            httpURLConnection.setRequestMethod("GET");
            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            stringBuilder = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line + '\n');
            }
            jsonString = stringBuilder.toString();
            httpURLConnection.disconnect();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonString;
    }

}
