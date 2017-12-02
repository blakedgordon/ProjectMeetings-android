package edu.calbaptist.android.projectmeetings;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import edu.calbaptist.android.projectmeetings.Exceptions.ChooseAccountException;
import edu.calbaptist.android.projectmeetings.Exceptions.GooglePlayServicesAvailabilityException;
import edu.calbaptist.android.projectmeetings.Exceptions.RequestPermissionException;

import static edu.calbaptist.android.projectmeetings.MainActivity.REQUEST_AUTHORIZATION;

public class NewRecordingActivity extends AppCompatActivity {

    private Button mRecordButton;

    private MediaRecorder mMediaRecorder;

    private boolean mStartRecording = false;

    private static String[] recordedFiles;

    private static String mFilePath;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {android.Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final String LOG_TAG = "AudioRecord";

    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, NewRecordingActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_recording);

        recordedFiles = getDir("recordings", MODE_PRIVATE).list();

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        mRecordButton = findViewById(R.id.recording_button);

        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordPressed(mStartRecording);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ListFragment listFragment = (ListFragment)
                getFragmentManager().findFragmentById(R.id.recording_list_fragment);
        ArrayAdapter adapter = (ArrayAdapter) listFragment.getListAdapter();
        adapter.clear();
        adapter.addAll(recordedFiles);
        adapter.notifyDataSetChanged();
    }

    private void recordPressed(boolean recording) {
        if (!recording) {
            mRecordButton.setText(R.string.stop_recording);

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(c.getTime());
            mFilePath = getDir("recordings", MODE_PRIVATE).getAbsolutePath();
            mFilePath += "/" + formattedDate + ".aac";
            
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            mMediaRecorder.setOutputFile(mFilePath);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            try {
                mMediaRecorder.prepare();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }

            mMediaRecorder.start();

            mStartRecording = true;
        } else {
            mRecordButton.setText(R.string.start_recording);
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;

            mStartRecording = false;

            // Update the list from the new recording
            ListFragment listFragment = (ListFragment)
                    getFragmentManager().findFragmentById(R.id.recording_list_fragment);
            ArrayAdapter adapter = (ArrayAdapter) listFragment.getListAdapter();
            String[] directories = mFilePath.split("/");
            String fileName = directories[directories.length - 1];
            adapter.add(fileName);
            adapter.notifyDataSetChanged();

            // Add the file name to the recordedFiles array
            final int N = recordedFiles.length;
            recordedFiles = Arrays.copyOf(recordedFiles, N + 1);
            recordedFiles[N] = fileName;

            // Upload the file to Drive
            File uploadFile = new File(mFilePath);
            try {
                DriveFiles.getInstance().uploadFileToDrive(uploadFile, fileName, "audio/x-aac");
            } catch (Exception e) {
                e.printStackTrace();
            }

            mFilePath = null;
        }
    }

    public static String[] getRecordedFiles() {
        return recordedFiles;
    }
}
