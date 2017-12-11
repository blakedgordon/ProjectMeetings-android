package edu.calbaptist.android.projectmeetings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.Collections;

import static edu.calbaptist.android.projectmeetings.MainActivity.REQUEST_AUTHORIZATION;
import static edu.calbaptist.android.projectmeetings.MainActivity.prefs;

/**
 * Created by Austin on 10/31/2017.
 */

public class FolderViewActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener{

    SharedPreferences prefs = App.context.getSharedPreferences(
            "edu.calbaptist.android.projectmeetings.Account_Name",
            Context.MODE_PRIVATE);

    private static final String TAG = "SignInActivity";

    private EditText folderNameEditText;
    private Button createFolderButton;
    public Drive driveService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folderview);

        getSupportActionBar().setTitle("Drive Folder");

        // Create New Folder
        folderNameEditText = findViewById(R.id.edit_text_folder_name);
        createFolderButton = findViewById(R.id.button_create_folder);
        createFolderButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new requestCreateFolder(folderNameEditText.getText().toString()).execute();
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
        private String name;

        requestCreateFolder(String name){
            this.name = name.replace("\n", "").replace("\r", "");
        }

        @Override
        protected Void doInBackground(Void... params) {
            File fileMetadata = new File();
            fileMetadata.setName(name);
            fileMetadata.setPermissionIds(Collections.singletonList(prefs.getString("email",null)));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            try {
                File file = driveService.files().create(fileMetadata)
                        .setFields("id")
                        .execute();
                System.out.println("Folder ID: " + file.getId());

//                SharedPreferences.Editor editor = prefs.edit();
//                editor.putString("DefaultFolder", file.getId());
//                editor.apply();

                Intent returnIntent = new Intent();
                returnIntent.putExtra("folder_name", name);
                returnIntent.putExtra("folder_id", file.getId());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();

//                finish();
//                startActivity(getIntent());
            } catch (UserRecoverableAuthIOException e){
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                showToast("Unable to create folder :(");
            } catch (IOException e){
                e.printStackTrace();
                showToast("Unable to create folder :(");
            }
            return null;
        }
    }

    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            }
        });
    }
}
