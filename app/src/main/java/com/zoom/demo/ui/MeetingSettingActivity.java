package com.zoom.demo.ui;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.zoom.demo.in_meeting_function.zoom_meeting_ui.ZoomMeetingUISettingHelper;

import us.zoom.sdk.InviteOptions;
import us.zoom.sdk.ZoomSDK;
import com.zoom.demo.R;

public class MeetingSettingActivity extends FragmentActivity implements CompoundButton.OnCheckedChangeListener {

    LinearLayout settingContain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_setting);

        Switch btnCustomUI = ((Switch) findViewById(R.id.btn_custom_ui));

        boolean isCustomUI = ZoomSDK.getInstance().getMeetingSettingsHelper().isCustomizedMeetingUIEnabled();

        btnCustomUI.setChecked(isCustomUI);

        settingContain = findViewById(R.id.settings_contain);
        settingContain.setVisibility(isCustomUI ? View.GONE : View.VISIBLE);
        for (int i = 0, count = settingContain.getChildCount(); i < count; i++) {
            View child = settingContain.getChildAt(i);
            if (null != child && child instanceof Switch) {
                ((Switch) child).setOnCheckedChangeListener(this);
                initCheck((Switch) child);
            }
        }

        btnCustomUI.setOnCheckedChangeListener(this);

    }

    private void initCheck(Switch view) {
        switch (view.getId()) {
            case R.id.btn_auto_connect_audio: {
                view.setChecked(ZoomSDK.getInstance().getMeetingSettingsHelper().isAutoConnectVoIPWhenJoinMeetingEnabled());
                break;
            }
            case R.id.btn_mute_my_mic: {
                view.setChecked(ZoomSDK.getInstance().getMeetingSettingsHelper().isMuteMyMicrophoneWhenJoinMeetingEnabled());
                break;
            }
            case R.id.btn_turn_off_video: {
                view.setChecked(ZoomSDK.getInstance().getMeetingSettingsHelper().isTurnOffMyVideoWhenJoinMeetingEnabled());
                break;
            }
            case R.id.btn_hide_no_video_user: {
                view.setChecked(ZoomSDK.getInstance().getMeetingSettingsHelper().isHideNoVideoUsersEnabled());
                break;
            }
            case R.id.btn_auto_switch_video: {
                view.setChecked(ZoomSDK.getInstance().getMeetingSettingsHelper().isSwitchVideoLayoutAccordingToUserCountEnabled());
                break;
            }
            case R.id.btn_gallery_video: {
                view.setChecked(ZoomSDK.getInstance().getMeetingSettingsHelper().isGalleryVideoViewDisabled());
                break;
            }
            case R.id.btn_show_tool_bar: {
                view.setChecked(ZoomSDK.getInstance().getMeetingSettingsHelper().isAlwaysShowMeetingToolbarEnabled());
                break;
            }
            case R.id.btn_show_larger_share: {
                view.setChecked(ZoomSDK.getInstance().getMeetingSettingsHelper().isLargeShareVideoSceneEnabled());
                break;
            }

            case R.id.btn_no_video_title_share: {
                view.setChecked(ZoomSDK.getInstance().getMeetingSettingsHelper().isNoVideoTileOnShareScreenEnabled());
                break;
            }

            case R.id.btn_no_leave_btn: {
                view.setChecked(ZoomSDK.getInstance().getMeetingSettingsHelper().isNoLeaveMeetingButtonForHostEnabled());
                break;
            }

            case R.id.btn_no_tips_user_event: {
                view.setChecked(ZoomSDK.getInstance().getMeetingSettingsHelper().isNoUserJoinOrLeaveTipEnabled());
                break;
            }
            case R.id.btn_no_drive_mode: {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_driving_mode);
                break;
            }
            case R.id.btn_no_end_dialog: {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_meeting_end_message);
                break;
            }
            case R.id.btn_hidden_title_bar: {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_titlebar);
                break;
            }
            case R.id.btn_hidden_invite: {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_invite);
                break;
            }
            case R.id.btn_hidden_bottom_bar: {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_bottom_toolbar);
                break;
            }

            case R.id.btn_hidden_dial_in_via_phone: {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_dial_in_via_phone);
                break;
            }

            case R.id.btn_invite_option_enable_all: {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().invite_options==InviteOptions.INVITE_ENABLE_ALL);
                break;
            }
            case R.id.btn_no_video: {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_video);
                break;
            }
            case R.id.btn_no_audio: {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_audio);
                break;
            }
            case R.id.btn_no_meeting_error_message: {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_meeting_error_message);
                break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.btn_custom_ui: {
                ZoomSDK.getInstance().getMeetingSettingsHelper().setCustomizedMeetingUIEnabled(isChecked);
                settingContain.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                break;
            }
            case R.id.btn_auto_connect_audio: {
                ZoomSDK.getInstance().getMeetingSettingsHelper().setAutoConnectVoIPWhenJoinMeeting(isChecked);
                break;
            }
            case R.id.btn_mute_my_mic: {
                ZoomSDK.getInstance().getMeetingSettingsHelper().setMuteMyMicrophoneWhenJoinMeeting(isChecked);
                break;
            }
            case R.id.btn_turn_off_video: {
                ZoomSDK.getInstance().getMeetingSettingsHelper().setTurnOffMyVideoWhenJoinMeeting(isChecked);
                break;
            }
            case R.id.btn_hide_no_video_user: {
                ZoomSDK.getInstance().getMeetingSettingsHelper().setHideNoVideoUsersEnabled(isChecked);
                break;
            }
            case R.id.btn_auto_switch_video: {
                ZoomSDK.getInstance().getMeetingSettingsHelper().setSwitchVideoLayoutAccordingToUserCountEnabled(isChecked);
                break;
            }
            case R.id.btn_gallery_video: {
                ZoomSDK.getInstance().getMeetingSettingsHelper().setGalleryVideoViewDisabled(!isChecked);
                break;
            }
            case R.id.btn_show_tool_bar: {
                ZoomSDK.getInstance().getMeetingSettingsHelper().setAlwaysShowMeetingToolbarEnabled(isChecked);
                break;
            }
            case R.id.btn_show_larger_share: {
                ZoomSDK.getInstance().getMeetingSettingsHelper().setLargeShareVideoSceneEnabled(isChecked);
                break;
            }

            case R.id.btn_no_video_title_share: {
                ZoomSDK.getInstance().getMeetingSettingsHelper().setNoVideoTileOnShareScreenEnabled(isChecked);
                break;
            }

            case R.id.btn_no_leave_btn: {
                ZoomSDK.getInstance().getMeetingSettingsHelper().setNoLeaveMeetingButtonForHostEnabled(isChecked);
                break;
            }

            case R.id.btn_no_tips_user_event: {
                ZoomSDK.getInstance().getMeetingSettingsHelper().setNoUserJoinOrLeaveTipEnabled(isChecked);
                break;
            }
            case R.id.btn_no_drive_mode: {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_driving_mode = isChecked;
                break;
            }
            case R.id.btn_no_end_dialog: {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_meeting_end_message = isChecked;
                break;
            }
            case R.id.btn_hidden_title_bar: {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_titlebar = isChecked;
                break;
            }
            case R.id.btn_hidden_invite: {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_invite = isChecked;
                break;
            }
            case R.id.btn_hidden_bottom_bar: {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_bottom_toolbar = isChecked;
                break;
            }

            case R.id.btn_hidden_dial_in_via_phone: {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_dial_in_via_phone = isChecked;
                break;
            }

            case R.id.btn_invite_option_enable_all: {
                ZoomMeetingUISettingHelper.getMeetingOptions().invite_options = isChecked ? InviteOptions.INVITE_ENABLE_ALL :
                        InviteOptions.INVITE_DISABLE_ALL;
                break;
            }
            case R.id.btn_no_video: {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_video = isChecked;
                break;
            }
            case R.id.btn_no_audio: {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_audio = isChecked;
                break;
            }
            case R.id.btn_no_meeting_error_message: {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_meeting_error_message = isChecked;
                break;
            }
            case R.id.btn_force_start_video:
            {
                ZoomSDK.getInstance().getMeetingSettingsHelper().enableForceAutoStartMyVideoWhenJoinMeeting(isChecked);
                break;
            }
            case R.id.btn_force_stop_video:
            {
                ZoomSDK.getInstance().getMeetingSettingsHelper().enableForceAutoStopMyVideoWhenJoinMeeting(isChecked);
                break;
            }
            case R.id.btn_show_audio_select_dialog:
            {
                ZoomSDK.getInstance().getMeetingSettingsHelper().disableAutoShowSelectJoinAudioDlgWhenJoinMeeting(isChecked);
                break;
            }

        }
    }
}