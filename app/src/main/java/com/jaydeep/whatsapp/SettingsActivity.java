package com.jaydeep.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private ProgressDialog loadingBar;
    private Toolbar SettingsToolBar;

    private String currentUserID;
    private FirebaseAuth fAuth;
    private DatabaseReference RootRef;
    private StorageReference UserProfileImagesRef;

    private static final int GalleryPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SettingsToolBar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Account Settings");


        fAuth = FirebaseAuth.getInstance();
        currentUserID = fAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        updateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.profile_image);
        loadingBar = new ProgressDialog(this);

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });
        
        RetrieveUseInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if(requestCode == GalleryPick  && resultCode == RESULT_OK && data != null) {
                Uri ImageUri = data.getData();

//                // start picker to get image for cropping and then use the image in cropping activity
//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .setAspectRatio(1,1)
//                        .start(this);
                CropImage.activity(ImageUri)
                        .setAspectRatio(1,1)
                        .start(this);
            }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){

//                loadingBar.setTitle("Set Profile Image");
//                loadingBar.setMessage("Please wait your profile image is updating...");
//                loadingBar.setCanceledOnTouchOutside(false);
//                loadingBar.show();
//
//                Uri resultUri =result.getUri();
//
//                StorageReference filePath = UserProfileImagesRef.child(currentUserID + ".jpg");
//
//                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                        if(task.isSuccessful()) {
//                            Toast.makeText(SettingsActivity.this, "Profile Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
//
//                            final String downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
//
//                            RootRef.child("Users").child(currentUserID).child("image")
//                                    .setValue(downloadUrl)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if(task.isSuccessful()){
//                                                loadingBar.dismiss();
//                                                Toast.makeText(SettingsActivity.this, "Image Saved", Toast.LENGTH_SHORT).show();
//                                            } else {
//                                                Toast.makeText(SettingsActivity.this, "Error : "+ task.getException().getMessage() , Toast.LENGTH_SHORT).show();
//                                                loadingBar.dismiss();
//                                            }
//                                        }
//                                    });
//                        }
//                        else {
//                            loadingBar.dismiss();
//                            Toast.makeText(SettingsActivity.this, "Error : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait your profile image is updating");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                Uri resultUri = result.getUri();
                final StorageReference filePath  = UserProfileImagesRef.child(currentUserID + ".jpg");
                UploadTask uploadTask =  filePath.putFile(resultUri);
                Task<Uri>uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return filePath.getDownloadUrl();
                    }
                })
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                if (task.isSuccessful()) {

                                    //  Uri downloadUri = task.getResult();
                                    // Getting image upload ID.
                                    final String downloadURL = task.getResult().toString();
                                    String ImageUploadId = RootRef.push().getKey();

                                    // Adding image upload id s child element into databaseReference.

                                    RootRef.child("Users").child(currentUserID).child("image")
                                            .setValue(downloadURL);
                                    Toast.makeText(SettingsActivity.this , "Data Successfully Uploaded" , Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                } else {
                                    // Handle failures
                                    // ...
                                    loadingBar.dismiss();
                                    Toast.makeText(SettingsActivity.this , "Error While Uploaded" , Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
        }
    }

    private void RetrieveUseInfo() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("image"))){
                            String retrieveUserName = snapshot.child("name").getValue().toString().trim();
                            String retrieveStatus = snapshot.child("status").getValue().toString().trim();
                            String retrieveProfileImage = snapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                            //Glide.with(SettingsActivity.this).load(retrieveProfileImage).into(userProfileImage);

                        }
                        else if((snapshot.exists()) && (snapshot.hasChild("name"))){
                            String retrieveUserName = snapshot.child("name").getValue().toString().trim();
                            String retrieveStatus = snapshot.child("status").getValue().toString().trim();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);


                        }
                        else {
                            Toast.makeText(SettingsActivity.this, "Please set and update your profile information...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void UpdateSettings() {
        String setUserName = userName.getText().toString().trim();
        String setUserStatus = userStatus.getText().toString().trim();


        if(TextUtils.isEmpty(setUserName)){
            userName.setError("User Name is required!");
            return;
        }
        if(TextUtils.isEmpty(setUserStatus)) {
            userStatus.setError("Status is required!");
            return;
        }

        HashMap<String, Object> profileMap = new HashMap<>();
        profileMap.put("uid",currentUserID);
        profileMap.put("name",setUserName);
        profileMap.put("status",setUserStatus);

        RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(SettingsActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
