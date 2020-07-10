package auto.cn.nfcstudy;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.primitives.Bytes;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * 基于 RTD-URI类型Tag读取
 */
public class AtyNfcReadNdef extends AppCompatActivity {
    private TextView tvTitle, tvContent;
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_nfc_read_ndef);
        tvContent = findViewById(R.id.tv_content);
        tvTitle = findViewById(R.id.tv_title);
        //check nfc
        nfcCheck();
        //init nfc
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resolveIntent(intent);
    }

    private void enableForegroundDispatch() {
        if (mAdapter != null)
            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    private void disableForegroundDispatch() {
        if (mAdapter != null) mAdapter.disableForegroundDispatch(this);
    }
//检查nfc功能是否正常
    private void nfcCheck() {
        mAdapter = NfcAdapter.getDefaultAdapter(this);//检测手机是否支持nfc
        if (mAdapter == null) {
            Toast.makeText(this, "Sorry，您的设备不支持NFC功能！", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!mAdapter.isEnabled()) {//检测手机的nfc功能是否打开
                Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);//跳转到打开nfc设置界面
                startActivity(intent);
                return;
            }
        }
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        //NDEF验证
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            NdefMessage[] messages = null;
            //获取标签对象
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            //获取ndef消息数组
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                messages = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    messages[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                //未知的标签
                byte[] empty = new byte[]{};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                messages = new NdefMessage[]{msg};
            }
            tvTitle.setText("Scan a TAG!");
            //解析
            processNDEFMsg(messages);

        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

        } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {

        } else {

        }
    }

    private void processNDEFMsg(NdefMessage[] messages) {
        if (messages == null || messages.length == 0) {
            Toast.makeText(this, "TAG内容为空", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < messages.length; i++) {
            int length = messages[i].getRecords().length;
            NdefRecord[] records = messages[i].getRecords();
            for (int j = 0; j < length; j++) {
                for (NdefRecord record : records) {
                    parseRTDUriRecord(record);
                }
            }
        }
    }

    private void parseRTDUriRecord(NdefRecord record) {
        Preconditions.checkArgument(Arrays.equals(record.getType(), NdefRecord.RTD_URI));
        byte[] payload = record.getPayload();
        String prefix = URI_PREFIX_MAP.get(payload[0]);
        byte[] fullUri = Bytes.concat(prefix.getBytes(Charset.forName("UTF-8")),
                Arrays.copyOfRange(payload, 1, payload.length));
        Uri uri = Uri.parse(new String(fullUri, Charset.forName("UTF-8")));
        tvContent.setText("REVL:"+uri);
    }

    private static final BiMap<Byte, String> URI_PREFIX_MAP = ImmutableBiMap.<Byte, String>builder()
            .put((byte) 0x00, "")
            .put((byte) 0x01, "http://www.")
            .put((byte) 0x02, "https://www.")
            .put((byte) 0x03, "http://")
            .put((byte) 0x04, "https://")
            .put((byte) 0x05, "tel:")
            .put((byte) 0x06, "mailto:")
            .put((byte) 0x07, "ftp://anonymous:anonymous@")
            .put((byte) 0x08, "ftp://ftp.")
            .put((byte) 0x09, "ftps://")
            .put((byte) 0x0A, "sftp://")
            .put((byte) 0x0B, "smb://")
            .put((byte) 0x0C, "nfs://")
            .put((byte) 0x0D, "ftp://")
            .put((byte) 0x0E, "dav://")
            .put((byte) 0x0F, "news:")
            .put((byte) 0x10, "telnet://")
            .put((byte) 0x11, "imap:")
            .put((byte) 0x12, "rtsp://")
            .put((byte) 0x13, "urn:")
            .put((byte) 0x14, "pop:")
            .put((byte) 0x15, "sip:")
            .put((byte) 0x16, "sips:")
            .put((byte) 0x17, "tftp:")
            .put((byte) 0x18, "btspp://")
            .put((byte) 0x19, "btl2cap://")
            .put((byte) 0x1A, "btgoep://")
            .put((byte) 0x1B, "tcpobex://")
            .put((byte) 0x1C, "irdaobex://")
            .put((byte) 0x1D, "file://")
            .put((byte) 0x1E, "urn:epc:id:")
            .put((byte) 0x1F, "urn:epc:tag:")
            .put((byte) 0x20, "urn:epc:pat:")
            .put((byte) 0x21, "urn:epc:raw:")
            .put((byte) 0x22, "urn:epc:")
            .put((byte) 0x23, "urn:nfc:")
            .build();
}

