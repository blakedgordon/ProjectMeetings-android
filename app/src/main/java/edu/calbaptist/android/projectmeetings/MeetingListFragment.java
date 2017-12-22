package edu.calbaptist.android.projectmeetings;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Iterator;

import edu.calbaptist.android.projectmeetings.models.Meeting;
import edu.calbaptist.android.projectmeetings.models.User;

/**
 *  Meeting List Fragment
 *  Shows a list of the meetings the the user is a part of.
 *
 *  @author Austin Brinegar, Caleb Solorio
 *  @version 1.0.0 12/20/17
 */

public class MeetingListFragment extends ListFragment implements ValueEventListener {
    private static final String TAG = "MeetingListFragment";

    private ArrayList<Meeting> meetings;
    private ProgressBar spinner;

    /**
     * Initializes MeetingListFragment.
     * @param inflater The inflater in which to inflate the fragment.
     * @param container The container in which the fragment belongs
     * @param savedInstanceState Contains any data sent from the previous activity.
     * @return the fragment View.
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        meetings = new ArrayList<>();
        return inflater.inflate(R.layout.fragment_meeting_list, container, false);
    }

    /**
     * Executes after the Activity's creation.
     * @param savedInstanceState Contains any data sent from the previous activity.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Updates the values in the list.
     * @param user The user to get the meeting data from.
     */
    public void updateList(User user){
        ArrayList<String> mIds = new ArrayList<>();

        if(user.getMeetings() != null && user.getMeetings().size() > 0) {
            mIds.addAll(user.getMeetings());

        }

        if(user.getInvites() != null && user.getInvites().size() > 0) {
            mIds.addAll(user.getInvites());

        }

        spinner = (ProgressBar) getActivity().findViewById(R.id.progress_bar_meeting_list);

        if(mIds.size() > 0) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            Iterator iterator = mIds.iterator();
            while (iterator.hasNext()) {
                String mid = (String) iterator.next();

                Query query = reference.child("meetings/" + mid);
                query.addListenerForSingleValueEvent(this);
            }
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RelativeLayout rel = getActivity().findViewById(R.id.layout_no_meetings);
                    rel.setVisibility(View.VISIBLE);
                    spinner.setVisibility(View.GONE);
                }
            });
        }
    }

    /**
     * Executes when the Firebase data changes (or is retrieved for the first time).
     * @param dataSnapshot The data returned from Firebase.
     */
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            Meeting m = new Meeting.MeetingBuilder()
                    .setName(dataSnapshot.child("name").getValue().toString())
                    .setObjective(dataSnapshot.child("objective").getValue().toString())
                    .setTime(Long.parseLong(dataSnapshot.child("time").getValue().toString()))
                    .setTimeLimit(Long.parseLong((dataSnapshot.child("time_limit").getValue().toString())))
                    .setDriveFolderId(dataSnapshot.child("drive_folder_id").getValue().toString())
                    .setMid(dataSnapshot.getKey())
                    .setUid(dataSnapshot.child("u_id").getValue().toString())
                    .build();

            meetings.add(m);
        }

        spinner.setVisibility(View.GONE);

        if(getActivity() != null) {
            ArrayAdapter adapter = new MeetingListAdapter(getActivity(), meetings);
            setListAdapter(adapter);
        }
    }

    /**
     * Executes on failure to retrieve data.
     * @param databaseError Contains data on the reason for the error.
     */
    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.e(TAG, "onCancelled: ", databaseError.toException());
    }
}
