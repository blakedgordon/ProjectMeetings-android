package edu.calbaptist.android.projectmeetings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 *  Meeting Messages Pager Adapter
 *  Functions as a standard PagerAdapter for MeetingMessageFragment.
 *
 *  @author Caleb Solorio
 *  @version 0.7.0 12/20/17
 */
public class MeetingMessagePagerAdapter extends FragmentStatePagerAdapter {
    private int currentPos;
    ArrayList<String> messages;

    /**
     * Initializes the MeetingMessagePagerAdapter.
     * @param fm The fragment manager to attach the adapter to.
     */
    public MeetingMessagePagerAdapter(FragmentManager fm) {
        super(fm);
        this.messages = new ArrayList<>();
    }

    /**
     * Gets a specific view associated with the adapter.
     * @param position The position of the message to retrieve a view of.
     * @return The MeetingMessageFragment associated with the message.
     */
    @Override
    public Fragment getItem(int position) {
        currentPos = position;

        Fragment fragment = new MeetingMessageFragment();
        Bundle args = new Bundle();
        args.putInt(MeetingMessageFragment.ARG_OBJECT, position + 1);
        args.putString(MeetingMessageFragment.MESSAGE, messages.get(position));
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Gets the number of messages in the adapter.
     * @return an integer specifying the number of messages.
     */
    @Override
    public int getCount() {
        return messages.size();
    }

    /**
     * Gets the user's current position in the adapter.
     * @return an integer specifying the user's current position.
     */
    public int getCurrentPos() {
        return currentPos;
    }

    /**
     * Adds a new message to the adapter.
     * @param message The message to add.
     */
    public void addMessage(String message) {
        messages.add(message);
        notifyDataSetChanged();
    }
}
