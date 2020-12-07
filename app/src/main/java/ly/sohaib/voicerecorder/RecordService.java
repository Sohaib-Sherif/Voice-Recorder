package ly.sohaib.voicerecorder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Calendar;

public class RecordService extends Service {

    private MediaRecorder recorder;
    private File outputFile;
    private long startingTime;

    public RecordService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1,createNotification());
        startRecording();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if(recorder != null){
            stopRecording();
        }
        super.onDestroy();
    }

    private void getRecorder() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioSamplingRate(44100);
        recorder.setAudioEncodingBitRate(192000);
        recorder.setAudioChannels(1);
    }


    public void startRecording(){
        getRecorder();
        outputFile = RecordUtils.generateNewRecordFile();
        recorder.setOutputFile(outputFile.getAbsolutePath());
        try{
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        startingTime = System.currentTimeMillis();

    }

    public void stopRecording(){
        recorder.stop();
        recorder.reset();
        recorder.release();
        recorder = null;

        long elapsedTime = (System.currentTimeMillis() - startingTime);

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, outputFile.getAbsolutePath());
        System.out.println(outputFile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, outputFile.getName());
        System.out.println(outputFile.getName());
        values.put(MediaStore.MediaColumns.SIZE, outputFile.length());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.MediaColumns.DATE_ADDED, Calendar.getInstance().getTimeInMillis()/1000);
        values.put(MediaStore.Audio.Media.ARTIST, getString(R.string.app_name));
        values.put(MediaStore.Audio.Media.DURATION, elapsedTime);
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(outputFile.getAbsolutePath());
        getContentResolver().insert(uri, values);

        stopForeground(true);
    }

    private Notification createNotification() {
        Builder mBuilder =
                new Builder(getApplicationContext(), NotificationChannel.DEFAULT_CHANNEL_ID)
                        .setSmallIcon(R.drawable.start_recording_24dp)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.toast_recording_start))
                        .setOngoing(true);
        Intent notifyIntent = new Intent(getApplicationContext(), MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), 0,
                new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT));

        return mBuilder.build();
    }
}
