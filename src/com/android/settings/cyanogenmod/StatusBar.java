/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cyanogenmod;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.util.Helpers;
import com.android.settings.widget.SeekBarPreference;

public class StatusBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String STATUS_BAR_BATTERY = "status_bar_battery";

    private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
    private static final String STATUS_BAR_SIGNAL = "status_bar_signal";
    private static final String STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
    private static final String STATUS_BAR_TRANSPARENCY = "status_bar_transparency";
    private static final String STATUS_BAR_CATEGORY_GENERAL = "status_bar_general";
    private static final String NOTIFICATION_PANEL_TRANSPARENCY = "notification_panel_transparency";

    private ListPreference mStatusBarBattery;
    private ListPreference mStatusBarCmSignal;
    private SeekBarPreference mStatusbarTransparency;
    private SeekBarPreference mNotificationpanelTransparency;
    private CheckBoxPreference mStatusBarBrightnessControl;
    private CheckBoxPreference mStatusBarNotifCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarBrightnessControl = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
        mStatusBarBattery = (ListPreference) prefSet.findPreference(STATUS_BAR_BATTERY);
        mStatusBarCmSignal = (ListPreference) prefSet.findPreference(STATUS_BAR_SIGNAL);

<<<<<<< HEAD
        float statusbarAlpha;
        try{
            statusbarAlpha = Settings.System.getFloat(getActivity()
                     .getContentResolver(), Settings.System.STATUS_BAR_TRANSPARENCY);
        } catch (Exception e) {
            statusbarAlpha = 0.0f;
                     Settings.System.putFloat(getActivity().getContentResolver(), Settings.System.STATUS_BAR_TRANSPARENCY, 0.0f);
=======
        mStatusBarClock.setChecked(Settings.System.getInt(resolver, Settings.System.STATUS_BAR_CLOCK, 1) == 1);
        mStatusBarClock.setOnPreferenceChangeListener(this);

        if (DateFormat.is24HourFormat(getActivity())) {
            ((PreferenceCategory) prefSet.findPreference(STATUS_BAR_CLOCK_CATEGORY))
                    .removePreference(prefSet.findPreference(STATUS_BAR_AM_PM));
        } else {
            mStatusBarAmPm = (ListPreference) prefSet.findPreference(STATUS_BAR_AM_PM);
            int statusBarAmPm = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_AM_PM, 2);

            mStatusBarAmPm.setValue(String.valueOf(statusBarAmPm));
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntry());
            mStatusBarAmPm.setOnPreferenceChangeListener(this);
>>>>>>> 04813751975f8b3fbab7a79a99091eea7e626b08
        }
        mStatusbarTransparency = (SeekBarPreference) prefSet.findPreference(STATUS_BAR_TRANSPARENCY);
        mStatusbarTransparency.setProperty(Settings.System.STATUS_BAR_TRANSPARENCY);
        mStatusbarTransparency.setInitValue((int) (statusbarAlpha * 100));
        mStatusbarTransparency.setOnPreferenceChangeListener(this);

        float notificationAlpha;
        try{
            notificationAlpha = Settings.System.getFloat(getActivity()
                     .getContentResolver(), Settings.System.NOTIFICATION_PANEL_TRANSPARENCY);
        } catch (Exception e) {
            notificationAlpha = 0.0f;
                     Settings.System.putFloat(getActivity().getContentResolver(), Settings.System.NOTIFICATION_PANEL_TRANSPARENCY, 0.0f);
        }

        mNotificationpanelTransparency = (SeekBarPreference) prefSet.findPreference(NOTIFICATION_PANEL_TRANSPARENCY);
        mNotificationpanelTransparency.setProperty(Settings.System.NOTIFICATION_PANEL_TRANSPARENCY);
        mNotificationpanelTransparency.setInitValue((int) (notificationAlpha * 100));
        mNotificationpanelTransparency.setOnPreferenceChangeListener(this);

        mStatusBarBrightnessControl.setChecked(Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1);
        mStatusBarBrightnessControl.setOnPreferenceChangeListener(this);

        try {
            if (Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
                    == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                mStatusBarBrightnessControl.setEnabled(false);
                mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
            }
        } catch (SettingNotFoundException e) {
        }

        int statusBarBattery = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_BATTERY, 0);
        mStatusBarBattery.setValue(String.valueOf(statusBarBattery));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
        mStatusBarBattery.setOnPreferenceChangeListener(this);

        int signalStyle = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_SIGNAL_TEXT, 0);
        mStatusBarCmSignal.setValue(String.valueOf(signalStyle));
        mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntry());
        mStatusBarCmSignal.setOnPreferenceChangeListener(this);

        mStatusBarNotifCount = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NOTIF_COUNT);
        mStatusBarNotifCount.setChecked(Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_NOTIF_COUNT, 0) == 1);
        mStatusBarNotifCount.setOnPreferenceChangeListener(this);

        PreferenceCategory generalCategory =
                (PreferenceCategory) findPreference(STATUS_BAR_CATEGORY_GENERAL);

        if (Utils.isWifiOnly(getActivity())) {
            generalCategory.removePreference(mStatusBarCmSignal);
        }

        if (Utils.isTablet(getActivity())) {
            generalCategory.removePreference(mStatusBarBrightnessControl);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	if (preference == mStatusBarBattery) {
            int statusBarBattery = Integer.valueOf((String) newValue);
            int index = mStatusBarBattery.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_BATTERY, statusBarBattery);
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);
            return true;
       } else if (preference == mStatusbarTransparency) {
            float val = Float.parseFloat((String) newValue);
             Settings.System.putFloat(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_TRANSPARENCY,
                    val / 100);
            return true;
        } else if (preference == mNotificationpanelTransparency) {
            float val = Float.parseFloat((String) newValue);
            Settings.System.putFloat(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.NOTIFICATION_PANEL_TRANSPARENCY,
                    val / 100);
            return true;
    	} else if (preference == mStatusBarCmSignal) {
            int signalStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarCmSignal.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_SIGNAL_TEXT, signalStyle);
            mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntries()[index]);
            return true;
        }
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver resolver = getActivity().getContentResolver();
        boolean value;
	if (preference == mStatusBarBrightnessControl) {
            value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, value ? 1 : 0);
            return true;
        } else if (preference == mStatusBarNotifCount) {
            value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_NOTIF_COUNT, value ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);

    }
}
