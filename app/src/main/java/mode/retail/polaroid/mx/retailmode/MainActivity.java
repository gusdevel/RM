package mode.retail.polaroid.mx.retailmode;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import mode.retail.polaroid.mx.retailmode.utils.DeviceInfoUtils;
import mode.retail.polaroid.mx.retailmode.utils.Utilerias;

public class MainActivity extends AppCompatActivity {

    //private PolicyManager policyManager;

    private DownloadManager downloadManager = null;
    private TextView textViewProgressInfo = null;
    private ProgressBar progressBar;

    private String ENDPOINT_WS_COSULTA_VIDEO = "http://10.140.125.38/DiamondElectronics/Polaroid/PolaroidAPI/public/api/phones/";
    private String URL_DOWNLOAD_VIDEO;

    String DEMO_VIDEO = "https://www.dropbox.com/s/kmv8a9jvmh9f87x/rm.mp4?dl=1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        lock.disableKeyguard();

        /*
        policyManager = new PolicyManager(this);
        policyManager.disableAdmin();
        */

        textViewProgressInfo = (TextView) findViewById(R.id.textInfoProgress);
        textViewProgressInfo.setText(getResources().getString(R.string.text_info_progress_start));

        // SOLO ES DEMOSTRATIVO...... ELIMINAR
        //Toast.makeText(getApplicationContext(), "MODELO: " + DeviceInfoUtils.getDeviceName(), Toast.LENGTH_SHORT).show();

        Context context = getApplicationContext();
        //if(isOnlineNet(context)) {
        if (false) { //nunca entrará
            if (haveStoragePermission()) {
                textViewProgressInfo.setText(getResources().getString(R.string.text_info_progress_download_video));

                // Call WebService
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //Toast.makeText(getApplicationContext(), "MODELO: " + DeviceInfoUtils.getDeviceName(), Toast.LENGTH_SHORT).show();

                            URL endpoint = new URL(ENDPOINT_WS_COSULTA_VIDEO + "/" + DeviceInfoUtils.getDeviceName());

                            HttpsURLConnection conn = (HttpsURLConnection) endpoint.openConnection();
                            if (conn.getResponseCode() == 200) {
                                InputStream responseBody = conn.getInputStream();
                                InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                                JsonReader jsonReader = new JsonReader(responseBodyReader);

                                jsonReader.beginObject(); // Start processing the JSON object
                                while (jsonReader.hasNext()) { // Loop through all keys
                                    String key = jsonReader.nextName(); // Fetch the next key
                                    String value = jsonReader.nextString();
                                }
                                jsonReader.close();
                                conn.disconnect();
                            }
                        } catch (Exception e) {
                            Log.d("ERROR", e.toString());
                        }
                    }
                });

                String nameVideo = getResources().getString(R.string.retail_mode_video_sufix);
                /* Valida la existencia de videos */
                if(!isFileExist(nameVideo)) {
                    //Toast.makeText(getApplicationContext(), "No existe video, iniciando descarga", Toast.LENGTH_LONG).show();

                    /* Descarga del video */
                    //URL_DOWNLOAD_VIDEO = getResources().getString(R.string.retail_mode_video_url);
                    URL_DOWNLOAD_VIDEO = DEMO_VIDEO;

                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(URL_DOWNLOAD_VIDEO));
                    request.setDescription(getResources().getString(R.string.download_manager_description));
                    request.setTitle(getResources().getString(R.string.download_manager_title));

                    // in order for this if to run, you must use the android 3.2 to compile your app
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    }

                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nameVideo);
                    //request.setDestinationInExternalPublicDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), nameVideo);

                    // get download service and enqueue file
                    downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    downloadManager.enqueue(request);

                    registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                } else {
                    //Toast.makeText(getApplicationContext(), "Existe video, iniciando reproduccion", Toast.LENGTH_LONG).show();

                    startRetailModeActivity();
                }
            } else {
                /* Sin permisos de guardar en el storage */
                textViewProgressInfo.setText(getResources().getString(R.string.text_info_progress_permission_error));
            }
        } else {
            try {
                if (null != Utilerias.readVideosFromDirectory() && Utilerias.readVideosFromDirectory().size() > 0) {
                    textViewProgressInfo.setText(getResources().getString(R.string.text_info_progress_playing_video));

                    Toast.makeText(getApplicationContext(), "Sin conectividad a WIFI", Toast.LENGTH_SHORT).show();
                    startRetailModeActivity();
                } else {
                    progressBar = (ProgressBar) findViewById(R.id.loading_progress_xml);

                    progressBar.setVisibility(View.GONE);
                    textViewProgressInfo.setText("No hay contenido para reproducir");
                }
            } catch (Exception e) {
                progressBar = (ProgressBar) findViewById(R.id.loading_progress_xml);

                progressBar.setVisibility(View.GONE);
                textViewProgressInfo.setText("No hay contenido para reproducir");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    BroadcastReceiver receiver = new BroadcastReceiver () {
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(downloadManager.ACTION_DOWNLOAD_COMPLETE) ){
            textViewProgressInfo.setText(getResources().getString(R.string.text_info_progress_download_video_success));

            unregisterReceiver(receiver);
            finishActivity(99);

            //Toast.makeText(getApplicationContext(), getResources().getString(R.string.text_info_progress_playing_video), Toast.LENGTH_SHORT).show();

            startRetailModeActivity();
        }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event){
        String DEBUG_TAG = "RM";
        int action = MotionEventCompat.getActionMasked(event);

        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                Log.d(DEBUG_TAG,"Action was DOWN");

                Intent i = new Intent(MainActivity.this, MainActivity.class);
                startActivity(i);

                return true;
            case (MotionEvent.ACTION_MOVE) :
                Log.d(DEBUG_TAG,"Action was MOVE");
                return true;
            case (MotionEvent.ACTION_UP) :
                Log.d(DEBUG_TAG,"Action was UP");
                return true;
            case (MotionEvent.ACTION_CANCEL) :
                Log.d(DEBUG_TAG,"Action was CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE) :
                Log.d(DEBUG_TAG,"Movement occurred outside bounds " +
                        "of current screen element");
                return true;
            default :
                return super.onTouchEvent(event);
        }
    }

    /* Checks if external storage is available for read and write */
    public  boolean haveStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission error","You have permission");
                return true;
            } else {
                Log.e("Permission error","You have asked for permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //you dont need to worry about these stuff below api level 23
            Log.e("Permission error","You already have the permission");
            return true;
        }
    }

    /**
     * Valida a conexion a INTERNET
     * @param context
     * @return
     */
    public static boolean isOnlineNet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected() && (networkInfo.getType() == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * Valida la existencia de archivos
     * @param name
     * @return
     */
    public boolean isFileExist(String name) {
        boolean existeArchivo = false;
        File[] files = readVideosFromDirectory();

        for (File file : files) {
            if (file.getName().equalsIgnoreCase(name)) {
                existeArchivo = true;
                break;
            }
        }

        return existeArchivo;
    }

    public void startRetailModeActivity() {
        Intent i = new Intent(MainActivity.this, RetailModeActivity.class);
        startActivity(i);
    }

    /**
     * Obtiene listado de videos en el directorio
     *
     * @return
     */
    private File[] readVideosFromDirectory(){
        List<File> videos = new ArrayList<File>();;
        File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return f.listFiles();
    }
}
