
package com.android.settings.cyanogenmod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Spannable;
import android.view.Display;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.util.CMDProcessor;
import com.android.settings.util.Helpers;

public class UserInterface extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    public static final String TAG = "UserInterface";
    private static final String KEY_TABLET_UI = "tablet_ui";
    private static final String DUAL_PANE_PREFS = "dual_pane_prefs";

    Preference mLcdDensity;
    private CheckBoxPreference mTabletUI;
    private ListPreference mDualPanePrefs;

    int newDensityValue;

    DensityChanger densityFragment;

    private ContentResolver mContentResolver;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.user_interface_settings);

        PreferenceScreen prefs = getPreferenceScreen();

        mLcdDensity = findPreference("lcd_density_setup");
        String currentProperty = SystemProperties.get("ro.sf.lcd_density");
        try {
            newDensityValue = Integer.parseInt(currentProperty);
        } catch (Exception e) {
            getPreferenceScreen().removePreference(mLcdDensity);
        }

        mLcdDensity.setSummary(getResources().getString(R.string.current_lcd_density) + currentProperty);

        mTabletUI = (CheckBoxPreference) findPreference(KEY_TABLET_UI);
	if (mTabletUI != null)
        mTabletUI.setChecked(Settings.System.getInt(mContentResolver,
                        Settings.System.TABLET_MODE, 0) == 1);

        mDualPanePrefs = (ListPreference) prefs.findPreference(DUAL_PANE_PREFS);
        mDualPanePrefs.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDualPanePrefs) {
            int value = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.DUAL_PANE_PREFS, value);
            getActivity().recreate();
            return true;
        }
        return false;
    }


    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
	boolean value;
        if (preference == mLcdDensity) {
            ((PreferenceActivity) getActivity())
            .startPreferenceFragment(new DensityChanger(), true);
            return true;
        } else if (preference == mTabletUI && (mTabletUI != null)) {
            value = mTabletUI.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.TABLET_MODE,
                    value ? 1 : 0);
            IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.checkService(
                    Context.WINDOW_SERVICE));
            try {
                wm.clearForcedDisplaySize(Display.DEFAULT_DISPLAY);
            } catch (Exception e) {
            }
            return true;
	}
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
