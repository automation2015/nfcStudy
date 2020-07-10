package auto.cn.nfcstudy;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.Locale;

public class MyNDEFMegGet {
    public static NdefMessage getNdefMsg_RTD_URI(String data,byte identifierCode){
        Log.d("tag", "getNdefMsg_RTD_URI() called with: data = [" + data + "], identifierCode = [" + identifierCode + "]");
        byte[] uriField=data.getBytes(Charset.forName("US-ASCII"));
        byte[] payload=new byte[uriField.length+1];
        payload[0]=identifierCode;
        System.arraycopy(uriField,0,payload,1,uriField.length);

        NdefRecord record=new NdefRecord(NdefRecord.TNF_WELL_KNOWN,NdefRecord.RTD_URI,new byte[0],payload);
        return new NdefMessage(new NdefRecord[]{record});
    }
    public static NdefMessage getNdefMsg_RTD_TEXT(String data,boolean encodeInUTF8){
        Log.d("tag", "getNdefMsg_RTD_TEXT() called with: data = [" + data + "], encodeInUTF8 = [" + encodeInUTF8 + "]");
        Locale locale=new Locale("en","US");
        byte[] langBytes=locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding=encodeInUTF8?Charset.forName("UTF-8"):Charset.forName("UTF-16");
        int utfBit=encodeInUTF8?0:(1<<7);
        char status=(char)(utfBit+langBytes.length);
        byte[] textBytes=data.getBytes(utfEncoding);
        byte[] payload=new byte[langBytes.length+textBytes.length+1];
        payload[0]=(byte)status;
        System.arraycopy(langBytes,0,payload,1,langBytes.length);//复制语言码
        System.arraycopy(textBytes,0,payload,1+langBytes.length,textBytes.length);//复制实际文本数据

        NdefRecord record=new NdefRecord(NdefRecord.TNF_WELL_KNOWN,NdefRecord.RTD_TEXT,new byte[0],payload);
        return new NdefMessage(new NdefRecord[]{record});
    }
    public static NdefMessage getNdefMsg_Absolute_URI(String data){
        Log.d("tag", "getNdefMsg_Absolute_URI() called with: data = [" + data + "]");
        byte[] payload=data.getBytes(Charset.forName("US-ASCII"));
        NdefRecord record=new NdefRecord(NdefRecord.TNF_ABSOLUTE_URI,new byte[0],new byte[0],payload);
        return new NdefMessage(new NdefRecord[]{record});
    }

}
