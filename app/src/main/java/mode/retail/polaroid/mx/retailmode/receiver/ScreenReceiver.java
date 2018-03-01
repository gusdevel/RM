package mode.retail.polaroid.mx.retailmode.receiver;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import mode.retail.polaroid.mx.retailmode.MainActivity;
import mode.retail.polaroid.mx.retailmode.RetailModeActivity;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //String action = intent.getAction();

        RetailModeActivity.clearScreen();
    }
}
