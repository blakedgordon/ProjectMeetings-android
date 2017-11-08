package edu.calbaptist.android.projectmeetings;

import android.app.ListFragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by blakegordon on 11/7/17.
 */

public class RecordingsList extends ListFragment {
    private static final String LOG_TAG = "AudioRecord";
    private HashMap<String, MediaPlayer> playing = new HashMap<>();

    public RecordingsList() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String filePath = App.context.getDir("recordings", MODE_PRIVATE).getAbsolutePath();
        filePath += "/" + NewRecordingActivity.getRecordedFiles()[position];

        if (playing.get(filePath) == null) {
            MediaPlayer mPlayer = new MediaPlayer();
            try {
                FileInputStream fs = new FileInputStream(filePath);
                mPlayer.setDataSource(fs.getFD());
                mPlayer.prepare();
                mPlayer.start();
                playing.put(filePath, mPlayer);
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed: " + e.toString());
                e.printStackTrace();
            }
        } else {
            playing.get(filePath).stop();
            playing.put(filePath, null);
        }

    }
}
