package com.example.chatapplication.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ActivitySignUpBinding;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        binding.buttonSignUp.setOnClickListener(v -> {
            if (isValidSignUpDetails()) {
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        if (encodedImage == null) {
            encodedImage = "/9j/4AAQSkZJRgABAQAAAQABAAD/4gIoSUNDX1BST0ZJTEUAAQEAAAIYAAAAAAIQAABtbnRyUkdCIFhZWiAAAAAAAAAAAAAAAABhY3NwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAA9tYAAQAAAADTLQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAlkZXNjAAAA8AAAAHRyWFlaAAABZAAAABRnWFlaAAABeAAAABRiWFlaAAABjAAAABRyVFJDAAABoAAAAChnVFJDAAABoAAAAChiVFJDAAABoAAAACh3dHB0AAAByAAAABRjcHJ0AAAB3AAAADxtbHVjAAAAAAAAAAEAAAAMZW5VUwAAAFgAAAAcAHMAUgBHAEIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFhZWiAAAAAAAABvogAAOPUAAAOQWFlaIAAAAAAAAGKZAAC3hQAAGNpYWVogAAAAAAAAJKAAAA+EAAC2z3BhcmEAAAAAAAQAAAACZmYAAPKnAAANWQAAE9AAAApbAAAAAAAAAABYWVogAAAAAAAA9tYAAQAAAADTLW1sdWMAAAAAAAAAAQAAAAxlblVTAAAAIAAAABwARwBvAG8AZwBsAGUAIABJAG4AYwAuACAAMgAwADEANv/bAEMAEAsMDgwKEA4NDhIREBMYKBoYFhYYMSMlHSg6Mz08OTM4N0BIXE5ARFdFNzhQbVFXX2JnaGc+TXF5cGR4XGVnY//bAEMBERISGBUYLxoaL2NCOEJjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY//AABEIAJYAlgMBIgACEQEDEQH/xAAbAAABBQEBAAAAAAAAAAAAAAAAAgMEBQYBB//EAEMQAAEDAgQCBwQFCQgDAAAAAAEAAgMEEQUSITFBUQYTImFxgZGhscHRFBUyUnMHIyQzNDVCovAWRGJyg5Lh8SVDU//EABoBAAIDAQEAAAAAAAAAAAAAAAMEAAIFAQb/xAAtEQACAgEEAQIFAgcAAAAAAAABAgADEQQSITFBBRMUIjJR8GFxIzNCgZGx0f/aAAwDAQACEQMRAD8A9AQhCkkEITFZVwUNO6epkEcbdyV0DPAkjyr8RxzDsNJZU1DetAJ6pnadte1htfvssjjPS6qrA6GiDqWG/wBsH84fMbcNvVZtPVaInl5zM1tZ04lcSKKkawX0dKcxI8Bax8yqOox/FahwdJXzAgW/NuyD+Wyrk9S0slVJkjtpqXHYJv26aVLEYAnCcdwkmqa2UdbJLPIdBmcXFSocLkdrM4M7hqVe0dFDRx5Y23dxedyuzQOc8ubbXgs+r1ap7CnS+DFLrXx8ki0eeiZkp5pmA7gSEA+WyclmlmIMsj5COLnEpBFjY7oWngdzPZ2bszoJaQQSCNiFIjxGsjeHNqpSR95xI9CoyFCoPYnAxHRl3TdIpGgNqYg8feZofT/pXdLWU9W0ugkDrbjYjyWObTvc0HQX5pyKKaGVskbw17TcEFZdr6Q5w4Bjld9q/UMibVCrcMxE1DWxT2E3MbO+SskqGVvpOZoKwYZEEIQuy0EITFZVQ0VLJU1D8kUYu4qdyRnFcTp8Ko3VFS7TZrRu48gvNcUxapxeqM1Q85ASI4+DBfb/AJ7kjGMWnxetdUTEtZfsRBxIYO7015puqqRV1Uk4gigDyLRxNytbYAaBaump2HJ7nDJOE4bJiNU1mVwhB7bwNAOXio9XSy0dQ6Gdpa4ejhzHcrTAMYdRytpqh/6M46E/+s/L/vneHimKT4lNd5yxA9iPg35qqPqTqmUgbMfn9/uPw2IXb+sThEdPLiULKuN8kBJztYbE6HvHFX8cENODHThwizEjNufHvUfAMMe6B05GUyDskjh/z8lMex0bsr2lp71jepav3bTWp4H+4rbuxnHEfXCQNyB4rqjVZ7TRyCz9Jp/iLRXnEWdtozGpXB0jiNkhCF7JFCKFHQiROTmCdjgc4gkWadU4ymFu2TfkFIAsLBY2s9VCjbQcn7/8h0p8tBCELzkZhsr/AAyt+kx5H/rGDX/EOaoEuCV0EzZWfaabo1NprbPiXRtpmqQkRvbLG17DdrhcIW13HIpefdNsX+mVooYXXgpz29N5NQfTb17lsOkGI/VmDz1DXAS2yx3I+0dBa+9t7dy8pTmlryd5nDOKZNQ1NHHC+oiLGzsEkbrghzT4e5Q1JY6oqnsjLpZnNAZG0kuIA2aO7uWguczkQpNBQVFfUNip4pJLuAcWtvlB4nktTgnRBn63FO2eETHGw04kcfDlxWuiijhjbHExrGN0DWiwHkl7tWq/KnMg5EoaPCa2JtmdXELWyud7dLp76gcdXVWp37F/irtCwxpqxyRK+2MYMo34NO2+WRjgBxuCVVYhRVMJzyQuDAPtDUD02WxXEbTolFnuKIOzTq4x1MElR/rWeIWqr8Gp6sOfGBFMdcw2PiFnJKOelqhFMyxBvfgRzBWqdQj1MeuDM96HrIz1JCEIXjIzBCEKSQQhCkku8Fmz0zojvGdPA/0UKDhMrIalzpHBoLCNfEIWtp7R7YyY1Ww28yn/ACg1hdPS0TS6zWmVwvoSdB5ix9VSYdh9DU4fUTVFaIpGWNspOQX3I43202SOkc7qnpBXPcACJSzT/D2fgq1bgqY1BVbH7QoODzOta572sY0uc42AAuSe5b3o50fbhjBUVADqtw8oxyHfzP8ARp+hmFioqH10zA6OE5Ywde3vfyHv7ltJJGxsL3uDWjclUvsOdixW6zHElxCzB36papKjHCDlpoxlH8T+Pkq2bEqp3bkqXtAGpDso9iCumduTxAN6hUnyrzNZeyS2WN5IZI1xG4BvZYWXEaVru3UBxOtxd3uVvhmP4ZDSBkk2RwJucju136D+rK76VlGRzL06p7WwUwPz9JpV1UrOkOFPeGirAJNtWOA9SFPgqoagH6PPHKG75Hh1vRLtWy9iN7pLTNTTx1MWSQXG4twPNKDzx1SwQdkNlyMGdyDxMvUQPp5nRvGo2PMc02r/ABWmE1MZB9uMXB7uIVAsa+r22x4irrtMEIQgSkEIXCQBcmwXQM8CSdQpGFxw1lS6NzjYMLtPEfNCZGkt8jH7y6IXGRMBVTuqaqaofYOleXm3Mm6kUuE11ZD1sFOXR3sHFwF/C5UNehYOxow6iaGgAxsJA7wLr1lr+2BiEusKAYk7CKP6vwunpSbuY3ta37R1PlclV/SCs6qWKHKT2c5105fAq8WZ6S/vCP8ACHvKX0w3W5MTtG9SDKDEcXlEroqchjRu62pNtf67lV53SSZnuLnHcuNyu1P7TJ/mKIW9sEp8DmOU0pUo2iPRx8SnVxdRYScTb3lxytQ95ccrUtjA0d6kktsN6QV9CQ18pqIr6slNzw2O428O5b2CVsscc0Zux7Q4HmCvKZJLaDfmtjg9bUdRRQ9aertGLWG2nFJamgMMrxAXXCnGfM1iz9TQTMmkyR3YCSLHgtCo847YPcsWylbeGhbh8uZmkLlVIRVTCw+273poyuPILHKEHEVzHlCrZw1wYNdLp0uJ3Kg1pDZMxNgG6rT9JrB1Iz9jKPyMRqLGZ8NresgylxYWkHbUg/BCppHmSRzzxKF6d6Uc5Ij1a7FAncQgbS4jVU7CS2KV7BfkCQt5hH7BRfhR+4LJ9LKP6H0gqQGkMlPWtub3zbn/AHZlNwrpNDS0cUVTHK58XZBYAQQNuI8EpYC6AiD1CFgMTbrM9Jf3hH+EPeVplmekv7wj/CHvKHo/5sVfqZCdv6TIT94oYbOBSqj9fJ/mKbWpjEfX6RJKbe8uOVqZ6x32WnRORvDRqNea4DmdxHWMDR380iSS2g3SXzi1hoU3mHNTIkxOrXYR/cf9P4LIZhzWvwf+4/6fwQ7D8szvUP6P3m1TM/8ACnlSYnjcFJVSQGOR0jANrWJIvvfvWNWjOcKI9cQE5lTV/tc34jvemk3DK6cySPtdzy4gbC+qcWRqKzXayHxFAcwJsLnZUeL1LZJerjNwNHEc+SdxDE2uY6GnN76Of8lWwTyU07J4XZZIzma617Fbfpujav8AjPwfAh66/Jln0ZwyLFcQkgnJDWxF4tzuB8UK66CxPqKiuxKYEuccgfoA4k5naf7fVCLqL3FhCmMiK/KBQl9PT1zGX6smOQga2O1zyBv5uWGXr+JUTMQw+ekk0bK0i9r5TwPkbFeSVEElNUSQTNLJI3Frm8iETSvldv2kM9C6L1razBoQXAyQjq3gcLbey3tSOkdE6WNlTG25jFn2+7z8visxhFZ/Z/G3RyTxTU7gGSPhcXMsbEOHO3zW9E0TnNa2Vhc4ZmgOFyOY7kM5qs3r1ErUwZ57PSNldmDi1x34qHLSTt2bmHNuq9Dq8Ho6nXq+qf8Aej09myq5ejkzbdTOx/PMC35p1dTU45OJVbHTjuYzqnRjtNcO8hIc62gWr+pcQ/8Ah/O35pP1TiDXgCncDzDh77ou+vHDCX+IPlZlGRvkdljY57uTRcqRFh1XKSGwPFvvDL71tm4ZWO2i/nb80/FglS+xkcyMcRe5Hw9qXNqDzF/i9Q/0V/5/BMnTYEA5rqiUOAOrGcfNanCKR09U2SxEcZzE8yNgrGDBaeLtTPMp79Applgp2BoLGN2AFgEvZqQRtSRdPa7izUN14jksjYYnyyGzGNLnHkAvPaufr6iad3ZzvLtTtc7XVh0zx5w/8bTOLToZjbzAv6H071j3SvcbuNzzKLo12KWPZjtie7jniXjcRgp4nC5e++gbttzUCrxGapu0nJH90fEqvzE8U/FWTw0s9Mx9oqjL1gsO1lNxqu/D1Cw2hcsfvLJUqxsvHDVIJJ3XFa9G8MOKYvFE5t4WHPKeGUcPM2HmjO+Bkwk33RmhOH4HTxPZllcOskFrG5117wLDyQrVCxWO4kmWgsf05wUyMGJ07CXMAbM0AfZ4O8tj3W5LYrhAcCCAQdCCrVuUbcJJ4wr6F4DWPhdYCxaW6WV7UdEMPgnMobK6Jx0YX6N7uftSfqSjZE5tOwxE63zF3sJWomqQRHUuCceRH8Nx1r8sVZZrtAJBsfHl4+5XixFTSTUxtKzTg4bFLpcQqqTSGUhv3TqPRcs0qv8ANWYAP95tEKgg6SHQVFODzdGfgfmp4xinc0EMlsddh80m2nsXsSNai9mWCFHpKsVb3CJhDWjtOdw5aKYGgITKVODC1j3Bleo24PLTkALuGY2Cz1aJxUO+kiz/AGW7lpXOawXcQB3qoxCCKulcZA4sy5bBxF/RFocIeYHV0hlAzzMBihY7EJjGQWkjUc7a+1M03UfSGfSus6n+Lq7ZtuF9FuXdEsMlib+blhdxLHm/tuqbE+iFTTtfLRSCoY0E5CLPtyHA6eHgmveR+M4j1TqFC/aVeNOoHVl8PDgy3a4N2Gw39VXJTmuY8te0tc02IIsQVLmmoDhVPFFTPFaHEzTFxsRc2AF+8cBtxur1r7aBBkxgnJzIbWue4NaC5xNgALkleodGcIGEYYGPH6RL25Tpofu3HAe+/NU/Q/o46nLcSrW2kIvCy+rb3uSOdtvHntsEnqbt3yLOQQhCUnYIQhSScc0OBBAIPAqsqqR0RL2as9ytFxdBxBWVLYMGUTmtc0tcAQdwRuoNRhMEpJjvE48tR6LQz0LH6x9h3Lgmn0rQACCCOI4o6WleVMQah1mYdg0od2ZWFvM3B9FIbRzNaGhlwBbcK6dSn+F3qEn6M/m1H+Kc9xeynf3ImH9dSPc7K0tcNWl1vgpz61xHZaG+OqSKV3FwHgnG0zBvcoDvuOTC1q6LtU8SP+cmdxcVJigDNXau9ycAAFgAPBdVMy4XyYIQlNjc7Yac1yEAJ6mS6YYMHtbX0sfbuGSMY0dq50dzJuQOO45KR0a6Jmne2sxNjTIPsQHXKeDidr93x21jIw3vPNLVze23aI5WpAwZxdQhAhIIQhSSCEIUkghCFJILiEKSRBiae7wSDCeBCELoME6LjOIhzS02NklCFaKEYMW2MuFxZKbDzPohC4TD1op7jjY2t2HqlIQqxgADqdQhCk7BCEKSQQhCkk//2Q==";
        }
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS).add(user).addOnSuccessListener(documentReference -> {
            loading(false);
            preferenceManager.putBoolean(Constants.KEY_IS_SIGN_IN, true);
            preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
            preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
            preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }).addOnFailureListener(exception -> {
            loading(false);
            showToast(exception.getMessage());
        });
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
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

    private Boolean isValidSignUpDetails() {
        if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Nhập tên tài khoản");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Nhập email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Thêm ảnh");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Nhập mật khẩu");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Nhập lại mật khẩu");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Mật khẩu nhập lại không khớp với mật khẩu đã nhập trước đó!");
            return false;
        } else {
            return true;
        }

    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }

}
