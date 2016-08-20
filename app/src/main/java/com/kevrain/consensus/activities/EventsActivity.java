package com.kevrain.consensus.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ListView;

import com.facebook.login.LoginManager;
import com.kevrain.consensus.R;
import com.kevrain.consensus.adapter.EventsArrayAdapter;
import com.kevrain.consensus.models.Events;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventsActivity extends AppCompatActivity {

    @BindView(R.id.lvEvents) ListView lvEvents;
    @BindView(R.id.fabCreateEvent) ImageButton btnCreateEvent;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    EventsArrayAdapter adapter;
    ArrayList<Events> events;
    private final int REQUEST_CODE = 20;

    //###### Network call to the Event Client to get Data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        events = new ArrayList<>();
        adapter = new EventsArrayAdapter(this, events);
        lvEvents.setAdapter(adapter);

        //###### Populate data into events list view here
    }

    private void populateData() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_events, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_logout:
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logOut() {
        LoginManager.getInstance().logOut();
        finish();
    }
}
