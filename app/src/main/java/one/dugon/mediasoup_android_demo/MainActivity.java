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

import java.util.HashMap;
import java.util.Map;

import one.dugon.mediasoup_android_sdk.Engine;
import one.dugon.mediasoup_android_sdk.Player;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Player player;

    Engine engine;

    String signalServer = "ws://198.18.0.1:4443";
    String roomId = "dev";
    String peerId = "abc";

    private HashMap<String,Player> remotePlayers;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkCamPermission();

        remotePlayers = new HashMap<>();
        player = findViewById(R.id.local_video_view);
        engine = new Engine(getApplicationContext());

        engine.setListener(new Engine.Listener() {
            @Override
            public void onPeer(String peerId, Engine.PeerState state) {
                if(state == Engine.PeerState.Join){
                    Log.d(TAG, "Join: "+ peerId);

                    addRemoteVideoRenderer(peerId);
                }else if(state == Engine.PeerState.Leave){
                    Log.d(TAG, "Leave: "+ peerId);

                    removeRemoteVideoRenderer(peerId);
                }
            }

            @Override
            public void onMedia(String peerId, String consumerId, Engine.MediaKind kind, boolean available) {
                if (kind == Engine.MediaKind.Video && available){
                    playRemote(peerId, consumerId);
                }
            }
        });

        Button myButton = findViewById(R.id.myButton);
        myButton.setOnClickListener((v)->{
            engine.connect(signalServer, roomId, peerId);
        });

        Button myButton2 = findViewById(R.id.myButton2);
        myButton2.setOnClickListener((v)->{
            engine.initView(player);
            engine.enableCam();
            engine.previewCam(player);
        });

        Button myButton3 = findViewById(R.id.myButton3);
        myButton3.setOnClickListener((v)->{
            engine.enableMic();
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

    public void addRemoteVideoRenderer(String peerId){

        runOnUiThread(()->{
            LinearLayout linearLayout = findViewById(R.id.top_player_container); // Get existing GridLayout

            Player remotePlayer = new Player(this);

            remotePlayer.setId(View.generateViewId());
//            remotePlayer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
//            renderer.setZOrderMediaOverlay(true);
//            renderer.setEnableHardwareScaler(true);

            engine.initView(remotePlayer);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(linearLayout.getWidth() / 3 ,LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0,8,0,0);
            remotePlayer.setLayoutParams(params);

            linearLayout.addView(remotePlayer);

            remotePlayers.put(peerId, remotePlayer);

        });
    }

    public void removeRemoteVideoRenderer(String peerId) {
        runOnUiThread(()-> {
            LinearLayout linearLayout = findViewById(R.id.top_player_container); // Get existing GridLayout

            Player remotePlayer = remotePlayers.get(peerId);
            linearLayout.removeView(remotePlayer);

            remotePlayers.remove(peerId);
        });
    }

    public void playRemote(String peerId, String consumerId) {

        runOnUiThread(()-> {
            Player remotePlayer = remotePlayers.get(peerId);

            engine.play(remotePlayer, consumerId);
        });

    }

}