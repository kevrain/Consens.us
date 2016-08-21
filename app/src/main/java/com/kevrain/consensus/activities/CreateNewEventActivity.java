package com.kevrain.consensus.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.kevrain.consensus.R;
import com.kevrain.consensus.adapter.EventLocationsArrayAdapter;
import com.kevrain.consensus.fragments.DatePickerFragment;
import com.kevrain.consensus.models.Location;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateNewEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.btnAddDate)Button btnAddDate;
    @BindView(R.id.btnAdd)Button btnAdd;
    @BindView(R.id.rvLocations) RecyclerView rvLocations;
    @BindView(R.id.etLocation) EditText etLocation;

    ArrayList<Location> locations;
    EventLocationsArrayAdapter locationsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        // Custom toolbar for displaying rounded profile image
        getSupportActionBar().setCustomView(R.layout.toolbar_new_event);

        locations = new ArrayList<>();

        locationsAdapter = new EventLocationsArrayAdapter(locations);
        rvLocations.setAdapter(locationsAdapter);

        rvLocations.setLayoutManager(new LinearLayoutManager(this));

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(etLocation.getText().toString()) && !TextUtils.isEmpty(btnAddDate.getText().toString())) {
                   locations.add(0, new Location(etLocation.getText().toString(), btnAddDate.getText().toString()));
                   locationsAdapter.notifyItemChanged(0);

                    etLocation.setText("");
                    btnAddDate.setText("");
                }
            }
        });
    }

    // attach to an onclick handler to show the date picker
    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    // handle the date selected
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        // store the values selected into a Calendar instance
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.format(c.getTime());
        btnAddDate.setText(""+  dateFormat.format(c.getTime()));
        Log.d("Test",btnAddDate.getText().toString());
    }

    public void closeActivity(View view) {
        if (view.getId() == R.id.ibSubmit) {

        }

        Intent data = new Intent();
        data.putExtra("Code", 200);
        finish();
    }
}
