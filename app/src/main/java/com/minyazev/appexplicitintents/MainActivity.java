package com.minyazev.appexplicitintents;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.Manifest;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private static final String CAMERA = "CAMERA";
    private static final String READ_CONTACTS = "READ_CONTACTS";
    private static final String TAG = "AppExplicitIntents";
    private Button btnCamera, btnGetContact, btnSendInfo;

    private TextView tvContactName;
    private ImageView ivImage;
    private String permission = "UNDEFINED";

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted-> {
                if(isGranted){
                    if(permission.equals(CAMERA)) {
                        launchCamera();
                    }
                    if(permission.equals(READ_CONTACTS)) {
                        getContact();
                    }
                } else{
                    Log.d(TAG, "PERMISSION DENIED ");
                }
            });
    private ActivityResultLauncher<Intent> photoResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if(result.getResultCode() == RESULT_OK){
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap)extras.get("data");
                    ivImage.setImageBitmap(imageBitmap);
                }else{
                    Log.e(TAG, "ERROR: SOME ERROR WITH CAMERA" );
                }
            }
    );
    private void launchCamera(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoResultLauncher.launch(takePictureIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivImage=findViewById(R.id.imageView);
        btnCamera = findViewById(R.id.btnCapture);
        btnCamera.setOnClickListener(v->{
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                permission = "CAMERA";
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });


        btnSendInfo = findViewById(R.id.btnSendInfoToContact);

        btnSendInfo.setOnClickListener(view -> {
            sendInfoToContact();
        });

        btnGetContact=findViewById(R.id.btnGetContact);
        tvContactName = findViewById(R.id.tvContact);
        btnGetContact.setOnClickListener(view -> {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) ==
                    PackageManager.PERMISSION_GRANTED) {
                getContact();
            } else {
                permission = "READ_CONTACTS";
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
            }
        });
    }
    private ActivityResultLauncher<Intent> getContactActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if(result.getResultCode() == RESULT_OK){

                    Intent data = result.getData();
                    if (data != null) {

                        Uri contactUri = data.getData();
                        String[] queryFields = new String[]{
                                ContactsContract.Contacts.DISPLAY_NAME
                        };

                        Cursor c =this.getContentResolver().query(contactUri, queryFields,null,null,null);
                        try{
                            if(c.getCount()==0){
                                return ;
                            }
                            c.moveToFirst();
                            String name = c.getString(0);
                            tvContactName.setText(name);
                        } finally {
                            c.close();
                        }

                    }
                }else{
                    Log.e(TAG, "ERROR: SOME ERROR WITH CAMERA" );
                }
            }
    );

    private void sendInfoToContact(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "I have get up at 7:30");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Early report");
        Intent intentChooser = Intent.createChooser(intent, "SendReport");
        startActivity(intentChooser);
    }

    private void getContact (){
        Intent intentGetContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        getContactActivityLauncher.launch(intentGetContact);
    }

}