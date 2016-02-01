package com.pedometer.asamapps.Models;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.joanzapata.iconify.widget.IconTextView;
import com.pedometer.asamapps.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FontAwesomeViewHolderMenu {
    @Bind(R.id.item_icon) public IconTextView itemIcon;
    @Bind(R.id.item_name) public TextView itemName;

    public FontAwesomeViewHolderMenu(View view) {
        ButterKnife.bind(this, view);
    }
}
