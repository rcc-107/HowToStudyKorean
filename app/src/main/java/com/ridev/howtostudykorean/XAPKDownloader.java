package com.ridev.howtostudykorean;

import com.google.android.vending.expansion.downloader.impl.DownloaderService;

/**
 * Created by Rica on 5/17/2017.
 */

public class XAPKDownloader extends DownloaderService {

    public static final String BASE64_PUBLIC_KEY = null;
    public static final byte[] SALT = new byte[]{ 1, 42, -12, -1, 54, 98,
            -100, -12, 43, 2, -8, -4, 9, 5, -106, -107, -33, 45, -1, 84};

    @Override
    public String getPublicKey() {
        return BASE64_PUBLIC_KEY;
    }

    @Override
    public byte[] getSALT() {
        return SALT;
    }

    @Override
    public String getAlarmReceiverClassName() {
        return null;
    }
}
