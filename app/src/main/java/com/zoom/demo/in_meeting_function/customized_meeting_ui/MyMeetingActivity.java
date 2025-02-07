package com.zoom.demo.in_meeting_function.customized_meeting_ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;


import us.zoom.sdk.InMeetingEventHandler;
import us.zoom.sdk.InMeetingService;
import us.zoom.sdk.InMeetingUserInfo;
import us.zoom.sdk.MeetingService;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.MobileRTCRenderInfo;
import us.zoom.sdk.MobileRTCShareView;
import us.zoom.sdk.MobileRTCVideoUnitRenderInfo;
import us.zoom.sdk.MobileRTCVideoView;
import us.zoom.sdk.MobileRTCVideoViewManager;
import us.zoom.sdk.ZoomSDK;
import com.zoom.demo.R;
import com.zoom.demo.in_meeting_function.customized_meeting_ui.audio.MeetingAudioCallback;
import com.zoom.demo.in_meeting_function.customized_meeting_ui.audio.MeetingAudioHelper;
import com.zoom.demo.in_meeting_function.customized_meeting_ui.other.MeetingCommonCallback;
import com.zoom.demo.in_meeting_function.customized_meeting_ui.remote_control.MeetingRemoteControlHelper;
import com.zoom.demo.in_meeting_function.customized_meeting_ui.share.MeetingShareCallback;
import com.zoom.demo.in_meeting_function.customized_meeting_ui.share.MeetingShareHelper;
import com.zoom.demo.in_meeting_function.customized_meeting_ui.user.MeetingUserCallback;
import com.zoom.demo.in_meeting_function.customized_meeting_ui.video.MeetingVideoCallback;
import com.zoom.demo.in_meeting_function.customized_meeting_ui.video.MeetingVideoHelper;
import com.zoom.demo.in_meeting_function.customized_meeting_ui.view.MeetingOptionBar;
import com.zoom.demo.in_meeting_function.customized_meeting_ui.view.MeetingWindowHelper;
import com.zoom.demo.in_meeting_function.customized_meeting_ui.view.adapter.AttenderVideoAdapter;
import com.zoom.demo.in_meeting_function.customized_meeting_ui.view.share.AnnotateToolbar;
import com.zoom.demo.in_meeting_function.customized_meeting_ui.view.share.CustomShareView;
import com.zoom.demo.ui.APIUserStartJoinMeetingActivity;
import com.zoom.demo.ui.LoginUserStartJoinMeetingActivity;

public class MyMeetingActivity extends FragmentActivity implements MeetingVideoCallback.VideoEvent,
        MeetingAudioCallback.AudioEvent, MeetingShareCallback.ShareEvent,
        MeetingUserCallback.UserEvent, MeetingCommonCallback.CommonEvent {

    private final static String TAG = MyMeetingActivity.class.getSimpleName();

    public final static int REQUEST_PLIST = 1001;

    public final static int REQUEST_CAMERA_CODE = 1010;

    public final static int REQUEST_AUDIO_CODE = 1011;

    public final static int REQUEST_SHARE_SCREEN_PERMISSION = 1001;

    protected final static int REQUEST_SYSTEM_ALERT_WINDOW = 1002;

    private int currentLayoutType = -1;
    private final int LAYOUT_TYPE_PREVIEW = 0;
    private final int LAYOUT_TYPE_WAITHOST = 1;
    private final int LAYOUT_TYPE_IN_WAIT_ROOM = 2;
    private final int LAYOUT_TYPE_ONLY_MYSELF = 3;
    private final int LAYOUT_TYPE_ONETOONE = 4;
    private final int LAYOUT_TYPE_LIST_VIDEO = 5;
    private final int LAYOUT_TYPE_VIEW_SHARE = 6;
    private final int LAYOUT_TYPE_SHARING_VIEW = 7;
    private final int LAYOUT_TYPE_WEBINAR_ATTENDEE = 8;

    private View mWaitJoinView;
    private View mWaitRoomView;
    private TextView mConnectingText;

    private LinearLayout videoListLayout;

    private boolean mMeetingFailed = false;

    public static long mCurShareUserId = -1;

    private MobileRTCVideoView mDefaultVideoView;
    private MobileRTCVideoViewManager mDefaultVideoViewMgr;

    private MeetingAudioHelper meetingAudioHelper;

    private MeetingVideoHelper meetingVideoHelper;

    private MeetingShareHelper meetingShareHelper;

    private MeetingRemoteControlHelper remoteControlHelper;

    private MeetingService mMeetingService;

    private InMeetingService mInMeetingService;

    private Intent mScreenInfoData;

    private MobileRTCShareView mShareView;
    private AnnotateToolbar mDrawingView;
    private FrameLayout mMeetingVideoView;

    private View mNormalSenceView;

    private CustomShareView customShareView;

    private RecyclerView mVideoListView;

    private AttenderVideoAdapter mAdapter;

    MeetingOptionBar meetingOptionBar;

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mMeetingService = ZoomSDK.getInstance().getMeetingService();
        mInMeetingService = ZoomSDK.getInstance().getInMeetingService();
        if (mMeetingService == null || mInMeetingService == null) {
            finish();
            return;
        }

        meetingAudioHelper = new MeetingAudioHelper(audioCallBack);
        meetingVideoHelper = new MeetingVideoHelper(this, videoCallBack);
        meetingShareHelper = new MeetingShareHelper(this, shareCallBack);

        registerListener();

        setContentView(R.layout.my_meeting_layout);

        gestureDetector = new GestureDetector(new GestureDetectorListener());
        meetingOptionBar = (MeetingOptionBar) findViewById(R.id.meeting_option_contain);
        meetingOptionBar.setCallBack(callBack);
        mMeetingVideoView = (FrameLayout) findViewById(R.id.meetingVideoView);
        mShareView = (MobileRTCShareView) findViewById(R.id.sharingView);
        mDrawingView = (AnnotateToolbar) findViewById(R.id.drawingView);

        mWaitJoinView = (View) findViewById(R.id.waitJoinView);
        mWaitRoomView = (View) findViewById(R.id.waitingRoom);

        LayoutInflater inflater = getLayoutInflater();

        mNormalSenceView = inflater.inflate(R.layout.layout_meeting_content_normal, null);
        mDefaultVideoView = (MobileRTCVideoView) mNormalSenceView.findViewById(R.id.videoView);

        customShareView = (CustomShareView) mNormalSenceView.findViewById(R.id.custom_share_view);
        remoteControlHelper = new MeetingRemoteControlHelper(customShareView);
        mMeetingVideoView.addView(mNormalSenceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mConnectingText = (TextView) findViewById(R.id.connectingTxt);

        mVideoListView = (RecyclerView) findViewById(R.id.videoList);
        mVideoListView.bringToFront();

        videoListLayout = findViewById(R.id.videoListLayout);

        mVideoListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new AttenderVideoAdapter(this, getWindowManager().getDefaultDisplay().getWidth(), pinVideoListener);
        mVideoListView.setAdapter(mAdapter);

        refreshToolbar();
    }

    MeetingVideoHelper.VideoCallBack videoCallBack = new MeetingVideoHelper.VideoCallBack() {
        @Override
        public boolean requestVideoPermission() {

            if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MyMeetingActivity.this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_CODE);
                return false;
            }
            return true;
        }

        @Override
        public void showCameraList(PopupWindow popupWindow) {
            popupWindow.showAsDropDown(meetingOptionBar.getSwitchCameraView(), 0, 20);
        }
    };

    MeetingAudioHelper.AudioCallBack audioCallBack = new MeetingAudioHelper.AudioCallBack() {
        @Override
        public boolean requestAudioPermission() {
            if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MyMeetingActivity.this, new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_CODE);
                return false;
            }
            return true;
        }

        @Override
        public void updateAudioButton() {
            meetingOptionBar.updateAudioButton();
        }
    };

    MeetingShareHelper.MeetingShareUICallBack shareCallBack = new MeetingShareHelper.MeetingShareUICallBack() {
        @Override
        public void showShareMenu(PopupWindow popupWindow) {
            popupWindow.showAtLocation((View) meetingOptionBar.getParent(), Gravity.BOTTOM | Gravity.CENTER, 0, 150);
        }

        @Override
        public MobileRTCShareView getShareView() {
            return mShareView;
        }
    };


    AttenderVideoAdapter.ItemClickListener pinVideoListener = new AttenderVideoAdapter.ItemClickListener() {
        @Override
        public void onItemClick(View view, int position, long userId) {
            if (currentLayoutType == LAYOUT_TYPE_VIEW_SHARE || currentLayoutType == LAYOUT_TYPE_SHARING_VIEW) {
                return;
            }
            mDefaultVideoViewMgr.removeAllVideoUnits();
            MobileRTCVideoUnitRenderInfo renderInfo = new MobileRTCVideoUnitRenderInfo(0, 0, 100, 100);
            mDefaultVideoViewMgr.addAttendeeVideoUnit(userId, renderInfo);
        }
    };

    class GestureDetectorListener extends GestureDetector.SimpleOnGestureListener {

        public GestureDetectorListener() {
            super();
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            if (mDrawingView.isAnnotationStarted() || remoteControlHelper.isEnableRemoteControl()) {
                meetingOptionBar.hideOrShowToolbar(true);
                return true;
            }
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if ((videoListLayout.getVisibility() == View.VISIBLE && (e.getX() >= videoListLayout.getLeft() || e.getY() <= meetingOptionBar.getTopBarHeight())) || e.getY() >= meetingOptionBar.getBottomBarTop()) {
                    return true;
                }
            } else {
                if ((videoListLayout.getVisibility() == View.VISIBLE && (e.getY() >= videoListLayout.getTop() || e.getY() <= meetingOptionBar.getTopBarHeight())) || e.getY() >= meetingOptionBar.getBottomBarTop()) {
                    return true;
                }
            }
            if (mMeetingService.getMeetingStatus() == MeetingStatus.MEETING_STATUS_INMEETING) {
                meetingOptionBar.hideOrShowToolbar(meetingOptionBar.isShowing());
            }
            return true;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    private void refreshToolbar() {
        if (mMeetingService.getMeetingStatus() == MeetingStatus.MEETING_STATUS_INMEETING) {
            mConnectingText.setVisibility(View.GONE);
            meetingOptionBar.updateMeetingNumber(mInMeetingService.getCurrentMeetingNumber() + "");
            meetingOptionBar.refreshToolbar();
        } else {
            if (mMeetingService.getMeetingStatus() == MeetingStatus.MEETING_STATUS_CONNECTING) {
                mConnectingText.setVisibility(View.VISIBLE);
            } else {
                mConnectingText.setVisibility(View.GONE);
            }
            meetingOptionBar.hideOrShowToolbar(true);
        }
    }


    private void updateAnnotationBar() {
        if (mCurShareUserId > 0 && !isMySelfWebinarAttendee()) {
            if (meetingShareHelper.isSenderSupportAnnotation(mCurShareUserId)) {
                if (mInMeetingService.isMyself(mCurShareUserId) && !meetingShareHelper.isSharingScreen()) {
                    mDrawingView.setVisibility(View.VISIBLE);
                } else {
                    if (currentLayoutType == LAYOUT_TYPE_VIEW_SHARE) {
                        mDrawingView.setVisibility(View.VISIBLE);
                    } else {
                        mDrawingView.setVisibility(View.GONE);
                    }
                }
            } else {
                mDrawingView.setVisibility(View.GONE);
            }

        } else {
            mDrawingView.setVisibility(View.GONE);
        }
    }

    private void checkShowVideoLayout() {

        mDefaultVideoViewMgr = mDefaultVideoView.getVideoViewManager();
        if (mDefaultVideoViewMgr != null) {
            int newLayoutType = getNewVideoMeetingLayout();
            if (currentLayoutType != newLayoutType) {
                removeOldLayout(currentLayoutType);
                currentLayoutType = newLayoutType;
                addNewLayout(newLayoutType);
            }
        }
        updateAnnotationBar();
    }

    private int getNewVideoMeetingLayout() {
        int newLayoutType = -1;
        if (mMeetingService.getMeetingStatus() == MeetingStatus.MEETING_STATUS_WAITINGFORHOST) {
            newLayoutType = LAYOUT_TYPE_WAITHOST;
            return newLayoutType;
        }

        if (mMeetingService.getMeetingStatus() == MeetingStatus.MEETING_STATUS_IN_WAITING_ROOM) {
            newLayoutType = LAYOUT_TYPE_IN_WAIT_ROOM;
            return newLayoutType;
        }

        if (meetingShareHelper.isOtherSharing()) {
            newLayoutType = LAYOUT_TYPE_VIEW_SHARE;
        } else if (meetingShareHelper.isSharingOut() && !meetingShareHelper.isSharingScreen()) {
            newLayoutType = LAYOUT_TYPE_SHARING_VIEW;
        } else {
            List<Long> userlist = mInMeetingService.getInMeetingUserList();
            int userCount = 0;
            if (userlist != null) {
                userCount = userlist.size();
            }

            if (userCount > 1) {
                int preCount = userCount;
                for (int i = 0; i < preCount; i++) {
                    InMeetingUserInfo userInfo = mInMeetingService.getUserInfoById(userlist.get(i));
                    if (mInMeetingService.isWebinarMeeting()) {
                        if (userInfo != null && userInfo.getInMeetingUserRole() == InMeetingUserInfo.InMeetingUserRole.USERROLE_ATTENDEE) {
                            userCount--;
                        }
                    }
                }
            }

            if (userCount == 0) {
                newLayoutType = LAYOUT_TYPE_PREVIEW;
            } else if (userCount == 1) {
                newLayoutType = LAYOUT_TYPE_ONLY_MYSELF;
            } else {
                newLayoutType = LAYOUT_TYPE_LIST_VIDEO;
            }
        }
        return newLayoutType;
    }

    private void removeOldLayout(int type) {
        if (type == LAYOUT_TYPE_WAITHOST) {
            mWaitJoinView.setVisibility(View.GONE);
            mMeetingVideoView.setVisibility(View.VISIBLE);
        } else if (type == LAYOUT_TYPE_IN_WAIT_ROOM) {
            mWaitRoomView.setVisibility(View.GONE);
            mMeetingVideoView.setVisibility(View.VISIBLE);
        } else if (type == LAYOUT_TYPE_PREVIEW || type == LAYOUT_TYPE_ONLY_MYSELF || type == LAYOUT_TYPE_ONETOONE) {
            mDefaultVideoViewMgr.removeAllVideoUnits();
        } else if (type == LAYOUT_TYPE_LIST_VIDEO || type == LAYOUT_TYPE_VIEW_SHARE) {
            mDefaultVideoViewMgr.removeAllVideoUnits();
            mDefaultVideoView.setGestureDetectorEnabled(false);
        } else if (type == LAYOUT_TYPE_SHARING_VIEW) {
            mShareView.setVisibility(View.GONE);
            mMeetingVideoView.setVisibility(View.VISIBLE);
        }

        if (type != LAYOUT_TYPE_SHARING_VIEW) {
            if (null != customShareView) {
                customShareView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void addNewLayout(int type) {
        if (type == LAYOUT_TYPE_WAITHOST) {
            mWaitJoinView.setVisibility(View.VISIBLE);
            refreshToolbar();
            mMeetingVideoView.setVisibility(View.GONE);
        } else if (type == LAYOUT_TYPE_IN_WAIT_ROOM) {
            mWaitRoomView.setVisibility(View.VISIBLE);
            videoListLayout.setVisibility(View.GONE);
            refreshToolbar();
            mMeetingVideoView.setVisibility(View.GONE);
            mDrawingView.setVisibility(View.GONE);
        } else if (type == LAYOUT_TYPE_PREVIEW) {
            showPreviewLayout();
        } else if (type == LAYOUT_TYPE_ONLY_MYSELF) {
            showOnlyMeLayout();
        } else if (type == LAYOUT_TYPE_ONETOONE) {
            showOne2OneLayout();
        } else if (type == LAYOUT_TYPE_LIST_VIDEO) {
            showVideoListLayout();
        } else if (type == LAYOUT_TYPE_VIEW_SHARE) {
            showViewShareLayout();
        } else if (type == LAYOUT_TYPE_SHARING_VIEW) {
            showSharingViewOutLayout();
        }
    }

    private void showPreviewLayout() {
        MobileRTCVideoUnitRenderInfo renderInfo1 = new MobileRTCVideoUnitRenderInfo(0, 0, 100, 100);
        mDefaultVideoView.setVisibility(View.VISIBLE);
        mDefaultVideoViewMgr.addPreviewVideoUnit(renderInfo1);
        videoListLayout.setVisibility(View.GONE);
    }

    private void showOnlyMeLayout() {
        mDefaultVideoView.setVisibility(View.VISIBLE);
        videoListLayout.setVisibility(View.GONE);
        MobileRTCVideoUnitRenderInfo renderInfo = new MobileRTCVideoUnitRenderInfo(0, 0, 100, 100);
        InMeetingUserInfo myUserInfo = mInMeetingService.getMyUserInfo();
        if (myUserInfo != null) {
            if (isMySelfWebinarAttendee()) {
                mDefaultVideoViewMgr.addActiveVideoUnit(renderInfo);
            } else {
                mDefaultVideoViewMgr.addAttendeeVideoUnit(myUserInfo.getUserId(), renderInfo);
            }
        }
    }


    private void showOne2OneLayout() {
        mDefaultVideoView.setVisibility(View.VISIBLE);
        videoListLayout.setVisibility(View.VISIBLE);

        MobileRTCVideoUnitRenderInfo renderInfo = new MobileRTCVideoUnitRenderInfo(0, 0, 100, 100);
        //options.aspect_mode = MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_PAN_AND_SCAN;
        mDefaultVideoViewMgr.addActiveVideoUnit(renderInfo);

        mAdapter.setUserList(mInMeetingService.getInMeetingUserList());
        mAdapter.notifyDataSetChanged();

    }

    private void showVideoListLayout() {
        MobileRTCVideoUnitRenderInfo renderInfo = new MobileRTCVideoUnitRenderInfo(0, 0, 100, 100);
        //options.aspect_mode = MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_PAN_AND_SCAN;
        mDefaultVideoViewMgr.addActiveVideoUnit(renderInfo);
        videoListLayout.setVisibility(View.VISIBLE);
        updateAttendeeVideos(mInMeetingService.getInMeetingUserList(), 0);
    }

    private void showSharingViewOutLayout() {
        mAdapter.setUserList(null);
        mAdapter.notifyDataSetChanged();
        videoListLayout.setVisibility(View.GONE);
        mMeetingVideoView.setVisibility(View.GONE);
        mShareView.setVisibility(View.VISIBLE);
    }


    private void updateAttendeeVideos(List<Long> userlist, int action) {
        if (action == 0) {
            mAdapter.setUserList(userlist);
            mAdapter.notifyDataSetChanged();
        } else if (action == 1) {
            mAdapter.addUserList(userlist);
        } else {

            Long userId = mAdapter.getSelectedUserId();
            if (userlist.contains(userId)) {
                List<Long> inmeetingUserList = mInMeetingService.getInMeetingUserList();
                if (inmeetingUserList.size() > 0) {
                    mDefaultVideoViewMgr.removeAllVideoUnits();
                    MobileRTCVideoUnitRenderInfo renderInfo = new MobileRTCVideoUnitRenderInfo(0, 0, 100, 100);
                    mDefaultVideoViewMgr.addAttendeeVideoUnit(inmeetingUserList.get(0), renderInfo);
                }
            }
            mAdapter.removeUserList(userlist);
        }
    }

    private void showViewShareLayout() {
        if (!isMySelfWebinarAttendee()) {
            mDefaultVideoView.setVisibility(View.VISIBLE);
            mDefaultVideoView.setOnClickListener(null);
            mDefaultVideoView.setGestureDetectorEnabled(true);
            long shareUserId = mInMeetingService.activeShareUserID();
            MobileRTCRenderInfo renderInfo1 = new MobileRTCRenderInfo(0, 0, 100, 100);
            mDefaultVideoViewMgr.addShareVideoUnit(shareUserId, renderInfo1);
            updateAttendeeVideos(mInMeetingService.getInMeetingUserList(), 0);

            customShareView.setMobileRTCVideoView(mDefaultVideoView);
            remoteControlHelper.refreshRemoteControlStatus();

        } else {
            mDefaultVideoView.setVisibility(View.VISIBLE);
            mDefaultVideoView.setOnClickListener(null);
            mDefaultVideoView.setGestureDetectorEnabled(true);
            long shareUserId = mInMeetingService.activeShareUserID();
            MobileRTCRenderInfo renderInfo1 = new MobileRTCRenderInfo(0, 0, 100, 100);
            mDefaultVideoViewMgr.addShareVideoUnit(shareUserId, renderInfo1);
        }

        mAdapter.setUserList(null);
        mAdapter.notifyDataSetChanged();
        videoListLayout.setVisibility(View.INVISIBLE);
    }

    private boolean isMySelfWebinarAttendee() {
        InMeetingUserInfo myUserInfo = mInMeetingService.getMyUserInfo();
        if (myUserInfo != null && mInMeetingService.isWebinarMeeting()) {
            return myUserInfo.getInMeetingUserRole() == InMeetingUserInfo.InMeetingUserRole.USERROLE_ATTENDEE;
        }
        return false;
    }

    private boolean isMySelfWebinarHostCohost() {
        InMeetingUserInfo myUserInfo = mInMeetingService.getMyUserInfo();
        if (myUserInfo != null && mInMeetingService.isWebinarMeeting()) {
            return myUserInfo.getInMeetingUserRole() == InMeetingUserInfo.InMeetingUserRole.USERROLE_HOST
                    || myUserInfo.getInMeetingUserRole() == InMeetingUserInfo.InMeetingUserRole.USERROLE_COHOST;
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        meetingVideoHelper.checkVideoRotation(this);
        updateVideoListMargin(!meetingOptionBar.isShowing());
    }


    @Override
    protected void onResume() {
        super.onResume();
        MeetingWindowHelper.getInstance().hiddenMeetingWindow(false);
        checkShowVideoLayout();
        meetingVideoHelper.checkVideoRotation(this);
        mDefaultVideoView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDefaultVideoView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != remoteControlHelper) {
            remoteControlHelper.onDestroy();
        }
        unRegisterListener();
    }

    MeetingOptionBar.MeetingOptionBarCallBack callBack = new MeetingOptionBar.MeetingOptionBarCallBack() {
        @Override
        public void onClickBack() {
            onBackPressed();
        }

        @Override
        public void onClickSwitchCamera() {
            meetingVideoHelper.switchCamera();
        }

        @Override
        public void onClickLeave() {
            showLeaveMeetingDialog();
        }

        @Override
        public void onClickAudio() {
            meetingAudioHelper.switchAudio();
        }

        @Override
        public void onClickVideo() {
            meetingVideoHelper.switchVideo();
        }

        @Override
        public void onClickShare() {
            meetingShareHelper.onClickShare();
        }

        @Override
        public void onClickChats() {
            mInMeetingService.showZoomParticipantsUI(MyMeetingActivity.this, REQUEST_PLIST);
        }

        @Override
        public void onClickDisconnectAudio() {
            meetingAudioHelper.disconnectAudio();
        }

        @Override
        public void onClickSwitchLoudSpeaker() {
            meetingAudioHelper.switchLoudSpeaker();
        }

        @Override
        public void showMoreMenu(PopupWindow popupWindow) {
            popupWindow.showAtLocation((View) meetingOptionBar.getParent(), Gravity.BOTTOM | Gravity.RIGHT, 0, 150);
        }

        @Override
        public void onHidden(boolean hidden) {
            updateVideoListMargin(hidden);
        }
    };


    @Override
    public void onBackPressed() {
        if (mMeetingService.getMeetingStatus() == MeetingStatus.MEETING_STATUS_INMEETING) {
            //stop share
            if (currentLayoutType == LAYOUT_TYPE_VIEW_SHARE) {
                mDefaultVideoViewMgr.removeShareVideoUnit();
                currentLayoutType = -1;
            }
            showMainActivity();
        } else {
            showLeaveMeetingDialog();
        }
    }


    private void updateVideoListMargin(boolean hidden) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) videoListLayout.getLayoutParams();
        params.bottomMargin = hidden ? 0 : meetingOptionBar.getBottomBarHeight();
        if (Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation) {
            params.bottomMargin = 0;
        }
        videoListLayout.setLayoutParams(params);
        videoListLayout.bringToFront();
    }


    private void showMainActivity() {

        Class clz=LoginUserStartJoinMeetingActivity.class;
        if(!ZoomSDK.getInstance().isLoggedIn())
        {
            clz=APIUserStartJoinMeetingActivity.class;
        }
        Intent intent = new Intent(this, clz);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void showPsswordDialog(final boolean needPassword, final boolean needDisplayName, final InMeetingEventHandler handler) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Need password or displayName");
        View view = LayoutInflater.from(this).inflate(R.layout.layout_input_password_name, null);
        builder.setView(view);

        final EditText pwd = view.findViewById(R.id.edit_pwd);
        final EditText name = view.findViewById(R.id.edit_name);
        pwd.setVisibility(needPassword ? View.VISIBLE : View.GONE);
        name.setVisibility(needPassword ? View.VISIBLE : View.GONE);

        builder.setNegativeButton("Leave", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mInMeetingService.leaveCurrentMeeting(true);
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = pwd.getText().toString();
                String userName = name.getText().toString();
                if (needPassword && TextUtils.isEmpty(password) || (needDisplayName && TextUtils.isEmpty(userName))) {
                    onMeetingNeedPasswordOrDisplayName(needPassword, needDisplayName, handler);
                    return;
                }
                handler.setMeetingNamePassword(password, userName);

            }
        });
        builder.create().show();
    }


    private void updateVideoView(List<Long> userList, int action) {
        if (currentLayoutType == LAYOUT_TYPE_LIST_VIDEO || currentLayoutType == LAYOUT_TYPE_VIEW_SHARE) {
            if (mVideoListView.getVisibility() == View.VISIBLE) {
                updateAttendeeVideos(userList, action);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_SHARE_SCREEN_PERMISSION:
                if (resultCode != RESULT_OK) {
                    if (us.zoom.videomeetings.BuildConfig.DEBUG)
                        Log.d(TAG, "onActivityResult REQUEST_SHARE_SCREEN_PERMISSION no ok ");
                    break;
                }
                startShareScreen(data);
                break;
            case REQUEST_SYSTEM_ALERT_WINDOW:
                meetingShareHelper.startShareScreenSession(mScreenInfoData);
                break;
        }
    }


    boolean finished = false;

    @Override
    public void finish() {
        if (!finished) {
            showMainActivity();
        }
        finished = true;
        super.finish();
    }

    private void showLeaveMeetingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (mInMeetingService.isMeetingConnected()) {
            if (mInMeetingService.isMeetingHost()) {
                builder.setTitle("End or leave meeting")
                        .setPositiveButton("End", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                mInMeetingService.leaveCurrentMeeting(true);
                            }
                        }).setNeutralButton("Leave", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        mInMeetingService.leaveCurrentMeeting(false);
                    }
                });
            } else {
                builder.setTitle("Leave meeting")
                        .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                mInMeetingService.leaveCurrentMeeting(false);
                            }
                        });
            }
        } else {
            builder.setTitle("Leave meeting")
                    .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            mInMeetingService.leaveCurrentMeeting(true);
                        }
                    });
        }
        builder.setNegativeButton("Cancel", null).create();
        builder.create().show();
    }

    private void showJoinFailDialog(int error) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Meeting Fail")
                .setMessage("Error:" + error)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create();
        dialog.show();
    }

    private void showWebinarNeedRegisterDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Need register to join this webinar meeting ")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mInMeetingService.leaveCurrentMeeting(true);
                    }
                }).create();
        dialog.show();
    }

    private void showEndOtherMeetingDialog(final InMeetingEventHandler handler) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Meeting Alert")
                .setMessage("You have a meeting that is currently in-progress. Please end it to start a new meeting.")
                .setPositiveButton("End Other Meeting", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.endOtherMeeting();
                    }
                }).setNeutralButton("Leave", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        mInMeetingService.leaveCurrentMeeting(true);
                    }
                }).create();
        dialog.show();
    }

    @SuppressLint("NewApi")
    protected void startShareScreen(Intent data) {
        if (data == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 24 && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            mScreenInfoData = data;
            startActivityForResult(intent, REQUEST_SYSTEM_ALERT_WINDOW);
        } else {
            meetingShareHelper.startShareScreenSession(data);
        }
    }

    public int checkSelfPermission(String permission) {
        if (permission == null || permission.length() == 0) {
            return PackageManager.PERMISSION_DENIED;
        }
        try {
            return checkPermission(permission, android.os.Process.myPid(), android.os.Process.myUid());
        } catch (Throwable e) {
            return PackageManager.PERMISSION_DENIED;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissions == null || grantResults == null) {
            return;
        }

        for (int i = 0; i < permissions.length; i++) {
            if (Manifest.permission.RECORD_AUDIO.equals(permissions[i])) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    meetingAudioHelper.switchAudio();
                }
            } else if (Manifest.permission.CAMERA.equals(permissions[i])) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    meetingVideoHelper.switchVideo();
                }
            }
        }
    }


    @Override
    public void onUserAudioStatusChanged(long userId) {
        meetingAudioHelper.onUserAudioStatusChanged(userId);
    }

    @Override
    public void onUserAudioTypeChanged(long userId) {
        meetingAudioHelper.onUserAudioTypeChanged(userId);
    }

    @Override
    public void onMyAudioSourceTypeChanged(int type) {
        meetingAudioHelper.onMyAudioSourceTypeChanged(type);
    }

    @Override
    public void onUserVideoStatusChanged(long userId) {
        meetingOptionBar.updateVideoButton();
        meetingOptionBar.updateSwitchCameraButton();
    }

    @Override
    public void onShareActiveUser(long userId) {
        meetingShareHelper.onShareActiveUser(mCurShareUserId, userId);
        mCurShareUserId = userId;
        meetingOptionBar.updateShareButton();
        checkShowVideoLayout();
    }

    @Override
    public void onShareUserReceivingStatus(long userId) {

    }

    @Override
    public void onMeetingUserJoin(List<Long> userList) {
        checkShowVideoLayout();
        updateVideoView(userList, 1);
    }

    @Override
    public void onMeetingUserLeave(List<Long> userList) {
        checkShowVideoLayout();
        updateVideoView(userList, 2);
    }

    @Override
    public void onWebinarNeedRegister() {
        showWebinarNeedRegisterDialog();
    }

    @Override
    public void onMeetingFail(int errorCode, int internalErrorCode) {
        mMeetingFailed = true;
        mMeetingVideoView.setVisibility(View.GONE);
        mConnectingText.setVisibility(View.GONE);
        showJoinFailDialog(errorCode);
    }

    @Override
    public void onMeetingLeaveComplete(long ret) {
        meetingShareHelper.stopShare();
        if (!mMeetingFailed)
            finish();
    }

    @Override
    public void onMeetingStatusChanged(MeetingStatus meetingStatus, int errorCode, int internalErrorCode) {
        checkShowVideoLayout();
        refreshToolbar();
    }

    @Override
    public void onMeetingNeedPasswordOrDisplayName(boolean needPassword, boolean needDisplayName, InMeetingEventHandler handler) {
        showPsswordDialog(needPassword, needDisplayName, handler);
    }

    @Override
    public void onMeetingNeedColseOtherMeeting(InMeetingEventHandler inMeetingEventHandler) {
        showEndOtherMeetingDialog(inMeetingEventHandler);
    }

    @Override
    public void onJoinWebinarNeedUserNameAndEmail(InMeetingEventHandler inMeetingEventHandler) {
        inMeetingEventHandler.setRegisterWebinarInfo("test", "test@example.com", false);
    }

    private void unRegisterListener() {
        MeetingAudioCallback.getInstance().removeListener(this);
        MeetingVideoCallback.getInstance().removeListener(this);
        MeetingShareCallback.getInstance().removeListener(this);
        MeetingUserCallback.getInstance().removeListener(this);
        MeetingCommonCallback.getInstance().removeListener(this);
    }


    private void registerListener() {
        MeetingAudioCallback.getInstance().addListener(this);
        MeetingVideoCallback.getInstance().addListener(this);
        MeetingShareCallback.getInstance().addListener(this);
        MeetingUserCallback.getInstance().addListener(this);
        MeetingCommonCallback.getInstance().addListener(this);

    }
}

