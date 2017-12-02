package edu.calbaptist.android.projectmeetings;

import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static edu.calbaptist.android.projectmeetings.MainActivity.REQUEST_AUTHORIZATION;

/**
 * Created by Austin on 10/31/2017.
 */

public class FolderViewActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener{

    private static final int CAMERA_REQUEST = 1888;
    private String mCurrentPhotoPath;
    private String mCurrentPhotoName;

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
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    java.io.File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(this,
                                "edu.calbaptist.android.projectmeetings.fileprovider",
                                photoFile);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }
                }
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

    private java.io.File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        java.io.File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        java.io.File image = java.io.File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        mCurrentPhotoName = image.getName();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            java.io.File image = new java.io.File(mCurrentPhotoPath);
            try {
                DriveFiles.getInstance().uploadFileToDrive(image, mCurrentPhotoName, "image/jpeg");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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
