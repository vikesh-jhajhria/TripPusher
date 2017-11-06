package com.trippusher.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.trippusher.AppStatus;
import com.trippusher.R;
import com.trippusher.activity.ActivityChat;

import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

/**
 * Created by Desktop-KS on 8/1/2017.
 */

public class FragmentMessage extends Fragment {
    private RecyclerView RvMessagelst;
    private AdapterMessage Adapter;
    private List<Messageitemlist> Messageitemlist = new ArrayList<>();
    private List<list1> list1 = new ArrayList<>();
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String AirlineId, BaseAirportId, UserId, MessageResult, password;
    private DatabaseReference myRefs, usersRefs, conversationsRefs;
    private FirebaseDatabase database;
    Map<String, String> myMap = new HashMap<>();
    String fcm_id;
    //static int count = 0;
    String values, LastMessage, Location, timestamp, IsRead, FromId, ToId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = prefs.edit();
        AirlineId = prefs.getString("airlineId", null);
        BaseAirportId = prefs.getString("AirportId", null);
        UserId = prefs.getString("userId", null);
        MessageResult = prefs.getString("MessageResult", null);
        fcm_id = prefs.getString("fcm_id", null);
        password = prefs.getString("password", null);
        View rootView = inflater.inflate(R.layout.fragment_message, container, false);
        RvMessagelst = (RecyclerView) rootView.findViewById(R.id.RvMessagelst);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        RvMessagelst.setHasFixedSize(true);
        RvMessagelst.setLayoutManager(manager);
        Adapter = new AdapterMessage(Messageitemlist);
        RvMessagelst.setAdapter(Adapter);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        database = FirebaseDatabase.getInstance();
        if (AppStatus.getInstance(getContext()).isOnline()) {
            usersRefs = database.getReference("users");
            conversationsRefs = database.getReference("conversations");
            myRefs = database.getReference("users").child(fcm_id).child("conversations");
            myRefs.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot items : dataSnapshot.getChildren()) {
                        String key = items.getKey().toString();
                        String values = items.child("location").getValue().toString();
                        list1.add(new list1(
                                key,
                                values));
                        myMap.put(key, values);
                    }
                    for (Map.Entry<String, String> entry : myMap.entrySet()) {
                        String Sendeerids = entry.getKey();
                        values = entry.getValue();
                        Query lastQuery = conversationsRefs.child(values).orderByKey().limitToLast(1);
                        lastQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot item : dataSnapshot.getChildren()) {
                                    try {
                                        Location = dataSnapshot.getKey();
                                        LastMessage = item.child("content").getValue().toString();
                                        FromId = item.child("fromID").getValue().toString();
                                        IsRead = item.child("isRead").getValue() == null ? "false": item.child("isRead").getValue().toString();
                                        timestamp = item.child("timestamp").getValue().toString();
                                        ToId = item.child("toID").getValue().toString();
                                        for (list1 items : list1) {
                                            if (Location.equals(items.Location)) {
                                                for (Messageitemlist listItem : Messageitemlist) {
                                                    if (listItem.SendersFcmId.equalsIgnoreCase(items.SenderUID)) {
                                                        Messageitemlist.remove(listItem);
                                                        Adapter.notifyDataSetChanged();
                                                        break;
                                                    }
                                                }
                                                Messageitemlist.add(new Messageitemlist(
                                                        items.SenderUID,
                                                        items.Location,
                                                        LastMessage,
                                                        FromId,
                                                        IsRead,
                                                        timestamp,
                                                        ToId));
                                            }
                                        }
                                        Collections.sort(Messageitemlist, new Comparator<Messageitemlist>() {
                                            @Override
                                            public int compare(final Messageitemlist object1, final Messageitemlist object2) {
                                                return object2.Time.compareTo(object1.Time);
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                                Adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Log.w("TAG", "Failed to read value.", error.toException());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("TAG", "Failed to read value.", error.toException());
                }
            });
        } else {
            Toast.makeText(getContext(), "Please check network connection and try again", Toast.LENGTH_SHORT).show();
        }
        Adapter.notifyDataSetChanged();
    }

    public class list1 {
        private String SenderUID;
        private String Location;

        public list1(String Senderuid, String locations) {
            this.SenderUID = Senderuid;
            this.Location = locations;
        }
    }

    public class Messageitemlist implements Comparable<Messageitemlist> {
        public String SendersFcmId;
        public String Location;
        public String LastMessages;
        public String ChatFromId;
        public String IssRead;
        public String Time;
        public String ChatToId;

        public Messageitemlist(String senderFcmId, String location, String lastMessage, String chatFromId, String isRead, String time, String chatToId) {
            this.SendersFcmId = senderFcmId;
            this.Location = location;
            this.LastMessages = lastMessage;
            this.ChatFromId = chatFromId;
            this.IssRead = isRead;
            this.Time = time;
            this.ChatToId = chatToId;
        }

        @Override
        public final int compareTo(Messageitemlist f) {
            return this.Time.compareTo(f.Time);
        }
    }

    public class AdapterMessage extends RecyclerView.Adapter<AdapterMessage.MyViewHolder> {
        private List<FragmentMessage.Messageitemlist> mModelList;

        public AdapterMessage(List<FragmentMessage.Messageitemlist> modelList) {
            mModelList = modelList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_message, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder ViewHolder, final int position) {
            final FragmentMessage.Messageitemlist model = mModelList.get(position);

            //ViewHolder.Image.setImageResource(R.drawable.userc);

            usersRefs.child(model.SendersFcmId).child("credentials").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        Map map1 = (Map) dataSnapshot.getValue();
                        if (map1 != null) {
                            String SenderName = (String) map1.get("name");
                            ViewHolder.txtMsgName.setText(SenderName);
                            String profilePicLink = (String) map1.get("profilePicLink");
                            new AsyncTask<String, String, Bitmap>() {
                                @Override
                                protected Bitmap doInBackground(String... args) {
                                    try {
                                        String pic = args[0];
                                        InputStream in = new URL(pic).openStream();
                                        Bitmap b = BitmapFactory.decodeStream(in);
                                        return b;
                                    } catch (Exception e) {
                                        // log error
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Bitmap result) {
                                    if ( result!= null) {
                                        ViewHolder.Image.setDrawingCacheEnabled(true);
                                        ViewHolder.Image.setImageBitmap(result);
                                    }
                                }
                            }.execute(profilePicLink);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("TAG", "Failed to read value.", error.toException());
                }
            });
            if (model.IssRead.equals("false")) {
                if (!model.ChatFromId.equals(fcm_id)) {
                    ViewHolder.txtMsgName.setTypeface(null, Typeface.BOLD);
                    ViewHolder.txtMsgMessage.setTextColor(Color.BLUE);
                    conversationsRefs.child(model.Location).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int count = 0;
                            for (DataSnapshot item : dataSnapshot.getChildren()) {
                                try {
                                    IsRead = item.child("isRead").getValue().toString();
                                    if (IsRead.equals("false")) {
                                        count++;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            String name = ViewHolder.txtMsgName.getText().toString();
                            name = name.replaceAll("\\(.*?\\) ?", "");
                            ViewHolder.txtMsgName.setText(Html.fromHtml(name + " <font color='#0000FF'>" + "(" + count + ")" + " </font>"));
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.w("TAG", "Failed to read value.", error.toException());
                        }
                    });
                }
            } else {
                if (!model.ChatFromId.equals(fcm_id)) {
                    ViewHolder.txtMsgName.setTypeface(null, Typeface.NORMAL);
                    ViewHolder.txtMsgMessage.setTextColor(Color.BLACK);
                }
            }
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            Calendar c = Calendar.getInstance();
            cal.setTimeInMillis(Long.parseLong(model.Time) * 1000L);
            String date = DateFormat.format("dd-MM-yyyy hh:mm aa", cal).toString();
            String TxtTime1 = DateFormat.format("MM/dd/yyyy hh:mm aa", cal).toString();
            String TxtTime2 = DateFormat.format("hh:mm aa", cal).toString();
            Date currentDate = new Date();
            c.setTime(currentDate);
            String date22 = DateFormat.format("dd-MM-yyyy", c).toString();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date date1 = sdf.parse(date22);
                Date date2 = sdf.parse(date);
                if (date1.after(date2)) {
                    ViewHolder.TxtTime.setText(TxtTime1);
                }
                if (date1.before(date2)) {
                    System.out.println("Date1 is before Date2");
                }
                if (date1.equals(date2)) {
                    ViewHolder.TxtTime.setText(TxtTime2);
                }
            } catch (ParseException ex) {
                ex.printStackTrace();
            }

            ViewHolder.txtMsgMessage.setText(model.LastMessages);
            ViewHolder.llrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), ActivityChat.class);
                    intent.putExtra("ChatLocation",model.Location);
                    intent.putExtra("ReceiverFcmId",model.SendersFcmId);
                    intent.putExtra("BitmapImage", ViewHolder.Image.getDrawingCache());
                    getContext().startActivity(intent);

                }
            });
            ViewHolder.DeleteAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                    UserId = prefs.getString("userId", null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setMessage("Are you sure you want to delete Message ?");
                    builder.setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    conversationsRefs.child(model.Location).removeValue();
                                    mModelList.remove(position);
                                    Adapter.notifyItemRemoved(position);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    TextView title = new TextView(view.getContext());
                    title.setText("Warning");
                    title.setPadding(10, 10, 10, 10);
                    title.setGravity(Gravity.CENTER);
                    title.setTextColor(Color.BLACK);
                    alert.setCustomTitle(title);
                    alert.show();
                }
            });
        }


        @Override
        public int getItemCount() {
            return mModelList == null ? 0 : mModelList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            private View view;
            public LinearLayout llrow;
            public TextView txtMsgName;
            public TextView TxtTime;
            public ImageView Image;
            public RelativeLayout Relaymsgrow;
            public EmojiconTextView txtMsgMessage;
            public ImageView DeleteAll;

            private MyViewHolder(View itemView) {
                super(itemView);
                view = itemView;
                Relaymsgrow = (RelativeLayout) itemView.findViewById(R.id.Relaymsgrow);
                llrow = (LinearLayout) itemView.findViewById(R.id.laymsgrow);
                txtMsgName = (TextView) itemView.findViewById(R.id.txtSenderName);
                TxtTime = (TextView) itemView.findViewById(R.id.TxtTime);
                Image = (ImageView) itemView.findViewById(R.id.userimg);
                txtMsgMessage = (EmojiconTextView) itemView.findViewById(R.id.txtMsgMessage);
                DeleteAll = (ImageView) itemView.findViewById(R.id.imgdelete);
            }
        }
    }
}