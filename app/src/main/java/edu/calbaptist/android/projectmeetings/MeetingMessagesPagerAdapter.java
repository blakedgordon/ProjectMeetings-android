package edu.calbaptist.android.projectmeetings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by csolo on 11/30/2017.
 */

public class MeetingMessagesPagerAdapter extends FragmentStatePagerAdapter {
    private int currentPos;
    ArrayList<MeetingMessage> messages;

    public MeetingMessagesPagerAdapter(FragmentManager fm) {
        super(fm);

        this.messages = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        currentPos = position;

        Fragment fragment = new MeetingMessageObjectFragment();
        Bundle args = new Bundle();
        // Our object is just an integer :-P
        args.putInt(MeetingMessageObjectFragment.ARG_OBJECT, position + 1);
        args.putSerializable(MeetingMessageObjectFragment.MEETING_OBJECT, messages.get(position));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return messages.size();
    }
    
    public int getCurrentPos() {
        return currentPos;
    }

    public void addMessage(MeetingMessage message) {
        messages.add(message);
        notifyDataSetChanged();
    }
}
