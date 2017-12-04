package edu.calbaptist.android.projectmeetings;

import android.app.ListFragment;
import android.content.Context;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.calbaptist.android.projectmeetings.Exceptions.RestClientException;

/**
 * Created by Austin on 12/1/2017.
 */

public class CurrentMeetingsFragment extends ListFragment
        implements AdapterView.OnItemClickListener{

    private static final String TAG = "CurrentMeetingsFragment";
    final static SharedPreferences prefs = App.context.getSharedPreferences(
            "edu.calbaptist.android.projectmeetings.Account_Name",
            Context.MODE_PRIVATE);
    static ArrayList<String> meetingIds;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_meetings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        meetingIds = new ArrayList<>();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String uID = prefs.getString("uID",null);
                String firebaseToken = prefs.getString("FirebaseToken",null);
                RestClient.getUserByUid(uID, firebaseToken, new Callback.RestClientUser() {
                    @Override
                    void onTaskExecuted(User user) {
                        Log.d(TAG, "onTaskExecuted: " + user.getDisplayName());
                        meetingIds.addAll(user.getMeetings());
                        ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, meetingIds.toArray());
                        setListAdapter(adapter);
                    }

                    @Override
                    void onTaskFailed(RestClientException e) {
                        Log.d(TAG, "onTaskFailed with " + e.getResponseCode()
                                + ": " + e.getJson().toString());
                    }

                    @Override
                    void onExceptionRaised(Exception e) {
                        Log.d(TAG, "onExceptionRaised: " + e.getMessage());
                    }
                });
            }
        });




        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }


}
