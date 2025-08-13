package one.dugon.mediasoup_android_demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import one.dugon.mediasoup_android_sdk.Engine;
import one.dugon.mediasoup_android_sdk.Player;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Player player;
    private Player remotePlayer;

    Engine engine;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkCamPermission();

        player = findViewById(R.id.local_video_view);
        engine = new Engine(getApplicationContext());

        engine.onTrack = (String trackId)->{
            Log.d(TAG, "onTrack");

            addRemoteVideoRenderer(trackId);
        };

        Button myButton = findViewById(R.id.myButton);
        myButton.setOnClickListener((v)->{
            engine.connect();
        });

        Button myButton2 = findViewById(R.id.myButton2);
        myButton2.setOnClickListener((v)->{
            engine.initView(player);
            engine.enableCam();
            engine.previewCam(player);
        });
    }

    private void checkCamPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // camera ok
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void addRemoteVideoRenderer(String trackId){

        runOnUiThread(()->{
            LinearLayout linearLayout = findViewById(R.id.top_player_container); // Get existing GridLayout

            remotePlayer = new Player(this);

            remotePlayer.setId(View.generateViewId());
//            remotePlayer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
//            renderer.setZOrderMediaOverlay(true);
//            renderer.setEnableHardwareScaler(true);

            engine.initView(remotePlayer);

//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,linearLayout.getHeight() / 3);
//            params.setMargins(0,8,0,0);
//            renderer.setLayoutParams(params);

            linearLayout.addView(remotePlayer);

//            videoTrack.addSink(renderer);

//            remoteRenderers.add(renderer);

            engine.play(remotePlayer, trackId);
        });

    }
}