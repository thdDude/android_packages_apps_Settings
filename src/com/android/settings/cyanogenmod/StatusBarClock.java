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
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.DateFormat;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class StatusBarClock extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String PREF_ENABLE = "clock_style";
    private static final String PREF_AM_PM_STYLE = "clock_am_pm_style";
    private static final String STATUS_BAR_CLOCK_COLOR = "status_bar_clock_color";
    private static final String PREF_CLOCK_WEEKDAY = "clock_weekday";

    ListPreference mClockStyle;
    ListPreference mClockAmPmstyle;
    private Preference mStatusBarClockColor;
    ListPreference mClockWeekday;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.statusbar_clock_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        mClockStyle = (ListPreference) findPreference(PREF_ENABLE);
        mClockStyle.setOnPreferenceChangeListener(this);
        mClockStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_CLOCK_STYLE,
                1)));

        mClockAmPmstyle = (ListPreference) prefSet.findPreference(PREF_AM_PM_STYLE);

        if (DateFormat.is24HourFormat(getActivity())) {
                mClockAmPmstyle.setEnabled(false);
                mClockAmPmstyle.setSummary(R.string.status_bar_am_pm_info);
        } else {
                mClockAmPmstyle.setEnabled(true);
	int clockAmPmstyle = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
		Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, 2);
        mClockAmPmstyle.setValue(String.valueOf(clockAmPmstyle));
	mClockAmPmstyle.setSummary(mClockAmPmstyle.getEntry());
        mClockAmPmstyle.setOnPreferenceChangeListener(this);
	}

	mStatusBarClockColor = (Preference) prefSet.findPreference(STATUS_BAR_CLOCK_COLOR);

        mClockWeekday = (ListPreference) findPreference(PREF_CLOCK_WEEKDAY);
        mClockWeekday.setOnPreferenceChangeListener(this);
        mClockWeekday.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_CLOCK_WEEKDAY,
                0)));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mClockAmPmstyle) {
            int clockAmPmstyle = Integer.valueOf((String) newValue);
	    int index = mClockAmPmstyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, clockAmPmstyle);
            mClockAmPmstyle.setSummary(mClockAmPmstyle.getEntries()[index]);
            return true;
        } else if (preference == mClockStyle) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_STYLE, val);
            return true;
        } else if (preference == mClockWeekday) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_WEEKDAY, val);
            return true;
        }
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

	if (preference == mStatusBarClockColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mColorListener, Settings.System.getInt(getActivity().getApplicationContext()
                    .getContentResolver(), Settings.System.STATUS_BAR_CLOCK_COLOR, 0xFF33B5E5));
            cp.setDefaultColor(0xFF33B5E5);
            cp.show();
            return true;
	}
        return false;
    }

    ColorPickerDialog.OnColorChangedListener mColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.STATUS_BAR_CLOCK_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };

}
