package com.evacupet.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evacupet.R;
import com.evacupet.adapter.ChatAdapter;
import com.evacupet.utility.BaseUtility;
import com.evacupet.utility.ConnectionUtil;
import com.evacupet.utility.Constant;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends DashboardActivity implements View.OnClickListener {
    private static final String TAG = ChatActivity.class.getSimpleName();
    @BindView(R.id.rv_chat_list)
    RecyclerView rvChatList;
    @BindView(R.id.et_chat_message)
    EditText etChatMessage;
    @BindView(R.id.iv_send_message)
    ImageView ivSendMessage;
    private boolean wasInBackground = true;
    private Handler mHandler;
    private Runnable runnable;
    private ChatAdapter chatAdapter;
    private String evacuatedById;
    private ArrayList<ParseObject> allChatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content);
        getLayoutInflater().inflate(R.layout.activity_chat_new, contentFrameLayout);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        setChatAdapter();
        ivSendMessage.setOnClickListener(this);
        if (getIntent().hasExtra(Constant.TRACk_ANIMAL)) {
            final ParseObject object = getIntent().getParcelableExtra(Constant.TRACk_ANIMAL);
            if (object.getParseObject("Owner").getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                if (object.has("EvacuatedBy")) {
                    evacuatedById = object.getParseObject("EvacuatedBy").getObjectId();
                }
            } else {
                evacuatedById = object.getParseObject("Owner").getObjectId();
            }

            if (ConnectionUtil.isInternetOn(this)) {
                getAllChat();
                refreshList();
            } else {
                Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }

            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.getInBackground(evacuatedById, new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser object, ParseException e) {
                    if (e == null) {
                        titleName.setText(object.getString("FirstName"));
                    } else {
                        Log.e(TAG, e.getMessage());
                    }
                }
            });
        }
    }

    @Override
    public void onImageSuccess(Intent data) {

    }

    private void refreshList() {
        mHandler = new Handler();
        final int delay = 5000; //milliseconds

        mHandler.postDelayed(new Runnable() {
            public void run() {
                if (wasInBackground) {
                    getAllChat();
                    mHandler.postDelayed(this, delay);
                }
            }
        }, delay);
    }

    private void setChatAdapter() {
        chatAdapter = new ChatAdapter(this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        rvChatList.setLayoutManager(manager);
        rvChatList.setAdapter(chatAdapter);

    }


    @Override
    public void onPause() {
        super.onPause();
        wasInBackground = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        wasInBackground = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_send_message:
                String msg = etChatMessage.getText().toString();
                if (!TextUtils.isEmpty(msg)) {
                    if (ConnectionUtil.isInternetOn(this)) {
                        sendMessage();

                    } else {
                        Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                    }
                }
        }
    }

    private void getAllChat() {
        allChatList = new ArrayList<>();
        List<String> fromUser = new ArrayList<String>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("chat");
        fromUser.add(ParseUser.getCurrentUser().getObjectId());
        fromUser.add(evacuatedById);

        query.whereContainedIn("from_user", fromUser);
        query.whereContainedIn("to_user", fromUser);
        query.orderByDescending("createdAt");

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {

                        allChatList.addAll(objects);
                        chatAdapter.setData(allChatList);
                        chatAdapter.notifyDataSetChanged();


                    }

                } else {
                    Log.e("error = ", e.getMessage());
                }
            }
        });
    }

    private void sendMessage() {
        String message = etChatMessage.getText().toString();
        ParseObject parseObject = new ParseObject("chat");
        parseObject.put("message", message);
        parseObject.put("from_user", ParseUser.getCurrentUser().getObjectId());
        parseObject.put("to_user", evacuatedById);

        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    new BaseUtility().sendNotification(evacuatedById, etChatMessage.getText().toString());
                    etChatMessage.setText("");
                    getAllChat();
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    public void onBackPressed() {
        if (slideMenuLayout.isLeftSlideOpen() || slideMenuLayout.isRightSlideOpen()) {
            slideMenuLayout.closeLeftSlide();
            slideMenuLayout.closeRightSlide();
        } {

            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);

        }
    }


}
