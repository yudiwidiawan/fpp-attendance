package com.odoo.core.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by makan on 24/08/2017.
 */

public class FetchAddressIntentService extends IntentService {

    private static final String TAG = FetchAddressIntentService.class.getSimpleName();

    public FetchAddressIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
