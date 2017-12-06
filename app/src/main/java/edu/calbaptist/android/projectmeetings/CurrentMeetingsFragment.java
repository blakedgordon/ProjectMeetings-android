package edu.calbaptist.android.projectmeetings;

import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Austin on 12/1/2017.
 */

public class CurrentMeetingsFragment extends ListFragment {

    private static final String TAG = "CurrentMeetingsFragment";
    final static SharedPreferences prefs = App.context.getSharedPreferences(
            "edu.calbaptist.android.projectmeetings.Account_Name",
            Context.MODE_PRIVATE);
    Map<String, Meeting> meetingHashMap = new LinkedHashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_meetings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        buildMeetings();
    }

    private void buildMeetings(){
        String uId = prefs.getString("uID",null);
        final ArrayList<Meeting> meetings = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("meetings").orderByChild("u_id").equalTo(uId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
                                .setInvites(invites)
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
    }

    private String unixToDateTime(Long unix){
        Date date = new java.util.Date(unix);
        return date.toString();
    }

}
