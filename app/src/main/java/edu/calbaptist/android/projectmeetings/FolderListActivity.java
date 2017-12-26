package edu.calbaptist.android.projectmeetings;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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

import edu.calbaptist.android.projectmeetings.utils.DriveFiles;

import static edu.calbaptist.android.projectmeetings.SignInActivity.REQUEST_AUTHORIZATION;

/**
 *  Folder List Activity
 *  Shows the folders belonging to the user, allowing the user to select one.
 *
 *  @author Austin Brinegar, Caleb Solorio
 *  @version 1.0.0 12/20/17
 */
public class FolderListActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{
    public static final String TAG = "SignInActivity";

    private EditText mFolderNameEditText;
    private Button mCreateFolderButton;
    public Drive mDriveService;

    /**
     * Initializes FolderListActivity
     * @param savedInstanceState Contains any data sent from the previous activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_list);

        getSupportActionBar().setTitle("Drive Folder");

        // Create New Folder
        mFolderNameEditText = findViewById(R.id.edit_text_folder_name);
        mCreateFolderButton = findViewById(R.id.button_create_folder);
        mCreateFolderButton.setOnClickListener(this);

        try {
            mDriveService = DriveFiles.getInstance().getDriveService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles OnClick events.
     * @param view the view in which the event occurred.
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_create_folder:
                new requestCreateFolder(mFolderNameEditText.getText().toString()).execute();
                break;
        }
    }

    /**
     * Handles a connection failure.
     * @param connectionResult Specifies the result.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showToast("Unable to connect to Google API Services.");
    }

    /**
     * Shows the given string in a short Toast.
     */
    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            }
        });
    }

    /**
     * Handles the creation of a folder asynchronously.
     */
    private class requestCreateFolder extends  AsyncTask<Void, Void, Void> {
        private String name;

        requestCreateFolder(String name){
            this.name = name.replace("\n", "")
                    .replace("\r", "");
        }

        @Override
        protected Void doInBackground(Void... params) {
            File fileMetadata = new File();
            fileMetadata.setName(name);
            fileMetadata.setPermissionIds(Collections
                    .singletonList(App.PREFERENCES.getString("email",null)));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            try {
                File file = mDriveService.files().create(fileMetadata)
                        .setFields("id")
                        .execute();

                Intent returnIntent = new Intent();
                returnIntent.putExtra("folder_name", name);
                returnIntent.putExtra("folder_id", file.getId());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
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
}
