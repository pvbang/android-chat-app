package com.example.chatapplication.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.databinding.ActivityChangeInfoUserBinding;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ChangeInfoUserActivity extends AppCompatActivity {

    private ActivityChangeInfoUserBinding binding;
    private User user;
    private String encodedImage, encodedImageBackground;
    private PreferenceManager preferenceManager;
    private int seePass = 0, seeConfirmPass = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangeInfoUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        user = (User) getIntent().getSerializableExtra("user");
        encodedImage = user.image;
        setData();
        setListeners();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.buttonChange.setOnClickListener(v -> {
            if (passwordOK()) {
                loading(true);
                change();
            }
        });
        binding.imageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        binding.imageBackgroundProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImageBackground.launch(intent);
        });

        binding.seePassword.setOnClickListener(v -> {
            if (seePass == 0) {
                binding.inputPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                seePass = 1;
            } else {
                binding.inputPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                seePass = 0;
            }
        });

        binding.seeConfirmPassword.setOnClickListener(v -> {
            if (seeConfirmPass == 0) {
                binding.inputConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                seeConfirmPass = 1;
            } else {
                binding.inputConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                seeConfirmPass = 0;
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setData() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID)).get().addOnSuccessListener(documentReference -> {
            loading(false);
            binding.imageProfile.setImageBitmap(getBitmapFromEncodedString(encodedImage));
            binding.inputName.setText(documentReference.getString(Constants.KEY_NAME));
            binding.inputEmail.setText(documentReference.getString(Constants.KEY_EMAIL));
            binding.inputPassword.setText(documentReference.getString(Constants.KEY_PASSWORD));
            binding.inputConfirmPassword.setText(documentReference.getString(Constants.KEY_PASSWORD));

            encodedImageBackground = documentReference.getString(Constants.KEY_IMAGE_BACKGROUND);
            binding.imageBackgroundProfile.setImageBitmap(getBitmapFromEncodedString(documentReference.getString(Constants.KEY_IMAGE_BACKGROUND)));
            binding.inputNumber.setText(documentReference.getString(Constants.KEY_NUMBER));
            binding.inputGender.setText(documentReference.getString(Constants.KEY_GENDER));
            binding.inputBirthDay.setText(documentReference.getString(Constants.KEY_BIRTHDAY));
            binding.inputIntroduceYourself.setText(documentReference.getString(Constants.KEY_INTRODUCE_YOURSEFT));

        }).addOnFailureListener(exception -> {
            loading(false);
            showToast(exception.getMessage());
        });

    }

    private void change() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_NAME, binding.inputName.getText().toString());
        documentReference.update(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        documentReference.update(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        documentReference.update(Constants.KEY_IMAGE, encodedImage);
        documentReference.update(Constants.KEY_IMAGE_BACKGROUND, encodedImageBackground);
        documentReference.update(Constants.KEY_NUMBER, binding.inputNumber.getText().toString());
        documentReference.update(Constants.KEY_BIRTHDAY, binding.inputBirthDay.getText().toString());
        documentReference.update(Constants.KEY_GENDER, binding.inputGender.getText().toString());
        documentReference.update(Constants.KEY_INTRODUCE_YOURSEFT, binding.inputIntroduceYourself.getText().toString());

        preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
        preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
        preferenceManager.putString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());

        new Handler().postDelayed(() -> {
            loading(false);
            showToast("Cập nhật thông tin cá nhân thành công!");
        }, 2000);

    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 1000;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitMap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitMap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> pickImageBackground = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageBackgroundProfile.setImageBitmap(bitmap);
                            binding.textAddImageBackground.setVisibility(View.GONE);
                            encodedImageBackground = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean passwordOK() {
        if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Mật khẩu nhập lại không khớp với mật khẩu đã nhập trước đó!");
            return false;
        } else {
            return true;
        }

    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonChange.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonChange.setVisibility(View.VISIBLE);
        }
    }
}