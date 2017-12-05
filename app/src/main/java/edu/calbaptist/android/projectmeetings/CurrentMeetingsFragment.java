package edu.calbaptist.android.projectmeetings;

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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        List<Meeting> MeetingList = new ArrayList(meetingHashMap.values()); // convert hashmap to array
        Meeting m = MeetingList.get(position);
        System.out.println(m.getName() + "--------------------------------------------------------------");
        System.out.println(m.getDriveFolderId() + " drive folder id");
        System.out.println(m.getTime() + " time");
        System.out.println(m.getMid() + "mId");
        Intent intent = new Intent(getActivity(), MeetingActivity.class);
        intent.putExtra("meeting", m);
        User user = new User.UserBuilder()
                .setUid(prefs.getString("uID",null))
                .setDisplayName(prefs.getString("DisplayName",null))
                .setFirebaseToken(prefs.getString("FirebaseToken",null))
                .build();
        System.out.println(user.getDisplayName() + " user display name");
        System.out.println(user.getUid() + " uid");
        System.out.println("firebase token " + user.getFirebaseToken());
        intent.putExtra("user",user);
        startActivity(intent);
    }

    private ArrayList<Meeting> buildMeetings(){
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
                                .setInvites(invites)
                                .build();
                        meetings.add(m);
                    }
                }

                for(Meeting m : meetings){
                    String name = m.getName() + "\n" + m.getObjective() +  "\n" +
                            unixToDateTime(m.getTime());
                    meetingHashMap.put(name, m);
                }
                ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, meetingHashMap.keySet().toArray());
                setListAdapter(adapter);
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