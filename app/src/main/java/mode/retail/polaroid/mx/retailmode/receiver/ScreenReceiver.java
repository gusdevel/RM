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
        String action = intent.getAction();
        switch (action) {
            case Intent.ACTION_SCREEN_OFF:
                RetailModeActivity.clearScreen();
                break;

            case Intent.ACTION_SCREEN_ON:
                // and do whatever you need to do here
                RetailModeActivity.clearScreen();
        }
    }
}
