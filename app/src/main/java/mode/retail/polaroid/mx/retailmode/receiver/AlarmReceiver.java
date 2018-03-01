package mode.retail.polaroid.mx.retailmode.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mode.retail.polaroid.mx.retailmode.services.RMIntentService;

public class AlarmReceiver extends BroadcastReceiver {

    public static final int REQUEST_CODE = 12345;

    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, RMIntentService.class);
        i.putExtra("foo", "bar");
        context.startService(i);
    }
}
