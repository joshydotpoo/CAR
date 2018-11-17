package com.car.carsquad.carapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.util.Calendar;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mCancelSearch;
    private String startPt;
    private String endPt;
    private TextView mDisplayDate;
    private TextView mDisplayTime;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //GOOGLE PLACES AUTOCOMPLETE
        PlaceAutocompleteFragment autocompleteFragment1 = (PlaceAutocompleteFragment) getFragmentManager()
                .findFragmentById(R.id.post_start_point1);
        autocompleteFragment1.setHint("ENTER START POINT");
        ImageView startIcon = (ImageView)((LinearLayout)autocompleteFragment1.getView()).getChildAt(0);
        startIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_start));
        autocompleteFragment1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                startPt = place.getName().toString();
                //place.getLatLng();
            }
            @Override
            public void onError(Status status) {
            }
        });
        PlaceAutocompleteFragment autocompleteFragment2 = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.post_destination_point1);
        autocompleteFragment2.setHint("ENTER DESTINATION");
        ImageView endIcon = (ImageView)((LinearLayout)autocompleteFragment1.getView()).getChildAt(0);
        endIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_end));
        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                endPt = place.getName().toString();
            }
            @Override
            public void onError(Status status) {
            }
        });

        mCancelSearch = (Button) findViewById(R.id.cancel_search);
        mCancelSearch.setOnClickListener(this);
        mDisplayDate = (TextView) findViewById(R.id.tvDate);
        mDisplayTime = (TextView) findViewById(R.id.tvTime);

        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(
                        SearchActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener,
                        year, month, day);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
                dialog.show();
            }
        });

        mDisplayTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
                TimePickerDialog dialog = new TimePickerDialog(
                        SearchActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth, mTimeSetListener,
                        hour, minute, true);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.show();
            }
        });
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;
                Log.d("DriverPostActivity", "onDateSet: mm/dd/yyyy: " + month + "/" + day + "/" + year);
                String date = "Departure date: " + month + "/" + day + "/" + year;
                mDisplayDate.setText(date);
            }
        };
        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                Log.d("DriverPostActivity", "onTimeSet: hh:mm: " + hour + "/" + minute);
                String time = "Departure time: " + checkDigit(hour) + ":" + checkDigit(minute);
                mDisplayTime.setText(time);
            }
        };
    }

    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    public void onClick(View view) {
        if(view == mCancelSearch){
            finish();
            startActivity(new Intent(this, RiderActivity.class));
        }
    }
}
