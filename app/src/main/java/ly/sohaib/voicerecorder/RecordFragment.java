package ly.sohaib.voicerecorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment {

    private FloatingActionButton recordButton;
    private Chronometer chronometer;
    private boolean isPlaying = false;
    private String chronoValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null){
            isPlaying = savedInstanceState.getBoolean("isPlaying");
            chronometer.setText(chronoValue);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isPlaying",isPlaying);
        chronoValue = chronometer.getText().toString();
        System.out.println(chronoValue);
        outState.putString("chronoValue",chronoValue);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        recordButton = view.findViewById(R.id.recordButton);
        chronometer = view.findViewById(R.id.chronometer);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recordButton.setOnClickListener(e -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkPermissions()) {
                    getActivity().requestPermissions(getPermissions(),
                            10);
                } else {
                    isPlaying = !isPlaying;
                    startOrStopRecording();
                }
            }
            else {
                isPlaying = !isPlaying;
                startOrStopRecording();
            }
        });
    }

    private void startOrStopRecording() {
        Intent intent = new Intent(getActivity(), RecordService.class);
        chronometer.setBase(SystemClock.elapsedRealtime());
        if (isPlaying) {
            recordButton.setImageResource(R.drawable.stop_recording_24dp);
            Toast.makeText(getActivity(), R.string.toast_recording_start, Toast.LENGTH_SHORT).show();
            chronometer.start();
            getActivity().startService(intent);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            recordButton.setImageResource(R.drawable.start_recording_24dp);
            chronometer.stop();
            getActivity().stopService(intent);
            Toast.makeText(getActivity(), R.string.toast_recording_stop, Toast.LENGTH_SHORT).show();
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean checkPermissions(){
        boolean permission_mic = getActivity().checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED;
        boolean permission_storage = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
        return permission_mic || permission_storage;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private String[] getPermissions(){
        List<String> permissions = new ArrayList<>();
        boolean permission_mic = getActivity().checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED;
        boolean permission_storage = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
        if(permission_mic)
            permissions.add(Manifest.permission.RECORD_AUDIO);
        if (permission_storage)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permissions.toArray(new String[0]);

    }
}
