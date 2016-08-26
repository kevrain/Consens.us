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
import android.widget.Toast;

import com.kevrain.consensus.R;
import com.kevrain.consensus.adapter.PollOptionsArrayAdapter;
import com.kevrain.consensus.fragments.DatePickerFragment;
import com.kevrain.consensus.models.Group;
import com.kevrain.consensus.models.Poll;
import com.kevrain.consensus.models.PollOption;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateNewPollActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.btnAddDate)Button btnAddDate;
    @BindView(R.id.btnAdd)Button btnAdd;
    @BindView(R.id.rvPollOptions) RecyclerView rvPollOptions;
    @BindView(R.id.etPollOption) EditText etPollOption;
    @BindView(R.id.etEventName) EditText etEventName;

    ArrayList<PollOption> pollOptions;
    PollOptionsArrayAdapter locationsAdapter;
    Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_poll);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        // Custom toolbar for displaying rounded profile image
        getSupportActionBar().setCustomView(R.layout.toolbar_new_poll);

        pollOptions = new ArrayList<>();

        locationsAdapter = new PollOptionsArrayAdapter(pollOptions);
        rvPollOptions.setAdapter(locationsAdapter);

        rvPollOptions.setLayoutManager(new LinearLayoutManager(this));

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(etPollOption.getText().toString()) && !TextUtils.isEmpty(btnAddDate.getText().toString())) {

                    //Check if the location/date is already part of the list
                    boolean isDuplicate = false;
                    for (int i = 0; i< pollOptions.size(); i++)
                    {
                        if(pollOptions.get(i).getDate().toString().equalsIgnoreCase(btnAddDate.getText().toString()) &&
                                pollOptions.get(i).getName().toString().equalsIgnoreCase(etPollOption.getText().toString())) {
                            isDuplicate= true;
                        }
                    }

                    if(isDuplicate==false)
                    {
                        pollOptions.add(0, new PollOption(etPollOption.getText().toString(), btnAddDate.getText().toString()));
                        locationsAdapter.notifyItemChanged(0);
                    }

                    etPollOption.setText("");
                    btnAddDate.setText("");
                }
            }
        });
    }

    // Attach to an onclick handler to show the date picker
    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

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

    public void saveNewPoll(View view) {
        String groupID = getIntent().getStringExtra("groupID");
        boolean isInvalidData = false;

        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        query.include("polls");
        String pollName = etEventName.getText().toString();

        if(pollName.length() <1 || pollName==null) {
            isInvalidData = true;
            Toast.makeText(this, "Please provide a poll name", Toast.LENGTH_LONG).show();
        }

        if(pollOptions.size()<2) {
            isInvalidData = true;
            Toast.makeText(this, "Poll should have atleast 2 locations", Toast.LENGTH_LONG).show();
        }

        if(!isInvalidData) {
            query.getInBackground(groupID, new GetCallback<Group>() {
                public void done(Group groupItem, ParseException e) {
                    if (e == null) {
                        group = groupItem;

                        final Poll newPoll = new Poll();
                        newPoll.setPollName(etEventName.getText().toString());
                        newPoll.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                group.addPoll(newPoll);

                                for (int i = 0; i < pollOptions.size(); i++) {
                                    final PollOption loc = pollOptions.get(i);
                                    loc.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            newPoll.addPollOption(loc);
                                        }
                                    });
                                }

                                //Close and go to events activity
                                Intent intent = new Intent(getApplicationContext(), PollsActivity.class);
                                intent.putExtra("Code", 20);
                                intent.putExtra("pollID", newPoll.getObjectId());
                                intent.putExtra("pollName", newPoll.getPollName());
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        });
                    }
                }
            });
        }
    }
}

