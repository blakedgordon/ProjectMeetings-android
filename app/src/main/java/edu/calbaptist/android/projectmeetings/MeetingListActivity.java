package edu.calbaptist.android.projectmeetings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Austin on 11/30/2017.
 */

public class MeetingListActivity extends AppCompatActivity{

    private Button newMeeting;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetinglist);

        newMeeting = findViewById(R.id.CreateMeeting);
        newMeeting.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                switchActivity(MeetingCreationActivity.class);
            }
        });
    }

    private void switchActivity(Class activity){
        startActivity(new Intent(this, activity));
    }

}
