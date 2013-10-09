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

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Display;
import android.view.Window;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.notificationlight.ColorPickerView;

public class LockscreenMisc extends SettingsPreferenceFragment {

    private static final String KEY_LOCKSCREEN_TARGETS = "lockscreen_targets";
    private static final String KEY_STYLE_PREF = "lockscreen_style";
    private static final String KEY_CIRCLES_LOCK_BG_COLOR = "circles_lock_bg_color";
    private static final String KEY_CIRCLES_LOCK_RING_COLOR = "circles_lock_ring_color";
    private static final String KEY_CIRCLES_LOCK_HALO_COLOR = "circles_lock_halo_color";
    private static final String KEY_CIRCLES_LOCK_WAVE_COLOR = "circles_lock_wave_color";
    private static final String KEY_OPTIMUS_COLOR_CATEGORY = "optimus_color";
    private static final int LOCK_STYLE_JB = 0;
    private static final int LOCK_STYLE_OP4 = 4;


    private Preference mLockBgColor;
    private Preference mLockRingColor;
    private Preference mLockHaloColor;
    private Preference mLockWaveColor;

    private int mLockscreenStyle;
    private boolean mUseJbLockscreen;
    private boolean mUseOp4Lockscreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_misc_settings);

        mLockBgColor = (Preference) findPreference(KEY_CIRCLES_LOCK_BG_COLOR);
        mLockRingColor = (Preference) findPreference(KEY_CIRCLES_LOCK_RING_COLOR);
        mLockHaloColor = (Preference) findPreference(KEY_CIRCLES_LOCK_HALO_COLOR);
        mLockWaveColor = (Preference) findPreference(KEY_CIRCLES_LOCK_WAVE_COLOR);

    }

	private void check_lockscreentarget() {
            mLockscreenStyle = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_STYLE, 0);
            mUseJbLockscreen = (mLockscreenStyle == LOCK_STYLE_JB);
            Preference lockTargets = findPreference(KEY_LOCKSCREEN_TARGETS);
            if (lockTargets != null) {
		if (!mUseJbLockscreen) {
		getPreferenceScreen().removePreference(lockTargets);
		}
	    }
	}

	private void check_optimus() {
       	PreferenceCategory optimuscolorCategory = (PreferenceCategory) findPreference(KEY_OPTIMUS_COLOR_CATEGORY);
            mLockscreenStyle = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_STYLE, 0);
            mUseOp4Lockscreen = (mLockscreenStyle == LOCK_STYLE_OP4);
            if (optimuscolorCategory != null) {
		if (!mUseOp4Lockscreen) {
		getPreferenceScreen().removePreference(optimuscolorCategory);
		}
	    }
	}

    public void onResume() {
        super.onResume();
	    check_lockscreentarget();
	    check_optimus();
    }


    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mLockBgColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mCirclesBgColorListener, Settings.System.getInt(getActivity()
                    .getApplicationContext()
                    .getContentResolver(), Settings.System.CIRCLES_LOCK_BG_COLOR, 0xD2000000));
            cp.setDefaultColor(0xD2000000);
            cp.show();
            return true;
        } else if (preference == mLockRingColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mCirclesRingColorListener, Settings.System.getInt(getActivity()
                    .getApplicationContext()
                    .getContentResolver(), Settings.System.CIRCLES_LOCK_RING_COLOR, 0xFFFFFFFF));
            cp.setDefaultColor(0xFFFFFFFF);
            cp.show();
            return true;
        } else if (preference == mLockHaloColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mCirclesHaloColorListener, Settings.System.getInt(getActivity()
                    .getApplicationContext()
                    .getContentResolver(), Settings.System.CIRCLES_LOCK_HALO_COLOR, 0xFFFFFFFF));
            cp.setDefaultColor(0xFFFFFFFF);
            cp.show();
            return true;
        } else if (preference == mLockWaveColor) {
            ColorPickerDialog cp = new ColorPickerDialog(getActivity(),
                    mCirclesWaveColorListener, Settings.System.getInt(getActivity()
                    .getApplicationContext()
                    .getContentResolver(), Settings.System.CIRCLES_LOCK_WAVE_COLOR, 0xD2FFFFFF));
            cp.setDefaultColor(0xD2FFFFFF);
            cp.show();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

ColorPickerDialog.OnColorChangedListener mCirclesBgColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.CIRCLES_LOCK_BG_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };
    ColorPickerDialog.OnColorChangedListener mCirclesRingColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.CIRCLES_LOCK_RING_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };
    ColorPickerDialog.OnColorChangedListener mCirclesHaloColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.CIRCLES_LOCK_HALO_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };
    ColorPickerDialog.OnColorChangedListener mCirclesWaveColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.CIRCLES_LOCK_WAVE_COLOR, color);
            }
            public void colorUpdate(int color) {
            }
    };

}

