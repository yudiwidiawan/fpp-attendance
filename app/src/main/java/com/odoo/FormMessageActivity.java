package com.odoo;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.odoo.adapters.AttachmentAdapter;
import com.odoo.adapters.CommonListViewEditTextAdapter;
import com.odoo.adapters.ListItem;
import com.odoo.adapters.TaskAdapter;
import com.odoo.adapters.UserAutoCompleteAdapter;
import com.odoo.adapters.UserMessageAutoCompleteAdapter;
import com.odoo.base.addons.ir.feature.OFileManager;
import com.odoo.core.account.OdooLogin;
import com.odoo.core.support.OUser;
import com.odoo.models.AttachmentModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Array;
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


public class FormMessageActivity extends AppCompatActivity implements View.OnClickListener {

    String theDate, theHours;
    int task_id;
    private ImageButton imgBtnDate, closeAddMessage, plusHours, minusHours;
    private String currentProjectID, currentTaskID;
    private OdooUtility odoo;
    private OUser user;
    private String uid, password, serverAddress, database, url;
    private String act = "add";
    private int timesheet_id = 0, project_id = 0, uids = 0;
    private long createTaskId, deleteTaskId, searchTaskPartnerId, createTaskIdAttachment,
            searchTaskAttachment, createAttachmentRel;
    private Calendar calendar;
    private int currentYear, currentMonth, currentDay;
    private Button btnSendMessage, btnNextLevel, btnPrevLevel, btnAddNotes,
            btnAddObstacles, btnAddNextToDo, btnLevel, btnAddAttachment;
    private ImageButton btnTakePicture,
            btnFromGallery;
    private LinearLayout layoutConfirmDelete;
    private String bodyContent, project_name;
    private TextView txvProjectTitle;
    private List dataToInput, conditionsPartner, attachmentToInput, consAttachment;
    private Map fieldsPartner, fieldsAttachment;
    private String[] level = new String[]{"#Level : Internal",
            "#Level : Local", "#Level : Japan"};
    private int levelLocation = 0;
    private EditText edtObstacles, edtNextActivities;
    private AutoCompleteTextView edtNotes;
    String createWriteWithoutSec;
    public boolean menuAttachmentOpen = false;
    private ArrayList<OUser> partnerModelArrayList = new ArrayList<>();
    private UserMessageAutoCompleteAdapter partnerAutoCompleteAdapter;
    private ScrollView scrollViewMessageUtama;
    private LinearLayout viewAttachment, uploadingProgress, listAttachments;
    private List attachment_ids = new ArrayList<>();
    private ProgressDialog dialog;
    private ListView lvAttachment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_message);
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
                project_name = getIntent().getStringExtra("project_name");

                btnSendMessage = (Button) findViewById(R.id.btnSendMessage);
                btnAddNotes = (Button) findViewById(R.id.btnNotes);
                btnLevel = (Button) findViewById(R.id.btnLevel);
                btnNextLevel = (Button) findViewById(R.id.btnNextLevel);
                btnPrevLevel = (Button) findViewById(R.id.btnPrevLevel);
                btnAddObstacles = (Button) findViewById(R.id.btnObstacles);
                btnAddNextToDo = (Button) findViewById(R.id.btnNextThingToDo);
                txvProjectTitle = (TextView) findViewById(R.id.txvProjectTitle);
                btnAddAttachment = (Button) findViewById(R.id.btnAttachFile);
                btnTakePicture = (ImageButton) findViewById(R.id.btnTakeImage);
                btnFromGallery = (ImageButton) findViewById(R.id.btnFromGallery);
                edtNotes = (AutoCompleteTextView) findViewById(R.id.edtNotes);
                edtObstacles = (EditText) findViewById(R.id.edtObstacles);
                edtNextActivities = (EditText) findViewById(R.id.edtNextToDo);
                scrollViewMessageUtama = (ScrollView) findViewById(R.id.layoutMessageUtama);
                viewAttachment = (LinearLayout) findViewById(R.id.layoutAttachment);
                uploadingProgress = (LinearLayout) findViewById(R.id.layoutUploading);
                listAttachments = (LinearLayout) findViewById(R.id.layoutAttachments);

                btnSendMessage.setOnClickListener(this);
                btnAddNotes.setOnClickListener(this);
                btnAddObstacles.setOnClickListener(this);
                btnAddNextToDo.setOnClickListener(this);
                btnNextLevel.setOnClickListener(this);
                btnPrevLevel.setOnClickListener(this);
                btnAddAttachment.setOnClickListener(this);
                //btnTakePicture.setOnClickListener(this);
                //btnFromGallery.setOnClickListener(this);

                setupDialog();

                if (shouldAskPermissions()) {
                    askPermissions();
                }

                if(Build.VERSION.SDK_INT > 9) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }
                imagesEncodedList = new ArrayList<String>();
                uids = Integer.parseInt(uid);
                SimpleDateFormat sdfCreateWrite = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                SimpleDateFormat sdfCreateWriteWSec = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //createWrite = sdfCreateWrite.format(new Date());
                createWriteWithoutSec = sdfCreateWriteWSec.format(new Date());

                txvProjectTitle.setText("#Project : " + project_name);

                getSupportActionBar().setBackgroundDrawable(getApplicationContext().getResources().getDrawable(R.color.odoo_theme));
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                /*List data = Arrays.asList(
                        Arrays.asList(72902));
                deleteTaskId = odoo.delete(listenerMessage, user.getDatabase(),
                        String.valueOf(user.getUserId()), user.getPassword(), "mail.message", data);*/

                fieldsPartner = new HashMap() {{
                    put("fields", Arrays.asList("id", "name"));
                }};

                conditionsPartner = Arrays.asList(Arrays.asList(
                        Arrays.asList("active", "=", true)));

                searchTaskPartnerId = odoo.search_read(listenerPartner, user.getDatabase(),
                        String.valueOf(user.getUserId()), user.getPassword(), "res.partner",
                        conditionsPartner, fieldsPartner);

                consAttachment = Arrays.asList(Arrays.asList(
                        Arrays.asList("res_id", "=", task_id)));

                fieldsAttachment = new HashMap() {{
                    put("fields", Arrays.asList("id", "checksum", "datas", "datas_filename"
                            , "db_datas", "local_url"
                    ));
                }};

                lvAttachment = (ListView) findViewById(R.id.lvAttachment);

                //searchTaskAttachment = odoo.search_read(listenerAttachment, user.getDatabase(),
                //      String.valueOf(user.getUserId()), user.getPassword(), "ir.attachment",
                //    consAttachment, fieldsAttachment);

                //Initialize Google Play Services
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(FormMessageActivity.this,
                            Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                        //Location Permission already granted
                        // buildGoogleApiClient();
                        btnTakePicture.setOnClickListener(this);
                        btnFromGallery.setOnClickListener(this);
                    } else {
                        //Request Location Permission
                        checkCameraPermission();
                    }
                } else {
                    btnTakePicture.setOnClickListener(this);
                    btnFromGallery.setOnClickListener(this);
                }

            } catch (NullPointerException err) {
                odoo.MessageDialog(FormMessageActivity.this, err.getMessage());
            }
        }


    }

    private void setupDialog() {
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setMessage("Loading");
        dialog.setCancelable(false);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

                new android.support.v7.app.AlertDialog.Builder(this)
                        .setTitle("Camera Permission Needed")
                        .setMessage("This app needs the Camera permission, please accept to camera location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(FormMessageActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        MY_PERMISSIONS_REQUEST_CAMERA);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }
    }

    XMLRPCCallback listenerPartner = new XMLRPCCallback() {
        @Override
        public void onResponse(long id, Object result) {
            if (id == searchTaskPartnerId) {
                final Object[] classObjs = (Object[]) result;
                final int length = classObjs.length;
                if (length > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < length; i++) {
                                Map<String, Object> classObjPer
                                        = (Map<String, Object>) classObjs[i];
                                OUser userModel = new OUser();
                                userModel.setUserId(Integer.parseInt(classObjPer.get("id").toString()));
                                userModel.setName(classObjPer.get("name").toString());
                                partnerModelArrayList.add(userModel);
                            }
                            partnerAutoCompleteAdapter = new UserMessageAutoCompleteAdapter(FormMessageActivity.this,
                                    partnerModelArrayList);
                            edtNotes.setAdapter(partnerAutoCompleteAdapter);
                            edtNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Log.d("selected id partner", partnerAutoCompleteAdapter.getItemId(position) + "");
                                }
                            });


                        }
                    });
                } else {
                    Log.d("user data", "null");
                }
            }
        }

        @Override
        public void onError(long id, XMLRPCException error) {

        }

        @Override
        public void onServerError(long id, XMLRPCServerException error) {

        }
    };

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

    private AttachmentAdapter attachmentAdapter;
    XMLRPCCallback listenerAttachment = new XMLRPCCallback() {
        @Override
        public void onResponse(long id, final Object result) {
            if (id == createTaskIdAttachment) {
                // result.toString() -> 12229...dst (id attachment-nya)
                attachment_ids.add(Arrays.asList(4, Integer.parseInt(result.toString())));
                Log.d("id1", result.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        uploadingProgress.setVisibility(View.GONE);
                        attachmentModels.add(tempModel);
                        attachmentAdapter = new AttachmentAdapter(FormMessageActivity.this, attachmentModels);
                        lvAttachment.setAdapter(attachmentAdapter);
                        attachmentAdapter.notifyDataSetChanged();
                        dialog.hide();
                    }
                });
            } else if (id == searchTaskAttachment) {
                final Object[] classObjs = (Object[]) result;
                final int length = classObjs.length;
                if (length > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, Object> classObjPer
                                    = (Map<String, Object>) classObjs[0];
                            Log.d("attachment :", classObjPer.toString());
                        }
                    });
                }
            }
        }

        @Override
        public void onError(long id, XMLRPCException error) {
            Log.d("XMLRPCException : ", error.toString());
        }

        @Override
        public void onServerError(long id, XMLRPCServerException error) {
            Log.d("XMLRPCServerExpc : ", error.toString());
        }
    };

    XMLRPCCallback listenerMessage = new XMLRPCCallback() {
        @Override
        public void onResponse(long id, final Object result) {
            if (id == createTaskId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        odoo.MessageDialog(FormMessageActivity.this, "Message Report has been sent!");
                        finish();
                    }
                });
            }
        }

        @Override
        public void onError(long id, XMLRPCException error) {
            Log.d("XMLRPCException : ", error.toString());
        }

        @Override
        public void onServerError(long id, XMLRPCServerException error) {
            Log.d("XMLRPCServerExpc : ", error.toString());
        }
    };

    XMLRPCCallback listenerAttachmentRel = new XMLRPCCallback() {
        @Override
        public void onResponse(long id, Object result) {
            if (id == createAttachmentRel) {
                //finish();
                Log.d("final id", result.toString());
            }
        }

        @Override
        public void onError(long id, XMLRPCException error) {
            Log.d("XMLRPCException : ", error.toString());
        }

        @Override
        public void onServerError(long id, XMLRPCServerException error) {
            Log.d("XMLRPCServerExpc : ", error.toString());
        }
    };

    public boolean validTanggalDanHours() {
        boolean valid = true;
        Date date = null;
        DateFormat readFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat writeFormat = new SimpleDateFormat("dd");
        try {
            date = readFormat.parse(theDate);
        } catch (ParseException e) {
            Log.d("error parse ", e.toString());
        }
        String tanggal = writeFormat.format(date);
        if (Integer.parseInt(tanggal) > currentDay + 2 || Integer.parseInt(tanggal) < currentDay - 2) {
            valid = false;
        }
        return valid;
    }

    @Override
    public void onBackPressed() {
        if (menuAttachmentOpen) {
            viewAttachment.setVisibility(View.GONE);
            scrollViewMessageUtama.setVisibility(View.VISIBLE);
            menuAttachmentOpen = false;
        } else {
            finish();
        }
        FormMessageActivity.this.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    private ArrayList<String> imagesEncodedList;
    private String imageEncoded;

    private ArrayList<AttachmentModel> attachmentModels = new ArrayList<>();


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }


    public boolean hasImageCaptureBug() {

        // list of known devices that have the bug
        ArrayList<String> devices = new ArrayList<String>();
        devices.add("android-devphone1/dream_devphone/dream");
        devices.add("generic/sdk/generic");
        devices.add("vodafone/vfpioneer/sapphire");
        devices.add("tmobile/kila/dream");
        devices.add("verizon/voles/sholes");
        devices.add("google_ion/google_ion/sapphire");

        return devices.contains(android.os.Build.BRAND + "/" + android.os.Build.PRODUCT + "/"
                + android.os.Build.DEVICE);

    }

    AttachmentModel tempModel = new AttachmentModel();
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // When an Image is picked
            if (requestCode == 110 && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                if (data.getData() != null) {

                    Uri mImageUri = data.getData();
                    listAttachments.requestFocus();

                    // Get the cursor
                    Cursor cursor = getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    ContentResolver cR = FormMessageActivity.this.getContentResolver();
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    final String type = cR.getType(mImageUri);
                    final String ext = mime.getExtensionFromMimeType(type);

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncoded = cursor.getString(columnIndex);

                    final InputStream imageStream = getContentResolver().openInputStream(mImageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    imageEncoded = encodeImage(selectedImage);
                    imagesEncodedList.add(imageEncoded);

                    final String name = createWriteWithoutSec + " - " + imagesEncodedList.size() + "." + ext;
                    AttachmentModel model = new AttachmentModel();
                    model.setName(name);
                    model.setAttachmentByte(imageEncoded);
                    tempModel = model;

                    attachmentToInput = Arrays.asList(new HashMap() {{
                        put("create_uid", uids);
                        put("write_uid", uids);
                        put("write_date", createWriteWithoutSec);
                        put("create_date", createWriteWithoutSec);
                        put("company_id", user.getCompanyId());
                        put("__last_update", createWriteWithoutSec);
                        put("res_model", "project.task"); // modelnya
                        put("name", name); // harus sertakan ekstensi
                        put("datas_fname", name); // harus sertakan ekstensi
                        put("mimetype", type); // ini mimetypenya jadi kalau image harus image/jpeg atau image/png gimana file
                        put("res_id", task_id); // ini id tasknya
                        put("datas", imageEncoded); // ini string base64 hasil konversi image ke base 64
                        put("db_datas", imageEncoded); // ini string base64 hasil konversi image ke base 64
                    }});

                    uploadingProgress.setVisibility(View.VISIBLE);
                    createTaskIdAttachment = odoo.create(listenerAttachment, database, uid, password,
                            "ir.attachment", attachmentToInput);

                    cursor.close();

                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        if (data.getClipData() != null) {
                            listAttachments.requestFocus();
                            ClipData mClipData = data.getClipData();
                            ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                            dataToInput = Arrays.asList();
                            for (int i = 0; i < mClipData.getItemCount(); i++) {
                                final int number = i;

                                ClipData.Item item = mClipData.getItemAt(i);
                                Uri uri = item.getUri();
                                mArrayUri.add(uri);

                                ContentResolver cR = FormMessageActivity.this.getContentResolver();
                                MimeTypeMap mime = MimeTypeMap.getSingleton();
                                final String type = cR.getType(uri);
                                final String ext = mime.getExtensionFromMimeType(type);

                                final InputStream imageStream = getContentResolver().openInputStream(uri);
                                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                                imageEncoded = encodeImage(selectedImage);
                                imagesEncodedList.add(imageEncoded);
                                //Log.v("LOG_TAG", "Selected Images" + encodedImage);
                                final String name = createWriteWithoutSec + " - " + number + "." + ext;
                                AttachmentModel model = new AttachmentModel();
                                model.setName(name);
                                model.setAttachmentByte(imageEncoded);
                                attachmentModels.add(model);

                                attachmentToInput = Arrays.asList(new HashMap() {{
                                    put("create_uid", uids);
                                    put("write_uid", uids);
                                    put("write_date", createWriteWithoutSec);
                                    put("create_date", createWriteWithoutSec);
                                    put("company_id", user.getCompanyId());
                                    put("__last_update", createWriteWithoutSec);
                                    put("res_model", "project.task"); // modelnya
                                    put("name", name); // harus sertakan ekstensi
                                    put("datas_fname", name); // harus sertakan ekstensi
                                    put("mimetype", type); // ini mimetypenya jadi kalau image harus image/jpeg atau image/png gimana file
                                    put("res_id", task_id); // ini id tasknya
                                    put("datas", imageEncoded); // ini string base64 hasil konversi image ke base 64
                                    put("db_datas", imageEncoded); // ini string base64 hasil konversi image ke base 64
                                }});

                                uploadingProgress.setVisibility(View.VISIBLE);
                                createTaskIdAttachment = odoo.create(listenerAttachment, database, uid, password,
                                        "ir.attachment", attachmentToInput);
                            }
                            //String arrayImages = gson.toJson(imagesEncodedList);
                            //scrollViewMessageUtama.setVisibility(View.GONE);
                            //viewAttachment.setVisibility(View.VISIBLE);
                            //menuAttachmentOpen = true;
                            Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
                            Log.v("LOG_TAG", "Selected Images URI" + mArrayUri.toString());

                        }
                    }
                }
            } else if (requestCode == 109) {
                Uri tempUri = mUri;

                listAttachments.requestFocus();
                ContentResolver cR = FormMessageActivity.this.getContentResolver();
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                final String type = cR.getType(tempUri);
                final String ext = mime.getExtensionFromMimeType(type);



                final InputStream imageStream = getContentResolver().openInputStream(tempUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.JPEG, 10, bos);

                imageEncoded = encodeImage(selectedImage);
                imagesEncodedList.add(imageEncoded);
                //Log.d("imgencoded", imageEncoded);

                final String name = createWriteWithoutSec + " - " + imagesEncodedList.size() + ".jpg";
                AttachmentModel model = new AttachmentModel();
                model.setName(name);
                model.setAttachmentByte(imageEncoded);
                attachmentModels.add(model);

                attachmentToInput = Arrays.asList(new HashMap() {{
                    put("create_uid", uids);
                    put("write_uid", uids);
                    put("write_date", createWriteWithoutSec);
                    put("create_date", createWriteWithoutSec);
                    put("company_id", user.getCompanyId());
                    put("__last_update", createWriteWithoutSec);
                    put("res_model", "project.task"); // modelnya
                    put("name", name); // harus sertakan ekstensi
                    put("datas_fname", name); // harus sertakan ekstensi
                    put("mimetype", "image/jpeg"); // ini mimetypenya jadi kalau image harus image/jpeg atau image/png gimana file
                    put("res_id", task_id); // ini id tasknya
                   put("datas", imageEncoded); // ini string base64 hasil konversi image ke base 64
                   put("db_datas", imageEncoded); // ini string base64 hasil konversi image ke base 64
                }});


                uploadingProgress.setVisibility(View.VISIBLE);

                //ImageView imgView = (ImageView) findViewById(R.id.testView);
                //imgView.setImageBitmap(selectedImage);

                createTaskIdAttachment = odoo.create(listenerAttachment, database, uid, password,
                        "ir.attachment", attachmentToInput);

                Log.d("imgencoded", attachmentToInput.toString());
            } else {
                Toast.makeText(this, "You haven't captured any Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
            Log.e("err", e.getMessage());
        }
    }


    private String encodeImage(String path) {
        File imagefile = new File(path);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(imagefile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bm = BitmapFactory.decodeStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        //Base64.de
        return encImage;

    }

    private String encodeImage(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);

        return encImage;
    }


    public void changeLevel(int type) {
        if ((levelLocation >= 0 && levelLocation < level.length)) {
            if (type < 1 && levelLocation > 0 && levelLocation < level.length) {
                levelLocation--;
                btnLevel.setText(level[levelLocation]);
            } else if (type > 1 && levelLocation >= 0 && levelLocation < level.length - 1) {
                levelLocation++;
                btnLevel.setText(level[levelLocation]);
            }
        }
    }

    @Override

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                btnTakePicture.setOnClickListener(this);
                btnFromGallery.setOnClickListener(this);
            } else {

                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();

            }

        }
    }


    List cth = new ArrayList<>();
    private Uri mUri;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSendMessage:
                if (!edtNotes.getText().toString().isEmpty() ||
                        !edtNextActivities.getText().toString().isEmpty() ||
                        !edtObstacles.getText().toString().isEmpty()) {
                    String combiHTML = "<p>";
                    combiHTML = combiHTML + txvProjectTitle.getText() + "<br><br>" +
                            btnLevel.getText() + "<br><br>#Notes : <br>";
                    String htmlStringNotes = Html.toHtml(new SpannableString(edtNotes.getText()));
                    combiHTML = combiHTML + htmlStringNotes + "<br><br>#Obstacles : <br>";
                    String htmlStringObstacles = Html.toHtml(new SpannableString(edtObstacles.getText()));
                    combiHTML = combiHTML + htmlStringObstacles + "<br><br>#Next Activities to Do : <br>";
                    String htmlStringNextActivities = Html.toHtml(new SpannableString(edtNextActivities.getText()));
                    final String finalHTML = combiHTML + htmlStringNextActivities;
                    cth.add(Arrays.asList(4, 738));
                    cth.add(Arrays.asList(4, 739));
                    dataToInput = Arrays.asList(new HashMap() {{
                        put("create_uid", uids);
                        put("write_uid", uids);
                        put("attachment_ids", attachment_ids);
                        put("write_date", createWriteWithoutSec);
                        put("create_date", createWriteWithoutSec);
                        put("date", createWriteWithoutSec);
                        put("__last_update", createWriteWithoutSec);
                        put("message_type", "comment");
                        put("model", "project.task");
                        put("needaction", false);
                        put("res_id", task_id);
                        put("website_published", true);
                        put("body", finalHTML);
                    }});
                    Log.d("final", dataToInput.toString());
                    createTaskId = odoo.create(listenerMessage, database, uid, password,
                            "mail.message", dataToInput);
                } else {
                    if (edtNotes.getText().toString().isEmpty()) {
                        odoo.MessageDialog(FormMessageActivity.this, "Notes is empty," +
                                "please enter notes");
                    } else if (edtNextActivities.getText().toString().isEmpty()) {
                        odoo.MessageDialog(FormMessageActivity.this, "Next activities is empty," +
                                "please enter notes");
                    } else if (edtObstacles.getText().toString().isEmpty()) {
                        odoo.MessageDialog(FormMessageActivity.this, "Obstacles is empty," +
                                "please enter notes");
                    }
                }
                break;
            case R.id.btnNextLevel:
                changeLevel(2);
                break;
            case R.id.btnPrevLevel:
                changeLevel(-1);
                break;
            case R.id.btnTakeImage:
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mUri = Uri.fromFile(getOutputMediaFile());
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                startActivityForResult(takePicture, 109);
                break;
            case R.id.btnFromGallery:
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickPhoto.setType("image/*");
                pickPhoto.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                pickPhoto.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(pickPhoto, 110);//one can be replaced with any action code
                break;
        }
    }

    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "FPPReportCamera");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.d("FPPReportCamera", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
    }

}
