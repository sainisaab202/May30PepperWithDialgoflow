package com.example.may30pepperwithdialgoflow;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Say;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private QiContext qiContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QiSDK.register(this, this);
        Log.e("MainActivity", "-onCreate");
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        QiSDK.register(this, this);
    }

    @Override
    protected void onPause() {
        QiSDK.unregister(this, this);
        super.onPause();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;

        Log.e("Pepper", "focus gained");

        // Create a new say action.
        Say say = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Hello human!") // Set the text to say.
                .build(); // Build the say action.

        // Execute the action.
        say.run();

        DialogflowChatbot dialogflowChatbot = new DialogflowChatbot(qiContext);
        Chat chat = ChatBuilder.with(qiContext).withChatbot(dialogflowChatbot).build();
        chat.async().run();

    }

    @Override
    public void onRobotFocusLost() {
        Log.e("Pepper", "focus :LOST");
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.e("Pepper", "focus Refused");

    }

    private void saySomething(String textToSay) {
        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();

    }

    public void onSayButtonClicked(View view) {
//        saySomething("Hello, Pepper!");
        Log.e("MainActivity", "button clicked!");
    }
}