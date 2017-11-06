package com.trippusher.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trippusher.AppStatus;
import com.trippusher.JSONParser;
import com.trippusher.R;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

/**
 * Created by Desktop-KS on 8/11/2017.
 */

public class ActivityChat extends AppCompatActivity implements ValueEventListener {
    ImageView Back, deletemsg, repaly, imgoverflow;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    TextView txtNoMessage;
    private RecyclerView mRecyclerView;
    private MyChatAdapter Adapter;
    private List<ChatList> chatlist = new ArrayList<>();
    private EmojiconEditText emojEditText;
    private RelativeLayout RootView;
    private ImageView EmojKeyBord;
    JSONParser jsonParser = new JSONParser();
    private DatabaseReference myRef2;
    private FirebaseDatabase database;
    String message, ReceiverFcmId, ChatLocation, fcm_id;
    private ArrayList<String> arrayList = new ArrayList<String>();
    int mtype = 0;
    int i = 0;
    Bitmap SenderPic;
    List<String> tempDeleteItemList;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tempDeleteItemList = new ArrayList<>();
        setContentView(R.layout.activity_chat);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        fcm_id = prefs.getString("fcm_id", null);
        Back = (ImageView) findViewById(R.id.imgBack);
        deletemsg = (ImageView) findViewById(R.id.imgdelete);
        imgoverflow = (ImageView) findViewById(R.id.imgoverflow);
        mRecyclerView = (RecyclerView) findViewById(R.id.RvMessage);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        repaly = (ImageView) findViewById(R.id.imgrepaly);
        EmojKeyBord = (ImageView) findViewById(R.id.imgEmojKey);
        emojEditText = (EmojiconEditText) findViewById(R.id.txtEmojrepaly);
        txtNoMessage = (TextView) findViewById(R.id.txtNoMessage);
        RootView = (RelativeLayout) findViewById(R.id.RootView);
        EmojIconActions emojIcon = new EmojIconActions(this, RootView, emojEditText, EmojKeyBord);
        emojIcon.ShowEmojIcon();
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("TAG", "keyboard opened");
            }

            @Override
            public void onKeyboardClose() {
                Log.e("TAG", "Keyboard closed");
            }
        });
        Back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editor.putString("ChatBack", "ChatBack");
                editor.commit();
                Intent activity2 = new Intent(ActivityChat.this, ActivityMain.class);
                startActivity(activity2);
                finish();
            }
        });

        if (AppStatus.getInstance(ActivityChat.this).isOnline()) {
            database = FirebaseDatabase.getInstance();
            Bundle bundle = getIntent().getExtras();
            SenderPic = bundle.getParcelable("BitmapImage");
            ChatLocation = bundle.getString("ChatLocation");
            ReceiverFcmId = bundle.getString("ReceiverFcmId");
            if (SenderPic != null) {
                myRef2 = database.getReference("conversations").child(ChatLocation);
                myRef2.addValueEventListener(this);
            } else {
                database.getReference("users").child(ReceiverFcmId).child("credentials").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            Map map1 = (Map) dataSnapshot.getValue();
                            if (map1 != null) {
                                String profilePicLink = (String) map1.get("profilePicLink");
                                new AsyncTask<String, String, Void>() {
                                    Bitmap b;

                                    @Override
                                    protected Void doInBackground(String... args) {
                                        try {
                                            String pic = args[0];
                                            InputStream in = new URL(pic).openStream();
                                            b = BitmapFactory.decodeStream(in);
                                        } catch (Exception e) {
                                            // log error
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void result) {
                                        if (b != null) {
                                            SenderPic = b;
                                            myRef2 = database.getReference("conversations").child(ChatLocation);
                                            myRef2.addValueEventListener(ActivityChat.this);
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
            }
        } else {
            Toast.makeText(ActivityChat.this, "Please check network connection and try again", Toast.LENGTH_SHORT).show();
        }
        deletemsg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (AppStatus.getInstance(ActivityChat.this).isOnline()) {
                    if (arrayList.size() > 0) {
                        tempDeleteItemList = new ArrayList<String>();
                        tempDeleteItemList = arrayList;
                        myRef2.removeEventListener(ActivityChat.this);
                        for (int j = 0; j < arrayList.size(); j++) {
                            database.getReference("conversations").child(ChatLocation).child(arrayList.get(j)).removeValue();
                        }
                        myRef2.addValueEventListener(ActivityChat.this);
                        deletemsg.setVisibility(View.GONE);
                        imgoverflow.setVisibility(View.VISIBLE);
                        i = 0;
                    }
                } else {
                    Toast.makeText(ActivityChat.this, "Please check network connection and try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
        imgoverflow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(ActivityChat.this, deletemsg);
                menu.inflate(R.menu.popup);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        imgoverflow.setVisibility(View.GONE);
                        deletemsg.setVisibility(View.VISIBLE);
                        i = 1;
                        Adapter.notifyDataSetChanged();
                        return false;
                    }
                });
                MenuPopupHelper menuHelper = new MenuPopupHelper(ActivityChat.this, (MenuBuilder) menu.getMenu(), deletemsg);
                menuHelper.setForceShowIcon(true);
                menuHelper.show();
            }
        });

        repaly.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!emojEditText.getText().toString().matches("")) {
                    Boolean boolean1 = Boolean.valueOf("false");
                    int time = (int) (System.currentTimeMillis() / 1000);
                    ChatData data = new ChatData();
                    //ChatList data = new ChatList();
                    data.setcontent(emojEditText.getText().toString());
                    data.setfromID(fcm_id);
                    data.setisRead(boolean1);
                    data.settimestamp(time);
                    data.settoID(ReceiverFcmId);
                    data.settype("text");
                    myRef2.child(myRef2.push().getKey()).setValue(data);
                    PushNotification pushNotification = new PushNotification();
                    pushNotification.execute(emojEditText.getText().toString());
                    //pushNotification.execute(StringEscapeUtils.escapeJava(emojEditText.getText().toString()));
                    //Log.d("emoji",StringEscapeUtils.escapeJava(emojEditText.getText().toString()));
                    emojEditText.setText("");
                }
            }
        });
    }

    private class PushNotification extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            String TextMessage = args[0];
            ArrayList ParametarPushNotification = new ArrayList();
            ParametarPushNotification.add(new BasicNameValuePair("message", TextMessage));
            ParametarPushNotification.add(new BasicNameValuePair("sender_id", fcm_id));
            ParametarPushNotification.add(new BasicNameValuePair("receiver_id", ReceiverFcmId));
            ParametarPushNotification.add(new BasicNameValuePair("location", ChatLocation));
            JSONObject json = jsonParser.makeHttpRequest(AppStatus.getbaseurl().baseurl() + "send_message", "POST", ParametarPushNotification);
            return json;
        }

        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {
                    int status_id = result.getInt("status_id");
                    String MSG = result.getString("status_msg");
                    if (status_id != 0) {
                        Log.d("MSG", MSG);
                    } else {
                        Log.d("MSG", MSG);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        chatlist.clear();
        //Log.v("onDataChange", "onDataChange===");
        for (DataSnapshot item : dataSnapshot.getChildren()) {
            if (item.child("toID").getValue() != null) {
                String value6 = item.child("toID").getValue().toString();
                //Log.v("ID", value6 + "    ==     " + UID);
                if (value6.equals(fcm_id)) {
                    //Log.v("Content", item.child("content").getValue().toString());
                    boolean isItemDeleted = false;
                    for (String str : tempDeleteItemList) {
                        if (item.getKey().equalsIgnoreCase(str)) {
                            isItemDeleted = true;
                            break;
                        }
                    }
                    if (!isItemDeleted && item.child("isRead").getValue().toString().equalsIgnoreCase("false")) {
                        Boolean boolean2 = Boolean.valueOf("true");
                        myRef2.child(item.getKey()).child("isRead").setValue(boolean2);
                    }
                }
                if (item.child("type").getValue().toString().equals("location")) {
                    mtype = 3;
                    if(!item.child("fromID").getValue().toString().equals(fcm_id)){
                        chatlist.add(new ChatList(mtype,
                                item.child("content").getValue().toString(),
                                item.getKey(),
                                Integer.parseInt(item.child("timestamp").getValue().toString()),
                                item.child("toID").getValue().toString(),
                                SenderPic));
                    }
                } else if (item.child("toID").getValue().toString().equals(fcm_id)) {
                    mtype = 1;
                    chatlist.add(new ChatList(mtype,
                            item.child("content").getValue().toString(),
                            item.getKey(),
                            Integer.parseInt(item.child("timestamp").getValue().toString()),
                            item.child("toID").getValue().toString(),
                            SenderPic));
                } else {
                    mtype = 2;
                    chatlist.add(new ChatList(mtype,
                            item.child("content").getValue().toString(),
                            item.getKey(),
                            Integer.parseInt(item.child("timestamp").getValue().toString()),
                            item.child("toID").getValue().toString(),
                            SenderPic));
                }

            }
        }
        Adapter = new MyChatAdapter(ActivityChat.this, chatlist);
        mRecyclerView.setAdapter(Adapter);
    }

    @Override
    public void onCancelled(DatabaseError error) {
        Log.w("TAG", "Failed to read value.", error.toException());
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finishAffinity();
            //System.exit(0);
            return;
        } else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                    //finish();
                }
            }, 2000);
        }


    }

    public static class ChatData {
        private String content;
        private String fromID;
        private Boolean isRead;
        private int timestamp;
        private String toID;
        private String type;

        public ChatData() {
            // empty constructor
        }

        public String getcontent() {
            return content;
        }

        public void setcontent(String contents) {
            content = contents;
        }

        public String getfromID() {
            return fromID;
        }

        public void setfromID(String fromid) {
            fromID = fromid;
        }

        public Boolean getisRead() {
            return isRead;
        }

        public void setisRead(Boolean isread) {
            isRead = isread;
        }

        public int gettimestamp() {
            return timestamp;
        }

        public void settimestamp(int timestamps) {
            timestamp = timestamps;
        }

        public String gettoID() {
            return toID;
        }

        public void settoID(String toid) {
            toID = toid;
        }

        public String gettype() {
            return type;
        }

        public void settype(String types) {
            type = types;
        }
    }

    public class ChatList {
        private int mType;
        private String mMessage;
        private String msg_id;
        private String To_id;
        private int times;
        private boolean isSelected = false;
        private Bitmap senderPic;

        public String getMessage() {
            return mMessage;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setMessage(String mMessage) {
            this.mMessage = mMessage;
        }
        public ChatList(int mType, String mMessage, String msg_id, int times, String to_id, Bitmap img) {
            this.mType = mType;
            this.mMessage = mMessage;
            this.msg_id = msg_id;
            this.times = times;
            this.To_id = to_id;
            this.senderPic = img;

    }}

    public class MyChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<ChatList> list;
        Context context;

        public MyChatAdapter(Context context, List<ChatList> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getItemViewType(int position) {
            return list.get(position).mType;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 1) {
                View view = LayoutInflater.from(context).inflate(R.layout.items_left, parent, false);
                return new ViewHolderLeft(view);
            }
            if (viewType == 2) {
                View view = LayoutInflater.from(context).inflate(R.layout.items_right, parent, false);
                return new ViewHolderRight(view);
            }
            if (viewType == 3) {
                View view = LayoutInflater.from(context).inflate(R.layout.items_center, parent, false);
                return new ViewHolderCenter(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            final ChatList model = list.get(position);
                /*Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                Calendar c = Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(model.times) * 1000L);
                String date = DateFormat.format("dd-MM-yyyy hh:mm aa", cal).toString();
                String TxtTime1 = DateFormat.format("MM/dd/yyyy hh:mm aa", cal).toString();
                String TxtTime2 = DateFormat.format("hh:mm aa", cal).toString();
                Date currentDate = new Date();
                c.setTime(currentDate);
                String date22 = DateFormat.format("dd-MM-yyyy", c).toString();*/

            if (holder instanceof MyChatAdapter.ViewHolderLeft) {
                ((ViewHolderLeft) holder).msg.setText(StringEscapeUtils.unescapeJava(model.mMessage));
                ((ViewHolderLeft) holder).senderPic.setImageBitmap(model.senderPic);
                /*try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    Date date1 = sdf.parse(date22);
                    Date date2 = sdf.parse(date);
                    ((ViewHolderLeft) holder).msg.setText(Html.fromHtml(((ViewHolderLeft) holder).msg.getText()+"   "+"  <small><sub><small>"+TxtTime2+"</small></small></sub></small>"));

                } catch (ParseException ex) {
                    ex.printStackTrace();
                }*/
                if (i == 1) {
                    ((ViewHolderLeft) holder).chk.setVisibility(View.VISIBLE);
                    ((ViewHolderLeft) holder).chk.setChecked(model.isSelected());
                    ((ViewHolderLeft) holder).chk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            model.setSelected(!model.isSelected());
                            ((ViewHolderLeft) holder).chk.setChecked(model.isSelected());
                            if (model.isSelected()) {
                                arrayList.add(model.msg_id);
                            } else {
                                arrayList.remove(model.msg_id);
                            }
                        }
                    });
                }
            } else if (holder instanceof MyChatAdapter.ViewHolderRight) {
                ((ViewHolderRight) holder).msg.setText(StringEscapeUtils.unescapeJava(list.get(position).mMessage));
                /*try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    Date date1 = sdf.parse(date22);
                    Date date2 = sdf.parse(date);
                    ((ViewHolderRight) holder).msg.setText(Html.fromHtml(((ViewHolderRight) holder).msg.getText()+"   "+"  <small><sub><small>"+TxtTime2+"</small></small></sub></small>"));

                } catch (ParseException ex) {
                    ex.printStackTrace();
                }*/
                if (i == 1) {
                    ((ViewHolderRight) holder).chk.setVisibility(View.VISIBLE);
                    ((ViewHolderRight) holder).chk.setChecked(model.isSelected());
                    ((ViewHolderRight) holder).chk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            model.setSelected(!model.isSelected());
                            ((ViewHolderRight) holder).chk.setChecked(model.isSelected());
                            if (model.isSelected()) {
                                arrayList.add(model.msg_id);
                            } else {
                                arrayList.remove(model.msg_id);
                            }
                        }
                    });
                }
            } else if (holder instanceof MyChatAdapter.ViewHolderCenter) {
                ((ViewHolderCenter) holder).txtTripIds.setText(list.get(position).mMessage);
                if (i == 1) {
                    ((ViewHolderCenter) holder).chk.setVisibility(View.VISIBLE);
                    ((ViewHolderCenter) holder).chk.setChecked(model.isSelected());
                    ((ViewHolderCenter) holder).chk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            model.setSelected(!model.isSelected());
                            ((ViewHolderCenter) holder).chk.setChecked(model.isSelected());
                            if (model.isSelected()) {
                                arrayList.add(model.msg_id);
                            } else {
                                arrayList.remove(model.msg_id);
                            }
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolderLeft extends RecyclerView.ViewHolder {
            private View view;
            public EmojiconTextView msg;
            public ImageView senderPic;
            public LinearLayout row;
            public CheckBox chk;

            private ViewHolderLeft(View itemView) {
                super(itemView);
                view = itemView;
                row = (LinearLayout) itemView.findViewById(R.id.layAllMsgrow);
                msg = (EmojiconTextView) itemView.findViewById(R.id.txtLeft);
                senderPic = (ImageView) itemView.findViewById(R.id.imgSenderPic);
                chk = (CheckBox) itemView.findViewById(R.id.chk);
            }
        }

        class ViewHolderRight extends RecyclerView.ViewHolder {
            private View view;
            public EmojiconTextView msg;
            public CheckBox chk;
            public LinearLayout row;

            public ViewHolderRight(View itemView) {
                super(itemView);
                view = itemView;
                row = (LinearLayout) itemView.findViewById(R.id.layAllMsgrow);
                msg = (EmojiconTextView) itemView.findViewById(R.id.txtright);
                chk = (CheckBox) itemView.findViewById(R.id.chk);
            }
        }

        class ViewHolderCenter extends RecyclerView.ViewHolder {
            private View view;
            public TextView txtTripIds;
            public CheckBox chk;

            public ViewHolderCenter(View itemView) {
                super(itemView);
                view = itemView;
                txtTripIds = (TextView) itemView.findViewById(R.id.txtTripIds);
                chk = (CheckBox) itemView.findViewById(R.id.chk);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myRef2.removeEventListener(this);
    }
}
