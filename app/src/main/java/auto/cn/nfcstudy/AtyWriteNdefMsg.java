package auto.cn.nfcstudy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class AtyWriteNdefMsg extends AppCompatActivity {
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private Button btnWrite;
    private EditText etTag;
    private AlertDialog alertDialog;
    private String payload;
    private NdefMessage ndefMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_write_ndef_msg);
        btnWrite=findViewById(R.id.btn_write);
        etTag=findViewById(R.id.et_tag);
        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableForegroundDispatch();
                AlertDialog.Builder builder=new AlertDialog.Builder(AtyWriteNdefMsg.this);
                builder.setTitle("Touch tag to write")
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                disableForegroundDispatch();
                            }
                        });
                alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        });
        etTag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                payload=s.toString();
                Log.d("tag", "afterTextChanged() called with: payload = [" + payload+ "]");
            }
        });
        //check nfc
        nfcCheck();
        //init nfc
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
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
    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        Tag tag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(supportedTechs(tag.getTechList())){
            ndefMessage=MyNDEFMegGet.getNdefMsg_RTD_URI(payload,(byte)0x01);
            new WriteTask(this,ndefMessage,tag).execute();

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
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resolveIntent(intent);
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



    private void enableForegroundDispatch() {
        if (mAdapter != null)
            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    private void disableForegroundDispatch() {
        if (mAdapter != null) mAdapter.disableForegroundDispatch(this);
    }

    static class WriteTask extends AsyncTask<Void, Void, Void> {
        Activity activity = null;
        NdefMessage msg = null;
        Tag tag = null;
        String text = null;

        WriteTask(Activity host, NdefMessage msg, Tag tag) {
            this.activity = host;
            this.msg = msg;
            this.tag = tag;
        }

        @Override
        protected Void doInBackground(Void... params) {
            int size = msg.toByteArray().length;
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                NdefFormatable formatable = NdefFormatable.get(tag);
                if (formatable != null) {
                    try {
                        formatable.connect();
                        formatable.format(msg);
                    } catch (IOException e) {
                        text="Failed to connect Tag!";
                        Log.d("tag", "doInBackground() called with: params = [" + params
                                + "],Failed to connect Tag!");
                        e.printStackTrace();
                    } catch (FormatException e) {
                        text="Failed to format Tag!";
                        Log.d("tag", "doInBackground() called with: params = [" + params
                                + "],Failed to format Tag!");
                        e.printStackTrace();
                    } finally {
                        if (formatable != null) {
                            try {
                                formatable.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }else{
                    text="NDEF not support your tag!";
                    Log.d("tag", "doInBackground() called with: params = [" + params
                            + "],NDEF not support your tag");
                }
            }else {
                try {
                    ndef.connect();
                    if(!ndef.isWritable()){
                        text="Tag read only!";
                        Log.d("tag", "doInBackground() called with: params = [" + params
                                + "],Tag read only");
                    }else if(ndef.getMaxSize()<size) {
                        text="Tag is too small!";
                        Log.d("tag", "doInBackground() called with: params = [" + params
                                + "],Tag is too small!");
                    }else {
                        ndef.writeNdefMessage(msg);
                    }
                } catch (IOException e) {
                    text="Fail to connect Tag!";
                    Log.d("tag", "doInBackground() called with: params = [" + params
                            + "],Fail to connect Tag!");
                    e.printStackTrace();
                } catch (FormatException e) {
                    text="Fail to write NdefMessage Tag!";
                    Log.d("tag", "doInBackground() called with: params = [" + params
                            + "],Fail to write NdefMessage Tag!");
                    e.printStackTrace();
                }finally {
                    try {
                        ndef.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(text!=null){
                Toast.makeText(activity,text,Toast.LENGTH_SHORT).show();
            }
            activity.finish();
        }
    }
}
