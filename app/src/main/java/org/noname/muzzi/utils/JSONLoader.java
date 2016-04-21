package org.noname.muzzi.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class JSONLoader {

    public JSONArray getJSONFromUrl(String urlString) {
        JSONArray jsonArray = null;
        String jsonString = "";
        InputStream inputStream = null;

        // Making HTTP request
        HttpURLConnection urlConnection;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            if (urlConnection != null) {
                inputStream = urlConnection.getInputStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (inputStream != null) {
            Charset cp1124 = Charset.forName("cp1124");
            Charset utf8 = Charset.forName("UTF-8");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        inputStream, cp1124), 8);
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\n");
                }
                inputStream.close();
                jsonString = stringBuilder.toString();
                byte[] jsonStringBytes = jsonString.getBytes(cp1124);
                jsonString = new String(jsonStringBytes, utf8);
            } catch (Exception e) {
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }
        }
        // try parse the string to a JSON object
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON Array
        return jsonArray;
    }
}