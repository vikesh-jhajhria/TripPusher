package com.trippusher.fragment;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
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
 * Created by Desktop-KS on 7/20/2017.
 */

public class FragmentAddTrip extends Fragment {
    TextView UserAirline, SpnBase, SpnDays, SpnPosition, SpnFlightTimeHrs, SpnFlightTimeMins, BtnDate;
    Button PostTrip;
    EditText Gift, TripID, Message;
    String allow_call, AirlineTitle, AirportCode, AirlineId, AirportId, Userid, resultBase;
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
    Fragment fragment;
    FragmentManager fragmentManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = prefs.edit();
        AirlineTitle = prefs.getString("airlineTitle", null);
        AirlineId = prefs.getString("airlineId", null);
        AirportId = prefs.getString("AirportId", null);
        AirportCode = prefs.getString("AirportCode", null);
        Userid = prefs.getString("userId", null);
        resultBase = prefs.getString("resultBase", null);
        final View view = inflater.inflate(R.layout.fragment_add_trip, container, false);
        UserAirline = (TextView) view.findViewById(R.id.txtUserAirline);
        SpnPosition = (TextView) view.findViewById(R.id.spnPosition);
        SpnFlightTimeHrs = (TextView) view.findViewById(R.id.spnHrs);
        SpnFlightTimeMins = (TextView) view.findViewById(R.id.spnMins);
        TripID = (EditText) view.findViewById(R.id.txtTripID);
        Message = (EditText) view.findViewById(R.id.txtMsg);
        Gift = (EditText) view.findViewById(R.id.txtgift);
        SpnBase = (TextView) view.findViewById(R.id.SpnBase);
        SpnDays = (TextView) view.findViewById(R.id.spnDays);
        allow_calls = (Switch) view.findViewById(R.id.swtAllowCalls);
        swtGift = (Switch) view.findViewById(R.id.swtGift);
        BtnDate = (TextView) view.findViewById(R.id.btndate);
        PostTrip = (Button) view.findViewById(R.id.btnPostTrip);
        layDays = (LinearLayout) view.findViewById(R.id.layDays);
        SpnBase.setText(AirportCode);
        UserAirline.setText(AirlineTitle);
        Gift.setVisibility(view.GONE);
        swtGift.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Gift.setVisibility(view.VISIBLE);
                } else {
                    Gift.setVisibility(view.GONE);
                    Gift.setText("");
                }
            }
        });
        SpnBase.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    DiaBaseAirport = new AlertDialog.Builder(getContext()).create();
                    DiaBaseAirport.setTitle("Select Airport");
                    BaseAirportlistView = new ListView(getContext());
                    JSONArray array2 = new JSONArray(resultBase);
                    for (int i = 0; i < array2.length(); i++) {
                        airportList.add(new AirportList(
                                array2.getJSONObject(i).getString("pk_airport_id"),
                                array2.getJSONObject(i).getString("airport_code")));
                    }
                    BaseAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, airportList);
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
                //datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            }
        });
        SpnDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiaDays = (new AlertDialog.Builder(getContext())).create();
                DiaDays.setTitle("Days");
                DayslistView = new ListView(getContext());
                if (DaysAdapter == null) {
                    for (int i = 1; i <= 6; i++) {
                        daysList.add(new DaysList(i, String.valueOf(i)));
                    }
                    DaysAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, daysList);
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
                DiaPosition = (new AlertDialog.Builder(getContext())).create();
                DiaPosition.setTitle("Position");
                PositionlistView = new ListView(getContext());
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
                    PositionAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, positionList);
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
                DiaHrs = (new AlertDialog.Builder(getContext())).create();
                DiaHrs.setTitle("Hrs");
                HrslistView = new ListView(getContext());
                if (HrsAdapter == null) {
                    for (int i = 1; i <= 40; i++) {
                        hrsList.add(new HrsList(String.valueOf(i)));
                    }
                    HrsAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, hrsList);
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
                DiaMins = (new AlertDialog.Builder(getContext())).create();
                DiaMins.setTitle("Mins");
                MinslistView = new ListView(getContext());
                if (MinsAdapter == null) {
                    for (int i = 0; i <= 59; i++) {
                        minList.add(new MinList(String.valueOf(i)));
                    }
                    MinsAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, minList);
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
        PostTrip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (SpnBase.getText().toString().matches("Base")) {
                    Toast.makeText(getContext(), "Please Select Base", Toast.LENGTH_SHORT).show();
                } else if (BtnDate.getText().toString().matches("Start Date")) {
                    Toast.makeText(getContext(), "Please Select Date", Toast.LENGTH_SHORT).show();
                } else if (SpnDays.getText().toString().matches("Select Days")) {
                    Toast.makeText(getContext(), "Please Select Days", Toast.LENGTH_SHORT).show();
                } else if (TripID.getText().toString().matches("")) {
                    Toast.makeText(getContext(), "Please Enter Your Trip ID", Toast.LENGTH_SHORT).show();
                } else if (SpnPosition.getText().toString().matches("Position")) {
                    Toast.makeText(getContext(), "Please Select Position", Toast.LENGTH_SHORT).show();
                } else if (SpnFlightTimeHrs.getText().toString().matches("Hrs")) {
                    Toast.makeText(getContext(), "Please Enter Your Flight Time Hrs", Toast.LENGTH_SHORT).show();
                } else if (SpnFlightTimeMins.getText().toString().matches("Mins")) {
                    Toast.makeText(getContext(), "Please Enter Your Flight Time Mins", Toast.LENGTH_SHORT).show();
                } else if (swtGift.isChecked()) {
                    if (Gift.getText().toString().matches("")) {
                        Toast.makeText(getContext(), "Please Enter Gift Amount", Toast.LENGTH_SHORT).show();
                    } else {
                        nextCode();
                    }
                } else {
                    nextCode();
                }
            }
        });
        return view;
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
                AirlineId,
                Userid,
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
            String URL = AppStatus.getbaseurl().baseurl() + "post_trip";
            String airline_id = args[0];
            String user_id = args[1];
            String source_airport_id = args[2];
            String date = args[3];
            String no_of_days = args[4];
            String trip_id = args[5];
            String position = args[6];
            String flight_time_hrs = args[7];
            String flight_time_mins = args[8];
            String job_amount = args[9];
            String message = args[10];
            String allow_call_value = args[11];
            ArrayList parametarLogin = new ArrayList();
            parametarLogin.add(new BasicNameValuePair("airline_id", airline_id));
            parametarLogin.add(new BasicNameValuePair("user_id", user_id));
            parametarLogin.add(new BasicNameValuePair("source_airport_id", source_airport_id));
            parametarLogin.add(new BasicNameValuePair("date", date));
            parametarLogin.add(new BasicNameValuePair("no_of_days", no_of_days));
            parametarLogin.add(new BasicNameValuePair("trip_id", trip_id));
            parametarLogin.add(new BasicNameValuePair("position", position));
            parametarLogin.add(new BasicNameValuePair("flight_time_hrs", flight_time_hrs));
            parametarLogin.add(new BasicNameValuePair("flight_time_mins", flight_time_mins));
            parametarLogin.add(new BasicNameValuePair("job_amount", job_amount));
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
                        Toast.makeText(getContext(), MSG, Toast.LENGTH_LONG).show();
                        fragment = new FragmentMyTrip();
                        fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
                    } else {
                        Toast.makeText(getContext(), MSG, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Unable to retrieve any data from server", Toast.LENGTH_LONG).show();
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

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
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