package diff.strazzere.anti;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import diff.strazzere.anti.debugger.FindDebugger;
import diff.strazzere.anti.emulator.FindEmulator;
import diff.strazzere.anti.monkey.FindMonkey;
import diff.strazzere.anti.taint.FindTaint;

public class MainActivity extends Activity {
    private static final int UPDATE_LOGINFO = 0;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.nihao);
        new Thread() {
            @Override
            public void run() {
                super.run();
                isTaintTrackingDetected();

                isMonkeyDetected();

                isDebugged();

                isQEmuEnvDetected();

            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_LOGINFO) {
                textView.setText(textView.getText() + "\n" + msg.getData().getString("str")); //UI更改操作
            }
        }
    };

    public void logd(String str) {
        Message message = new Message();
        message.what = UPDATE_LOGINFO;
        Bundle bundle = new Bundle();
        bundle.putString("str", str);
        message.setData(bundle);
        handler.sendMessage(message);

    }

    public boolean isQEmuEnvDetected() {
        logd("Checking for QEmu env...");
        logd("hasKnownDeviceId : " + FindEmulator.hasKnownDeviceId(getApplicationContext()));
        logd("hasKnownPhoneNumber : " + FindEmulator.hasKnownPhoneNumber(getApplicationContext()));
        logd("isOperatorNameAndroid : " + FindEmulator.isOperatorNameAndroid(getApplicationContext()));
        logd("hasKnownImsi : " + FindEmulator.hasKnownImsi(getApplicationContext()));
        logd("hasEmulatorBuild : " + FindEmulator.hasEmulatorBuild(getApplicationContext()));
        logd("hasPipes : " + FindEmulator.hasPipes());
        logd("hasQEmuDriver : " + FindEmulator.hasQEmuDrivers());
        logd("hasQEmuFiles : " + FindEmulator.hasQEmuFiles());
        logd("hasGenyFiles : " + FindEmulator.hasGenyFiles());
        logd("hasEmulatorAdb :" + FindEmulator.hasEmulatorAdb());
        logd("hasQEmuProps :" + FindEmulator.hasQEmuProps());
        logd("hasVmosProProps :" + FindEmulator.hasVmosProps());
        for (String abi : Build.SUPPORTED_ABIS) {
            if (abi.equalsIgnoreCase("armeabi-v7a")) {
                logd("hitsQemuBreakpoint : " + FindEmulator.checkQemuBreakpoint());
            }
        }
        if (FindEmulator.hasKnownDeviceId(getApplicationContext())
                || FindEmulator.hasKnownImsi(getApplicationContext())
                || FindEmulator.hasEmulatorBuild(getApplicationContext())
                || FindEmulator.hasKnownPhoneNumber(getApplicationContext()) || FindEmulator.hasPipes()
                || FindEmulator.hasQEmuDrivers() || FindEmulator.hasEmulatorAdb()
                || FindEmulator.hasQEmuFiles()
                || FindEmulator.hasGenyFiles()
                || FindEmulator.hasQEmuProps()
                || FindEmulator.hasVmosProps()) {
            logd("emulator environment detected.");
            return true;
        } else {
            logd("emulator environment not detected.");
            return false;
        }
    }

    public boolean isTaintTrackingDetected() {
        logd("Checking for Taint tracking...");
        logd("hasAppAnalysisPackage : " + FindTaint.hasAppAnalysisPackage(getApplicationContext()));
        logd("hasTaintClass : " + FindTaint.hasTaintClass());
        logd("hasTaintMemberVariables : " + FindTaint.hasTaintMemberVariables());
        if (FindTaint.hasAppAnalysisPackage(getApplicationContext()) || FindTaint.hasTaintClass()
                || FindTaint.hasTaintMemberVariables()) {
            logd("Taint tracking was detected.");
            return true;
        } else {
            logd("Taint tracking was not detected.");
            return false;
        }
    }

    public boolean isMonkeyDetected() {
        logd("Checking for Monkey user...");
        logd("isUserAMonkey : " + FindMonkey.isUserAMonkey());

        if (FindMonkey.isUserAMonkey()) {
            logd("Monkey user was detected.");
            return true;
        } else {
            logd("Monkey user was not detected.");
            return false;
        }
    }

    public boolean isDebugged() {
        logd("Checking for debuggers...");

        boolean tracer = false;
        try {
            tracer = FindDebugger.hasTracerPid();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        if (FindDebugger.isBeingDebugged() || tracer) {
            logd("Debugger was detected");
            return true;
        } else {
            logd("No debugger was detected.");
            return false;
        }
    }

    public void log(String msg) {
        Log.v("AntiEmulator", msg);
    }
}
