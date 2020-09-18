package com.example.rtsptest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class RecordService extends Service
{
    Surface surface;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(){
        super.onCreate();
        String CHANNEL_ID = "channel 1";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"recording", NotificationManager.IMPORTANCE_LOW);

        ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID).setContentTitle("").setContentText("").build();
        startForeground(2,notification);
        // 서비스는 한번 실행되면 계속 실행된 상태로 있는다.
        // 따라서 서비스 특성상 intent를 받아서 처리하기에 적합하지않다.
        // intent에 대한 처리는 onStartCommand()에서 처리해준다.
    }

    /** 요놈이 중요
     * @return**/
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        if (intent == null) {
//            return Service.START_STICKY; //서비스가 종료되어도 자동으로 다시 실행시켜줘!
        } else {


            // intent가 null이 아니다.
            // 액티비티에서 intent를 통해 전달한 내용을 뽑아낸다.(if exists)
            int command = intent.getIntExtra("command",-1);
            // etc..
            surface =  new MainActivity().surface;
            MediaRecorder mediaRecorder = new MediaRecorder();

            if(command == 1)   //녹화시작
            {
                File dir = new File(Environment.getExternalStorageDirectory() +
                        "/Pictures/Rtsp_Record");
                if (!dir.exists()) {
                    Log.d("File", "create");
                    dir.mkdir();
                }

                Calendar c = Calendar.getInstance();
                String filename = "RTSP_" +
                        c.get(Calendar.YEAR) + "_" +
                        (c.get(Calendar.MONTH) + 1) + "_" +
                        c.get(Calendar.DAY_OF_MONTH)
                        + "_" +
                        c.get(Calendar.HOUR_OF_DAY) +
                        c.get(Calendar.MINUTE) +
                        c.get(Calendar.SECOND);

                File tempFile = new File(dir.getAbsolutePath() + "/" + filename + ".mp4");
                mediaRecorder.setOutputFile(tempFile);
                mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mediaRecorder.setAudioSamplingRate(44100);
                mediaRecorder.setAudioEncodingBitRate(96000);
                mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
                mediaRecorder.setVideoEncodingBitRate(12000000);
                mediaRecorder.setVideoFrameRate(30);
                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e) {
                    System.out.println("error occurs in start");
                    System.out.println("error occurs in start");
                    System.out.println("error occurs in start");
                    System.out.println("error occurs in start");
                    System.out.println("error occurs in start");
                    System.out.println("error occurs in start");
                    System.out.println("error occurs in start");
                    System.out.println("error occurs in start");
                    System.out.println("error occurs in start");
                    System.out.println("error occurs in start");
                    System.out.println("error occurs in start");
                    System.out.println("error occurs in start");
                    System.out.println("error occurs in start");
                    System.out.println("error occurs in start");

                    e.printStackTrace();
                }
            }
            else   //녹화 끝
            {
                Toast.makeText(getApplicationContext(),"record-stop",Toast.LENGTH_LONG);
                mediaRecorder.stop();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}