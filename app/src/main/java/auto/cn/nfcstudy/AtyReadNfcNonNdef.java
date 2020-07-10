package auto.cn.nfcstudy;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class AtyReadNfcNonNdef extends AppCompatActivity {
    private TextView tvTitle, tvContent;
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_read_nfc_non_ndef);
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
    public static final byte[] KEY_A={(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0x00};

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        //获取标签对象
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Boolean isAuth=false;
       if(supportedTechs(tag.getTechList())) {
           MifareClassic mfc = MifareClassic.get(tag);
           if (mfc != null) {
               try {
                   mfc.connect();
                   int nSecont = mfc.getSectorCount();
                   Log.d("tag", "resolveIntent() called with: nSecont = [" + nSecont + "]");
                   for(int i=0;i<nSecont;i++){
                       if (mfc.authenticateSectorWithKeyA(i,MifareClassic.KEY_DEFAULT)) {
                        isAuth=true;
                       }else if(mfc.authenticateSectorWithKeyA(i,KEY_A)){
                           isAuth=true;
                       }else {
                           isAuth=false;
                       }
                       if(isAuth){
                           int nBlock=mfc.getBlockCountInSector(i);
                           Log.d("tag", "resolveIntent() called with: nBlock = [" + nBlock + "]");
                           for(int j=0;j<nBlock;j++){
                               byte[] data=mfc.readBlock(j);
                               Log.d("tag", "resolveIntent() called with: data = [" + data + "]");
                           }
                       }
                   }
               } catch (IOException e) {
                   e.printStackTrace();
               }
           } else {
               Log.d("tag", "Your Tag is not MifareClassic!");
           }
       }
    }
    private boolean supportedTechs(String[] techList) {
        boolean isSupport =false;
        for(String s:techList){
            Log.d("tag", "supportedTechs() called with: s = [" + s + "]");
        }
        for(String s:techList){
            Log.d("tag", "supportedTechs() called with: s = [" + s + "]");
            if(s.equals("android.nfc.tech.MifareClassic")){
                isSupport=false;
            }else if(s.equals("android.nfc.tech.MifareUltralight")){
                isSupport=false;
            }else if(s.equals("android.nfc.tech.Ndef")){
                isSupport=true;
            }else if(s.equals("android.nfc.tech.NdefA")){
                isSupport=false;
            }
            //...
            else {
                isSupport=false;
            }
        }
        return isSupport;
    }
}
