package com.trippusher.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.trippusher.AppStatus;
import com.trippusher.JSONParser;
import com.trippusher.R;
import com.trippusher.classes.AirportList;
import com.trippusher.classes.DaysList;
import com.trippusher.classes.HrsList;
import com.trippusher.classes.MinList;
import com.trippusher.classes.PositionList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Desktop-KS on 8/10/2017.
 */

public class ActivityEditMyTrip extends AppCompatActivity {
    TextView UserAirline, SpnBase, SpnDays, SpnPosition, SpnFlightTimeHrs, SpnFlightTimeMins, BtnDate, TripID;
    Button BtnUpdateTrip;
    EditText Gift, Message;
    String allow_call, AirlineTitle, AirportCode, AirlineId, AirportId, Userid, resultBase, postTripId;
    ArrayAdapter BaseAdapter, DaysAdapter, PositionAdapter, HrsAdapter, MinsAdapter;
    Switch allow_calls, swtGift;
    LinearLayout layDays;
    ListView DayslistView, BaseAirportlistView, PositionlistView, HrslistView, MinslistView;
    AlertDialog DiaBaseAirport, DiaDays, DiaPosition, DiaHrs, DiaMins;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    JSONParser jsonParser = new JSONParser();
    private List<AirportList> airportList = new ArrayList<>();
    private List<DaysList> daysList = new ArrayList<>();
    private List<PositionList> positionList = new ArrayList<>();
    private List<HrsList> hrsList = new ArrayList<>();
    private List<MinList> minList = new ArrayList<>();
    ImageView Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_my_trip);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        AirlineTitle = prefs.getString("airlineTitle", null);
        AirlineId = prefs.getString("airlineId", null);
        AirportId = prefs.getString("AirportId", null);
        AirportCode = prefs.getString("AirportCode", null);
        Userid = prefs.getString("userId", null);
        resultBase = prefs.getString("resultBase", null);
        Bundle bundle = getIntent().getExtras();
        postTripId = bundle.getString("MyPostTripId");
        UserAirline = (TextView) findViewById(R.id.txtUserAirline);
        SpnPosition = (TextView) findViewById(R.id.spnPosition);
        SpnFlightTimeHrs = (TextView) findViewById(R.id.spnHrs);
        SpnFlightTimeMins = (TextView) findViewById(R.id.spnMins);
        TripID = (TextView) findViewById(R.id.txtTripID);
        Message = (EditText) findViewById(R.id.txtMsg);
        Gift = (EditText) findViewById(R.id.txtgift);
        SpnBase = (TextView) findViewById(R.id.SpnBase);
        SpnDays = (TextView) findViewById(R.id.spnDays);
        allow_calls = (Switch) findViewById(R.id.swtAllowCalls);
        swtGift = (Switch) findViewById(R.id.swtGift);
        BtnDate = (TextView) findViewById(R.id.btndate);
        BtnUpdateTrip = (Button) findViewById(R.id.btnPostTrip);
        layDays = (LinearLayout) findViewById(R.id.layDays);
        Back = (ImageView) findViewById(R.id.imgBack);
        UserAirline.setText(AirlineTitle);
        Gift.setVisibility(View.GONE);
        Back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        new AsyncTask<String, String, JSONObject>() {
            String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "get_trip_details";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected JSONObject doInBackground(String... args) {
                ArrayList parametarGetAirline = new ArrayList();
                parametarGetAirline.add(new BasicNameValuePair("post_trip_id", postTripId));
                JSONObject json = jsonParser.makeHttpRequest(GetAirlineURL, "POST", parametarGetAirline);
                return json;
            }

            protected void onPostExecute(JSONObject result) {
                try {
                    if (result != null) {
                        int status_id = result.getInt("status_id");
                        String MSG = result.getString("status_msg");
                        if (status_id != 0) {
                            JSONArray array = new JSONArray(result.getString("data"));
                            UserAirline.setText(array.getJSONObject(0).getString("airline_title"));
                            SpnBase.setText(array.getJSONObject(0).getString("base_airport_code"));
                            JSONArray array1 = new JSONArray(resultBase);
                            for (int i = 0; i < array1.length(); i++) {
                                if (SpnBase.getText().equals(array1.getJSONObject(i).getString("airport_code"))) {
                                    AirportId = array1.getJSONObject(i).getString("pk_airport_id");
                                }
                            }
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date testDate = null;
                            try {
                                testDate = sdf.parse(array.getJSONObject(0).getString("start_date"));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                            String newFormat = formatter.format(testDate);
                            BtnDate.setText(newFormat);
                            SpnDays.setText(array.getJSONObject(0).getString("no_of_days"));
                            TripID.setText(array.getJSONObject(0).getString("trip_id"));
                            SpnPosition.setText(array.getJSONObject(0).getString("position"));
                            SpnFlightTimeHrs.setText(array.getJSONObject(0).getString("flight_time_hrs"));
                            SpnFlightTimeMins.setText(array.getJSONObject(0).getString("flight_time_mins"));
                            if (!array.getJSONObject(0).getString("gift").equals("0.00")) {
                                swtGift.setChecked(true);
                                Gift.setText(array.getJSONObject(0).getString("gift"));
                            } else {
                                swtGift.setChecked(false);
                            }
                            Message.setText(array.getJSONObject(0).getString("message"));
                            if (!array.getJSONObject(0).getString("allow_call").equals(0)) {
                                allow_calls.setChecked(true);
                            } else {
                                allow_calls.setChecked(false);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), MSG, Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
        swtGift.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Gift.setVisibility(View.VISIBLE);
                } else {
                    Gift.setVisibility(View.GONE);
                    Gift.setText("");
                }
            }
        });
        SpnBase.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    DiaBaseAirport = new AlertDialog.Builder(ActivityEditMyTrip.this).create();
                    DiaBaseAirport.setTitle("Select Airport");
                    BaseAirportlistView = new ListView(ActivityEditMyTrip.this);
                    if (BaseAdapter == null) {
                        JSONArray array2 = new JSONArray(resultBase);
                        for (int i = 0; i < array2.length(); i++) {
                            airportList.add(new AirportList(
                                    array2.getJSONObject(i).getString("pk_airport_id"),
                                    array2.getJSONObject(i).getString("airport_code")));
                        }
                        BaseAdapter = new ArrayAdapter(ActivityEditMyTrip.this, android.R.layout.simple_list_item_1, airportList);
                    }
                    BaseAirportlistView.setAdapter(BaseAdapter);
                    DiaBaseAirport.setView(BaseAirportlistView);
                    DiaBaseAirport.show();
                    BaseAirportlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            DiaBaseAirport.cancel();
                            String s = BaseAirportlistView.getItemAtPosition(position).toString();
                            SpnBase.setText(s);
                            try {
                                JSONArray array1 = new JSONArray(resultBase);
                                for (int i = 0; i < array1.length(); i++) {
                                    if (SpnBase.getText().equals(array1.getJSONObject(i).getString("airport_code"))) {
                                        AirportId = array1.getJSONObject(i).getString("pk_airport_id");
                                    }
                                }
                            } catch (JSONException e) {
                            }
                        }

                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        BtnDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                datePicker();
            }
        });
        SpnDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiaDays = (new AlertDialog.Builder(ActivityEditMyTrip.this)).create();
                DiaDays.setTitle("Days");
                DayslistView = new ListView(ActivityEditMyTrip.this);
                if (DaysAdapter == null) {
                    for (int i = 1; i <= 6; i++) {
                        daysList.add(new DaysList(i, String.valueOf(i)));
                    }
                    DaysAdapter = new ArrayAdapter(ActivityEditMyTrip.this, android.R.layout.simple_list_item_1, daysList);
                }
                DayslistView.setAdapter(DaysAdapter);
                DiaDays.setView(DayslistView);
                DiaDays.show();
                DayslistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        DiaDays.cancel();
                        String s = DayslistView.getItemAtPosition(position).toString();
                        SpnDays.setText(s);
                    }
                });
            }
        });
        SpnPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiaPosition = (new AlertDialog.Builder(ActivityEditMyTrip.this)).create();
                DiaPosition.setTitle("Position");
                PositionlistView = new ListView(ActivityEditMyTrip.this);
                if (PositionAdapter == null) {
                    positionList.add(new PositionList("FM01"));
                    positionList.add(new PositionList("FA01"));
                    positionList.add(new PositionList("FA02"));
                    positionList.add(new PositionList("FA03"));
                    positionList.add(new PositionList("FA04"));
                    positionList.add(new PositionList("FA05"));
                    positionList.add(new PositionList("FA06"));
                    positionList.add(new PositionList("FA07"));
                    positionList.add(new PositionList("FA08"));
                    positionList.add(new PositionList("FA09"));
                    positionList.add(new PositionList("FA010"));
                    positionList.add(new PositionList("FA011"));
                    positionList.add(new PositionList("FA012"));
                    PositionAdapter = new ArrayAdapter(ActivityEditMyTrip.this, android.R.layout.simple_list_item_1, positionList);
                }
                PositionlistView.setAdapter(PositionAdapter);
                DiaPosition.setView(PositionlistView);
                DiaPosition.show();
                PositionlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        DiaPosition.cancel();
                        String s = PositionlistView.getItemAtPosition(position).toString();
                        SpnPosition.setText(s);
                    }
                });
            }
        });
        SpnFlightTimeHrs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiaHrs = (new AlertDialog.Builder(ActivityEditMyTrip.this)).create();
                DiaHrs.setTitle("Hrs");
                HrslistView = new ListView(ActivityEditMyTrip.this);
                if (HrsAdapter == null) {
                    for (int i = 1; i <= 40; i++) {
                        hrsList.add(new HrsList(String.valueOf(i)));
                    }
                    HrsAdapter = new ArrayAdapter(ActivityEditMyTrip.this, android.R.layout.simple_list_item_1, hrsList);
                }
                HrslistView.setAdapter(HrsAdapter);
                DiaHrs.setView(HrslistView);
                DiaHrs.show();
                HrslistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        DiaHrs.cancel();
                        String s = HrslistView.getItemAtPosition(position).toString();
                        SpnFlightTimeHrs.setText(s);
                    }
                });
            }
        });
        SpnFlightTimeMins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiaMins = (new AlertDialog.Builder(ActivityEditMyTrip.this)).create();
                DiaMins.setTitle("Mins");
                MinslistView = new ListView(ActivityEditMyTrip.this);
                if (MinsAdapter == null) {
                    for (int i = 0; i <= 59; i++) {
                        minList.add(new MinList(String.valueOf(i)));
                    }
                    MinsAdapter = new ArrayAdapter(ActivityEditMyTrip.this, android.R.layout.simple_list_item_1, minList);
                }

                MinslistView.setAdapter(MinsAdapter);
                DiaMins.setView(MinslistView);
                DiaMins.show();
                MinslistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        DiaMins.cancel();
                        String s = MinslistView.getItemAtPosition(position).toString();
                        SpnFlightTimeMins.setText(s);
                    }
                });
            }
        });
        BtnUpdateTrip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (SpnBase.getText().toString().matches("Base")) {
                    Toast.makeText(ActivityEditMyTrip.this, "Please Select Base", Toast.LENGTH_SHORT).show();
                } else if (BtnDate.getText().toString().matches("Start Date")) {
                    Toast.makeText(ActivityEditMyTrip.this, "Please Select Date", Toast.LENGTH_SHORT).show();
                } else if (SpnDays.getText().toString().matches("Select Days")) {
                    Toast.makeText(ActivityEditMyTrip.this, "Please Select Days", Toast.LENGTH_SHORT).show();
                } else if (TripID.getText().toString().matches("")) {
                    Toast.makeText(ActivityEditMyTrip.this, "Please Enter Your Trip ID", Toast.LENGTH_SHORT).show();
                } else if (SpnPosition.getText().toString().matches("Position")) {
                    Toast.makeText(ActivityEditMyTrip.this, "Please Select Position", Toast.LENGTH_SHORT).show();
                } else if (SpnFlightTimeHrs.getText().toString().matches("Hrs")) {
                    Toast.makeText(ActivityEditMyTrip.this, "Please Enter Your Flight Time Hrs", Toast.LENGTH_SHORT).show();
                } else if (SpnFlightTimeMins.getText().toString().matches("Mins")) {
                    Toast.makeText(ActivityEditMyTrip.this, "Please Enter Your Flight Time Mins", Toast.LENGTH_SHORT).show();
                } else if (swtGift.isChecked()) {
                    if (Gift.getText().toString().matches("")) {
                        Toast.makeText(ActivityEditMyTrip.this, "Please Enter Gift Amount", Toast.LENGTH_SHORT).show();
                    } else {
                        nextCode();
                    }
                } else {
                    nextCode();
                }
            }
        });
    }

    private void nextCode() {
        if (allow_calls.isChecked()) {
            allow_call = "1";
        } else {
            allow_call = "0";
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date = format.format(Date.parse(BtnDate.getText().toString()));
        AddTrip addTrip = new AddTrip();
        addTrip.execute(
                postTripId,
                AirportId,
                date,
                SpnDays.getText().toString(),
                TripID.getText().toString(),
                SpnPosition.getText().toString(),
                SpnFlightTimeHrs.getText().toString(),
                SpnFlightTimeMins.getText().toString(),
                Gift.getText().toString(),
                Message.getText().toString(),
                allow_call.toString());
    }

    private class AddTrip extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            String URL = AppStatus.getbaseurl().baseurl() + "update_my_trip";
            String postTripId = args[0];
            String source_airport_id = args[1];
            String date = args[2];
            String no_of_days = args[3];
            String trip_id = args[4];
            String position = args[5];
            String flight_time_hrs = args[6];
            String flight_time_mins = args[7];
            String gift = args[8];
            Log.d("gift", gift);
            String message = args[9];
            String allow_call_value = args[10];
            ArrayList parametarLogin = new ArrayList();
            parametarLogin.add(new BasicNameValuePair("post_trip_id", postTripId));
            parametarLogin.add(new BasicNameValuePair("source_airport_id", source_airport_id));
            parametarLogin.add(new BasicNameValuePair("date", date));
            parametarLogin.add(new BasicNameValuePair("no_of_days", no_of_days));
            parametarLogin.add(new BasicNameValuePair("trip_id", trip_id));
            parametarLogin.add(new BasicNameValuePair("position", position));
            parametarLogin.add(new BasicNameValuePair("flight_time_hrs", flight_time_hrs));
            parametarLogin.add(new BasicNameValuePair("flight_time_mins", flight_time_mins));
            parametarLogin.add(new BasicNameValuePair("job_amount", gift));
            parametarLogin.add(new BasicNameValuePair("message", message));
            parametarLogin.add(new BasicNameValuePair("allow_call", allow_call_value));
            JSONObject json = jsonParser.makeHttpRequest(URL, "POST", parametarLogin);
            return json;
        }

        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {
                    int status_id = result.getInt("status_id");
                    String MSG = result.getString("status_msg");
                    if (status_id != 0) {
                        Toast.makeText(ActivityEditMyTrip.this, MSG, Toast.LENGTH_LONG).show();
                        /*editor.putString("EditMyTrips", "Yes");
                        editor.commit();*/
                        Intent intent = new Intent(ActivityEditMyTrip.this, ActivityMain.class);
                        intent.putExtra("EditMyTrips", "Yes");
                        startActivity(intent);
                    } else {
                        Toast.makeText(ActivityEditMyTrip.this, MSG, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ActivityEditMyTrip.this, "Unable to retrieve any data from server", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private void datePicker() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(ActivityEditMyTrip.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String Day;
                        String Month;
                        String Year = String.valueOf(year);
                        if ((monthOfYear + 1) < 10) {
                            Month = "0" + (monthOfYear + 1);
                        } else {
                            Month = String.valueOf(monthOfYear + 1);
                        }
                        if (dayOfMonth < 10) {
                            Day = "0" + dayOfMonth;
                        } else {
                            Day = String.valueOf(dayOfMonth);
                        }
                        String date_time = Month + "/" + Day + "/" + Year;
                        BtnDate.setText(date_time);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }
}