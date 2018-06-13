package com.newsapp.rohit.singlesignon1.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.newsapp.rohit.singlesignon1.R;
import com.newsapp.rohit.singlesignon1.utlis.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;

    private TextView mLogin;

    private String mPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sso";

    private SharedPreferences mSharedPreferences;

    private ProgressBar mProgressBar;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEmail = (EditText)findViewById(R.id.editText_email);
        mPassword = (EditText)findViewById(R.id.editText_pass);
        mLogin = (TextView) findViewById(R.id.login);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);

        mSharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME,MODE_PRIVATE);

        File dir = new File(mPath);
        if (!dir.exists()){
            dir.mkdir();
        }

        handler = new Handler();
        onClickListeners();

        if (!checkPermissionForExtertalStorage()){
            try {
                requestPermissionForExtertalStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public boolean checkPermissionForExtertalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public void requestPermissionForExtertalStorage() throws Exception {
        try {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            //resume tasks needing this permission

        }else {
            //Toast.makeText(MainActivity.this,"You must grant the permission in order to run this app",Toast.LENGTH_LONG).show();

        }
    }

    private void onClickListeners() {

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = mEmail.getText().toString();
                String pass = mPassword.getText().toString();

                if (email.length() == 0) {
                    Toast.makeText(MainActivity.this,"Email cannot be left blanked",Toast.LENGTH_LONG).show();
                    mEmail.requestFocus();
                }else if (pass.length() == 0) {
                    Toast.makeText(MainActivity.this,"Password cannot be left blanked",Toast.LENGTH_LONG).show();
                    mPassword.requestFocus();
                }else {
                    String encryptedCredentials = encryptData(email + "\n" + pass, pass);
                    storeCredentials(encryptedCredentials);
                    storeKey(mPassword.getText().toString());

                    showProgress();
                }

            }
        });
    }

    private void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(View.GONE);
                startActivity(new Intent(MainActivity.this,SuccessfulLogIn.class));
            }
        },2500);
    }

    /*private void launchAnotherApp() {

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final ComponentName cn = new ComponentName("com.newsapp.rohit.singlesignon2",
                "com.newsapp.rohit.singlesignon2.MainActivity");
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try
        {
            startActivity(intent);
        }catch(ActivityNotFoundException e){
            Toast.makeText(MainActivity.this,"Activity Not Found",Toast.LENGTH_SHORT).show();
        }
    }*/

    private void storeKey(String key) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.KEY,key);
        editor.apply();
    }

    private String encryptData(String data, String password) {
        SecretKeySpec key = generateKey(password);
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE,key);
            byte[] encVal = cipher.doFinal(data.getBytes());
            String encryptedString = Base64.encodeToString(encVal,Base64.DEFAULT);
            return encryptedString;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return null;
    }

    private SecretKeySpec generateKey(String password) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = password.getBytes("UTF-8");
            messageDigest.update(bytes,0,bytes.length);
            byte[] key = messageDigest.digest();
            return new SecretKeySpec(key,"AES");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.e("NoSuchAlgorithm" , e.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e("EncodingException", e.toString());
        }


        return null;
    }

    private void storeCredentials(String data){
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "SSO");
            if (!root.exists()) {
                root.mkdirs();
            }
            File credentials = new File(root, "credentials.txt");
            FileWriter writer = new FileWriter(credentials);
            writer.append(data);
            writer.flush();
            writer.close();
            Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            Toast.makeText(MainActivity.this,"error",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


}
