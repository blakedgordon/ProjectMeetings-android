package edu.calbaptist.android.projectmeetings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 *  Meeting Message Object Fragment
 *  Initializes a Meeting Message Object Fragment.
 *
 *  @author Caleb Solorio
 *  @version 0.7.0 12/20/17
 */
public class MeetingMessageFragment extends Fragment {
    public static final String ARG_OBJECT = "object";
    public static final String MESSAGE = "message";

    /**
     * Initializes MeetingMessageFragment.
     * @param inflater The inflater in which to inflate the fragment.
     * @param container The container in which the fragment belongs
     * @param savedInstanceState Contains any data sent from the previous activity.
     * @return the fragment View.
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_meeting_message, container, false);
        Bundle args = getArguments();
        String message = args.getString(MESSAGE);
        assert message != null;
        ((TextView) rootView.findViewById(R.id.text_message_item)).setText(message);
        return rootView;
    }
}
