package com.example.may30pepperwithdialgoflow;


import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.BaseChatbotReaction;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.SpeechEngine;

import java.util.concurrent.ExecutionException;

public class SimpleSayReaction extends BaseChatbotReaction {

    private Future<Void> sayFuture;
    String answer;

    public SimpleSayReaction(QiContext context, String answer){
        super(context);
        sayFuture = null;
        this.answer = answer;
    }

    @Override
    public void runWith(SpeechEngine speechEngine) {
        Say say = SayBuilder.with(speechEngine).withText(answer).build();
        sayFuture = say.async().run();
        try{
            sayFuture.get();
        } catch (ExecutionException e) {
            Log.e("SimpleSayReaction", "Error during say:"+e.getMessage());
        }
    }

    @Override
    public void stop() {
        sayFuture.requestCancellation();
    }
}
