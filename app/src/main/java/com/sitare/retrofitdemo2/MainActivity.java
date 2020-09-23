package com.sitare.retrofitdemo2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView text;
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            }
        }


        Button uploadButton = (Button) findViewById(R.id.uploadButton);
        imageView = findViewById(R.id.imageCamera);
        text = findViewById(R.id.textView2);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            InputStream inputStream = null;
            try {
                assert uri != null;
                inputStream = getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Bitmap bMap = BitmapFactory.decodeStream(inputStream);
            imageView.setImageURI(uri);
            text.setText("Image Path: " + Environment.getExternalStorageDirectory() + uri.getPath());
            uploadFile(bMap);

        }
    }


    private void uploadFile(Bitmap bMap) {

        File originalFile = null;
        //create a file to write bitmap data
        // Log.d("RD2", "uploadFile: " + fileUri.getPath());

        try {
            Log.d("RD2", "checkpoint -- uploadFile: ");
            originalFile = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.jpg");
            Log.d("RD2", "checkpoint 2 -- uploadFile: ");
            originalFile.createNewFile();
            Log.d("RD2", "uploadFile: " + originalFile.getAbsolutePath());

            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bMap.compress(Bitmap.CompressFormat.JPEG, 0, bos); // YOU can also save it in JPEG
            byte[] bitmapdata = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(originalFile);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("RD2", "Failed to convert bitmap");
            Log.d("RD2", e.getLocalizedMessage());
        }


        if (originalFile.exists()) {
            Log.d("RD2", "file exists");
        } else {
            Log.d("RD2", "file does not exist");
        }

        RequestBody filePart = RequestBody.create(
                MediaType.parse("image/*"),
                originalFile);

        MultipartBody.Part file = MultipartBody.Part.createFormData("file", originalFile.getName(), filePart);

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("https://postman-echo.com/")
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();

        UserClient client = retrofit.create(UserClient.class);

        Call<ResponseBody> call = client.upload(file);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("RD2", "Success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("RD2", "Failure " + t.getLocalizedMessage() + "msg: " + t.toString());

            }
        });

    }
}