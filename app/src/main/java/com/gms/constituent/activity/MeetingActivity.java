package com.gms.constituent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.gms.constituent.R;
import com.gms.constituent.adapter.MeetingListAdapter;
import com.gms.constituent.adapter.UserListAdapter;
import com.gms.constituent.bean.support.Meeting;
import com.gms.constituent.bean.support.MeetingList;
import com.gms.constituent.bean.support.User;
import com.gms.constituent.bean.support.UserList;
import com.gms.constituent.helper.AlertDialogHelper;
import com.gms.constituent.helper.ProgressDialogHelper;
import com.gms.constituent.interfaces.DialogClickListener;
import com.gms.constituent.servicehelpers.ServiceHelper;
import com.gms.constituent.serviceinterfaces.IServiceListener;
import com.gms.constituent.utils.GMSConstants;
import com.gms.constituent.utils.PreferenceStorage;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MeetingActivity extends AppCompatActivity implements View.OnClickListener, IServiceListener, DialogClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = MeetingActivity.class.getName();


    private String whatRes = "";
    private ServiceHelper serviceHelper;
    private ProgressDialogHelper progressDialogHelper;
    private ListView listView;
    private ArrayList<Meeting> meetings = new ArrayList<>();
    private MeetingListAdapter meetingListAdapter;
    private RelativeLayout toolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetings);
        findViewById(R.id.img_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });
        serviceHelper = new ServiceHelper(this);
        serviceHelper.setServiceListener(this);
        progressDialogHelper = new ProgressDialogHelper(this);

        listView = (ListView) findViewById(R.id.meeting_list);
        listView.setOnItemClickListener(this);

        getMeetingList();

    }

    private void getMeetingList() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(GMSConstants.KEY_USER_ID, PreferenceStorage.getUserId(this));
//            jsonObject.put(GMSConstants.KEY_USER_ID, "1");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
        String url = PreferenceStorage.getClientUrl(this) + GMSConstants.GET_MEETINGS;
        serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onAlertPositiveClicked(int tag) {

    }

    @Override
    public void onAlertNegativeClicked(int tag) {

    }

    private boolean validateResponse(JSONObject response) {
        boolean signInSuccess = false;
        if ((response != null)) {
            try {
                String status = response.getString("status");
                String msg = response.getString(GMSConstants.PARAM_MESSAGE);
                Log.d(TAG, "status val" + status + "msg" + msg);

                if ((status != null)) {
                    if (status.equalsIgnoreCase("success")) {
                        signInSuccess = true;
                    } else {
                        signInSuccess = false;
                        Log.d(TAG, "Show error dialog");
                        AlertDialogHelper.showSimpleAlertDialog(this, msg);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return signInSuccess;
    }

    @Override
    public void onResponse(JSONObject response) {
        progressDialogHelper.hideProgressDialog();
        if (validateResponse(response)) {
            Gson gson = new Gson();
            MeetingList meetingList = gson.fromJson(response.toString(), MeetingList.class);
            if (meetingList.getMeetingArrayList() != null && meetingList.getMeetingArrayList().size() > 0) {
//                    this.ongoingServiceArrayList.addAll(ongoingServiceList.getserviceArrayList());
                updateListAdapter(meetingList.getMeetingArrayList());
            } else {
                if (meetings != null) {
                    meetings.clear();
                    updateListAdapter(meetingList.getMeetingArrayList());
                }
            }
        }
    }

    protected void updateListAdapter(ArrayList<Meeting> meetingArrayList) {
        meetings.clear();
        meetings.addAll(meetingArrayList);
        if (meetingListAdapter == null) {
            meetingListAdapter = new MeetingListAdapter(this, meetings);
            listView.setAdapter(meetingListAdapter);
        } else {
            meetingListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onError(String error) {
        progressDialogHelper.hideProgressDialog();
        AlertDialogHelper.showSimpleAlertDialog(this, error);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onEvent list item clicked" + position);
        Meeting meeting = null;
        if ((meetingListAdapter != null) && (meetingListAdapter.ismSearching())) {
            Log.d(TAG, "while searching");
            int actualindex = meetingListAdapter.getActualEventPos(position);
            Log.d(TAG, "actual index" + actualindex);
            meeting = meetings.get(actualindex);
        } else {
            meeting = meetings.get(position);
        }
        PreferenceStorage.saveUserId(this, meeting.getid());
        Intent intent = new Intent(this, MeetingDetailActivity.class);
        intent.putExtra("serviceObj", meeting);
        startActivity(intent);    }
}