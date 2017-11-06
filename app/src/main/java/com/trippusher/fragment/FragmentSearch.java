package com.trippusher.fragment;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
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
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.trippusher.JSONParser;
import com.trippusher.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Desktop-KS on 7/26/2017.
 */

public class FragmentSearch extends Fragment {
    Fragment fragment;
    FragmentManager fragmentManager;
    TextView SpnSearchBase, SpnMaxDays, SpnMinDays, SpnSearchGift, Searchdate, UserAirline;
    Button btnSearch;
    Switch Gift;
    ArrayAdapter BaseAirAdapter, MinDaysAdapter, MaxDaysAdapter, GiftAdapter;
    String AirlineTitle, AirportCode, AirportId, resultBase, AirlineId, date, MinDays, MaxDays, notification_id;
    ListView MinDaylistView, MaxDaylistView, GiftlistView, BaseAirportlistView;
    AlertDialog DiaMaxDay, DiaBaseAirport, DiaMinDay, DiaGift;
    LinearLayout layMinDays, layMaxDays;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private List<AirportLists> AirportList = new ArrayList<>();
    private List<MinsList> MinsList = new ArrayList<>();
    private List<MaxList> MaxList = new ArrayList<>();
    private List<GiftList> GiftList = new ArrayList<>();
    int gift_id = 2;
    JSONParser jsonParser = new JSONParser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = prefs.edit();
        resultBase = prefs.getString("resultBase", null);
        AirlineId = prefs.getString("airlineId", null);
        AirlineTitle = prefs.getString("airlineTitle", null);
        AirportId = prefs.getString("AirportId", null);
        AirportCode = prefs.getString("AirportCode", null);
        notification_id = prefs.getString("notification_id", null);
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        UserAirline = (TextView) rootView.findViewById(R.id.txtSearchAirlineId);
        SpnSearchBase = (TextView) rootView.findViewById(R.id.SpnSearchBase);
        SpnSearchGift = (TextView) rootView.findViewById(R.id.SpnSearchGift);
        SpnMinDays = (TextView) rootView.findViewById(R.id.SpnMinDays);
        SpnMaxDays = (TextView) rootView.findViewById(R.id.SpnMaxDays);
        layMinDays = (LinearLayout) rootView.findViewById(R.id.layMinDays);
        layMaxDays = (LinearLayout) rootView.findViewById(R.id.layMaxDays);
        Gift = (Switch) rootView.findViewById(R.id.swtGift);
        Searchdate = (TextView) rootView.findViewById(R.id.btnSearchdate);
        btnSearch = (Button) rootView.findViewById(R.id.btnSearch);
        SpnSearchBase.setText(AirportCode);
        UserAirline.setText(AirlineTitle);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (SpnSearchBase.getText().toString().matches("Select Base")) {
                    Toast.makeText(getContext(), "Please Select Base", Toast.LENGTH_SHORT).show();
                } else {
                    if (Searchdate.getText().toString().matches("Start Date")) {
                        Toast.makeText(getContext(), "Please Select Date", Toast.LENGTH_SHORT).show();
                    } else {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        date = format.format(Date.parse(Searchdate.getText().toString()));
                        if (SpnMinDays.getText().toString().matches("Min Days")) {
                            Toast.makeText(getContext(), "Please Select Min Days", Toast.LENGTH_SHORT).show();
                        } else {
                            if (SpnMaxDays.getText().toString().matches("Max Days")) {
                                Toast.makeText(getContext(), "Please Select Max Days", Toast.LENGTH_SHORT).show();
                            } else {
                                fragment = new FragmentSearchTrip();
                                Bundle bundle = new Bundle();
                                bundle.putString("SpnSearchBase", AirportId);
                                bundle.putString("Date", date);
                                bundle.putString("SpnMinDays", SpnMinDays.getText().toString());
                                bundle.putString("SpnMaxDays", SpnMaxDays.getText().toString());
                                bundle.putString("SpnSearchGift", String.valueOf(gift_id));
                                bundle.putString("NotificationTrip", null);
                                fragment.setArguments(bundle);
                                fragmentManager = getActivity().getSupportFragmentManager();
                                fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
                            }
                        }
                    }
                }
            }
        });

        SpnSearchBase.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    DiaBaseAirport = new AlertDialog.Builder(getContext()).create();
                    DiaBaseAirport.setTitle("Select Airport");
                    BaseAirportlistView = new ListView(getContext());
                    if (BaseAirAdapter == null) {
                        JSONArray array2 = new JSONArray(resultBase);
                        for (int i = 0; i < array2.length(); i++) {
                            AirportList.add(new AirportLists(
                                    array2.getJSONObject(i).getString("pk_airport_id"),
                                    array2.getJSONObject(i).getString("airport_code")));
                        }
                        BaseAirAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, AirportList);
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
                DiaMinDay = (new AlertDialog.Builder(getContext())).create();
                DiaMinDay.setTitle("Minimum Days");
                MinDaylistView = new ListView(getContext());
                if (MinDaysAdapter == null) {
                    for (int i = 1; i <= 6; i++) {
                        MinsList.add(new MinsList(String.valueOf(i)));
                    }
                    MinDaysAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, MinsList);
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

                DiaMaxDay = (new AlertDialog.Builder(getContext())).create();
                DiaMaxDay.setTitle("Maximum Days");
                MaxDaylistView = new ListView(getContext());
                if (MaxDaysAdapter == null) {
                    for (int i = 1; i <= 6; i++) {
                        MaxList.add(new MaxList(String.valueOf(i)));
                    }
                    MaxDaysAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, MaxList);
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
                DiaGift = (new AlertDialog.Builder(getContext())).create();
                DiaGift.setTitle("Gift");
                GiftlistView = new ListView(getContext());
                if (GiftAdapter == null) {
                    GiftList.add(new GiftList(2, "Both"));
                    GiftList.add(new GiftList(1, "Yes"));
                    GiftList.add(new GiftList(0, "No"));
                    GiftAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, GiftList);
                }

                GiftlistView.setAdapter(GiftAdapter);
                DiaGift.setView(GiftlistView);
                DiaGift.show();
                GiftlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        DiaGift.cancel();
                        String s = GiftlistView.getItemAtPosition(position).toString();
                        final GiftList model = GiftList.get(position);
                        gift_id = model.Gift_id;
                        SpnSearchGift.setText(s);
                    }
                });
            }
        });
        return rootView;
    }

    public class GiftList {
        public int Gift_id;
        public String Gift;

        public GiftList(int Gift_id, String Gift) {
            this.Gift_id = Gift_id;
            this.Gift = Gift;
        }

        @Override
        public String toString() {
            return this.Gift;
        }
    }

    public class MinsList {
        public String MinsDays;

        public MinsList(String MinsDays) {
            this.MinsDays = MinsDays;
        }

        @Override
        public String toString() {
            return this.MinsDays;
        }
    }

    public class MaxList {
        public String MaxDays;

        public MaxList(String MaxDays) {
            this.MaxDays = MaxDays;
        }

        @Override
        public String toString() {
            return this.MaxDays;
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
                        Searchdate.setText(date_time);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    public class AirportLists {
        private String pk_airport_id;
        private String airport_code;

        public AirportLists(String pk_airport_id, String airport_code) {
            this.pk_airport_id = pk_airport_id;
            this.airport_code = airport_code;
        }

        @Override
        public String toString() {
            return this.airport_code;
        }
    }
}