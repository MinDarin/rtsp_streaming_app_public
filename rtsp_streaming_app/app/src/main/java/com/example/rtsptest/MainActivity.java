package com.example.rtsptest;

import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.projection.MediaProjection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.PixelCopy;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.yuneec.videostreaming.RTSPPlayer;
import com.yuneec.videostreaming.VideoPlayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {
    RTSPPlayer rtspPlayer;
    String url = "";
    String baseurl = "";
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    EditText editText;
    ImageButton capture;
    HandlerThread captureHandlerThread;
//    private static final String videoFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/MediaProjection.mp4";

//    File recordOutput;
    File captureOutput;
    static Surface surface;
    boolean first = true;
    LinearLayout layout;
    private String[] DroneList;
    private String[] DroneName;

    String before_url ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DroneList = getIntent().getExtras().getStringArray("DroneList"); //Dynamo db에서 가져온 영상 주소
        DroneName = getIntent().getExtras().getStringArray("DroneName"); //어플에 노출될 이름

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        PermissionHelper ph = new PermissionHelper();
        String base_url = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov"; //실제 주소는 영업 비밀이기에 rtps 예제 주소로 변경하였습니다.
        final String[] rtspexample = new String[DroneList.length];

        for(int i = 0 ; i < DroneList.length; i++)
            rtspexample[i] = base_url + DroneList[i];  //실제 영상 소스 주소
        url = rtspexample[0];
        before_url = rtspexample[0];
       if(! ph.hasPermissions(this))
       {
            ph.requestPermissions(this);
       }
        for(int i = 0 ; i <DroneList.length; i++)
            System.out.println("receive list is"+DroneList[i]);

        layout = (LinearLayout)findViewById(R.id.ListSpace);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, DroneName);
        Spinner sp = findViewById(R.id.sp);
        sp.setAdapter(adapter);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {
                Log.d("url_is", rtspexample[i]);
                url = rtspexample[i];
                if(!url.equals(before_url))         //주소가 바뀌었을 때만 rtsp Player 교체
                {
                    try {
                        if (rtspPlayer != null && rtspPlayer.isPlaying()) {
                            rtspPlayer.stop();
                            rtspPlayer.releasePlayer();
                            rtspPlayer = null;
                        }
                        rtspPlayer = (RTSPPlayer) VideoPlayer.getPlayer(VideoPlayer.PlayerType.LIVE_STREAM);
                        rtspPlayer.initializePlayer();
                        rtspPlayer.setSurface(surface);
                        rtspPlayer.setDataSource(url);
                        rtspPlayer.start();
                        before_url = url;
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

//        checkSelfPermission();

        if(!first)
        {
            Intent intent = getIntent();
            this.url = intent.getExtras().getString("url");
        }


        surfaceView = findViewById(R.id.videoView);
        editText = new EditText(MainActivity.this);


        //캡처 버튼
        capture = findViewById(R.id.btn_capture);
        capture.setOnClickListener(listener_capture);
        //녹화 버튼

       //yuneec SDK에 RTSP 설정하기
/*
        rtspPlayer = (RTSPPlayer) VideoPlayer.getPlayer(VideoPlayer.PlayerType.LIVE_STREAM);
        rtspPlayer.initializePlayer();
*/

/*        try {
            rtspPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (VideoPlayerException e) {
            e.printStackTrace();
        }*/

        first = false;

        //surfaceview 콜백함수 설정
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(callback);
    }

    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        //스트리밍 할 화면 생성하기f
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Log.d("surface is created", "eeeee");
            surface = surfaceHolder.getSurface();
            Log.d("created_surface_at_callback " , surface.toString());
            try {
                    rtspPlayer = (RTSPPlayer) VideoPlayer.getPlayer(VideoPlayer.PlayerType.LIVE_STREAM);
                    rtspPlayer.initializePlayer();

                    rtspPlayer.setSurface(surface);
                    rtspPlayer.setDataSource(url);
                    rtspPlayer.start();
                    before_url = url;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        }

        @Override
        //스트리밍이 끝난후 surface 풀기
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Log.d("Surface is Destroyed!!", "eeeee");
            try {
                if(rtspPlayer != null && rtspPlayer.isPlaying()) {
                    rtspPlayer.stop();
                    rtspPlayer.releasePlayer();
                    rtspPlayer = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    /*녹화 및 캡처한 파일을 저장할 수 있는
    경로 및 해당 경로에 폴더를 생성해주는 함수*/
    private File createOutputFIle(String ext) {
        File tempFile;
        String filename;

        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+ "");
        Log.d("ss dir",dir.toString());
        if (!dir.exists()) {
            Log.d("File", "create");
            dir.mkdir();
        }

        Calendar c = Calendar.getInstance();
        filename = "RTSP_" +
                c.get(Calendar.YEAR) + "_" +
                (c.get(Calendar.MONTH) + 1) + "_" +
                c.get(Calendar.DAY_OF_MONTH)
                + "_" +
                c.get(Calendar.HOUR_OF_DAY) +
                c.get(Calendar.MINUTE) +
                c.get(Calendar.SECOND);

        tempFile = new File(dir.getAbsolutePath() + "/" + filename + ext);
        return tempFile;
    }

    Button.OnClickListener listener_capture = new Button.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onClick(View view) {
            Toast.makeText(getApplicationContext(), "Capture", Toast.LENGTH_SHORT).show();
            takeScreenshot();
        }
    };

    /* 실행 중인 RTSP 영상을 캡처하고
        캡처한 이미지를 내부 저장소 및 갤러리에 저장하는 함수*/
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void takeScreenshot() {
        captureOutput = createOutputFIle(".jpg");
        final Bitmap captureBitmap = Bitmap.createBitmap(surfaceView.getWidth(),
                surfaceView.getHeight(),
                Bitmap.Config.ARGB_8888);
        captureHandlerThread = new HandlerThread("PixelCopier");

        captureHandlerThread.start();
        PixelCopy.request(surfaceView, captureBitmap, new PixelCopy.OnPixelCopyFinishedListener() {
            @Override
            public void onPixelCopyFinished(int i) {
                if (i == PixelCopy.SUCCESS && captureBitmap != null) {
                    try {


                            FileOutputStream outputStream
                                    = new FileOutputStream(captureOutput);
                            ByteArrayOutputStream outputData
                                    = new ByteArrayOutputStream();
                            captureBitmap.compress(Bitmap.CompressFormat.JPEG,
                                    90, outputData);
                            outputData.writeTo(outputStream);
                            outputStream.flush();
                            outputStream.close();

                            //갤러리에 보이도록 intent로 넘기기
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                    Uri.fromFile(captureOutput)));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                if (captureBitmap == null)
                    Log.d("bitmap", "null");
                captureHandlerThread.quitSafely();
            }
        }, new Handler(captureHandlerThread.getLooper()));
    }


}