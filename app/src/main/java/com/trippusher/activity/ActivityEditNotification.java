package com.trippusher.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.trippusher.classes.GiftList;
import com.trippusher.classes.MaxList;
import com.trippusher.classes.MinList;

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
 * Created by Desktop-KS on 8/11/2017.
 */

public class ActivityEditNotification extends AppCompatActivity {
    ImageView Back;
    EditText NotificationTitle;
    Button btnUpdate;
    TextView SpnSearchBase, SpnMaxDays, SpnMinDays, SpnSearchGift, Searchdate, UserAirline;
    Switch Gift;
    ArrayAdapter BaseAirAdapter, MinDaysAdapter, MaxDaysAdapter, GiftAdapter;
    String AirlineTitle, AirportId, resultBase, AirlineId, date, MinDays, MaxDays, notification_id, userId;
    ListView MinDaylistView, MaxDaylistView, GiftlistView, BaseAirportlistView;
    android.support.v7.app.AlertDialog DiaMaxDay, DiaBaseAirport, DiaMinDay, DiaGift;
    LinearLayout layMinDays, layMaxDays;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private List<AirportList> airportList = new ArrayList<>();
    private List<MinList> minList = new ArrayList<>();
    private List<MaxList> maxList = new ArrayList<>();
    private List<GiftList> giftList = new ArrayList<>();
    int gift_id = 2;
    JSONParser jsonParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notification);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        userId = prefs.getString("userId", null);
        resultBase = prefs.getString("resultBase", null);
        AirlineTitle = prefs.getString("airlineTitle", null);
        AirlineId = prefs.getString("airlineId", null);
        notification_id = prefs.getString("notification_id", null);
        UserAirline = (TextView) findViewById(R.id.txtSearchAirlineId);
        SpnSearchBase = (TextView) findViewById(R.id.SpnNotificationBase);
        SpnSearchGift = (TextView) findViewById(R.id.SpnSearchGift);
        SpnMinDays = (TextView) findViewById(R.id.SpnMinDays);
        SpnMaxDays = (TextView) findViewById(R.id.SpnMaxDays);
        NotificationTitle = (EditText) findViewById(R.id.txtNotificationTitle);
        layMinDays = (LinearLayout) findViewById(R.id.layMinDays);
        layMaxDays = (LinearLayout) findViewById(R.id.layMaxDays);
        Gift = (Switch) findViewById(R.id.swtGift);
        Searchdate = (TextView) findViewById(R.id.btnSearchdate);
        btnUpdate = (Button) findViewById(R.id.btnSearch);
        Back = (ImageView) findViewById(R.id.imgBack);
        UserAirline.setText(AirlineTitle);
        Back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        new AsyncTask<String, String, JSONObject>() {
            String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "get_edit_notification";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected JSONObject doInBackground(String... args) {
                ArrayList parametarGetAirline = new ArrayList();
                parametarGetAirline.add(new BasicNameValuePair("notification_id", notification_id));
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
                            NotificationTitle.setText(array.getJSONObject(0).getString("notification_title"));
                            AirportId = array.getJSONObject(0).getString("source_airport_id");
                            try {
                                JSONArray array1 = new JSONArray(resultBase);
                                for (int i = 0; i < array1.length(); i++) {
                                    if (AirportId.equals(array1.getJSONObject(i).getString("pk_airport_id"))) {
                                        SpnSearchBase.setText(array1.getJSONObject(i).getString("airport_code"));
                                    }
                                }
                            } catch (JSONException e) {
                            }
                            if (!array.getJSONObject(0).getString("date").equals("0")) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                Date testDate = null;
                                try {
                                    testDate = sdf.parse(array.getJSONObject(0).getString("date"));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                                String newFormat = formatter.format(testDate);
                                Searchdate.setText(newFormat);
                            }
                            SpnMinDays.setText(array.getJSONObject(0).getString("min_days"));
                            SpnMaxDays.setText(array.getJSONObject(0).getString("max_days"));
                            if (array.getJSONObject(0).getString("gift").equals("2.00")) {
                                SpnSearchGift.setText("Both");
                                gift_id = 2;
                            }
                            if (array.getJSONObject(0).getString("gift").equals("1.00")) {
                                SpnSearchGift.setText("Yes");
                                gift_id = 1;
                            }
                            if (array.getJSONObject(0).getString("gift").equals("0.00")) {
                                SpnSearchGift.setText("No");
                                gift_id = 0;
                            }
                        } else {
                            Toast.makeText(getApplication(), MSG, Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (NotificationTitle.getText().toString().matches("")) {
                    Toast.makeText(getApplication(), "Please input your notification title", Toast.LENGTH_SHORT).show();
                } else if (SpnSearchBase.getText().toString().matches("Select Base")) {
                    Toast.makeText(getApplication(), "Please Select Base", Toast.LENGTH_SHORT).show();
                } else if (Searchdate.getText().toString().matches("Start Date")) {
                    Toast.makeText(getApplication(), "Please Select Date", Toast.LENGTH_SHORT).show();
                } else {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    date = format.format(Date.parse(Searchdate.getText().toString()));
                    if (SpnMinDays.getText().toString().matches("Min Days")) {
                        Toast.makeText(getApplication(), "Please Select Min Days", Toast.LENGTH_SHORT).show();
                    } else if (SpnMaxDays.getText().toString().matches("Max Days")) {
                        Toast.makeText(getApplication(), "Please Select Max Days", Toast.LENGTH_SHORT).show();
                    } else {
                        AddNotification addnotification = new AddNotification();
                        addnotification.execute(
                                notification_id,
                                userId,
                                NotificationTitle.getText().toString(),
                                String.valueOf(gift_id),
                                AirlineId,
                                date,
                                SpnMinDays.getText().toString(),
                                SpnMaxDays.getText().toString(),
                                AirportId
                        );
                    }
                }
            }
        });

        SpnSearchBase.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    DiaBaseAirport = new AlertDialog.Builder(ActivityEditNotification.this).create();
                    DiaBaseAirport.setTitle("Select Airport");
                    BaseAirportlistView = new ListView(ActivityEditNotification.this);
                    if (BaseAirAdapter == null) {
                        JSONArray array2 = new JSONArray(resultBase);
                        for (int i = 0; i < array2.length(); i++) {
                            airportList.add(new AirportList(
                                    array2.getJSONObject(i).getString("pk_airport_id"),
                                    array2.getJSONObject(i).getString("airport_code")));
                        }
                        BaseAirAdapter = new ArrayAdapter(ActivityEditNotification.this, android.R.layout.simple_list_item_1, airportList);
                    }
                    BaseAirportlistView.setAdapter(BaseAirAdapter);
                    DiaBaseAirport.setView(BaseAirportlistView);
                    DiaBaseAirport.show();

                    BaseAirportlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            DiaBaseAirport.cancel();
                            String s = BaseAirportlistView.getItemAtPosition(position).toString();
                            SpnSearchBase.setText(s);
                            try {
                                JSONArray array1 = new JSONArray(resultBase);
                                for (int i = 0; i < array1.length(); i++) {
                                    if (SpnSearchBase.getText().equals(array1.getJSONObject(i).getString("airport_code"))) {
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
        Searchdate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                datePicker();
            }
        });
        SpnMinDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiaMinDay = (new AlertDialog.Builder(ActivityEditNotification.this)).create();
                DiaMinDay.setTitle("Minimum Days");
                MinDaylistView = new ListView(ActivityEditNotification.this);
                if (MinDaysAdapter == null) {
                    for (int i = 1; i <= 6; i++) {
                        minList.add(new MinList(String.valueOf(i)));
                    }
                    MinDaysAdapter = new ArrayAdapter(ActivityEditNotification.this, android.R.layout.simple_list_item_1, minList);
                }
                MinDaylistView.setAdapter(MinDaysAdapter);
                DiaMinDay.setView(MinDaylistView);
                DiaMinDay.show();
                MinDaylistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        DiaMinDay.cancel();
                        String s = MinDaylistView.getItemAtPosition(position).toString();
                        SpnMinDays.setText(s);
                        MinDays = s;
                    }
                });
            }
        });
        SpnMaxDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DiaMaxDay = (new AlertDialog.Builder(ActivityEditNotification.this)).create();
                DiaMaxDay.setTitle("Maximum Days");
                MaxDaylistView = new ListView(ActivityEditNotification.this);
                if (MaxDaysAdapter == null) {
                    for (int i = 1; i <= 6; i++) {
                        maxList.add(new MaxList(String.valueOf(i)));
                    }
                    MaxDaysAdapter = new ArrayAdapter(ActivityEditNotification.this, android.R.layout.simple_list_item_1, maxList);
                }
                MaxDaylistView.setAdapter(MaxDaysAdapter);
                DiaMaxDay.setView(MaxDaylistView);
                DiaMaxDay.show();
                MaxDaylistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        DiaMaxDay.cancel();
                        String s = MaxDaylistView.getItemAtPosition(position).toString();
                        SpnMaxDays.setText(s);
                        MaxDays = s;
                    }
                });
            }
        });
        SpnSearchGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiaGift = (new AlertDialog.Builder(ActivityEditNotification.this)).create();
                DiaGift.setTitle("Gift");
                GiftlistView = new ListView(ActivityEditNotification.this);
                if (GiftAdapter == null) {
                    giftList.add(new GiftList(2, "Both"));
                    giftList.add(new GiftList(1, "Yes"));
                    giftList.add(new GiftList(0, "No"));
                    GiftAdapter = new ArrayAdapter(ActivityEditNotification.this, android.R.layout.simple_list_item_1, giftList);
                }

                GiftlistView.setAdapter(GiftAdapter);
                DiaGift.setView(GiftlistView);
                DiaGift.show();
                GiftlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        DiaGift.cancel();
                        String s = GiftlistView.getItemAtPosition(position).toString();
                        final GiftList model = giftList.get(position);
                        gift_id = model.Gift_id;
                        SpnSearchGift.setText(s);
                    }
                });
            }
        });

    }

    private class AddNotification extends AsyncTask<String, String, JSONObject> {
        String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "add_search_notification";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            String notification_id = args[0],
                    userid = args[1],
                    notificationtitle = args[2],
                    SpnSearchGift = args[3],
                    AirlineId = args[4],
                    Date = args[5],
                    SpnMinDays = args[6],
                    SpnMaxDays = args[7],
                    BaseAirportId = args[8];
            ArrayList parametarGetAirline = new ArrayList();
            parametarGetAirline.add(new BasicNameValuePair("notification_id", notification_id));
            parametarGetAirline.add(new BasicNameValuePair("user_id", userid));
            parametarGetAirline.add(new BasicNameValuePair("notification_title", notificationtitle));
            parametarGetAirline.add(new BasicNameValuePair("gift", SpnSearchGift));
            parametarGetAirline.add(new BasicNameValuePair("airline_id", AirlineId));
            parametarGetAirline.add(new BasicNameValuePair("date", Date));
            parametarGetAirline.add(new BasicNameValuePair("min_days", SpnMinDays));
            parametarGetAirline.add(new BasicNameValuePair("max_days", SpnMaxDays));
            parametarGetAirline.add(new BasicNameValuePair("source_airport_id", BaseAirportId));
            JSONObject json = jsonParser.makeHttpRequest(GetAirlineURL, "POST", parametarGetAirline);
            return json;
        }

        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {

                    int status_id = result.getInt("status_id");
                    String MSG = result.getString("status_msg");
                    if (status_id != 0) {
                        Toast.makeText(getApplicationContext(), MSG, Toast.LENGTH_LONG).show();
                        editor.putString("EditNotification", "Yes");
                        editor.commit();
                        Intent intent = new Intent(getApplicationContext(), ActivityMain.class);
                        intent.putExtra("EditNotification", "EditNotification");
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), MSG, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to retrieve any data from server", Toast.LENGTH_LONG).show();
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

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
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
                        Searchdate.setText(date_time);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }
}