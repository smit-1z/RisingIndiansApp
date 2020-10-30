package com.example.risingindians;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import id.zelory.compressor.Compressor;


public class NewEventActivity extends AppCompatActivity{

    private ImageView newEventImage;
    private EditText newEventDesc;
    private Button newEventbtn;
    private Uri eventImageUri;
    private ProgressDialog progressDialog;
    private StorageReference eventImagesReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String current_user_id;
    private String desc;
    private String eventrandonKey, downloadImageUrl;
    private Bitmap compressedImageFile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        eventImagesReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Add New Event");
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#E26347"));

        // Set BackgroundDrawable
        actionBar.setBackgroundDrawable(colorDrawable);



        newEventImage = findViewById(R.id.add_new_image);
        newEventDesc = findViewById(R.id.event_desc);
        newEventbtn = findViewById(R.id.submit_event_btn);
        progressDialog = new ProgressDialog(this);


        newEventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(NewEventActivity.this);
            }
        });

        newEventbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidateEventData();
            }
        });




}


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                eventImageUri = result.getUri();
                newEventImage.setImageURI(eventImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }

    private void ValidateEventData(){

        desc = newEventDesc.getText().toString();

        if(TextUtils.isEmpty(desc)){
            Toast.makeText(NewEventActivity.this,"Please write description",Toast.LENGTH_SHORT).show();
        }
        if(eventImageUri == null){
            Toast.makeText(NewEventActivity.this,"Please select image",Toast.LENGTH_SHORT).show();
        }
        else {
            StoreEventInformation();
        }
    }


    private void StoreEventInformation(){

        progressDialog.setTitle("Uploading");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        eventrandonKey =  UUID.randomUUID().toString();

        File newImageFile = new File(eventImageUri.getPath());
        try {

            compressedImageFile = new Compressor(NewEventActivity.this)
                    .setMaxHeight(720)
                    .setMaxWidth(720)
                    .setQuality(50)
                    .compressToBitmap(newImageFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        final StorageReference filePath = eventImagesReference.child("Events").child(eventrandonKey +".jpg");


        final UploadTask uploadTask = filePath.putBytes(imageData);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                String message = e.toString();
                Toast.makeText(NewEventActivity.this,"Error: " + message,Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(NewEventActivity.this,"Event Uploaded Successfully",Toast.LENGTH_SHORT).show();

                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                        if(!task.isSuccessful()) {
                            throw task.getException();
                        }

                        downloadImageUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){


                            downloadImageUrl = task.getResult().toString();
                            Toast.makeText(NewEventActivity.this,"Event successfully added ",Toast.LENGTH_SHORT).show();

                            SaveEventInfoToDatabase();


                        }
                        else{
                            progressDialog.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(NewEventActivity.this,"Error: " + message,Toast.LENGTH_SHORT).show();

                        }
                    }
                });

            }
        });


    }

    private void SaveEventInfoToDatabase(){

        HashMap<String, Object> eventMap = new HashMap<>();
        eventMap.put("image_url",downloadImageUrl);
        eventMap.put("desc",desc);
        eventMap.put("user_id", current_user_id);
        eventMap.put("timestamp", FieldValue.serverTimestamp());


        firebaseFirestore.collection("Events").add(eventMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful()){

                    progressDialog.dismiss();
                    Toast.makeText(NewEventActivity.this, "Event was added", Toast.LENGTH_LONG).show();
                    Intent mainIntent = new Intent(NewEventActivity.this, HomeActivity.class);
                    startActivity(mainIntent);
                    finish();

                } else {
                    String message= task.getException().toString();
                    Toast.makeText(NewEventActivity.this,"Error :"+ message ,Toast.LENGTH_SHORT).show();

                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                String message = e.toString();
                Toast.makeText(NewEventActivity.this,"Error: " + message,Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }



}
