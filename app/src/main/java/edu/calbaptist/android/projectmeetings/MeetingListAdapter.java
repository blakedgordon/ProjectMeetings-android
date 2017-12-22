package edu.calbaptist.android.projectmeetings;

import android.app.Activity;
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
import android.widget.Toast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import edu.calbaptist.android.projectmeetings.async.meeting.DeleteMeetingAsync;
import edu.calbaptist.android.projectmeetings.async.user.DeleteUserInviteAsync;
import edu.calbaptist.android.projectmeetings.exceptions.RestClientException;
import edu.calbaptist.android.projectmeetings.models.Meeting;
import edu.calbaptist.android.projectmeetings.models.User;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientJsonCallback;

/**
 *  Meeting List Adapter
 *  Shows adapts the meetings the the user is a part of to an appropriate view.
 *
 *  @author Caleb Solorio
 *  @version 1.0.0 12/20/17
 */

public class MeetingListAdapter extends ArrayAdapter<Meeting> {
    private static final String TAG = "MeetingLstAdapter";
    private static final SharedPreferences PREFERENCES = App.context.getSharedPreferences(
            App.context.getString(R.string.app_package), Context.MODE_PRIVATE);

    ArrayList<Meeting> meetings;

    /**
     * Initializes the adapter.
     * @param context The context of the adapter in relation to the rest of the app.
     * @param meetings The meetings for which the adapter is created.
     */
    public MeetingListAdapter(@NonNull Context context, ArrayList<Meeting> meetings) {
        super(context, R.layout.item_meeting, meetings);

        this.meetings = meetings;
    }

    /**
     * Returns the view of a specified adapter item.
     * @param position The position of the view in question.
     * @param convertView The view of the row in question.
     * @param parent Parent of the adapter view.
     * @return The view for the specified data.
     */
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView = convertView;

        if (convertView == null) {
            rowView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_meeting, parent, false);
            ViewHolder h = new ViewHolder();
            h.mItemMeeting = (RelativeLayout) rowView.findViewById(R.id.item_meeting);
            h.mDriveButton = (ImageView) rowView.findViewById(R.id.image_drive_button);
            h.mEditButton = (ImageView) rowView.findViewById(R.id.image_edit_button);
            h.mRemoveButton = (ImageView) rowView.findViewById(R.id.image_remove_button);
            rowView.setTag(h);
        }

        ViewHolder h = (ViewHolder) rowView.getTag();

        final Meeting meeting = meetings.get(position);

        h.mItemMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MeetingActivity.class);
                intent.putExtra(MeetingActivity.MEETING_KEY, meeting);
                User user = new User.UserBuilder()
                        .setUid(PREFERENCES.getString("u_id",null))
                        .setDisplayName(PREFERENCES.getString("display_name",null))
                        .setFirebaseToken(PREFERENCES.getString("firebase_token",null))
                        .build();

                intent.putExtra(MeetingActivity.USER_KEY,user);
                getContext().startActivity(intent);
            }
        });

        h.mDriveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://drive.google.com/open?id=" +
                        meeting.getDriveFolderId());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                getContext().startActivity(browserIntent);
            }
        });

        if(meeting.getUid().equals(PREFERENCES.getString("u_id", null))) {
            h.mEditButton.setVisibility(View.VISIBLE);
            h.mEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), EditMeetingActivity.class);
                    intent.putExtra(EditMeetingActivity.MEETING_KEY, meeting);
                    getContext().startActivity(intent);
                }
            });

            h.mRemoveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new DeleteMeetingAsync(meeting.getMid(),
                            new AsyncDeleteMeetingCallback()).execute();
                }
            });
        } else {
            h.mEditButton.setVisibility(View.GONE);

            h.mRemoveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new DeleteUserInviteAsync(meeting.getMid(),
                            new AsyncDeleteInviteCallback()).execute();
                }
            });
        }

        Date now = new Date();
        Date startDate = new Date(meeting.getTime());
        Date endDate = new Date(meeting.getTime() + meeting.getTimeLimit());
        boolean inProgress = now.after(startDate) && now.before(endDate);

        TextView textTitle = (TextView) rowView.findViewById(R.id.text_meeting_title);
        textTitle.setText(meeting.getName());

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a, MMMM dd");
        TextView textDate = (TextView) rowView.findViewById(R.id.text_meeting_date);
        textDate.setText(sdf.format(startDate));

        TextView textTime = (TextView) rowView.findViewById(R.id.text_item_meeting_time);
        if (inProgress) {
            LinearLayout slit = rowView.findViewById(R.id.layout_item_meeting_color_slit);
            slit.setVisibility(View.VISIBLE);
            textTime.setText(App.context.getString(R.string.in_progress));
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

    /**
     * Updates the adapters Meeting data.
     */
    public void updateList() {
        Activity context = (Activity) getContext();
        context.finish();
        context.startActivity(context.getIntent());
    }

    /**
     * Given a String, display it to the user in a short Toast.
     */
    private void showToast(final String message) {
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Specifies the components of an item's view.
     */
    public static class ViewHolder {
        public static RelativeLayout mItemMeeting;
        public static ImageView mDriveButton;
        public static ImageView mEditButton;
        public static ImageView mRemoveButton;
    }

    /**
     * Specifies the RestClientJsonCallback implementation after deleting a meeting.
     */
    private class AsyncDeleteMeetingCallback implements RestClientJsonCallback {
        @Override
        public void onTaskExecuted(JSONObject json) {
            updateList();
        }

        @Override
        public void onTaskFailed(RestClientException e) {
            Log.e(TAG, "onTaskFailed: ", e);
            showToast("Error deleting meeting :(");
        }

        @Override
        public void onExceptionRaised(Exception e) {
            Log.e(TAG, "onExceptionRaised: ", e);
            showToast("Error deleting meeting :(");
        }
    }

    /**
     * Specifies the RestClientJsonCallback implementation after leaving a meeting.
     */
    private class AsyncDeleteInviteCallback implements RestClientJsonCallback {
        @Override
        public void onTaskExecuted(JSONObject json) {
            updateList();
        }

        @Override
        public void onTaskFailed(RestClientException e) {
            Log.e(TAG, "onTaskFailed: ", e);
            showToast("Error leaving meeting :(");
        }

        @Override
        public void onExceptionRaised(Exception e) {
            Log.e(TAG, "onExceptionRaised: ", e);
            showToast("Error leaving meeting :(");
        }
    }
}
