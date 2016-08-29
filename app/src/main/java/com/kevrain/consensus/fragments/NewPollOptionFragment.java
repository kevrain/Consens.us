package com.kevrain.consensus.fragments;

import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.kevrain.consensus.R;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by iris on 8/28/16.
 */
public class NewPollOptionFragment extends DialogFragment {

    @BindView(R.id.btnSave) Button btnSave;
    @BindView(R.id.btnCancel) Button btnCancel;
    @BindView(R.id.tvSetDate) TextView tvSetDate;
    @BindView(R.id.etPollOptionName) EditText etPollOptionName;
    String optionDate;
    private Unbinder unbinder;

    private OnItemSaveListener listener;

    public interface OnItemSaveListener {
        void onItemSave(String date, String pollOption);
    }

    public NewPollOptionFragment() {
    }

    public static NewPollOptionFragment newInstance() {
        NewPollOptionFragment frag = new NewPollOptionFragment();
        return frag;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_poll_option, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.tvSetDate)
    public void onClickSetDate(View view) {
        showDatePickerDialog();
    }

    @OnClick(R.id.btnCancel)
    public void onCancel(View view) {
        dismiss();
    }

    @OnClick(R.id.btnSave)
    public void onSaveItem(View view) {
        String optionTitle = etPollOptionName.getText().toString();
        if (optionDate != null &&  optionTitle.length() > 0) {
            listener.onItemSave(optionDate, optionTitle);
            dismiss();
        }
    }

    public void showDatePickerDialog() {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setCallBack(new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                final Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, monthOfYear);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.format(c.getTime());
                optionDate =  dateFormat.format(c.getTime());
                tvSetDate.setText(optionDate);
                tvSetDate.setTextColor(getResources().getColor(R.color.colorBlack));
            }
        });
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnItemSaveListener) {
            listener = (OnItemSaveListener) context;
        } else {
            throw new ClassCastException(context.toString()
                + " must implement NewPollOptionFragment.OnItemSaveListener");
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}

