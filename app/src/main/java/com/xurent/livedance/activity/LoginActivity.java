package com.xurent.livedance.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.xurent.livedance.R;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.http.AuthorizationService;
import com.xurent.livedance.utils.OKhttpUtils;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity  {

    private Retrofit retrofit;
    private SharedPreferences sp;
    private ProgressDialog pd;
    private boolean login=true;
    private SwitchMaterial switchMaterial;
    private TextInputEditText username;
    private  TextInputEditText pwd;
    private  TextInputEditText repwd;
    private TextInputLayout layout_re_pwd;
    private TextInputLayout layout_pwd;
    private TextInputLayout layout_username;
    private TextView title;
    private Button bt_sure;
    private CheckBox checkBox;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        switchMaterial=findViewById(R.id.enabled_switch);
        username=findViewById(R.id.username);
        pwd=findViewById(R.id.pwd);
        repwd=findViewById(R.id.re_pwd);
        title=findViewById(R.id.login_title);
        layout_re_pwd=findViewById(R.id.layout_re_pwd);
        bt_sure=findViewById(R.id.bt_sure);
        layout_pwd=findViewById(R.id.layout_pwd);
        layout_username=findViewById(R.id.layout_username);
        checkBox=findViewById(R.id.checkbox);
        sp=this.getSharedPreferences("config", Context.MODE_PRIVATE);
        username.setText(sp.getString("user",""));
        pwd.setText(sp.getString("pwd",""));
        pd = new ProgressDialog(this);
        switchMaterial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    layout_re_pwd.setVisibility(View.VISIBLE);
                    title.setText("用户注册");
                    bt_sure.setText("注册");
                    login=false;
                }else{
                    layout_re_pwd.setVisibility(View.GONE);
                    title.setText("用户登录");
                    bt_sure.setText("登录");
                    login=true;
                }
            }
        });

        initNet();//初始化网络
    }



    public void sure(View view) {
        String user=username.getText().toString().trim();
        String password=pwd.getText().toString().trim();
        String repass=repwd.getText().toString().trim();
        if(TextUtils.isEmpty(user)){
            layout_username.setError("账号不能为空");
            return;
        }else {
            layout_username.setError("");
        }
        if(TextUtils.isEmpty(password)){
            layout_pwd.setError("密码不能为空");
            return;
        }else {
            layout_pwd.setError("");
        }
        if(checkBox.isChecked()){
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("user",user);
            editor.putString("pwd",password);
            editor.commit();
        }
        pd.setMessage("正在提交请稍后...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        //登录
        if(login){

            login(user,password);
                //注册
        }else {

            if (!repass.equals(password)) {
                layout_re_pwd.setError("密码不一致");
            }else{
                layout_re_pwd.setError("");
            }
            register(user,password);
        }

    }


    public void register(String user,String pwd){
        AuthorizationService service=retrofit.create(AuthorizationService.class);
        Call<JsonObject> call=service.register(user,pwd);
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                Headers header=response.headers();
                String cookie=header.get("Set-Cookie");
                System.out.println(data+","+response.message()+","+cookie);

                if(data.get("code").getAsInt()==0){
                    Intent intent=new Intent(LoginActivity.this, UpdateUserInfoActivity.class);
                    intent.putExtra("cookie",cookie);
                    startActivityForResult(intent,0);
                    pd.cancel();
                }else {
                    layout_username.setError(data.get("msg").getAsString());
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                System.out.println("连接失败");
            }
        });

    }

    public void login(String user,String passwordd){

        AuthorizationService service=retrofit.create(AuthorizationService.class);
        Call<JsonObject> call=service.login(user,passwordd);
        System.out.println(call.request());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                Headers header=response.headers();
                String cookie=header.get("Set-Cookie");
                System.out.println(data+","+response.message()+","+cookie);
                if(data.get("code").getAsInt()==0){
                    Constants.LOGIN=true;
                    Constants.token=cookie;
                    SharedPreferences.Editor editor=sp.edit();
                    editor.putString("token",cookie);
                    editor.commit();
                    setResult(0);
                    finish();
                }else {
                    pwd.setError(data.get("msg").getAsString());
                    pwd.setText("");
                    pwd.requestFocus();
                    setResult(1);
                }
                pd.cancel();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                System.out.println("连接失败");
                pd.cancel();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==1){
            switchMaterial.setChecked(false);

        }

    }

    public void initNet(){

         retrofit = new Retrofit.Builder()
                .baseUrl(Constants.token_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(this))
                // .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        retrofit=null;
    }
}
