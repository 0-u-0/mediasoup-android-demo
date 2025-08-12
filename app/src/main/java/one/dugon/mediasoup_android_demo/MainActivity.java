package one.dugon.mediasoup_android_demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import one.dugon.mediasoup_android_sdk.Player;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Player player;
    private Player remotePlayer;

    RoomClient roomClient;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkCamPermission();

        player = findViewById(R.id.fullscreen_video_view);

        Button myButton = findViewById(R.id.myButton);
        myButton.setOnClickListener((v)->{
            roomClient = new RoomClient(getApplicationContext());
            roomClient.connect();
        });

        Button myButton2 = findViewById(R.id.myButton2);
        myButton2.setOnClickListener((v)->{
            roomClient.initView(player);
            roomClient.enableCam();
            roomClient.previewCam(player);
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

}