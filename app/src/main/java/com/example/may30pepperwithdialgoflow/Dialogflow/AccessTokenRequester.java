package com.example.may30pepperwithdialgoflow.Dialogflow;

import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AccessTokenRequester {

    private static String TAG = AccessTokenRequester.class.getName();

    public static String requestAccessToken(String jwt) {

        // Create an OkHttpClient instance (can be done once for the entire app)
        OkHttpClient client = new OkHttpClient();

        // Construct the request body
        String requestBody = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=" + jwt;

        // Set up the HTTP request
        Request request = new Request.Builder()
                .url("https://oauth2.googleapis.com/token")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
//                .post(RequestBody.create(MediaType.parse("text/plain"), requestBody))
                .post(RequestBody.create(MediaType.parse("charset=utf-8"), requestBody))
                .build();

        // Execute the request
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                // Read the response
                String responseBody = response.body().string();
                // Handle the response as needed
                JSONObject accessToken = new JSONObject(responseBody);

                return accessToken.getString("access_token");
            } else {
                // Handle unsuccessful response (e.g., non-200 status code)
                Log.e(TAG,"Request unsuccessful. Response code: " + response.code());
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        //working with xiaomi but not with samsung
//        try {
//            // Set up the HTTP connection
//            URL url = new URL("https://oauth2.googleapis.com/token");
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            connection.setDoOutput(true);
//
//            // Construct the request body
//            String requestBody = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=" + jwt;
//
//            // Send the request
//            OutputStream outputStream = connection.getOutputStream();
//            outputStream.write(requestBody.getBytes("UTF-8"));
//            outputStream.close();
//
//            // Read the response
//            int responseCode = connection.getResponseCode();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            StringBuilder response = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                response.append(line);
//            }
//            reader.close();
//
//            JSONObject accessToken = new JSONObject(response.toString());
//
//
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                // Successful request
//                return accessToken.getString("access_token");
//            } else {
//                // Error response
//                Log.e(TAG ,"Error response: " + response.toString());
//                return null;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
    }
}