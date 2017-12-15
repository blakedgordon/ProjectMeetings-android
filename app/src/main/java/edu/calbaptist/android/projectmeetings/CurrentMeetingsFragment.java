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
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.calbaptist.android.projectmeetings.exceptions.RestClientException;

/**
 * Created by Austin on 12/1/2017.
 */

public class CurrentMeetingsFragment extends ListFragment {
    ArrayList<Meeting> meetings;

    private static final String TAG = "CurrentMeetingsFragment";
    final static SharedPreferences prefs = App.context.getSharedPreferences(
            "edu.calbaptist.android.projectmeetings.Account_Name",
            Context.MODE_PRIVATE);
    Map<String, Meeting> meetingHashMap = new LinkedHashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        meetings = new ArrayList<>();
        return inflater.inflate(R.layout.fragment_current_meetings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateUser();
    }

    private void updateUser(){
        final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull final Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
//                            String idToken = task.getResult().getToken();
//                            SharedPreferences settings = App.context.getSharedPreferences(
//                                    "edu.calbaptist.android.projectmeetings.Account_Name",
//                                    Context.MODE_PRIVATE);
//                            SharedPreferences.Editor editor = settings.edit();
//                            editor.putString("FirebaseToken", idToken);
//                            editor.apply();

                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    String uID = prefs.getString("u_id",null);
                                    Log.d(TAG, "run: " + uID);
                                    final String firebaseToken = prefs.getString("firebase_token",null);
                                    RestClient.getUserByUid(uID, firebaseToken, new Callback.RestClientUser() {
                                        @Override
                                        void onTaskExecuted(final User user) {
                                            Log.d(TAG, "onTaskExecuted: " + user.getFirebaseToken());

                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString("u_id",user.getUid());
                                            editor.putString("display_name",user.getDisplayName());
                                            editor.putString("email",user.getEmail());
                                            editor.putString("firebase_token", task.getResult().getToken());
                                            editor.putString("google_token", user.getGoogleToken());
                                            editor.putString("instance_id", user.getInstanceId());
                                            editor.apply();

                                            ArrayList<String> mids = new ArrayList<>();

                                            if(user.getMeetings() != null && user.getMeetings().size() > 0) {
                                                mids.addAll(user.getMeetings());

                                            }

                                            if(user.getInvites() != null && user.getInvites().size() > 0) {
                                                mids.addAll(user.getInvites());

                                            }

                                            final ProgressBar spinner = (ProgressBar) getActivity().findViewById(R.id.loadingMeetingsSpinner);

                                            if(mids.size() > 0) {
                                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                                                Iterator iterator = mids.iterator();

                                                while (iterator.hasNext()) {
                                                    String mid = (String) iterator.next();

                                                    Query query = reference.child("meetings/" + mid);
                                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Log.d(TAG, "onDataChange: " + dataSnapshot.toString());
                                                            if (dataSnapshot.exists()) {
                                                                Log.d(TAG, "onDataChange: ISSUE " + dataSnapshot.child("name").getValue().toString());

                                                                Meeting m = new Meeting.MeetingBuilder()
                                                                        .setName(dataSnapshot.child("name").getValue().toString())
                                                                        .setObjective(dataSnapshot.child("objective").getValue().toString())
                                                                        .setTime(Long.parseLong(dataSnapshot.child("time").getValue().toString()))
                                                                        .setTimeLimit(Long.parseLong((dataSnapshot.child("time_limit").getValue().toString())))
                                                                        .setDriveFolderId(dataSnapshot.child("drive_folder_id").getValue().toString())
                                                                        .setMid(dataSnapshot.getKey())
                                                                        .setUid(dataSnapshot.child("u_id").getValue().toString())
                                                                        .build();
                                                                Log.d(TAG, "onDataChange: NULL " + m.getUid());

                                                                meetings.add(m);
                                                            }

                                                            spinner.setVisibility(View.GONE);

                                                            ArrayAdapter adapter = new MeetingListAdapter(getActivity(), meetings);
                                                            setListAdapter(adapter);
                                                        }


                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                }
                                            } else {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        RelativeLayout rel = getActivity().findViewById(R.id.no_meetings);
                                                        rel.setVisibility(View.VISIBLE);
                                                        spinner.setVisibility(View.GONE);
                                                    }
                                                });
                                            }
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
                        }
                    }
                });

    }

    private ArrayList<Meeting> buildMeetings(){


        String uId = prefs.getString("u_id",null);
        final ArrayList<Meeting> meetings = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("meetings");//.orderByChild("u_id").equalTo(uId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());
                ArrayList<String> invites = new ArrayList<String>(Arrays.asList(new String[]{"austin@gmail.com", "blake@gmail.com"}));

                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        Meeting m = new Meeting.MeetingBuilder()
                                .setName(issue.child("name").getValue().toString())
                                .setObjective(issue.child("objective").getValue().toString())
                                .setTime(Long.parseLong(issue.child("time").getValue().toString()))
                                .setTimeLimit(Long.parseLong((issue.child("time_limit").getValue().toString())))
                                .setDriveFolderId(issue.child("drive_folder_id").getValue().toString())
                                .setMid(issue.getKey())
                                .setUid(issue.child("u_id").getValue().toString())
                                .build();
                        meetings.add(m);
                    }
                }

                ProgressBar spinner = (ProgressBar) getActivity().findViewById(R.id.loadingMeetingsSpinner);
                spinner.setVisibility(View.GONE);

                if(meetings.size() > 0) {
                    for(Meeting m : meetings){
                        String name = m.getName() + "\n" + m.getObjective() +  "\n" +
                                unixToDateTime(m.getTime());
                        meetingHashMap.put(name, m);
                    }
                    ArrayAdapter adapter = new MeetingListAdapter(getActivity(), meetings);
                    setListAdapter(adapter);
                } else {
                    RelativeLayout rel = getActivity().findViewById(R.id.no_meetings);
                    rel.setVisibility(View.VISIBLE);
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return meetings;
    }

    private String unixToDateTime(Long unix){
        Date date = new java.util.Date(unix);
        return date.toString();
    }

}
