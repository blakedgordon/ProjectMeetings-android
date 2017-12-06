package edu.calbaptist.android.projectmeetings;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static edu.calbaptist.android.projectmeetings.MainActivity.REQUEST_AUTHORIZATION;
import static edu.calbaptist.android.projectmeetings.MainActivity.prefs;

/**
 * Created by Austin on 10/31/2017.
 */

public class FolderViewActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "SignInActivity";


    private Button createFolder;
    public Drive driveService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folderview);

        // Create New Folder
        createFolder = findViewById(R.id.CreateFolder);
        createFolder.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new requestCreateFolder().execute();
            }
        });

        try {
            driveService = DriveFiles.getInstance().getDriveService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }

    private class requestCreateFolder extends  AsyncTask<Void, Void, Void> {

        requestCreateFolder(){

        }

        @Override
        protected Void doInBackground(Void... params) {
            File fileMetadata = new File();
            fileMetadata.setName("Meetings Folder");
            fileMetadata.setPermissionIds(Collections.singletonList(prefs.getString("email",null)));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            try {
                File file = driveService.files().create(fileMetadata)
                        .setFields("id")
                        .execute();
                System.out.println("Folder ID: " + file.getId());
                finish();
                startActivity(getIntent());
            } catch (UserRecoverableAuthIOException e){
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            } catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }
    }
}
