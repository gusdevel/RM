package mode.retail.polaroid.mx.retailmode.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mode.retail.polaroid.mx.retailmode.RetailModeActivity;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, RetailModeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
