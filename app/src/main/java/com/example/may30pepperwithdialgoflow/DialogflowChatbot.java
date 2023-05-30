package com.example.may30pepperwithdialgoflow;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseChatbot;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.ReplyPriority;
import com.aldebaran.qi.sdk.object.conversation.StandardReplyReaction;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.example.may30pepperwithdialgoflow.Dialogflow.StreamDetectIntent;

import java.io.IOException;

public class DialogflowChatbot extends BaseChatbot {

    QiContext context;

    protected DialogflowChatbot(QiContext context) {
        super(context);
        this.context = context;
    }

    @Override
    public StandardReplyReaction replyTo(Phrase phrase, Locale locale) {
        String inputText = phrase.getText();
        String response = "";
        StreamDetectIntent streamDetectIntent = new StreamDetectIntent();
        try {
             response = streamDetectIntent.detectIntentText(inputText, new MainActivity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(response == ""){
            response = streamDetectIntent.getQueryResult();
        }

        return new StandardReplyReaction(new SimpleSayReaction(context, response), ReplyPriority.NORMAL);
//        return new StandardReplyReaction(new SimpleSayReaction(context, "Hello, I am pepper bot!"), ReplyPriority.NORMAL);
    }
}

