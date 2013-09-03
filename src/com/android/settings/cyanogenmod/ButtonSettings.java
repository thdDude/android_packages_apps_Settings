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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Slog;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.util.ShortcutPickerHelper;
import com.android.settings.SettingsPreferenceFragment;

public class ButtonSettings extends SettingsPreferenceFragment implements
        ShortcutPickerHelper.OnPickListener, OnPreferenceChangeListener {

    private static final String KEY_MENU_ENABLED = "key_menu_enabled";
    private static final String KEY_BACK_ENABLED = "key_back_enabled";
    private static final String KEY_HOME_ENABLED = "key_home_enabled";

    private static final String HARDWARE_KEYS_CATEGORY_BINDINGS = "hardware_keys_bindings";
    private static final String HARDWARE_KEYS_HOME_LONG_PRESS = "hardware_keys_home_long_press";
    private static final String HARDWARE_KEYS_MENU_PRESS = "hardware_keys_menu_press";
    private static final String HARDWARE_KEYS_MENU_LONG_PRESS = "hardware_keys_menu_long_press";
    private static final String HARDWARE_KEYS_ASSIST_PRESS = "hardware_keys_assist_press";
    private static final String HARDWARE_KEYS_ASSIST_LONG_PRESS = "hardware_keys_assist_long_press";
    private static final String HARDWARE_KEYS_APP_SWITCH_PRESS = "hardware_keys_app_switch_press";
    private static final String HARDWARE_KEYS_APP_SWITCH_LONG_PRESS = "hardware_keys_app_switch_long_press";

    private static final String CATEGORY_VOLUME = "volume_keys";
    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_BACKLIGHT = "key_backlight";

    private static final String KEY_BUTTON_BACKLIGHT = "button_backlight";

    // Available custom actions to perform on a key press.
    // Must match values for KEY_HOME_LONG_PRESS_ACTION in:
    // frameworks/base/core/java/android/provider/Settings.java
    private static final int ACTION_NOTHING = 0;
    private static final int ACTION_MENU = 1;
    private static final int ACTION_APP_SWITCH = 2;
    private static final int ACTION_SEARCH = 3;
    private static final int ACTION_VOICE_SEARCH = 4;
    private static final int ACTION_IN_APP_SEARCH = 5;
    private static final int ACTION_LAST_APP = 6;
    private static final int ACTION_POWER = 7;
    private static final int ACTION_CUSTOM_APP = 8;

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    public static final int KEY_MASK_HOME = 0x01;
    public static final int KEY_MASK_BACK = 0x02;
    public static final int KEY_MASK_MENU = 0x04;
    public static final int KEY_MASK_ASSIST = 0x08;
    public static final int KEY_MASK_APP_SWITCH = 0x10;

    private CheckBoxPreference mMenuKeyEnabled;
    private CheckBoxPreference mBackKeyEnabled;
    private CheckBoxPreference mHomeKeyEnabled;

    private ListPreference mHomeLongPressAction;
    private ListPreference mMenuPressAction;
    private ListPreference mMenuLongPressAction;
    private ListPreference mAssistPressAction;
    private ListPreference mAssistLongPressAction;
    private ListPreference mAppSwitchPressAction;
    private ListPreference mAppSwitchLongPressAction;
    private CheckBoxPreference mShowActionOverflow;

    private ShortcutPickerHelper mPicker;
    private Preference mCustomAppPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
        final boolean hasAssistKey = (deviceKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;

        addPreferencesFromResource(R.xml.button_settings);
        PreferenceScreen prefSet = getPreferenceScreen();

        boolean hasAnyBindableKey = false;
        final PreferenceCategory volumeCategory =
                (PreferenceCategory) prefSet.findPreference(CATEGORY_VOLUME);
        final PreferenceCategory homeCategory =
                (PreferenceCategory) prefSet.findPreference(CATEGORY_HOME);

        mMenuKeyEnabled = (CheckBoxPreference) prefSet.findPreference(KEY_MENU_ENABLED);
        mBackKeyEnabled = (CheckBoxPreference) prefSet.findPreference(KEY_BACK_ENABLED);
        mHomeKeyEnabled = (CheckBoxPreference) prefSet.findPreference(KEY_HOME_ENABLED);

        mPicker = new ShortcutPickerHelper(this, this);

        mHomeLongPressAction = (ListPreference) prefSet.findPreference(
                HARDWARE_KEYS_HOME_LONG_PRESS);
        mMenuPressAction = (ListPreference) prefSet.findPreference(
                HARDWARE_KEYS_MENU_PRESS);
        mMenuLongPressAction = (ListPreference) prefSet.findPreference(
                HARDWARE_KEYS_MENU_LONG_PRESS);
        mAssistPressAction = (ListPreference) prefSet.findPreference(
                HARDWARE_KEYS_ASSIST_PRESS);
        mAssistLongPressAction = (ListPreference) prefSet.findPreference(
                HARDWARE_KEYS_ASSIST_LONG_PRESS);
        mAppSwitchPressAction = (ListPreference) prefSet.findPreference(
                HARDWARE_KEYS_APP_SWITCH_PRESS);
        mAppSwitchLongPressAction = (ListPreference) prefSet.findPreference(
                HARDWARE_KEYS_APP_SWITCH_LONG_PRESS);
        PreferenceCategory bindingsCategory = (PreferenceCategory) prefSet.findPreference(
                HARDWARE_KEYS_CATEGORY_BINDINGS);

        if (hasHomeKey) {
            hasAnyBindableKey = true;
            if (!getResources().getBoolean(R.bool.config_show_homeWake)) {
		homeCategory.removePreference(findPreference(Settings.System.HOME_WAKE_SCREEN));
		prefSet.removePreference(homeCategory);
            }

            String homeLongPressAction = Settings.System.getString(getContentResolver(),
                    Settings.System.KEY_HOME_LONG_PRESS_ACTION);
            if (hasAppSwitchKey) {
                if (homeLongPressAction == null)
                    homeLongPressAction = Integer.toString(ACTION_NOTHING);
            } else {
                if (homeLongPressAction == null)
                    homeLongPressAction = Integer.toString(ACTION_APP_SWITCH);
            }
            try {
                Integer.parseInt(homeLongPressAction);
                mHomeLongPressAction.setValue(homeLongPressAction);
                mHomeLongPressAction.setSummary(mHomeLongPressAction.getEntry());
            } catch (NumberFormatException e) {
                mHomeLongPressAction.setValue(Integer.toString(ACTION_CUSTOM_APP));
                mHomeLongPressAction.setSummary(mPicker.getFriendlyNameForUri(homeLongPressAction));
            }

            mHomeLongPressAction.setOnPreferenceChangeListener(this);
        } else {
            bindingsCategory.removePreference(mHomeLongPressAction);
        }

        if (hasMenuKey) {
            hasAnyBindableKey = true;
            String menuPressAction = Settings.System.getString(getContentResolver(),
                    Settings.System.KEY_MENU_ACTION);
            if (menuPressAction == null)
                menuPressAction = Integer.toString(ACTION_MENU);

            try {
                Integer.parseInt(menuPressAction);
                mMenuPressAction.setValue(menuPressAction);
                mMenuPressAction.setSummary(mMenuPressAction.getEntry());
            } catch (NumberFormatException e) {
                mMenuPressAction.setValue(Integer.toString(ACTION_CUSTOM_APP));
                mMenuPressAction.setSummary(mPicker.getFriendlyNameForUri(menuPressAction));
            }

            mMenuPressAction.setOnPreferenceChangeListener(this);

            String menuLongPressAction;
            menuLongPressAction = Settings.System.getString(getContentResolver(),
                    Settings.System.KEY_MENU_LONG_PRESS_ACTION);
            if (hasAssistKey) {
                if (menuLongPressAction == null)
                    menuLongPressAction = Integer.toString(ACTION_NOTHING);
            } else {
                if (menuLongPressAction == null)
                    menuLongPressAction = Integer.toString(ACTION_SEARCH);
            }
            try {
                Integer.parseInt(menuLongPressAction);
                mMenuLongPressAction.setValue(menuLongPressAction);
                mMenuLongPressAction.setSummary(mMenuLongPressAction.getEntry());
            } catch (NumberFormatException e) {
                mMenuLongPressAction.setValue(Integer.toString(ACTION_CUSTOM_APP));
                mMenuLongPressAction.setSummary(mPicker.getFriendlyNameForUri(menuLongPressAction));
            }

            mMenuLongPressAction.setOnPreferenceChangeListener(this);
        } else {
            bindingsCategory.removePreference(mMenuPressAction);
            bindingsCategory.removePreference(mMenuLongPressAction);
        }

        if (hasAssistKey) {
            hasAnyBindableKey = true;
            String assistPressAction = Settings.System.getString(getContentResolver(),
                    Settings.System.KEY_ASSIST_ACTION);
            if (assistPressAction == null)
                assistPressAction = Integer.toString(ACTION_SEARCH);

            try {
                Integer.parseInt(assistPressAction);
                mAssistPressAction.setValue(assistPressAction);
                mAssistPressAction.setSummary(mAssistPressAction.getEntry());
            } catch (NumberFormatException e) {
                mAssistPressAction.setValue(Integer.toString(ACTION_CUSTOM_APP));
                mAssistPressAction.setSummary(mPicker.getFriendlyNameForUri(assistPressAction));
            }

            mAssistPressAction.setOnPreferenceChangeListener(this);

            String assistLongPressAction = Settings.System.getString(getContentResolver(),
                    Settings.System.KEY_ASSIST_LONG_PRESS_ACTION);
            if (assistLongPressAction == null)
                assistLongPressAction = Integer.toString(ACTION_VOICE_SEARCH);

            try {
                Integer.parseInt(assistLongPressAction);
                mAssistLongPressAction.setValue(assistLongPressAction);
                mAssistLongPressAction.setSummary(mAssistLongPressAction.getEntry());
            } catch (NumberFormatException e) {
                mAssistLongPressAction.setValue(Integer.toString(ACTION_CUSTOM_APP));
                mAssistLongPressAction.setSummary(mPicker.getFriendlyNameForUri(assistLongPressAction));
            }

            mAssistLongPressAction.setOnPreferenceChangeListener(this);
        } else {
            bindingsCategory.removePreference(mAssistPressAction);
            bindingsCategory.removePreference(mAssistLongPressAction);
        }

        if (hasAppSwitchKey) {
            hasAnyBindableKey = true;
            String appSwitchPressAction = Settings.System.getString(getContentResolver(),
                    Settings.System.KEY_APP_SWITCH_ACTION);
            if (appSwitchPressAction == null)
                appSwitchPressAction = Integer.toString(ACTION_APP_SWITCH);

            try {
                Integer.parseInt(appSwitchPressAction);
                mAppSwitchPressAction.setValue(appSwitchPressAction);
                mAppSwitchPressAction.setSummary(mAppSwitchPressAction.getEntry());
            } catch (NumberFormatException e) {
                mAppSwitchPressAction.setValue(Integer.toString(ACTION_CUSTOM_APP));
                mAppSwitchPressAction.setSummary(mPicker.getFriendlyNameForUri(appSwitchPressAction));
            }

            mAppSwitchPressAction.setOnPreferenceChangeListener(this);

            String appSwitchLongPressAction = Settings.System.getString(getContentResolver(),
                    Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            if (appSwitchLongPressAction == null)
                appSwitchLongPressAction = Integer.toString(ACTION_NOTHING);

            try {
                Integer.parseInt(appSwitchLongPressAction);
                mAppSwitchLongPressAction.setValue(appSwitchLongPressAction);
                mAppSwitchLongPressAction.setSummary(mAppSwitchLongPressAction.getEntry());
            } catch (NumberFormatException e) {
                mAppSwitchLongPressAction.setValue(Integer.toString(ACTION_CUSTOM_APP));
                mAppSwitchLongPressAction.setSummary(mPicker.getFriendlyNameForUri(appSwitchLongPressAction));
            }

            mAppSwitchLongPressAction.setOnPreferenceChangeListener(this);
        } else {
            bindingsCategory.removePreference(mAppSwitchPressAction);
            bindingsCategory.removePreference(mAppSwitchLongPressAction);
        }

        mMenuKeyEnabled.setChecked((Settings.System.getInt(getActivity().
		getApplicationContext().getContentResolver(),
                Settings.System.KEY_MENU_ENABLED, 1) == 1));
        mBackKeyEnabled.setChecked((Settings.System.getInt(getActivity().
		getApplicationContext().getContentResolver(),
                Settings.System.KEY_BACK_ENABLED, 1) == 1));
        mHomeKeyEnabled.setChecked((Settings.System.getInt(getActivity().
		getApplicationContext().getContentResolver(),
                Settings.System.KEY_HOME_ENABLED, 1) == 1));

        if (hasAnyBindableKey) {
            mShowActionOverflow = (CheckBoxPreference)
                prefSet.findPreference(Settings.System.UI_FORCE_OVERFLOW_BUTTON);
        }

        if (!hasAnyBindableKey) {
            prefSet.removePreference(findPreference(Settings.System.HARDWARE_KEY_REBINDING));
        }

        if (Utils.hasVolumeRocker(getActivity())) {

            if (!getResources().getBoolean(R.bool.config_show_volumeRockerWake)) {
                volumeCategory.removePreference(findPreference(Settings.System.VOLUME_WAKE_SCREEN));
            }
        } else {
            prefSet.removePreference(volumeCategory);
        }

        final ButtonBacklightBrightness backlight =
                (ButtonBacklightBrightness) findPreference(KEY_BUTTON_BACKLIGHT);
        if (!backlight.isButtonSupported() && !backlight.isKeyboardSupported()) {
            prefSet.removePreference(backlight);
        }

    }

    private void handleCheckboxClick(CheckBoxPreference pref, String setting) {
        Settings.System.putInt(getContentResolver(), setting, pref.isChecked() ? 1 : 0);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = Integer.valueOf((String) newValue);
        if (value == ACTION_CUSTOM_APP) {
            mCustomAppPreference = preference;
            mPicker.pickShortcut();
            return true;
        } else {
            if (preference == mHomeLongPressAction) {
                int index = mHomeLongPressAction.findIndexOfValue((String) newValue);
                mHomeLongPressAction.setSummary(
                        mHomeLongPressAction.getEntries()[index]);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.KEY_HOME_LONG_PRESS_ACTION, value);
                return true;
            } else if (preference == mMenuPressAction) {
                int index = mMenuPressAction.findIndexOfValue((String) newValue);
                mMenuPressAction.setSummary(
                        mMenuPressAction.getEntries()[index]);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.KEY_MENU_ACTION, value);
                return true;
            } else if (preference == mMenuLongPressAction) {
                int index = mMenuLongPressAction.findIndexOfValue((String) newValue);
                mMenuLongPressAction.setSummary(
                        mMenuLongPressAction.getEntries()[index]);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.KEY_MENU_LONG_PRESS_ACTION, value);
                return true;
            } else if (preference == mAssistPressAction) {
                int index = mAssistPressAction.findIndexOfValue((String) newValue);
                mAssistPressAction.setSummary(
                        mAssistPressAction.getEntries()[index]);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.KEY_ASSIST_ACTION, value);
                return true;
            } else if (preference == mAssistLongPressAction) {
                int index = mAssistLongPressAction.findIndexOfValue((String) newValue);
                mAssistLongPressAction.setSummary(
                        mAssistLongPressAction.getEntries()[index]);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.KEY_ASSIST_LONG_PRESS_ACTION, value);
                return true;
            } else if (preference == mAppSwitchPressAction) {
                int index = mAppSwitchPressAction.findIndexOfValue((String) newValue);
                mAppSwitchPressAction.setSummary(
                        mAppSwitchPressAction.getEntries()[index]);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.KEY_APP_SWITCH_ACTION, value);
                return true;
            } else if (preference == mAppSwitchLongPressAction) {
                int index = mAppSwitchLongPressAction.findIndexOfValue((String) newValue);
                mAppSwitchLongPressAction.setSummary(
                        mAppSwitchLongPressAction.getEntries()[index]);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION, value);
                return true;
            }
        }
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
	boolean enabled;
        if (preference == mShowActionOverflow) {
            enabled = mShowActionOverflow.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.UI_FORCE_OVERFLOW_BUTTON,
                    enabled ? 1 : 0);
            // Show appropriate
            if (enabled) {
                Toast.makeText(getActivity(), R.string.hardware_keys_show_overflow_toast_enable,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), R.string.hardware_keys_show_overflow_toast_disable,
                        Toast.LENGTH_LONG).show();
            }
            return true;
        } else if (preference == mMenuKeyEnabled) {
            handleCheckboxClick(mMenuKeyEnabled, Settings.System.KEY_MENU_ENABLED);
            return true;
        } else if (preference == mBackKeyEnabled) {
            handleCheckboxClick(mBackKeyEnabled, Settings.System.KEY_BACK_ENABLED);
            return true;
        } else if (preference == mHomeKeyEnabled) {
            handleCheckboxClick(mHomeKeyEnabled, Settings.System.KEY_HOME_ENABLED);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
        Preference preference = mCustomAppPreference;
        if (preference == mHomeLongPressAction) {
            Settings.System.putString(getContentResolver(),
                    Settings.System.KEY_HOME_LONG_PRESS_ACTION, uri);
        } else if (preference == mMenuPressAction) {
            Settings.System.putString(getContentResolver(),
                    Settings.System.KEY_MENU_ACTION, uri);
        } else if (preference == mMenuLongPressAction) {
            Settings.System.putString(getContentResolver(),
                    Settings.System.KEY_MENU_LONG_PRESS_ACTION, uri);
        } else if (preference == mAssistPressAction) {
            Settings.System.putString(getContentResolver(),
                    Settings.System.KEY_ASSIST_ACTION, uri);
        } else if (preference == mAssistLongPressAction) {
            Settings.System.putString(getContentResolver(),
                    Settings.System.KEY_ASSIST_LONG_PRESS_ACTION, uri);
        } else if (preference == mAppSwitchPressAction) {
            Settings.System.putString(getContentResolver(),
                    Settings.System.KEY_APP_SWITCH_ACTION, uri);
        } else if (preference == mAppSwitchLongPressAction) {
            Settings.System.putString(getContentResolver(),
                    Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION, uri);
        }
        preference.setSummary(friendlyName);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
