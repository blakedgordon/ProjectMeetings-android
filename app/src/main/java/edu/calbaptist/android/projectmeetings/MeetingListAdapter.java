package edu.calbaptist.android.projectmeetings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by csolo on 12/5/2017.
 */

public class MeetingListAdapter extends ArrayAdapter<Meeting> {
    final static String TAG = "MeetingLstAdapter";

    private final static SharedPreferences prefs = App.context.getSharedPreferences(
            "edu.calbaptist.android.projectmeetings.Account_Name",
            Context.MODE_PRIVATE);

    private ArrayList<Meeting> meetings;

    public MeetingListAdapter(@NonNull Context context, ArrayList<Meeting> meetings) {
        super(context, 0, meetings);

        this.meetings = meetings;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView = convertView;

        if (convertView == null) {
            rowView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_meeting, parent, false);
            ViewHolder h = new ViewHolder();
            h.mItemMeeting = (RelativeLayout) rowView.findViewById(R.id.item_meeting);
            h.mDriveButton = (ImageView) rowView.findViewById(R.id.drive_button);
            h.mEditButton = (ImageView) rowView.findViewById(R.id.edit_button);
            rowView.setTag(h);
        }

        ViewHolder h = (ViewHolder) rowView.getTag();

        final Meeting meeting = meetings.get(position);

        h.mItemMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MeetingActivity.class);
                intent.putExtra("meeting", meeting);
                User user = new User.UserBuilder()
                        .setUid(prefs.getString("uID",null))
                        .setDisplayName(prefs.getString("DisplayName",null))
                        .setFirebaseToken(prefs.getString("FirebaseToken",null))
                        .build();
                intent.putExtra("user",user);
                getContext().startActivity(intent);
            }
        });

        h.mDriveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://drive.google.com/open?id=" + meeting.getDriveFolderId());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                getContext().startActivity(browserIntent);
            }
        });

        h.mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EditMeetingActivity.class);
                intent.putExtra("meeting", meeting);
                getContext().startActivity(intent);
            }
        });

        Date now = new Date();
        Date startDate = new Date(meeting.getTime());
        Date endDate = new Date(meeting.getTime() + meeting.getTimeLimit());
        boolean inProgress = now.after(startDate) && now.before(endDate);

        TextView textTitle = (TextView) rowView.findViewById(R.id.meeting_title);
        textTitle.setText(meeting.getName());

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a, MMMM dd");
        TextView textDate = (TextView) rowView.findViewById(R.id.meeting_date);
        textDate.setText(sdf.format(startDate));

        TextView textTime = (TextView) rowView.findViewById(R.id.item_meeting_time);
        if (inProgress) {
            LinearLayout slit = rowView.findViewById(R.id.item_meeting_color_slit);
            slit.setVisibility(View.VISIBLE);
            textTime.setText("In Progress");
        } else {
            int totalMinutes = (int) meeting.getTimeLimit() / 60000;
            StringBuilder builder = new StringBuilder();

            int hours = totalMinutes/60;
            if(hours > 0) {
                builder.append(hours + "h ");
            }

            int minutes = totalMinutes % 60;
            if(minutes > 0) {
                builder.append(minutes + "m");
            }

            textTime.setText(builder.toString());
        }

        return rowView;
    }

    public static class ViewHolder {
        public RelativeLayout mItemMeeting;
        public ImageView mDriveButton;
        public ImageView mEditButton;
    }
}
