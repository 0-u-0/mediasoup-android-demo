package one.dugon.mediasoup_android_demo;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import one.dugon.mediasoup_android_sdk.Dugon;
import one.dugon.mediasoup_android_sdk.LocalVideoSource;
import one.dugon.mediasoup_android_sdk.Player;
import one.dugon.mediasoup_android_sdk.RecvTransport;
import one.dugon.mediasoup_android_sdk.SendTransport;
import one.dugon.mediasoup_android_sdk.protoo.ProtooEventListener;
import one.dugon.mediasoup_android_sdk.protoo.ProtooSocket;


public class RoomClient {

    private static final String TAG = "RoomClient";
    private ProtooSocket protoo;
    private SendTransport sendTransport;
    private RecvTransport recvTransport;

    private LocalVideoSource localVideoSource;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public RoomClient(Context context){
        protoo = new ProtooSocket();
        Dugon.initialize(context);
    }
    public void connect(){

        protoo.setEventListener(new ProtooEventListener() {
            @Override
            public void onConnect() {
                Log.d(TAG, "onConnect");

                executor.execute(()->{
                    getRtpCaps();
                    createWebRTCTransport(false);
                    createWebRTCTransport(true);
                    join();
                });
            }

            @Override
            public void onDisconnect() {

            }

            @Override
            public void onRequest(JsonObject requestData) {

            }

            @Override
            public void onNotification() {

            }

            @Override
            public void onError() {

            }
        });

        protoo.connect("ws://198.18.0.1:4443", Map.of("roomId", "dev", "peerId", "abc"));
    }

    private void getRtpCaps(){
        JsonObject response = protoo.requestSync("getRouterRtpCapabilities");
        Dugon.load(response);
    }

    private void createWebRTCTransport(boolean isSender){
        JsonObject createData = new JsonObject();
        createData.addProperty("consuming", !isSender);
        createData.addProperty("forceTcp", false);
        createData.addProperty("producing", isSender);

        JsonObject response = protoo.requestSync("createWebRtcTransport", createData);

        String id = response.get("id").getAsString();
        JsonObject iceParameters = response.getAsJsonObject("iceParameters");
        JsonArray iceCandidates = response.getAsJsonArray("iceCandidates");
        JsonObject dtlsParameters = response.getAsJsonObject("dtlsParameters");

        if (isSender){
            sendTransport = Dugon.createSendTransport(id, iceParameters, iceCandidates, dtlsParameters);

            sendTransport.onConnect = (JsonObject dtls)->{

            };

            sendTransport.onProduce = (JsonObject pData)->{
                return "";
            };

        }else{
            recvTransport = Dugon.createRecvTransport(id,iceParameters,iceCandidates,dtlsParameters);
//            recvTransport.onTrack = (MediaStreamTrack track)->{
//                String kind = track.kind();
//                if(kind.equals("video")){
//                    var videotrack =  (VideoTrack)track;
//                    videotrack.setEnabled(true);
//
////                    myVideoTrack = videotrack;
//                    addRemoteVideoRenderer(videotrack);
//                }
//            };
            recvTransport.onConnect = (JsonObject dtls)->{

            };

        }

    }

    private void join(){
        JsonObject joinData = new JsonObject();
        JsonObject rtpCapabilitiesJson = Dugon.rtpCapabilities;
        JsonObject sctpCapabilitiesJson = Dugon.sctpCapabilities;

        JsonObject device = new JsonObject();
        device.addProperty("flag", "chrome");
        device.addProperty("name", "Chrome");
        device.addProperty("version", "129.0.0.0");

        joinData.add("device", device);
        joinData.add("rtpCapabilities", rtpCapabilitiesJson);
        joinData.add("sctpCapabilities", sctpCapabilitiesJson);
        joinData.addProperty("displayName", "gg");

        JsonObject response = protoo.requestSync("join", joinData);
        // TODO: 2025/3/16 handle response
    }

    public void enableCam(){
        localVideoSource = Dugon.createVideoSource();
    }

    public void previewCam(Player player){
        player.play(localVideoSource);
    }

    public void initView(Player player){
        Dugon.initView(player);
    }
}
