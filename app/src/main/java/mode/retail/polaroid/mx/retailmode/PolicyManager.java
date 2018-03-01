package mode.retail.polaroid.mx.retailmode;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

/**
 * Created by gustavo.arellano on 1/3/2018.
 */
public class PolicyManager {

    public static final int DPM_ACTIVATION_REQUEST_CODE = 100;

    private Context mContext;
    private DevicePolicyManager mDPM;
    private ComponentName adminComponent;

    public PolicyManager(Context context) {
        // TODO Auto-generated constructor stub
        this.mContext = context;
        mDPM = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(mContext.getPackageName(), mContext.getPackageName() + ".receiver.RMDeviceAdminReceiver");
    }

    public boolean isAdminActive() {
        return mDPM.isAdminActive(adminComponent);
    }

    public ComponentName getAdminComponent() {
        return adminComponent;
    }

    public void disableAdmin() {
        System.out.println("DISABLED ADMIN");
        System.out.println("DISABLED ADMIN");
        System.out.println("DISABLED ADMIN");
        System.out.println("DISABLED ADMIN");
        System.out.println("DISABLED ADMIN");
        mDPM.removeActiveAdmin(adminComponent);
    }
}