package com.evacupet.interfaceHelper;

import android.widget.ImageView;


import com.parse.ParseObject;

public interface UpdateAnimalImageClick {
    void itemClick(ParseObject data, int position, ImageView imageView);
}
