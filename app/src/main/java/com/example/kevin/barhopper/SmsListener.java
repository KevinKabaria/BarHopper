package com.example.kevin.barhopper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

/**
 * Created by kevin on 3/20/17.
 */

public class SmsListener extends BroadcastReceiver {
    public String receivedMessage = "";
    public String contactNum = "";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] message = null;

            message = Telephony.Sms.Intents.getMessagesFromIntent(intent);

            for (int i = 0; i < message.length; i++)
            {
                receivedMessage += message[i].getMessageBody();
            }
            System.out.println("Received: " + receivedMessage);
/*
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");

                msgs = new SmsMessage[pdus.length];

                for (int i = 0; i < msgs.length; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    receivedMessage += msgs[i].getMessageBody();
                }
                contactNum = msgs[0].getOriginatingAddress();
                System.out.println("RECEIVED MESSAGE: " + receivedMessage);
            }

*/
        }
    }
}
