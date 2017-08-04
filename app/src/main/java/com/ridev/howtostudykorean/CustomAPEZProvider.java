package com.ridev.howtostudykorean;

import android.net.Uri;

import com.android.vending.expansion.zipfile.APEZProvider;

import java.io.File;

/**
 * Created by Rica on 8/4/2017.
 */

public class CustomAPEZProvider extends APEZProvider {

    private static final String AUTHORITY = "com.ridev.howtostudykorean.provider";

    @Override
    public String getAuthority() {
        return AUTHORITY;
    }

    public static Uri buildUri(String filePath) {
        StringBuilder builder = new StringBuilder("content://");
        builder.append(AUTHORITY);
        builder.append(File.separator);
        builder.append(filePath);

        return Uri.parse(builder.toString());
    }
}
