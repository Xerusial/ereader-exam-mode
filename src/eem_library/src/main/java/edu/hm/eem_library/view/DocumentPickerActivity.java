package edu.hm.eem_library.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Date;
import java.util.Objects;

import edu.hm.eem_library.R;

/**
 * Core for all activities that need a filemanager to select a PDF document
 */
public abstract class DocumentPickerActivity extends AppCompatActivity {

    static final int REQUEST_CODE_READ_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    /**
     * Check if we have access to storage
     */
    protected final void checkFileManagerPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))
                Toast.makeText(getApplicationContext(), getString(R.string.toast_read_files_warning), Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            openFileManager();
        }
    }

    /**
     * Callback if user returns from a permission request dialog
     *
     * @param requestCode  requestCode of the permission
     * @param permissions  permission strings
     * @param grantResults accepted permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                openFileManager();
        }
    }

    /**
     * Open the android document picker
     */
    private void openFileManager() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        intent.putExtra("android.content.extra.FANCY", true);
        intent.putExtra("android.content.extra.SHOW_FILESIZE", true);
        startActivityForResult(intent, REQUEST_CODE_READ_STORAGE);
    }

    /**
     * Gets called when user returns for other application
     *
     * @param requestCode start intent code
     * @param resultCode  resultcode from other application
     * @param resultData  returned data from other application
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == REQUEST_CODE_READ_STORAGE) {
            if (resultCode == Activity.RESULT_OK) {
                // The document selected by the user won't be returned in the intent.
                // Instead, a URI to that document will be contained in the return intent
                // provided to this method as a parameter.
                // Pull that URI using resultData.getData().
                if (resultData != null) {
                    Uri uri = resultData.getData();
                    //Attention: Persistent Uris are limited to 128 files in total per app!!!
                    getContentResolver().takePersistableUriPermission(Objects.requireNonNull(uri), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    handleDocument(uri);
                } else {
                    handleDocument(null);
                }
            } else {
                handleDocument(null);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, resultData);
        }
    }

    /**
     * Option for children to implement actions after document has been selected
     *
     * @param uri document URI
     */
    abstract void handleDocument(@Nullable Uri uri);

    public static class Meta {
        public String name;
        public Date lastModifiedDate;
    }
}

