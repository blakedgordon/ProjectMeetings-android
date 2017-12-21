package edu.calbaptist.android.projectmeetings.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.calbaptist.android.projectmeetings.App;
import edu.calbaptist.android.projectmeetings.R;
import edu.calbaptist.android.projectmeetings.exceptions.ChooseAccountException;
import edu.calbaptist.android.projectmeetings.exceptions.GooglePlayServicesAvailabilityException;
import edu.calbaptist.android.projectmeetings.exceptions.RequestPermissionException;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 *  Drive Files
 *  Assists with the app's Drive functionality.
 *
 *  @author Blake Gordon, Caleb Solorio
 *  @version 1.0.0 10/30/17
 */

public class DriveFiles implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "DriveFiles";

    private static DriveFiles instance = null;
    private Drive driveService;
    private GoogleAccountCredential mCredential;
    private final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";

    /**
     * Initiates a DriveFiles object.
     * @throws GooglePlayServicesAvailabilityException If Google Play is unavailable
     * @throws ChooseAccountException If a Google account is unable to be selected.
     * @throws RequestPermissionException If a request for necessary permissions fails.
     */
    private DriveFiles() throws
            GooglePlayServicesAvailabilityException,
            ChooseAccountException, RequestPermissionException {
        final String[] SCOPES = { DriveScopes.DRIVE_METADATA_READONLY };
        mCredential = GoogleAccountCredential.usingOAuth2(
                App.context, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        String accountName = App.context.getSharedPreferences(
                App.context.getString(R.string.app_package), Context.MODE_PRIVATE)
                .getString(PREF_ACCOUNT_NAME, null);
        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);
        }

        getResultsFromApi();
    }

    /**
     * Initiates/retrieves a DriveFiles object.
     * @return a DriveFiles object.
     * @throws GooglePlayServicesAvailabilityException If Google Play is unavailable
     * @throws ChooseAccountException If a Google account is unable to be selected.
     * @throws RequestPermissionException If a request for necessary permissions fails.
     */
    public static DriveFiles getInstance() throws
            GooglePlayServicesAvailabilityException,
            ChooseAccountException, RequestPermissionException {
        if (instance == null) {
            instance = new DriveFiles();
        }
        return instance;
    }

    /**
     * Gets the Drive service.
     * @return a Drive object.
     */
    public Drive getDriveService() {
        return driveService;
    }

    /**
     * Shares a Drive folder with the specified users.
     * @param driveFolderId The id of the folder to share.
     * @param invitationsToAdd A list containing the emails of the users to add.
     * @throws IOException If the batch request fails.
     */
    public void shareFolder(String driveFolderId, ArrayList<String> invitationsToAdd) throws IOException {
        JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
            @Override
            public void onFailure(GoogleJsonError e,
                                  HttpHeaders responseHeaders)
                    throws IOException {
                // Handle error
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onSuccess(Permission permission,
                                  HttpHeaders responseHeaders)
                    throws IOException {
                Log.d(TAG, "onSuccess: Permission ID: " + permission.getId());
            }
        };

        final BatchRequest batch = driveService.batch();

        for(String email : invitationsToAdd.toArray(new String[0])) {
            Log.d(TAG, "run email: " + email);

            Permission userPermission = new Permission()
                    .setType("user")
                    .setRole("writer")
                    .setId(email)
                    .setEmailAddress(email);

            driveService.permissions().create(driveFolderId, userPermission)
                    .setFields("id")
                    .queue(batch, callback);
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    batch.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Unshares a Drive folder with the specified users.
     * @param driveFolderId The id of the folder.
     * @param invitationsToRemove A list containing the emails of the users to remove.
     * @throws IOException If the batch request fails.
     */
    public void unshareFolder(String driveFolderId, ArrayList<String> invitationsToRemove) throws IOException {
        JsonBatchCallback<Void> callback = new JsonBatchCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, HttpHeaders responseHeaders) throws IOException {
                Log.d(TAG, "onSuccess: Permission ID");
            }

            @Override
            public void onFailure(GoogleJsonError e,
                                  HttpHeaders responseHeaders)
                    throws IOException {
                // Handle error
                Log.d(TAG, "onFailure: " + e.getMessage());
            }
        };

        final BatchRequest batch = driveService.batch();

        for(String email : invitationsToRemove.toArray(new String[0])) {
            Log.d(TAG, "run email: " + email);

            driveService.permissions().delete(driveFolderId, email)
                    .setFields("id")
                    .queue(batch, callback);
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    batch.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() throws
            GooglePlayServicesAvailabilityException,
            ChooseAccountException, RequestPermissionException {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            driveService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName("Project Meetings")
                    .build();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() throws
            ChooseAccountException, RequestPermissionException,
            GooglePlayServicesAvailabilityException {
        if (EasyPermissions.hasPermissions(
                App.context, android.Manifest.permission.GET_ACCOUNTS)) {
            String accountName = App.context.getSharedPreferences(
                    "edu.calbaptist.android.projectmeetings.Account_Name", Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Throw to start a dialog from which the user can choose an account
                throw new ChooseAccountException();
            }
        } else {
            // Throw to request the GET_ACCOUNTS permission via a user dialog
            throw new RequestPermissionException();
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, App.context);
    }

    /**
     * RestClientCallbackFailure for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * RestClientCallbackFailure for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(App.context);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() throws GooglePlayServicesAvailabilityException {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(App.context);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            throw new GooglePlayServicesAvailabilityException(connectionStatusCode);
        }
    }
}
