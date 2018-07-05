/*
** Copyright 2013, The ChameleonOS Open Source Project
** Copyright 2016, The OmniROM Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package com.xtended.xtensions.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.settings.R;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {

    private final String TAG = getClass().getName();

    private static final String ANDROIDNS = "http://schemas.android.com/apk/res/android";
    private static final String SETTINGS = "http://schemas.android.com/apk/res/com.android.settings";
    private static final int DEFAULT_VALUE = 50;

    private int mMaxValue      = 100;
    private int mMinValue      = 0;
    private int mInterval      = 1;
    private int mCurrentValue;
    private String mUnitsLeft  = "";
    private String mUnitsRight = "";
    private SeekBar mSeekBar;
    private TextView mStatusText;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPreference(context, attrs);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPreference(context, attrs);
    }

    private void initPreference(Context context, AttributeSet attrs) {
        setValuesFromXml(attrs);
        setLayoutResource(R.layout.preference_seek_bar);
    }

    private void setValuesFromXml(AttributeSet attrs) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(
                      attrs, R.styleable.SeekBarPreference);

        mMaxValue = attrs.getAttributeIntValue(ANDROIDNS, "max", 100);
        mMinValue = attrs.getAttributeIntValue(SETTINGS, "min", 0);
        mUnitsLeft = getAttributeStringValue(attrs, SETTINGS, "unitsLeft", "");
        String units = getAttributeStringValue(attrs, SETTINGS, "units", "");
        mUnitsRight = getAttributeStringValue(attrs, SETTINGS, "unitsRight", units);

        Integer id = typedArray.getResourceId(R.styleable.SeekBarPreference_unitsRight, 0);
        if (id > 0) {
            mUnitsRight = getContext().getResources().getString(id);
        }
        id = typedArray.getResourceId(R.styleable.SeekBarPreference_unitsLeft, 0);
        if (id > 0) {
            mUnitsLeft = getContext().getResources().getString(id);
        }

        try {
            String newInterval = attrs.getAttributeValue(SETTINGS, "interval");
            if(newInterval != null)
                mInterval = Integer.parseInt(newInterval);
        } catch(Exception e) {
            Log.e(TAG, "Invalid interval value", e);
        }
        typedArray.recycle();
    }

    private String getAttributeStringValue(AttributeSet attrs, String namespace, String name, String defaultValue) {
        String value = attrs.getAttributeValue(namespace, name);
        if(value == null) {
            value = defaultValue;
        }
        return value;
    }

    @Override
    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);
        this.setShouldDisableView(true);
        if (mSeekBar != null) {
            mSeekBar.setEnabled(!disableDependent);
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mSeekBar = (SeekBar) holder.findViewById(R.id.seekbar);
        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setProgress(mCurrentValue - mMinValue);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setEnabled(isEnabled());

        mStatusText = (TextView)holder.findViewById(R.id.seekBarPrefValue);
        mStatusText.setText(String.valueOf(mCurrentValue));
        mStatusText.setMinimumWidth(30);

        TextView unitsRight = (TextView)holder.findViewById(R.id.seekBarPrefUnitsRight);
        unitsRight.setText(mUnitsRight);
        TextView unitsLeft = (TextView)holder.findViewById(R.id.seekBarPrefUnitsLeft);
        unitsLeft.setText(mUnitsLeft);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int newValue = progress + mMinValue;

        if(newValue > mMaxValue) {
            newValue = mMaxValue;
        } else if(newValue < mMinValue) {
            newValue = mMinValue;
        } else if(mInterval != 1 && newValue % mInterval != 0) {
            newValue = Math.round(((float)newValue)/mInterval)*mInterval;
        }

        // change rejected, revert to the previous value
        if (!callChangeListener(newValue)) {
            seekBar.setProgress(mCurrentValue - mMinValue);
            return;
        }

        // change accepted, store it
        mCurrentValue = newValue;
        mStatusText.setText(String.valueOf(newValue));
        persistInt(newValue);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        notifyChanged();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index){
        int defaultValue = ta.getInt(index, DEFAULT_VALUE);
        return defaultValue;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            mCurrentValue = getPersistedInt(mCurrentValue);
        } else {
            int temp = 0;
            try {
                temp = (Integer)defaultValue;
            } catch(Exception ex) {
                Log.e(TAG, "Invalid default value: " + defaultValue.toString());
            }
            persistInt(temp);
            mCurrentValue = temp;
        }
    }

    public void setValue(int value) {
        mCurrentValue = value;
    }

    public void setMaxValue(int value) {
        mMaxValue = value;
        if (mSeekBar != null) {
            mSeekBar.setMax(mMaxValue - mMinValue);
        }
    }

    public void setMinValue(int value) {
        mMinValue = value;
        if (mSeekBar != null) {
            mSeekBar.setMax(mMaxValue - mMinValue);
        }
    }

    @Override
    public void setEnabled (boolean enabled) {
        if (mSeekBar != null) {
            mSeekBar.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }
}
