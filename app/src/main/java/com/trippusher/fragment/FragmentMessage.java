package com.trippusher.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
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
import com.squareup.picasso.Picasso;
import com.trippusher.AppStatus;
import com.trippusher.R;
import com.trippusher.activity.ActivityChat;
import com.trippusher.vo.ConversationVo;
import com.trippusher.vo.MessageVo;

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
import java.util.Locale;
import java.util.Map;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

/**
 * Created by Desktop-KS on 8/1/2017.
 */

public class FragmentMessage extends Fragment {
    private RecyclerView rv_conversation;
    private ConversationAdapter adapter;
    //private List<Messageitemlist> Messageitemlist = new ArrayList<>();
    //private List<list1> list1 = new ArrayList<>();
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String AirlineId, BaseAirportId, UserId, MessageResult, password;
    private DatabaseReference myRefs;
    private FirebaseDatabase database;
    Map<String, String> myMap = new HashMap<>();
    String fcm_id;
    //static int count = 0;
    String values, LastMessage, Location, timestamp, IsRead, FromId, ToId;
    ArrayList<ConversationVo> conversationList = new ArrayList<>();

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
        rv_conversation = (RecyclerView) rootView.findViewById(R.id.RvMessagelst);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        rv_conversation.setHasFixedSize(true);
        rv_conversation.setLayoutManager(manager);
        adapter = new ConversationAdapter(conversationList);
        rv_conversation.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        database = FirebaseDatabase.getInstance();
        if (AppStatus.getInstance(getContext()).isOnline()) {
            myRefs = database.getReference("new_user").child(fcm_id).child("conversations");
            myRefs.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    conversationList.clear();
                    for (final DataSnapshot conversation : dataSnapshot.getChildren()) {
                        final ConversationVo conversationVo = new ConversationVo();
                        conversationVo.setKey(conversation.getKey());
                        conversationList.add(conversationVo);

                        Query lastQuery = myRefs.child(conversation.getKey()).child("messages").orderByKey().limitToLast(1);
                        lastQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot msg : dataSnapshot.getChildren()) {
                                    MessageVo data = new MessageVo();
                                    data.setcontent(msg.child("content").getValue().toString());
                                    data.setfromID(msg.child("fromID").getValue().toString());
                                    data.setisRead(Boolean.parseBoolean(msg.child("isRead").getValue().toString()));
                                    data.settimestamp(Integer.parseInt(msg.child("timestamp").getValue().toString()));
                                    data.settoID(msg.child("toID").getValue().toString());
                                    data.settype(msg.child("type").getValue().toString());
                                    conversationVo.setMessageVo(data);
                                    try {
                                        Collections.sort(conversationList, new Comparator<ConversationVo>() {
                                            @Override
                                            public int compare(final ConversationVo object1, final ConversationVo object2) {
                                                return object2.getMessageVo().compareTo(object1.getMessageVo());
                                            }
                                        });
                                    } catch (Exception e) {

                                    }

                                    adapter.notifyDataSetChanged();
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });
                    }

                    database.getReference("new_user").addValueEventListener(lastMsgListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (ConversationVo vo : conversationList) {
                                for (DataSnapshot user : dataSnapshot.getChildren()) {
                                    if (user.getKey().equalsIgnoreCase(vo.getKey())) {
                                        vo.setName(user.child("name").getValue().toString());
                                        vo.setEmail(user.child("email").getValue().toString());
                                        vo.setImage(user.child("profilePicLink").getValue().toString());
                                        break;
                                    }
                                }
                            }
                            //database.getReference("new_user").removeEventListener(lastMsgListener);
                            try {
                                Collections.sort(conversationList, new Comparator<ConversationVo>() {
                                    @Override
                                    public int compare(final ConversationVo object1, final ConversationVo object2) {
                                        return object2.getMessageVo().compareTo(object1.getMessageVo());
                                    }
                                });
                            } catch (Exception e) {

                            }
                            adapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("TAG", "Failed to read value.", error.toException());
                }
            });
        } else {
            Toast.makeText(getContext(), "Please check network connection and try again", Toast.LENGTH_SHORT).show();
        }
    }

    ValueEventListener lastMsgListener = null;


    public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.MyViewHolder> {
        private ArrayList<ConversationVo> mModelList;

        public ConversationAdapter(ArrayList<ConversationVo> modelList) {
            mModelList = modelList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_message, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder ViewHolder, final int position) {
            final ConversationVo model = mModelList.get(position);

            ViewHolder.Image.setImageResource(R.drawable.userc);
            if(model.getImage() != null && !model.getImage().isEmpty()) {
                Picasso.with(getContext()).load(model.getImage()).placeholder(R.drawable.userc).into(ViewHolder.Image);
            }
            ViewHolder.txtMsgName.setText(model.getName());
            MessageVo lastMessage = model.getMessageVo();
            if (lastMessage != null) {
                ViewHolder.txtMsgMessage.setText(lastMessage.getcontent());


                if (!lastMessage.getisRead()) {
                    if (!lastMessage.getfromID().equals(fcm_id)) {
                        ViewHolder.txtMsgName.setTypeface(null, Typeface.BOLD);
                        ViewHolder.txtMsgMessage.setTextColor(Color.BLUE);
                    }
                } else {
                    if (!lastMessage.getfromID().equals(fcm_id)) {
                        ViewHolder.txtMsgName.setTypeface(null, Typeface.NORMAL);
                        ViewHolder.txtMsgMessage.setTextColor(Color.BLACK);
                    }
                }
                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                Calendar c = Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(lastMessage.gettimestamp() + "") * 1000L);
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
            }

            ViewHolder.llrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), ActivityChat.class);
                    intent.putExtra("ReceiverFcmId", model.getKey());
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
                                    myRefs.child(model.getKey()).removeValue();
                                    mModelList.remove(position);
                                    adapter.notifyItemRemoved(position);
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