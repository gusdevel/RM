package mode.retail.polaroid.mx.retailmode;

import android.Manifest;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import mode.retail.polaroid.mx.retailmode.receiver.RMDeviceAdminReceiver;
import mode.retail.polaroid.mx.retailmode.receiver.ScreenReceiver;
import mode.retail.polaroid.mx.retailmode.services.TimeService;

import static android.Manifest.*;

public class RetailModeActivity extends AppCompatActivity {

    private Intent myService;
    static Window window;
    static Context context;
    int contTouch = 0;

    private static final String TAG = "RetailModeActivity";

    private String PATH_VIDEOS_DOWNLOAD = "/storage/emulated/0/Download/";

    boolean KEY_CLOSE_APP = false;

    private VideoView videoView;
    List<File> videos;
    int contadorVideos = 1;
    public static boolean isNewInstanceWindow = true;

    //
    private ComponentName mAdminComponentName;
    private DevicePolicyManager mDevicePolicyManager;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onStart() {
        super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();

        try {
            //SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            //SharedPreferences.Editor editor = sharedPref.edit();
            //editor.putString("password", "123");
            //editor.commit();

            // clearing app data
            /*String packageName = getApplicationContext().getPackageName();
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("adb root");
            runtime.exec("adb shell setenfoerce 0 v");
            runtime.exec("adb shell");
            runtime.exec("sqlite> update secure set value=123456 where name='lockscreen.password_type';");
            runtime.exec("sqlite> .exit");
            runtime.exec("adb reboot");*/
        } catch (Exception e) {
            System.err.println(e);
        }

        // start lock task mode if it's not already active
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        // ActivityManager.getLockTaskModeState api is not available in pre-M.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (!am.isInLockTaskMode()) {
                //startLockTask();
            }
        } else {
            if (am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE) {
                //startLockTask();
            }
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        contTouch = 0;
        KEY_CLOSE_APP = false;

        // Don't enable the profile again if this activity is being re-initialized.
        if (null == savedInstanceState) {
            mAdminComponentName = getComponentName();
            mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

            if (!mDevicePolicyManager.isAdminActive(mAdminComponentName)) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminComponentName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Retail Mode requiere ser administrador");
                startActivityForResult(intent, 1);

                //setDefaultCosuPolicies(true);
            } else {
                System.err.println("SÍ ES ADMIN");
            }

        }

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(RetailModeActivity.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        lock.disableKeyguard();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_retail_mode);

        /* ScreeenReceiver */
        //IntentFilter filter = new IntentFilter( Intent.ACTION_SCREEN_ON );
        //filter.addAction( Intent.ACTION_SCREEN_OFF );
        //registerReceiver(new ScreenReceiver(), filter);

        //policyManager = new PolicyManager(this);
        //policyManager.disableAdmin();

        //Stop TimerService
        if (null != myService) {
            stopService(myService);
        }

        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        videos = readVideosFromDirectory();
        videoView = (VideoView) findViewById(R.id.retailModeView);
        videoView.setVideoPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + videos.get(0).getName());
        videoView.requestFocus();
        videoView.start();

        /* Video bucle */
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (videos.size() > contadorVideos) {
                    videoView.setVideoPath(PATH_VIDEOS_DOWNLOAD + videos.get(contadorVideos).getName());
                    // Incrementa posicion de videos
                    contadorVideos++;
                } else {
                    videoView.setVideoPath(PATH_VIDEOS_DOWNLOAD + videos.get(0).getName());
                    contadorVideos = 1;
                }
                videoView.requestFocus();
                videoView.start();
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(new ScreenReceiver(), filter);

        ComponentName mDeviceAdminSample;
        mDeviceAdminSample = new ComponentName(this, RMDeviceAdminReceiver.class);

        String[] requiredPermissions = new String[]{
                permission.WRITE_SECURE_SETTINGS
            /* ETC.. */
        };

        Intent intent;
        if (Build.VERSION.SDK_INT > 22 && !hasPermissions(requiredPermissions)) {
            intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Retail Mode requiere ser administrador del dispositivo.");
            startActivityForResult(intent, 1);

            /*intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);*/
        } else {
            Toast.makeText(context, "Con tiene permisos", Toast.LENGTH_LONG).show();
        }
/*
        int adb = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.ADB_ENABLED, 0);
        // toggle the USB debugging setting
        adb = adb == 0 ? 1 : 0;
        Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.ADB_ENABLED, adb);*/

        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(context.DEVICE_POLICY_SERVICE);
        try {
            devicePolicyManager.setPasswordQuality(mDeviceAdminSample, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
            devicePolicyManager.setPasswordMinimumLength(mDeviceAdminSample, 5);

            boolean result = devicePolicyManager.resetPassword("1234567", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
            Toast.makeText(context, "button_lock_password_device..." + result, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }


        try {
            try {
                Class lockPatternUtilsCls = Class.forName("com.android.internal.widget.LockPatternUtils");
                Constructor lockPatternUtilsConstructor =
                        lockPatternUtilsCls.getConstructor(new Class[]{Context.class});
                lockPatternUtilsConstructor.setAccessible(true);
                Object lockPatternUtils = lockPatternUtilsConstructor.newInstance(RetailModeActivity.this);
                Method clearLockMethod = lockPatternUtils.getClass().getMethod("clearLock", boolean.class);
                clearLockMethod.setAccessible(true);
                clearLockMethod.invoke(lockPatternUtils, true);
                Method setLockScreenDisabledMethod = lockPatternUtils.getClass().getMethod("setLockScreenDisabled", boolean.class);
                setLockScreenDisabledMethod.setAccessible(true);
                setLockScreenDisabledMethod.invoke(lockPatternUtils, false);
            } catch (Exception e) {
                System.err.println("An InvocationTargetException was caught!");
                Throwable cause = e.getCause();
            }
            devicePolicyManager.setPasswordQuality(mDeviceAdminSample, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
            devicePolicyManager.setPasswordMinimumLength(mDeviceAdminSample, 0);
            boolean result = devicePolicyManager.resetPassword("12345", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        //Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
        //startActivityForResult(intent, 1);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public boolean hasPermissions(@NonNull String... permissions) {
        for (String permission : permissions)
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permission))
                return false;
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        window = this.getWindow();

        clearScreen();

        System.out.println(":::RetailModeActivity->onResume::: " + hasWindowFocus());

        //Stop TimerService
        if (null != myService) {
            stopService(myService);
        }

        videoView.start();
    }

    public void onUserInteraction() {
        super.onUserInteraction();
    }

    @Override
    public void onPause() {
        super.onPause();

        contTouch = 0;
        KEY_CLOSE_APP = false;

        System.out.println(":::RetailModeActivity->onPause::: " + hasWindowFocus());

        window = this.getWindow();


        System.out.println("::: has windows focus :::");
        myService = new Intent(RetailModeActivity.this, TimeService.class);
        //startService(myService);

    }

    @Override
    public void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());

        System.out.println(":::RetailModeActivity->onStop::: " + hasWindowFocus());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(myService);
        } else {
            startService(myService);


        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        System.out.println(":::RetailModeActivity->onDestroy:::");
        // The activity is about to be destroyed.

        if (contTouch > 2 && KEY_CLOSE_APP) {
            //Stop TimerService
            stopService(myService);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        if (index == 1) {
            System.out.println("mutli1");
        } else {
            System.out.println("single");

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                contTouch++;
            }
        }

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {

        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            KEY_CLOSE_APP = true;
        }

        if (keyCode == KeyEvent.KEYCODE_POWER || keyCode == KeyEvent.KEYCODE_HOME) {
            isNewInstanceWindow = false;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Obtiene listado de videos en el directorio
     *
     * @return
     */
    private List<File> readVideosFromDirectory() {
        List<File> videos = new ArrayList<File>();
        //File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        //File f = Environment.getExternalStoragePublicDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
        File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        File files[] = f.listFiles();
        for (File file : files) {
            if (getExtension(file).equalsIgnoreCase("MP4")) {
                System.out.println(file.getName());

                videos.add(file);
            }
        }
        return videos;
    }

    /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public static void clearScreen() {
        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);

            // Unlock the screen
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE, "RetailMode");
            wl.acquire();

            KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock kl = km.newKeyguardLock("RetailMode");
            kl.disableKeyguard();

            Intent myIntent = new Intent(context, RetailModeActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(myIntent);
        } catch (Exception e) {

        }

        // unlock screen
        //unlockScreen();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("RetailMode Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }
}