package edu.calbaptist.android.projectmeetings;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.calbaptist.android.projectmeetings.exceptions.ChooseAccountException;
import edu.calbaptist.android.projectmeetings.exceptions.GooglePlayServicesAvailabilityException;
import edu.calbaptist.android.projectmeetings.exceptions.RequestPermissionException;
import edu.calbaptist.android.projectmeetings.utils.DriveFiles;
import pub.devrel.easypermissions.EasyPermissions;

import static edu.calbaptist.android.projectmeetings.SignInActivity.REQUEST_ACCOUNT_PICKER;
import static edu.calbaptist.android.projectmeetings.SignInActivity.REQUEST_GOOGLE_PLAY_SERVICES;
import static edu.calbaptist.android.projectmeetings.SignInActivity.REQUEST_PERMISSION_GET_ACCOUNTS;

/**
 *  Folder List Fragment
 *  Assists with letting the user select a Drive folder.
 *
 *  @author Austin Brinegar, Caleb Solorio
 *  @version 1.0.0 12/20/17
 */

public class FolderListFragment extends ListFragment
        implements GoogleApiClient.OnConnectionFailedListener, AdapterView.OnItemClickListener{
    private final String TAG = "FolderListFragment";

    GoogleAccountCredential mCredential;
    private static final String[] SCOPES = { DriveScopes.DRIVE_METADATA, DriveScopes.DRIVE_FILE };
    Map<String,String> folders = new LinkedHashMap<>();

    /**
     * Initializes FolderListFragment.
     * @param inflater The inflater in which to inflate the fragment.
     * @param container The container in which the fragment belongs
     * @param savedInstanceState Contains any data sent from the previous activity.
     * @return the fragment View.
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_folder_list, container, false);
    }

    /**
     * Executes after the Activity's creation.
     * @param savedInstanceState Contains any data sent from the previous activity.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String accountName = App.context.getSharedPreferences(
                App.context.getString(R.string.app_package), Context.MODE_PRIVATE)
                .getString("accountName", null);

        mCredential = GoogleAccountCredential.usingOAuth2(
                getActivity().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        mCredential.setSelectedAccountName(accountName);

        new FolderListFragment.MakeRequestTask().execute();

        getListView().setOnItemClickListener(this);
    }

    /**
     * Shows an error dialog in the case of Google Play Services' unavailability.
     * @param connectionStatusCode Specifies the type of error.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                getActivity(),
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * Executes if the connection fails.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    /**
     * Executes when an item is clicked.
     * @param parent The parent of the item.
     * @param view The view of the item.
     * @param position The position of the item.
     * @param id The id of the item.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String folderName =  ((String) new ArrayList(folders.keySet()).get(position));
        String folderId = folders.get(folderName);

        Intent returnIntent = new Intent();
        returnIntent.putExtra("folder_name",
                folderName.replace("\n", "").replace("\r", ""));
        returnIntent.putExtra("folder_id", folderId);

        getActivity().setResult(Activity.RESULT_OK, returnIntent);
        getActivity().finish();
    }

    /**
     * Initializes an asynchronous request to create a Drive folder.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, Map<String,String>> {
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
                        getActivity(),
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
        protected Map<String,String> doInBackground(Void... params) {
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
        private Map<String,String> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            Map<String, String> fileInfo = new LinkedHashMap<>();
            FileList result = mService.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("mimeType = \"application/vnd.google-apps.folder\"") //only display folders
                    .execute();
            List<File> files = result.getFiles();
            if (files != null) {
                for (File file : files) {
                    fileInfo.put(String.format("%s \n",
                            file.getName()),file.getId());
                }
            }
            return fileInfo;
        }

        @Override
        protected void onPostExecute(Map<String, String> output) {
            folders = output;
            ArrayList<String> folderArray = new ArrayList<>();
            folderArray.addAll(output.keySet());
            ArrayAdapter adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_1, folderArray.toArray());
            setListAdapter(adapter);
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            SignInActivity.REQUEST_AUTHORIZATION);
                } else {
                    Log.e(TAG, "onCancelled: ", mLastError);
                }
            } else {
                Log.d(TAG, "Request cancelled.");
            }
        }
    }
}
