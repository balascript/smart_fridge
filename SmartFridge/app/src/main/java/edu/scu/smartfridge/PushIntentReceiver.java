package edu.scu.smartfridge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PushIntentReceiver extends BroadcastReceiver {
    public PushIntentReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("Intent Receiver","received Message");
    }
}
