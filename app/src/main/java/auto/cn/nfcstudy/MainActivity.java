package auto.cn.nfcstudy;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 读取nfc tag中的基本信息
 */
public class MainActivity extends AppCompatActivity {
    private NfcAdapter mAdapter = null;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mIntentFilter = null;
    private String[][] mTechList = null;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.tv_main);
        tv.setText("Scan a tag!");
        //检查手机的nfc功能是否正常
        nfcCheck();
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter intentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            intentFilter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        IntentFilter intentFilter1 = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        //....
        mIntentFilter = new IntentFilter[]{intentFilter};
        mTechList = null;//仅针对ACTION_TECH_DISCOVERED过滤器
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //获取tag中的信息
        tv.setText("Discover a tage:" + intent.getParcelableExtra(NfcAdapter.EXTRA_TAG));
    }

    private void nfcCheck() {
        mAdapter = NfcAdapter.getDefaultAdapter(this);//检测手机是否支持nfc
        if (mAdapter == null) {
            Toast.makeText(this, "设备没有nfc", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (!mAdapter.isEnabled()) {//检测手机的nfc功能是否打开
                Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);//跳转到打开nfc设置界面
                startActivity(intent);
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilter, mTechList);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }
}
