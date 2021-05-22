package com.example.onthecloudsocial;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class SetupActivity extends AppCompatActivity {


    private EditText UserName, FullName, Country;
    private Button SaveInformation;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;
    private Uri ProfilImageUri=null ;
    String currentUserID;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef;
    private StorageReference UserProfileImageRef;
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth=FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");


        UserName = (EditText) findViewById(R.id.setup_username);
        FullName =(EditText) findViewById(R.id.setup_full_name);
        Country = (EditText) findViewById(R.id.setup_country_name);
        SaveInformation =(Button) findViewById(R.id.setup_information_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);


        SaveInformation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SaveAccountSetupInformation();
            }
        });

        /*ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(galleryIntent,Gallery_Pic);
            }
        });*/

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_Pick);
            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("image"))
                    {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(ProfileImage);

                    }
                    else
                    {
                        Toast.makeText(SetupActivity.this,"Please select profile image.",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

       /* UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String image = dataSnapshot.child("profileimage").getValue().toString();

                    Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.ic_account_circle_black_24dp).into(ProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });*/
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode==Gallery_Pick &&resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            ProfilImageUri=data.getData();
            ProfileImage.setImageURI(null);
            ProfileImage.setImageURI(ProfilImageUri);
        }
    }


    private void SaveAccountSetupInformation() {

        final String username = UserName.getText().toString();
        final String fullname = FullName.getText().toString();
        final String country = Country.getText().toString();

        UserName.setError(null);
        if (ProfilImageUri==null || TextUtils.isEmpty(username))
        {
            if(ProfilImageUri==null)
            {
                Toast.makeText(SetupActivity.this, "Please choose profil image", Toast.LENGTH_SHORT).show();
            }
            if (TextUtils.isEmpty(username)) {
                Toast.makeText(SetupActivity.this, "Please write your username!", Toast.LENGTH_SHORT).show();
            }
            if (TextUtils.isEmpty(fullname)) {
                Toast.makeText(SetupActivity.this, "Please write your fullname!", Toast.LENGTH_SHORT).show();
            }
            if (TextUtils.isEmpty(country)) {
                Toast.makeText(SetupActivity.this, "Please write your country!", Toast.LENGTH_SHORT).show();
            }


        }

        else {

            String path;
            path = getFileExtension(ProfilImageUri);

            StorageReference childRef = UserProfileImageRef.child("UserProfile").child(mAuth.getCurrentUser().getUid()).child(username + "." + path);
            childRef.putFile(ProfilImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                  /*  String url =taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                    UsersRef.child("User_Name").setValue(username);
                    UsersRef.child("Full_Name").setValue(fullname);
                    UsersRef.child("Country").setValue(country);*/
                    Toast.makeText(SetupActivity.this, "Succesfull!", Toast.LENGTH_SHORT).show();
                    Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {



                            HashMap userMap = new HashMap();
                            userMap.put("username", username);
                            userMap.put("fullname", fullname);
                            userMap.put("country", country);
                            userMap.put("status", "Hey there, I'm using OntheCloud, developed by Betül ");
                            userMap.put("gender", "none");
                            userMap.put("dob", "");
                            userMap.put("job", "none");
                            userMap.put("image", uri.toString());

                            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        SendUserToTwitt();
                                        Toast.makeText(SetupActivity.this, "Your account is created succesfully.", Toast.LENGTH_LONG).show();
                                    } else {
                                        String message = task.getException().getMessage();
                                        Toast.makeText(SetupActivity.this, "Error occured." + message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }



    }


    private String getFileExtension(Uri uri)
    {

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private void SendUserToTwitt()
    {
        Intent mainIntent=new Intent(SetupActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }








    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth=FirebaseAuth.getInstance();
        currentUserID =mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_full_name);
        CountryName = (EditText) findViewById(R.id.setup_country_name);
        SaveInformationButton = (Button) findViewById(R.id.setup_information_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);


        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SaveAccountSetupInformation();
            }
        });
        /-*ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });  *-/
    }





   /-* protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();
            ProfilImageUri=data.getData();
            ProfileImage.setImageURI(null);
            ProfileImage.setImageURI(ProfilImageUri);

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we updating your profile image...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SetupActivity.this, "Profile Image stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
                            HashMap userMap = new HashMap();
                            userMap.put("image", filePath.toString());

                            UsersRef.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(SetupActivity.this, "Profile Image stored to Firebase Database Successfully...", Toast.LENGTH_SHORT).show();
                                                //loadingBar.dismiss();
                                                HashMap userMap = new HashMap();
                                                userMap.put("image", filePath.toString());
                                            }
                                            else
                                            {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SetupActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                               // loadingBar.dismiss();
                                            }
                                        }
                                    });
                        }
                    }
                });
            }
            else
            {
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                //loadingBar.dismiss();
            }
        }
    }*-/
  /-* protected void onActivityResult(int requestCode, int resultCode, Intent data){
       super.onActivityResult(requestCode,resultCode,data);

       if (requestCode==Gallery_Pick &&resultCode==RESULT_OK && data!=null && data.getData()!=null)
       {
           ProfilImageUri=data.getData();
           ProfileImage.setImageURI(null);
           ProfileImage.setImageURI(ProfilImageUri);
       }
   }*-/


   private void SaveAccountSetupInformation()
    {

        final String username = UserName.getText().toString();
        final String fullname = FullName.getText().toString();
        final String country = CountryName.getText().toString();


        UserName.setError(null);
       // if (UserProfileImageRef==null || TextUtils.isEmpty(username)) {

            if (TextUtils.isEmpty(username)) {
                Toast.makeText(SetupActivity.this, "Please write your username!", Toast.LENGTH_SHORT).show();
            }
            if (TextUtils.isEmpty(fullname)) {
                Toast.makeText(SetupActivity.this, "Please write your fullname!", Toast.LENGTH_SHORT).show();
            }
            if (TextUtils.isEmpty(country)) {
                Toast.makeText(SetupActivity.this, "Please write your country!", Toast.LENGTH_SHORT).show();
            }

       // }


        else {

           /-* String path;
            path = getFileExtension(ProfilImageUri);

            StorageReference childRef = UserProfileImageRef.child("UserProfile").child(mAuth.getCurrentUser().getUid()).child(username + "." + path);
            childRef.putFile(ProfilImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
*-/
           //public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


            Toast.makeText(SetupActivity.this, "Succesfull!", Toast.LENGTH_SHORT).show();
          //  Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
            //result.addOnSuccessListener(new OnSuccessListener<Uri>() {

               // public void onSuccess(Uri uri) {

                    HashMap userMap = new HashMap();
                    userMap.put("username", username);
                    userMap.put("fullname", fullname);
                    userMap.put("country", country);
                    userMap.put("status", "Hey there, I'm using OntheCloud, developed by Betül ");
                    userMap.put("gender", "none");
                    userMap.put("dob", "");
                    userMap.put("job", "none");


                    UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                SendUserToMainActivity();
                                Toast.makeText(SetupActivity.this, "Your account is created succesfully.", Toast.LENGTH_LONG).show();
                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, "Error occured." + message, Toast.LENGTH_SHORT).show();
                            }
                        }

                    });
                }
           // });
        //}
      //  });
                    //}
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
*/
}
