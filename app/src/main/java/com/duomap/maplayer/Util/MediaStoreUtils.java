package com.duomap.maplayer.Util;

import android.content.Intent;

/**
 * Created by Administrator on 2018-01-16.
 */

public class MediaStoreUtils {
    public static int REQUEST_PICTURE = 1;
    public static int PHOTO_REQUEST_CUT = 3;

    public static Intent getPickImageIntent() {
        final Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        return Intent.createChooser(intent, "请选择图片");
    }

}
