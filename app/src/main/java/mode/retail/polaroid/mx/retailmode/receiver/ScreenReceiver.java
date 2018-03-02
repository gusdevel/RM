package mode.retail.polaroid.mx.retailmode.receiver;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import mode.retail.polaroid.mx.retailmode.MainActivity;
import mode.retail.polaroid.mx.retailmode.RetailModeActivity;
import mode.retail.polaroid.mx.retailmode.services.TimeService;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //String action = intent.getAction();

        //Toast.makeText(context, "Receiver display", Toast.LENGTH_LONG).show();

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            RetailModeActivity.unlockScreen();

            // Unlock the screen
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK
                            | PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.ON_AFTER_RELEASE,
                    "RetailMode");
            wl.acquire();

            KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock kl = km.newKeyguardLock("RetailMode");
            kl.disableKeyguard();

            Intent i = new Intent(context, RetailModeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            context.startActivity(i);
            wl.release();
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Intent i = new Intent(context, RetailModeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            context.startActivity(i);
        }
    }
}
