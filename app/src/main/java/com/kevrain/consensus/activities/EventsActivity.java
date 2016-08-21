package com.kevrain.consensus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.facebook.login.LoginManager;
import com.kevrain.consensus.R;
import com.kevrain.consensus.adapter.EventsArrayAdapter;
import com.kevrain.consensus.models.Events;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventsActivity extends AppCompatActivity {

    @BindView(R.id.lvEvents)
    ListView lvEvents;
    @BindView(R.id.fabCreateEvent)
    FloatingActionButton fabCreateEvent;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btnStatus)
    Button btnStatus;

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

        btnStatus.setTag(0);
        btnStatus.setText("Interested");

        //###### Populate data into events list view here

        //Add New event
        fabCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Button click", "Buttton");
                Intent i = new Intent(EventsActivity.this, CreateNewEventActivity.class);
                startActivityForResult(i, REQUEST_CODE);
            }
        });
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
//        switch(item.getItemId()) {
//            case R.id.action_settings:
//                return true;
//            case R.id.action_logout:
//                logOut();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);

        //Log.d("DID WE COME HERE?", "onOptionsItemSelected");
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logOut() {
        //Log.d("Click on LOGOUT", "LOGOUT BUTTON");
        LoginManager.getInstance().logOut();
        finish();
    }

    private void statusChange() {
        int status = (Integer) btnStatus.getTag();

        if (status == 0) {
            btnStatus.setText("Going");
            btnStatus.setTag(1);
        } else if (status == 1) {
            btnStatus.setText("Interested");
            btnStatus.setTag(0);
        }
    }

}





