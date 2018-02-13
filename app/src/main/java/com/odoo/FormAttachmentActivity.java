package com.odoo;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableString;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.odoo.adapters.CommonListViewEditTextAdapter;
import com.odoo.adapters.ListItem;
import com.odoo.adapters.UserAutoCompleteAdapter;
import com.odoo.adapters.UserMessageAutoCompleteAdapter;
import com.odoo.base.addons.ir.feature.OFileManager;
import com.odoo.core.account.OdooLogin;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.BitmapUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;


public class FormAttachmentActivity extends Activity implements View.OnClickListener {

    int task_id;
    private ImageButton imgBtnDate, closeAddMessage, plusHours, minusHours;
    private OdooUtility odoo;
    private OUser user;
    private String uid, password, serverAddress, database, url;
    private int timesheet_id = 0, project_id = 0, uids = 0;
    private Calendar calendar;
    private int currentYear, currentMonth, currentDay;
    private Button btnSendMessage;
    private ImageView imgAttachmentPrev;
    private String img_binary;
    private EditText edtFileName;
    String createWriteWithoutSec;
    private LinearLayout layoutImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_attachment);
        if (url == "SAFE_PARCELABLE_NULL_STRING" || url == "") {
            removeAccount();
            finish();
        } else {
            try {
                user = OUser.current(this);
                uid = user.getUserId().toString();
                password = user.getPassword();
                serverAddress = user.getHost();
                database = user.getDatabase();
                odoo = new OdooUtility(user.getHost(), "object");
                task_id = getIntent().getIntExtra("task_id", 0);
                project_id = getIntent().getIntExtra("project_id", 0);
                img_binary = getIntent().getStringExtra("images");
                Gson gson = new Gson();

                Type type = new TypeToken<ArrayList<String>>() {}.getType();
                ArrayList<String> arrayList = gson.fromJson(img_binary, type);
                layoutImage = (LinearLayout) findViewById(R.id.layoutImage);
                imgAttachmentPrev = (ImageView) findViewById(R.id.imgAttachmentPreview);

                for(int i=0;i<arrayList.size();i++) {
                    ImageView imgView = (ImageView) findViewById(R.id.imgAttachmentPreview);
                    imgView.setImageBitmap(BitmapUtils.getBitmapImage(FormAttachmentActivity.this,
                            arrayList.get(i)));
                    layoutImage.addView(imgView);
                }

                closeAddMessage = (ImageButton) findViewById(R.id.closeAddAttachment);
                btnSendMessage = (Button) findViewById(R.id.btnAddAttachmenttoMessage);


                Bitmap bitmap = BitmapUtils.getBitmapImage(FormAttachmentActivity.this,
                        img_binary);
                imgAttachmentPrev.setImageBitmap(bitmap);

                closeAddMessage.setOnClickListener(this);
                btnSendMessage.setOnClickListener(this);
                //btnTakePicture.setOnClickListener(this);
                //btnFromGallery.setOnClickListener(this);


                uids = Integer.parseInt(uid);
                SimpleDateFormat sdfCreateWrite = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                SimpleDateFormat sdfCreateWriteWSec = new SimpleDateFormat("yyyyMMdd ");
                //createWrite = sdfCreateWrite.format(new Date());
                createWriteWithoutSec = sdfCreateWriteWSec.format(new Date());

                edtFileName.setText(createWriteWithoutSec);


            } catch (NullPointerException err) {
                odoo.MessageDialog(FormAttachmentActivity.this, err.getMessage());
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FormAttachmentActivity.this.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSendMessage:

                break;
            case R.id.closeAddMessage:
                finish();
                FormAttachmentActivity.this.overridePendingTransition(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                break;
        }
    }


}
