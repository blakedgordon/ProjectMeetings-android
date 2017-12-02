package edu.calbaptist.android.projectmeetings;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class MeetingMessageObjectFragment extends Fragment {
    public static final String ARG_OBJECT = "object";
    public static final String MEETING_OBJECT = "meeting";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.fragment_meeting_message_object, container, false);
        Bundle args = getArguments();
        MeetingMessage message = (MeetingMessage) args.getSerializable(MEETING_OBJECT);
        ((TextView) rootView.findViewById(R.id.message_item_text)).setText(message.getMsg());
        return rootView;
    }
}
