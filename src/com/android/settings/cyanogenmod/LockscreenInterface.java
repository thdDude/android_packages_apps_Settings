/*
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
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.SettingsPreferenceFragment;

public class LockscreenInterface extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "LockscreenInterface";
    public static final String KEY_SEE_TRHOUGH_PREF = "lockscreen_see_through";
    public static final String KEY_AUTO_ROTATE_PREF = "lockscreen_auto_rotate";
    public static final String KEY_TEXT_COLOR = "lockscreen_custom_text_color";
    private static final String KEY_ALWAYS_BATTERY_PREF = "lockscreen_battery_status";
    private static final String KEY_LOCKSCREEN_BUTTONS = "lockscreen_buttons";
    private static final String KEY_LOCKSCREEN_MAXIMIZE_WIDGETS = "lockscreen_maximize_widgets";

    private PreferenceScreen mLockscreenButtons;
    private CheckBoxPreference mSeeThrough;
    private CheckBoxPreference mAutoRotate;
    private Preference mTextColor;
    private ListPreference mBatteryStatus;
    private CheckBoxPreference mMaximizeWidgets;

    public boolean hasButtons() {
        return !getResources().getBoolean(com.android.internal.R.bool.config_showNavigationBar);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_interface_settings);

        mSeeThrough = (CheckBoxPreference) findPreference(KEY_SEE_TRHOUGH_PREF);
        mSeeThrough.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.LOCKSCREEN_SEE_THROUGH, 0) == 1));

        mAutoRotate = (CheckBoxPreference) findPreference(KEY_AUTO_ROTATE_PREF);
        mAutoRotate.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.LOCKSCREEN_AUTO_ROTATE, 0) == 1));

        mTextColor = (Preference) findPreference(KEY_TEXT_COLOR);

        // Battery status
        mBatteryStatus = (ListPreference) findPreference(KEY_ALWAYS_BATTERY_PREF);
        if (mBatteryStatus != null) {
            mBatteryStatus.setOnPreferenceChangeListener(this);
        }

        mMaximizeWidgets = (CheckBoxPreference)findPreference(KEY_LOCKSCREEN_MAXIMIZE_WIDGETS);
        if (!Utils.isPhone(getActivity())) {
            getPreferenceScreen().removePreference(mMaximizeWidgets);
            mMaximizeWidgets = null;
        } else {
            mMaximizeWidgets.setOnPreferenceChangeListener(this);
        }

        PreferenceScreen lockscreenButtons = (PreferenceScreen) findPreference(KEY_LOCKSCREEN_BUTTONS);
        if (!hasButtons()) {
            getPreferenceScreen().removePreference(lockscreenButtons);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSeeThrough) {
            int value = mSeeThrough.isChecked() ? 1 : 0;
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_SEE_THROUGH, value);
            return true;
       } else if (preference == mAutoRotate) {
            int value = mAutoRotate.isChecked() ? 1 : 0;
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_AUTO_ROTATE, value);
            return true;
        } else if (preference == mTextColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mTextColorListener, Settings.System.getInt(getActivity()
                    .getApplicationContext()
                    .getContentResolver(), Settings.System.LOCKSCREEN_CUSTOM_TEXT_COLOR, 0xFFFFFFFF));
            cp.setDefaultColor(0xFFFFFFFF);
            cp.show();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void onResume() {
        super.onResume();

        ContentResolver cr = getActivity().getContentResolver();
        if (mBatteryStatus != null) {
            int batteryStatus = Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY, 0);
            mBatteryStatus.setValueIndex(batteryStatus);
            mBatteryStatus.setSummary(mBatteryStatus.getEntries()[batteryStatus]);
        }

        if (mMaximizeWidgets != null) {
            mMaximizeWidgets.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_MAXIMIZE_WIDGETS, 0) == 1);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver cr = getActivity().getContentResolver();

        if (preference == mBatteryStatus) {
            int value = Integer.valueOf((String) objValue);
            int index = mBatteryStatus.findIndexOfValue((String) objValue);
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY, value);
            mBatteryStatus.setSummary(mBatteryStatus.getEntries()[index]);
            return true;
        } else if (preference == mMaximizeWidgets) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_MAXIMIZE_WIDGETS, value ? 1 : 0);
            return true;
        }
        return false;
    }
    ColorPickerDialog.OnColorChangedListener mTextColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.LOCKSCREEN_CUSTOM_TEXT_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };
}
