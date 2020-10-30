package com.example.risingindians;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class AccountSetupActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private Uri profileImageURI= null;
    private EditText setupName;
    private Button setupBtn;
    private ProgressBar setupProgress;
    private String user_id;
    private boolean isChanged = false;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);
        profileImage = findViewById(R.id.profile_image);
        setupName = findViewById(R.id.setup_name);
        setupBtn = findViewById(R.id.setup_btn);
        setupProgress = findViewById(R.id.setup_progressBar);

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#E26347"));

        // Set BackgroundDrawable
        actionBar.setBackgroundDrawable(colorDrawable);
        actionBar.setTitle(Html.fromHtml("<font color='#FFFFFF'> Rising Indians </font>"));

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        setupProgress.setVisibility(View.VISIBLE);
        setupBtn.setEnabled(false);
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if((task.isSuccessful())){

                    if(task.getResult().exists()){
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        profileImageURI = Uri.parse(image);

                        setupName.setText(name);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.ic_profile_image);

                        Glide.with(AccountSetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(profileImage);
                    }

                }
                else{
                    String message = task.getException().getMessage();
                    Toast.makeText(AccountSetupActivity.this,"FireStore Retrieve Error: " + message,Toast.LENGTH_SHORT).show();
                }
                setupProgress.setVisibility(View.INVISIBLE);
                setupBtn.setEnabled(true);

            }
        });

        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user_name = setupName.getText().toString();

                if (!TextUtils.isEmpty(user_name) && profileImageURI != null) {

                    setupProgress.setVisibility(View.VISIBLE);

                    if(isChanged) {

                        final StorageReference filePath = storageReference.child("profile_images").child(user_id + ".jpg");

                        //final UploadTask uploadTask = filePath.putFile(eventImageUri);

                        final UploadTask uploadTask = filePath.putFile(profileImageURI);

                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                String message = e.toString();
                                Toast.makeText(AccountSetupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                setupProgress.setVisibility(View.INVISIBLE);
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText(AccountSetupActivity.this, "Profile Image Uploaded Successfully", Toast.LENGTH_SHORT).show();

                                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                                        if (!task.isSuccessful()) {
                                            throw task.getException();
                                        }

                                        String downloadImageUrl = filePath.getDownloadUrl().toString();
                                        return filePath.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            storeFirestore(task, user_name);
                                        } else {
                                            setupProgress.setVisibility(View.INVISIBLE);
                                            String message = task.getException().toString();
                                            Toast.makeText(AccountSetupActivity.this, "Image Error: " + message, Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });

                            }
                        });

                    }
                    else{
                        storeFirestore(null,user_name);
                    }
                }
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if(ContextCompat.checkSelfPermission(AccountSetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(AccountSetupActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(AccountSetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {
                        OpenGallery();
                    }

                }
                else{
                    OpenGallery();
                }

            }

        });
    }

    private void storeFirestore(@NonNull Task<Uri> task, String user_name){

        String downloadImageUrl;

        if(task !=null){
        downloadImageUrl = task.getResult().toString();
        }
        else{
            downloadImageUrl = profileImageURI.toString();
        }

        //Toast.makeText(AccountSetupActivity.this,"The Image is uploaded",Toast.LENGTH_SHORT).show();
        //SaveEventInfoToDatabase();

        Map<String,String> userMap = new HashMap<>();
        userMap.put("name",user_name);
        userMap.put("image",downloadImageUrl);

        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(AccountSetupActivity.this,"User Settings Updated",Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(AccountSetupActivity.this,HomeActivity.class);
                    startActivity(mainIntent);
                    finish();

                }
                else{
                    String error = task.getException().getMessage();
                    Toast.makeText(AccountSetupActivity.this,"Firestore Error: " + error,Toast.LENGTH_SHORT).show();
                }
                setupProgress.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void OpenGallery(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(AccountSetupActivity.this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                profileImageURI = result.getUri();
                profileImage.setImageURI(profileImageURI);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }

}
