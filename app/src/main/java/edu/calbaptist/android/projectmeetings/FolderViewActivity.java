package edu.calbaptist.android.projectmeetings;

import android.accounts.Account;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.calbaptist.android.projectmeetings.Exceptions.ChooseAccountException;
import edu.calbaptist.android.projectmeetings.Exceptions.GooglePlayServicesAvailabilityException;
import edu.calbaptist.android.projectmeetings.Exceptions.RequestPermissionException;
import pub.devrel.easypermissions.EasyPermissions;

import static edu.calbaptist.android.projectmeetings.MainActivity.REQUEST_ACCOUNT_PICKER;
import static edu.calbaptist.android.projectmeetings.MainActivity.REQUEST_AUTHORIZATION;
import static edu.calbaptist.android.projectmeetings.MainActivity.REQUEST_GOOGLE_PLAY_SERVICES;
import static edu.calbaptist.android.projectmeetings.MainActivity.REQUEST_PERMISSION_GET_ACCOUNTS;

/**
 * Created by Austin on 10/31/2017.
 */

public class FolderViewActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener{

    private static final int CAMERA_REQUEST = 1888;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_media_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_photo:
                // Navigate to the new photo activity
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                return true;
            case R.id.add_recording:
                // Navigate to the new recording activity
                Intent intent = NewRecordingActivity.newIntent(this);
                startActivity(intent);
                return true;
            case R.id.add_file:
                //navigate to the new file activity
                Intent transfer = new Intent(this, NewFileActivity.class);
                startActivity(transfer);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private TextView mFolderText;
    private static final String TAG = "SignInActivity";
    GoogleAccountCredential mCredential;
    private static final String[] SCOPES = { DriveScopes.DRIVE_METADATA, DriveScopes.DRIVE_FILE };
    private Button createFolder;
    public Drive driveService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String accountName = App.context.getSharedPreferences(
                "edu.calbaptist.android.projectmeetings.Account_Name", Context.MODE_PRIVATE)
                .getString("accountName", null);

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        mCredential.setSelectedAccountName(accountName);

        setContentView(R.layout.activity_folderview);

        // Create New Folder
        createFolder = findViewById(R.id.CreateFolder);
        createFolder.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new requestCreateFolder().execute();
            }
        });
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
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            driveService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName("Project Meetings")
                    .build();
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
