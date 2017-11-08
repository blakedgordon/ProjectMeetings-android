package edu.calbaptist.android.projectmeetings;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.Permission;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import edu.calbaptist.android.projectmeetings.Exceptions.ChooseAccountException;
import edu.calbaptist.android.projectmeetings.Exceptions.GooglePlayServicesAvailabilityException;
import edu.calbaptist.android.projectmeetings.Exceptions.RequestPermissionException;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by blakegordon on 10/30/17.
 */

public class DriveFiles implements EasyPermissions.PermissionCallbacks {
    private static DriveFiles instance = null;
    private Drive driveService;
    private GoogleAccountCredential mCredential;

    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";

    private DriveFiles() throws
            GooglePlayServicesAvailabilityException,
            ChooseAccountException, RequestPermissionException {
        final String[] SCOPES = { DriveScopes.DRIVE_METADATA_READONLY };
        mCredential = GoogleAccountCredential.usingOAuth2(
                App.context, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        getResultsFromApi();
    }

    public static DriveFiles getInstance() throws
            GooglePlayServicesAvailabilityException,
            ChooseAccountException, RequestPermissionException {
        if (instance == null) {
            instance = new DriveFiles();
        }
        return instance;
    }

    public Drive getDriveService() {
        return driveService;
    }

    public void shareFolderWith(String fileId, String email) {
        JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
            @Override
            public void onFailure(GoogleJsonError e,
                                  HttpHeaders responseHeaders)
                    throws IOException {
                // Handle error
                System.err.println(e.getMessage());
            }

            @Override
            public void onSuccess(Permission permission,
                                  HttpHeaders responseHeaders)
                    throws IOException {
                System.out.println("Permission ID: " + permission.getId());
            }
        };

        BatchRequest batch = driveService.batch();
        Permission userPermission = new Permission()
                .setType("user")
                .setRole("writer")
                .setEmailAddress("user@example.com");
        try {
            driveService.permissions().create(fileId, userPermission)
                    .setFields("id")
                    .queue(batch, callback);

            batch.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
     * Callback for when a permission is granted using the EasyPermissions
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
     * Callback for when a permission is denied using the EasyPermissions
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
