package com.k7cl.ssidscan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    WifiManager wifiManager;
    Context context = this;
    Button refresh;
    Switch onoff;
    ListView list;
    boolean hasPermiison;
    String[][] arraySSIDs = new String[1000][10];
    static String[] arrayVuls = {
            "iPhone WiFi 拒绝服务风险\n" +
                    "影响版本iOS 14.4、iOS 14.6\n"+
                    "https://www.bleepingcomputer.com/news/security/iphone-bug-breaks-wifi-when-you-join-hotspot-with-unusual-name/",
            "Evil Twin 攻击风险\n" +
                    "SSID中红色标出的为可疑字符。\n" +
                    "这很可能是一个钓鱼WiFi，攻击者生成一个与正常WiFi名字一样或相似度极高的SSID来进行钓鱼。\n"+
                    "https://vipread.com/library/topic/3345\n" +
                    "https://cis.freebuf.com/?id=87"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refresh = (Button) findViewById(R.id.refresh);
        refresh.setOnClickListener(this);
        onoff = (Switch) findViewById(R.id.wlanSwitch);
        onoff.setOnClickListener(this);
        list = (ListView) findViewById(R.id.list);
        initWifiManager();
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED){
            onoff.setChecked(true);
        }
        chkPermiison();

//        setContentView(R.layout.listitem);
//        TextView SEC = (TextView) findViewById(R.id.sec);
//        Log.d("SECTextView:", SEC.getText().toString());
//        if ( SEC.getText().toString() == "危险"){
//            ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.parseColor("red"));
//            SpannableStringBuilder builder = new SpannableStringBuilder(SEC.getText().toString());
//            builder.setSpan(redSpan, 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            SEC.setText(builder);
//
//        }


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //parent 代表listView View 代表 被点击的列表项 position 代表第几个 id 代表列表编号
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                Toast.makeText(MainActivity.this, id+"", Toast.LENGTH_SHORT).show();

                Intent intent=new Intent();
                intent.setClass(MainActivity.this, SecInfo.class);
                Bundle bundle = new Bundle();
                bundle.putStringArray("arraySSID", arraySSIDs[position]);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh:
                if (hasPermiison){
                    boolean success = wifiManager.startScan();
                    if (!success) {
                        // scan failure handling
                        scanFailure();
                    }else {
                        Toast.makeText(context,"Scanning......",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    if (ContextCompat.checkSelfPermission(context, Permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        hasPermiison = true;
                        boolean success = wifiManager.startScan();
                        if (!success) {
                            // scan failure handling
                            scanFailure();
                        }else {
                            Toast.makeText(context,"Scanning......",Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(context,"No Location Permission!",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.wlanSwitch:
                if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED){
                    if(wifiManager.setWifiEnabled(true)){
                        onoff.setChecked(true);
                    }
                }else if(wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED){
                    if(wifiManager.setWifiEnabled(false)){
                        onoff.setChecked(false);
                    }
                }
                break;

        }
    }

    public void initWifiManager(){
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);
    }

    public void chkPermiison(){
        AndPermission.with(context)
            .runtime()
            .permission(Permission.ACCESS_FINE_LOCATION,
                        Permission.ACCESS_COARSE_LOCATION)
            .onGranted(new Action<List<String>>() {
                @Override
                public void onAction(List<String> permissions) {
                    // TODO what to do.
                    hasPermiison = true;
                    boolean success = wifiManager.startScan();
                    if (!success) {
                        // scan failure handling
                        scanFailure();
                    }
                }
            }).onDenied(new Action<List<String>>() {
                @Override
                public void onAction(List<String> permissions) {
                    // TODO what to do
                    hasPermiison = false;
                    Toast.makeText(context,"No Location Permission!",Toast.LENGTH_SHORT).show();
                    AndPermission.with(context)
                        .runtime()
                        .setting()
                        .start(-1);
                }
            })
            .start();
    }

    private static String[] insert(String[] arr, String str) {
        int size = arr.length;  //获取数组长度
        String[] tmp = new String[size + 1];  //新建临时字符串数组，在原来基础上长度加一
        for (int i = 0; i < size; i++){  //先遍历将原来的字符串数组数据添加到临时字符串数组
            tmp[i] = arr[i];
        }
        tmp[size] = str;  //在最后添加上需要追加的数据
        return tmp;  //返回拼接完成的字符串数组
    }

    private void scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();
        Toast.makeText(context,"Scan Finish!",Toast.LENGTH_SHORT).show();
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
//        arraySSIDs = null;

        for(int i=0;i<results.size();i++){
            String SSID = results.get(i).SSID;
            Log.d("scan",SSID);
            Map<String, String> map = new HashMap<String, String>();
            map.put("ssid", SSID);
            map.put("sec", chkSSID(SSID)[0]);
            map.put("enc", results.get(i).capabilities);
            map.put("sig", String.valueOf(results.get(i).level));
            list.add(map);

            arraySSIDs[i][0] = results.get(i).SSID;
            arraySSIDs[i][1] = results.get(i).BSSID;
            arraySSIDs[i][2] = results.get(i).capabilities;
            arraySSIDs[i][3] = chkSSID(SSID)[1];
            arraySSIDs[i][4] = chkSSID(SSID)[2];
        }
        setList(list);

    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
        Toast.makeText(context,"Scan Failed!",Toast.LENGTH_SHORT).show();
    }

    public void colorSec(){
        list.post(new Runnable() {
            public void run() {
                int start = list.getFirstVisiblePosition();
                for (int i = start, j = list.getLastVisiblePosition(); i <= j; i++){
                    View view = list.getChildAt(i);
                    Log.e("id",String.valueOf(i));
                    TextView sec = view.findViewById(R.id.sec);
                    if (sec.getText() == "危险"){
                        sec.setTextColor(Color.RED);
                    }else {
//                        sec.setTextColor(Color.GREEN);
                    }
                }

            }
        });
    }

    public void setList(List<Map<String, String>> ssidlist){

        SimpleAdapter adapter = new SimpleAdapter(this,ssidlist,R.layout.listitem,
                new String[]{"ssid","sec","enc","sig"},
                new int[]{R.id.ssid,R.id.sec,R.id.enc,R.id.sig});
        list.setAdapter(adapter);
        colorSec();
    }

    public static String string2Unicode(String string) {
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            // 取出每一个字符
            char c = string.charAt(i);
            // 转换为unicode
            unicode.append("\\u" + Integer.toHexString(c));
        }

        return unicode.toString();
    }

    public String[] chkSSID(String string) {
        String pattern = "%p(%s)+%n";
        boolean isMatch = Pattern.matches(pattern, string);
        if(isMatch){
            String[] r = {"危险", arrayVuls[0], "0"};
            return r;
        }
//        if(string.equals("%p%s%s%s%s%n")){
//            return "危险";
//        }
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            String  u = Integer.toHexString(c);
            if (Integer.parseInt(u,16)<126 || (Integer.parseInt(u,16)>=19968 && Integer.parseInt(u,16)<=40959)){
            } else {
                Log.e("danger",string);
                String[] r = {"危险", arrayVuls[1], String.valueOf(i+1)};
                return r;
            }
        }
        String[] r = {"安全", "", "0"};
        return r;
    }
}