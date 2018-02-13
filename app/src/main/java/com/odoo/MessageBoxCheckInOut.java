package com.odoo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.odoo.core.account.OdooLogin;
import com.odoo.core.support.OUser;

/**
 * Created by makan on 27/07/2017.
 */

public class MessageBoxCheckInOut extends Activity {
    /**
     * Called when the activity is first created.
     */
    private String url;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messageboxcincout);
        url = SharedData.getKey(this, "url").toString();
        if (url == "SAFE_PARCELABLE_NULL_STRING" || url == "") {
            removeAccount();
            finish();
        } else {
            try {
                OUser user = OUser.current(this);
                String timeCheck = getIntent().getStringExtra("check_time");
                int status = getIntent().getIntExtra("status", 0);
                setTitle("FPP Notification");
                Button btn = (Button) findViewById(R.id.Ok);
                TextView tvContent = (TextView) findViewById(R.id.contentText);
                TextView tvContent2 = (TextView) findViewById(R.id.contentText2);
                switch (status) {
                    case 1:
                        tvContent.setText("Checked in at " + timeCheck);
                        tvContent2.setText("Welcome, " + user.getName() + ". Good morning!");
                        btn.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                finish();
                                MessageBoxCheckInOut.this.overridePendingTransition(android.R.anim.fade_in,
                                        android.R.anim.fade_out);
                            }
                        });
                        break;
                    case 2:
                        tvContent.setText("Checked out at " + timeCheck);
                        tvContent2.setText("Goodbye, " + user.getName() + ". Have a good day!");
                        btn.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                finish();
                                MessageBoxCheckInOut.this.overridePendingTransition(android.R.anim.fade_in,
                                        android.R.anim.fade_out);
                            }
                        });
                        break;
                }
            } catch(NullPointerException err) {
                //startActivity(new Intent(MessageBoxCheckInOut.this, OdooLogin.class));
            }

        }

    }

    public void removeAccount() {
        AccountManager accountManager = (AccountManager) getApplicationContext().getSystemService(ACCOUNT_SERVICE);

        // loop through all accounts to remove them
        Account[] accounts = accountManager.getAccounts();
        for (int index = 0; index < accounts.length; index++) {
            if (accounts[index].type.intern() == "com.odoo.fpp.auth") {
                accountManager.removeAccount(accounts[index], null, null);
                Intent intent = new Intent(getApplicationContext(), OdooLogin.class);
                startActivity(intent);
                finish();
                break;
            }
        }
    }
}