package com.example.may30pepperwithdialgoflow.Dialogflow;

////-----------------------------------This project is using REST API to connect with dialogflow - voice in/out
////------------------------------------Specifically for Android 6.0
////------------------------------------As, Google built library have some dependencies that are NOT available in Android 6
////------------------------------------This solution is using following approach:

////------------------------------------| App |                                   | Google Servers |
////------------------------------------1. Creating and Sign JWT                       ---
////------------------------------------2. Use JWT to request token       --->         ---
////------------------------------------3.                                <---         Token response
////------------------------------------4. Use token to call google API   --->         Token response

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Base64;
import android.util.Log;

import com.google.api.gax.rpc.ApiException;
import com.google.protobuf.ByteString;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class StreamDetectIntent{

    private static String TAG = "StreamDetectIntent";

    public static Context context;
    String projectId, locationId, agentId, sessionId;

    String response;

    public StreamDetectIntent(){
        projectId = "dbc-chatbot-poc";
        //pepper GPT from sam
        locationId= "us-central1";
        agentId = "618fa728-2278-4c4c-8dfc-8967c3bac339";

//        greg
//        locationId= "northamerica-northeast1";
//        agentId = "17b8b94f-a3dd-46cb-b15d-891013b921d6";

//personal
//        projectId = "myprjmay2fordialogflow";
//        locationId= "us-central1";
//        agentId = "8bf30bd9-571f-4165-add2-28615ee9c474";
        sessionId = UUID.randomUUID().toString();
    }

    public void detectIntent(
            String audioFilePath,
            Context context)
            throws IOException, ApiException {

        this.context = context;

        try {
            String DIALOGFLOW_API_ENDPOINT = "https://"+locationId+"-dialogflow.googleapis.com/v3beta1/projects/"
                    + projectId + "/locations/"
                    + locationId + "/agents/"
                    + agentId + "/sessions/"
                    + sessionId + ":detectIntent";

            String audioData = "";

            //encoding byte[] of audio file into Base64 for the request
            audioData = Base64.encodeToString(convertAudioToByteArray(audioFilePath), Base64.NO_WRAP);

            // Prepare the request payload for TEXT request
//            JSONObject requestBody = new JSONObject()
//                    .put("queryInput", new JSONObject()
//                            .put("text", new JSONObject()
//                                    .put("text", query))
//                            .put("languageCode", "en"));

            // Prepare the request payload for AUDIO request
            JSONObject requestBody = new JSONObject()
                    .put("queryInput", new JSONObject()
                            .put("audio", new JSONObject()
                                    .put("config", new JSONObject()
                                            .put("audioEncoding", "AUDIO_ENCODING_AMR_WB")
                                            .put("sampleRateHertz", 16000))
                                    .put("audio", audioData))
                            .put("languageCode", "en-US"))
                    .put("outputAudioConfig", new JSONObject()
                            .put("audioEncoding", "OUTPUT_AUDIO_ENCODING_LINEAR_16")
                            .put("sampleRateHertz", 16000));

            // Create an OkHttpClient for making HTTP requests
            OkHttpClient client = new OkHttpClient();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
//                        Log.e(TAG, "in thread: "+Thread.currentThread().getName());

                        // generating JWT for requesting access token
                        String jwtToken = JwtGenerator.createJwt(context);

                        //requesting/getting access token
                        String accessToken = AccessTokenRequester.requestAccessToken(jwtToken);

                        // Prepare the HTTP request
                        Request request = new Request.Builder()
                                .url(DIALOGFLOW_API_ENDPOINT)
                                .addHeader("Authorization", "Bearer " + accessToken)
                                .addHeader("Content-Type", "application/json")
                                .post(RequestBody.create(MediaType.parse("application/json"), requestBody.toString()))
                                .build();

                        // Send the HTTP request to Dialogflow
                        Response response = client.newCall(request).execute();

                        // Handle the response
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            // Extract the response text from the response and use it in your app's logic or UI.
                            String fulfillmentText = jsonResponse.getJSONObject("queryResult")
                                    .getJSONArray("responseMessages").getJSONObject(0)
                                    .getJSONObject("text").getJSONArray("text").get(0).toString();

                            //extracting audio data from response
                            String outputAudio = jsonResponse.getString("outputAudio");

                            //playing audio output
                            playAudioBase64(outputAudio);

                            Log.e(TAG,"REST-Resp:"+ fulfillmentText);
                        } else {
                            Log.e(TAG, "REST-Resp"+"Error: " + response.code() + " " + response.message());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            };

            Thread restCall = new Thread(runnable);
            restCall.start();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        //-------------Following code doesn't work with Android 6.0 or less because of dependencies issues---------

//        SessionsSettings.Builder sessionsSettingsBuilder = SessionsSettings.newBuilder();
//
//        if (locationId.equals("global")) {
//            sessionsSettingsBuilder.setEndpoint("dialogflow.googleapis.com:443");
//        } else {
//            sessionsSettingsBuilder.setEndpoint(locationId + "-dialogflow.googleapis.com:443");
//        }
//
//
//        InputStream stream = context.getResources().openRawResource(R.raw.credentials);
//        GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
//                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
//
//        SessionsSettings sessionsSettings = sessionsSettingsBuilder.setCredentialsProvider(
//                FixedCredentialsProvider.create(credentials)).build();
//
//        // Instantiates a client.
//        // Note: close() needs to be called on the SessionsClient object to clean up resources
//        // such as threads. In the example below, try-with-resources is used,
//        // which automatically calls close().
//        try (SessionsClient sessionsClient = SessionsClient.create(sessionsSettings)) {
//
//            SessionName session = SessionName.of(projectId,locationId,agentId,sessionId);
//
//            Log.e(TAG, session.toString());
//
//            // Instructs the speech recognizer how to process the audio content.
//            // Note: hard coding audioEncoding and sampleRateHertz for simplicity.
//            // Audio encoding of the audio content sent in the query request.
//            InputAudioConfig inputAudioConfig =
//                    InputAudioConfig.newBuilder()
//                            .setAudioEncoding(AudioEncoding.AUDIO_ENCODING_AMR_WB)
//                            .setSampleRateHertz(16000)
//                            .build();
//
//            OutputAudioConfig outputAudioConfig = OutputAudioConfig.newBuilder()
//                    .setAudioEncoding(OutputAudioEncoding.OUTPUT_AUDIO_ENCODING_LINEAR_16)
//                    .setSampleRateHertz(16000)
//                    .build();
//
//            //converting our audio file int byteArray
//            byte[] audioData = convertAudioToByteArray(audioFilePath);
//
//            AudioInput audioInput = AudioInput.newBuilder()
//                    .setConfig(inputAudioConfig)
//                    .setAudio(ByteString.copyFrom(audioData))
//                    .build();
//
//            // Build the query with the InputAudioConfig
//            QueryInput queryInput = QueryInput.newBuilder()
//                    .setAudio(audioInput).build();
//
//            // Create the Bidirectional stream
//            BidiStream<StreamingDetectIntentRequest, StreamingDetectIntentResponse> bidiStream =
//                    sessionsClient.streamingDetectIntentCallable().call();
//
//            // The first request must **only** contain the audio configuration:
//            bidiStream.send(
//                    StreamingDetectIntentRequest.newBuilder()
//                            .setSession(session.toString())
//                            .setOutputAudioConfig(outputAudioConfig)
//                            .setQueryInput(QueryInput.newBuilder().setLanguageCode("en-US").setAudio(AudioInput.newBuilder().setConfig(inputAudioConfig).build()))
//                            .build());
//
//
//            //The second request sending audio data:
//            bidiStream.send(
//                    StreamingDetectIntentRequest.newBuilder()
//                            .setQueryInput(queryInput)
//                            .build());
//
//            // Tell the service you are done sending data
//            bidiStream.closeSend();
//
//            for (StreamingDetectIntentResponse response : bidiStream) {
//
//                if(response.getDetectIntentResponse().getQueryResult().hasTranscript()){
//                    Log.e("RESPONSE", "-----GOT Transcript-----");
//                    Log.e("RESPONSE", response.getDetectIntentResponse().getQueryResult().getTranscript());
//                }
//
//                //listeners from old code
//                this.onNext(response);
//                this.onCompleted();
//
//            }

//        }
    }

    //will return text coming from dialogflow
    public String detectIntentText(
            String textInput,
            Context context)
            throws IOException, ApiException {

        String result = "";
        this.context = context;

        try {
            String DIALOGFLOW_API_ENDPOINT = "https://"+locationId+"-dialogflow.googleapis.com/v3beta1/projects/"
                    + projectId + "/locations/"
                    + locationId + "/agents/"
                    + agentId + "/sessions/"
                    + sessionId + ":detectIntent";

            // Prepare the request payload for TEXT request
            JSONObject requestBody = new JSONObject()
                    .put("queryInput", new JSONObject()
                            .put("text", new JSONObject()
                                    .put("text", textInput))
                            .put("languageCode", "en"));


            // Create an OkHttpClient for making HTTP requests
            OkHttpClient client = new OkHttpClient();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
//                        Log.e(TAG, "in thread: "+Thread.currentThread().getName());

                        // generating JWT for requesting access token
                        String jwtToken = JwtGenerator.createJwt(context);

                        //requesting/getting access token
                        String accessToken = AccessTokenRequester.requestAccessToken(jwtToken);

                        // Prepare the HTTP request
                        Request request = new Request.Builder()
                                .url(DIALOGFLOW_API_ENDPOINT)
                                .addHeader("Authorization", "Bearer " + accessToken)
                                .addHeader("Content-Type", "application/json")
                                .post(RequestBody.create(MediaType.parse("application/json"), requestBody.toString()))
                                .build();

                        // Send the HTTP request to Dialogflow
                        Response response = client.newCall(request).execute();

                        // Handle the response
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            // Extract the response text from the response and use it in your app's logic or UI.
                            String fulfillmentText = jsonResponse.getJSONObject("queryResult")
                                    .getJSONArray("responseMessages").getJSONObject(0)
                                    .getJSONObject("text").getJSONArray("text").get(0).toString();

//                            //extracting audio data from response
//                            String outputAudio = jsonResponse.getString("outputAudio");
//
//                            //playing audio output
//                            playAudioBase64(outputAudio);

                            Log.e(TAG,"REST-Resp:"+ fulfillmentText);

                            setQueryResult(fulfillmentText);

                        } else {
                            Log.e(TAG, "REST-Resp"+"Error: " + response.code() + " " + response.message());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            };

            Thread restCall = new Thread(runnable);
            restCall.start();

            if(restCall.isDaemon()){
                return getQueryResult();
            }else{
                while (restCall.isAlive()){
                    //do nothing
                }
                return getQueryResult();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        //-------------Following code doesn't work with Android 6.0 or less because of dependencies issues---------

//        SessionsSettings.Builder sessionsSettingsBuilder = SessionsSettings.newBuilder();
//
//        if (locationId.equals("global")) {
//            sessionsSettingsBuilder.setEndpoint("dialogflow.googleapis.com:443");
//        } else {
//            sessionsSettingsBuilder.setEndpoint(locationId + "-dialogflow.googleapis.com:443");
//        }
//
//
//        InputStream stream = context.getResources().openRawResource(R.raw.credentials);
//        GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
//                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
//
//        SessionsSettings sessionsSettings = sessionsSettingsBuilder.setCredentialsProvider(
//                FixedCredentialsProvider.create(credentials)).build();
//
//        // Instantiates a client.
//        // Note: close() needs to be called on the SessionsClient object to clean up resources
//        // such as threads. In the example below, try-with-resources is used,
//        // which automatically calls close().
//        try (SessionsClient sessionsClient = SessionsClient.create(sessionsSettings)) {
//
//            SessionName session = SessionName.of(projectId,locationId,agentId,sessionId);
//
//            Log.e(TAG, session.toString());
//
//            // Instructs the speech recognizer how to process the audio content.
//            // Note: hard coding audioEncoding and sampleRateHertz for simplicity.
//            // Audio encoding of the audio content sent in the query request.
//            InputAudioConfig inputAudioConfig =
//                    InputAudioConfig.newBuilder()
//                            .setAudioEncoding(AudioEncoding.AUDIO_ENCODING_AMR_WB)
//                            .setSampleRateHertz(16000)
//                            .build();
//
//            OutputAudioConfig outputAudioConfig = OutputAudioConfig.newBuilder()
//                    .setAudioEncoding(OutputAudioEncoding.OUTPUT_AUDIO_ENCODING_LINEAR_16)
//                    .setSampleRateHertz(16000)
//                    .build();
//
//            //converting our audio file int byteArray
//            byte[] audioData = convertAudioToByteArray(audioFilePath);
//
//            AudioInput audioInput = AudioInput.newBuilder()
//                    .setConfig(inputAudioConfig)
//                    .setAudio(ByteString.copyFrom(audioData))
//                    .build();
//
//            // Build the query with the InputAudioConfig
//            QueryInput queryInput = QueryInput.newBuilder()
//                    .setAudio(audioInput).build();
//
//            // Create the Bidirectional stream
//            BidiStream<StreamingDetectIntentRequest, StreamingDetectIntentResponse> bidiStream =
//                    sessionsClient.streamingDetectIntentCallable().call();
//
//            // The first request must **only** contain the audio configuration:
//            bidiStream.send(
//                    StreamingDetectIntentRequest.newBuilder()
//                            .setSession(session.toString())
//                            .setOutputAudioConfig(outputAudioConfig)
//                            .setQueryInput(QueryInput.newBuilder().setLanguageCode("en-US").setAudio(AudioInput.newBuilder().setConfig(inputAudioConfig).build()))
//                            .build());
//
//
//            //The second request sending audio data:
//            bidiStream.send(
//                    StreamingDetectIntentRequest.newBuilder()
//                            .setQueryInput(queryInput)
//                            .build());
//
//            // Tell the service you are done sending data
//            bidiStream.closeSend();
//
//            for (StreamingDetectIntentResponse response : bidiStream) {
//
//                if(response.getDetectIntentResponse().getQueryResult().hasTranscript()){
//                    Log.e("RESPONSE", "-----GOT Transcript-----");
//                    Log.e("RESPONSE", response.getDetectIntentResponse().getQueryResult().getTranscript());
//                }
//
//                //listeners from old code
//                this.onNext(response);
//                this.onCompleted();
//
//            }

//        }
    }

    private void setQueryResult(String fulfillmentText) {
        response = fulfillmentText;
    }

    public String getQueryResult(){
        return response;
    }

    //using when sending our request
    private static byte[] convertAudioToByteArray(String filePath) throws IOException {
        FileInputStream fileInputStream = null;
        ByteArrayOutputStream byteOutputStream = null;

        try {
            fileInputStream = new FileInputStream(filePath);
            byteOutputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                byteOutputStream.write(buffer, 0, bytesRead);
            }

            return byteOutputStream.toByteArray();
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }

            if (byteOutputStream != null) {
                byteOutputStream.close();
            }
        }
    }

    //play audio from base64 encoded string
    private void playAudioBase64(String data) {
        try {
            MediaPlayer mediaPlayer;
            byte[] audioData = Base64.decode(data, Base64.DEFAULT);

            // Write audio data to a temporary file
            String tempAudioFilePath = getTempAudioFilePath();
            writeAudioDataToFile(audioData, tempAudioFilePath);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(tempAudioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Audio playback completed
                    Log.e("ResponseAudio","...is finished");
                    cleanupTempAudioFile(tempAudioFilePath);

                    //here we can start recording again todo
//                    MainActivity.startRecording();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to write audio data to a temporary file
    private void writeAudioDataToFile(byte[] audioData, String filePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filePath);
        fos.write(audioData);
        fos.close();
    }

    // Method to generate a temporary file path for storing audio data
    private String getTempAudioFilePath() {
        String cacheDir = context.getCacheDir().getAbsolutePath();
        return cacheDir + "/temp_audio_file.wav"; // Adjust the file extension if needed
    }

    // Method to clean up the temporary audio file
    private void cleanupTempAudioFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    //used only for Android 6.0+
    //NOT using this method in current implementation
    // Method to play audio bytes using Android MediaPlayer
    private void playAudioBytes(ByteString audioBytes) {
        try {
            MediaPlayer mediaPlayer;
            byte[] audioData = audioBytes.toByteArray();

            // Write audio data to a temporary file
            String tempAudioFilePath = getTempAudioFilePath();
            writeAudioDataToFile(audioData, tempAudioFilePath);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(tempAudioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Audio playback completed
                    Log.e("ResponseAudio","...is finished");
                    cleanupTempAudioFile(tempAudioFilePath);

                    //here we can start recording again todo
//                    MainActivity.startRecording();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
