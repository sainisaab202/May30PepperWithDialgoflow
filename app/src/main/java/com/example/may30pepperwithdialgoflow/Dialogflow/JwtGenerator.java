package com.example.may30pepperwithdialgoflow.Dialogflow;

import android.content.Context;

import com.example.may30pepperwithdialgoflow.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtGenerator {

    public static String createJwt(Context context) throws JSONException {
        // Read the credentials JSON file from the raw folder
        InputStream inputStream = context.getResources().openRawResource(R.raw.credentials);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        try{
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        String credentialsStringJson = stringBuilder.toString();

        // Parse the JSON and extract the private key value
        JSONObject json = new JSONObject(credentialsStringJson);

        String privateKeyString = json.getString("private_key");

        try {
            // Convert the private key string to a PrivateKey object
            privateKeyString = privateKeyString
                    .replace("-----BEGIN PRIVATE KEY-----\n", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("\n", "");
            byte[] privateKeyBytes = android.util.Base64.decode(privateKeyString, android.util.Base64.DEFAULT);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // Build the JWT payload
            long currentTime = System.currentTimeMillis() / 1000L;
            long expirationTime = currentTime + 3600; // 1 hour
            String scope = "https://www.googleapis.com/auth/dialogflow"; // Define the necessary scope for your use case

            String jwt = Jwts.builder()
                    .setIssuer(json.getString("client_email"))
                    .setSubject(json.getString("client_email"))
                    .setAudience("https://accounts.google.com/o/oauth2/token")
                    .setIssuedAt(new Date(currentTime * 1000))
                    .setExpiration(new Date(expirationTime * 1000))
                    .claim("scope", scope)
                    .signWith(privateKey, SignatureAlgorithm.RS256)
                    .compact();

            return jwt;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
