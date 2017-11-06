package com.trippusher;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Desktop-KS on 7/22/2017.
 */

public class Model {
    public static class PhoneNumberFormattingTextWatcher implements TextWatcher {
        EditText Edt;
        int keyDel = 0;

        public PhoneNumberFormattingTextWatcher(EditText edt) {
            Edt = edt;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            Edt.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        keyDel = 1;
                    } else {
                        keyDel = 0;
                    }
                    return false;
                }
            });
            if (keyDel == 0) {
                int len = Edt.getText().length();
                if (len == 3 || len == 8) {
                    if (len < 4) {
                        Edt.setText("(" + Edt.getText().toString() + ")");
                        Edt.setSelection(Edt.getText().length());
                    } else {
                        Edt.setText(Edt.getText().toString() + "-");
                        Edt.setSelection(Edt.getText().length());
                    }
                }
            } else {
                keyDel = 0;
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    /*public class TripItemList {
        public String base_airport;
        public String airline_title;
        public String image;
        public String start_date;
        public String hours;
        public String gift;
        public String post_trip_id;

        public TripItemList(String base_airport, String airline_title, String image,
                            String start_date, String hours, String gift, String post_trip_id) {
            this.base_airport = base_airport;
            this.airline_title = airline_title;
            this.image = image;
            this.start_date = start_date;
            this.hours = hours;
            this.gift = gift;
            this.post_trip_id = post_trip_id;
        }
    }*/
}

