package edu.calbaptist.android.projectmeetings;

import android.app.Dialog;
import android.app.ProgressDialog;
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
import static edu.calbaptist.android.projectmeetings.MainActivity.REQUEST_GOOGLE_PLAY_SERVICES;
import static edu.calbaptist.android.projectmeetings.MainActivity.REQUEST_PERMISSION_GET_ACCOUNTS;

/**
 * Created by Austin on 10/31/2017.
 */

public class FolderViewActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{

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
                Toast.makeText(this, R.string.add_photo, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.add_recording:
                // Navigate to the new recording activity
                Toast.makeText(this, R.string.add_recording, Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private TextView mFolderText;
    ProgressDialog mProgress;
    private static final String TAG = "SignInActivity";
    GoogleAccountCredential mCredential;
    private static final String[] SCOPES = { DriveScopes.DRIVE_METADATA_READONLY };
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private Button createFolder;
    public Drive driveService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_folderview);

        createFolder = findViewById(R.id.CreateFolder);
        createFolder.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
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
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();
        new FolderViewActivity.MakeRequestTask().execute();

        mFolderText = (TextView) findViewById(R.id.display_text);
        mFolderText.setVerticalScrollBarEnabled(true);
        mFolderText.setMovementMethod(new ScrollingMovementMethod());
        mFolderText.setText("");

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Drive API ...");

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                FolderViewActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;

        MakeRequestTask() {
            try {
                mService = DriveFiles.getInstance().getDriveService();
            } catch (GooglePlayServicesAvailabilityException e) {
                showGooglePlayServicesAvailabilityErrorDialog(e.connectionStatusCode);
            } catch (ChooseAccountException e) {
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            } catch (RequestPermissionException e) {
                EasyPermissions.requestPermissions(
                        FolderViewActivity.this,
                        "This app needs to access your Google account (via Contacts).",
                        REQUEST_PERMISSION_GET_ACCOUNTS,
                        android.Manifest.permission.GET_ACCOUNTS);
            }
        }

        /**
         * Background task to call Drive API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                e.printStackTrace();
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            List<String> fileInfo = new ArrayList<String>();
            FileList result = mService.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("mimeType = \"application/vnd.google-apps.folder\"") //only display folders
                    .execute();
            List<File> files = result.getFiles();
            if (files != null) {
                for (File file : files) {
                    fileInfo.add(String.format("%s \n",
                            file.getName()));
                }
            }
            return fileInfo;
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                mFolderText.setText("No results returned.");
            } else {
                mFolderText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    mFolderText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mFolderText.setText("Request cancelled.");
            }
        }
    }

}
