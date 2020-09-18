package com.example.rtsptest;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Login extends Activity
{
    TextView ResultHtml;
    private String login_result;

    private boolean saveLoginData;
    private String id;
    private String pwd;
    private EditText idText;
    private EditText pwdText;
    private CheckBox checkBox;
    private Button loginBtn;
    private SharedPreferences appData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_login);

        autologin();


        Button btn_send = (Button) findViewById(R.id.btn_login);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
                // Thread로 웹서버에 접속
                new Thread() {
                    public void run() {
                        String resultHtml = getResultHtml();

/*                        Bundle bun = new Bundle();
                        bun.putString("Test", resultHtml);
                        Message msg = handler.obtainMessage();
                        msg.setData(bun);
                        handler.sendMessage(msg);*/
                    }
                }.start();



            }
        });
        ResultHtml = (TextView)this.findViewById(R.id.result);
        //tvNaverHtml.setText(naverHtml);
    }
    private void autologin()
    {
        appData = getSharedPreferences("appData", MODE_PRIVATE);
        load();

        idText = (EditText) findViewById(R.id.text_id);
        pwdText = (EditText) findViewById(R.id.text_passwd);
        checkBox = (CheckBox) findViewById(R.id.checkbox);

        if (saveLoginData) {
            idText.setText(id);
            pwdText.setText(pwd);
            checkBox.setChecked(saveLoginData);
        }
    }
    // 설정값을 저장하는 함수
    private void save() {
        // SharedPreferences 객체만으론 저장 불가능 Editor 사용
        SharedPreferences.Editor editor = appData.edit();

        // 에디터객체.put타입( 저장시킬 이름, 저장시킬 값 )
        // 저장시킬 이름이 이미 존재하면 덮어씌움
        editor.putBoolean("SAVE_LOGIN_DATA", checkBox.isChecked());
        editor.putString("ID", idText.getText().toString().trim());
        editor.putString("PWD", pwdText.getText().toString().trim());

        // apply, commit 을 안하면 변경된 내용이 저장되지 않음
        editor.apply();
    }

    // 설정값을 불러오는 함수
    private void load() {
        // SharedPreferences 객체.get타입( 저장된 이름, 기본값 )
        // 저장된 이름이 존재하지 않을 시 기본값
        saveLoginData = appData.getBoolean("SAVE_LOGIN_DATA", false);
        id = appData.getString("ID", "");
        pwd = appData.getString("PWD", "");
    }

    private String getResultHtml(){
        String resultHtml = "";

        URL url =null;
        HttpURLConnection http = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        EditText EID = (EditText)findViewById(R.id.text_id);
        EditText EPasswd = (EditText)findViewById(R.id.text_passwd);
        String ID = EID.getText().toString();
        String Passwd = EPasswd.getText().toString();
        String RequestUrl;

        System.out.println("id is "+ID);
        System.out.println("password is "+Passwd);

        if(ID != "" && Passwd != "")
            RequestUrl = "https://e5sggq8tjg.execute-api.ap-northeast-2.amazonaws.com/TestDeploy/Test_Get"+"?id="+ID+"&passwd="+Passwd;
        else
            RequestUrl = "https://e5sggq8tjg.execute-api.ap-northeast-2.amazonaws.com/TestDeploy/Test_Get";

        try{
            url = new URL(RequestUrl);
            http = (HttpURLConnection) url.openConnection();
            http.setConnectTimeout(3*1000);
            http.setReadTimeout(3*1000);

            isr = new InputStreamReader(http.getInputStream());
            br = new BufferedReader(isr);

            String str = null;
            while ((str = br.readLine()) != null) {
                login_result = str;
//                resultHtml += str + "\n";
            }

            if(login_result.compareTo("\""+"Fail"+"\"") == 0 )
            {
                System.out.println("로그인실패");
                System.out.println("로그인실패");

                Toast.makeText(this,"아이디 혹은 비밀번호가 틀렸습니다.", Toast.LENGTH_LONG).show();
            }
            else
            {
/*
*/
                String[] dronelist = JSONParsing(login_result,"DroneList");
                String[] dronename = JSONParsing(login_result,"DroneName");
                Intent intent = new Intent(
                        getApplicationContext(), // 현재 화면의 제어권자
                        MainActivity.class); // 다음 넘어갈 클래스 지정
                intent.putExtra("DroneList",dronelist);
                intent.putExtra("DroneName",dronename);
                startActivity(intent); // 다음 화면으로 넘어간다
            }
        }catch(Exception e){

        }finally{
            if(http != null){
                try{http.disconnect();}catch(Exception e){}
            }

            if(isr != null){
                try{isr.close();}catch(Exception e){}
            }

            if(br != null){
                try{br.close();}catch(Exception e){}
            }
        }

        return resultHtml;
    }
    private String[] JSONParsing(String result,String target)
    {
        String[] dronelist = null;
        JSONObject jsonobject = null;
        try {
            jsonobject = new JSONObject(result);
            System.out.println("test1 "+jsonobject.toString());

            JSONArray ItemArray = jsonobject.getJSONArray("Items");
            System.out.println("test2 "+ItemArray.getJSONObject(0).toString());

            JSONObject DroneObject = ItemArray.getJSONObject(0);
            String temp_result = DroneObject.getString(target);
            System.out.println("test3" + temp_result);
            //JSON Parsing 끝 --> 결과는 ["drone1","drone2"] 꼴임.
            //따라서 추가적으로 StringParsing필요...

            String[] temp_dronelist;
            temp_dronelist = temp_result.split("\"");
            dronelist = new String[temp_dronelist.length/2];
            int list_idx = 0;
            System.out.println("length is "+temp_dronelist.length);
            for(int i = 1; i < temp_dronelist.length-1; i=i+2)
            {
                dronelist[list_idx] = temp_dronelist[i];
                System.out.println("보낼 list "+dronelist[list_idx]);
                list_idx++;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return dronelist;
    }
/*    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            String resultHtml = bun.getString("Test");
            ResultHtml.setText(resultHtml);
        }
    };*/
}
