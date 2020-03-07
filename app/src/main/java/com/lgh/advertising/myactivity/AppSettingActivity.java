package com.lgh.advertising.myactivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lgh.advertising.going.R;
import com.lgh.advertising.myclass.LatestMessage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AppSettingActivity extends Activity {

    private Context context;
    private boolean autoHideOnTaskList;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_setting);
        context = getApplicationContext();
        autoHideOnTaskList = getIntent().getBooleanExtra("myAppConfig.autoHideOnTaskList", false);
        intent = new Intent();
        intent.putExtra("myAppConfig.autoHideOnTaskList", autoHideOnTaskList);
        setResult(Activity.RESULT_OK, intent);
        Button openDetail = findViewById(R.id.setting_open);
        Button checkUpdate = findViewById(R.id.setting_update);
        TextView authorChat = findViewById(R.id.authorChat);
        CheckBox checkBox = findViewById(R.id.setting_autoHideOnTaskList);
        checkBox.setChecked(autoHideOnTaskList);
        openDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoHideOnTaskList = isChecked;
                intent.putExtra("myAppConfig.autoHideOnTaskList", autoHideOnTaskList);
            }
        });
        checkUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                @SuppressLint("StaticFieldLeak") AsyncTask<String, Integer, String> asyncTask = new AsyncTask<String, Integer, String>() {
                    private LatestMessage latestVersionMessage;
                    private AlertDialog waitDialog;
                    private boolean haveNewVersion;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        waitDialog = new AlertDialog.Builder(AppSettingActivity.this).setView(new ProgressBar(context)).setCancelable(false).create();
                        Window window = waitDialog.getWindow();
                        if (window != null)
                            window.setBackgroundDrawableResource(R.color.transparent);
                        waitDialog.show();

                    }

                    @Override
                    protected String doInBackground(String... strings) {
                        try {
                            OkHttpClient httpClient = new OkHttpClient();
                            Request request = new Request.Builder().get().url(strings[0]).build();
                            Response response = httpClient.newCall(request).execute();
                            latestVersionMessage = new Gson().fromJson(response.body().string(), LatestMessage.class);
                            response.close();
                            int versionCode = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA).versionCode;
                            String appName = latestVersionMessage.assets.get(0).name;
                            Matcher matcher = Pattern.compile("\\d+").matcher(appName);
                            if (matcher.find()) {
                                int newVersion = Integer.valueOf(matcher.group());
                                haveNewVersion = newVersion > versionCode;
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        waitDialog.dismiss();
                        if (haveNewVersion) {
                            View view = LayoutInflater.from(context).inflate(R.layout.view_update_message, null);
                            WebView webView = view.findViewById(R.id.webView_update);
                            WebSettings settings = webView.getSettings();
                            settings.setJavaScriptEnabled(true);
                            webView.loadData(latestVersionMessage.body, "text/html", "utf-8");
                            AlertDialog dialog = new AlertDialog.Builder(AppSettingActivity.this).setView(view).setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(latestVersionMessage.assets.get(0).browser_download_url));
                                    startActivity(intent);
                                }
                            }).create();
                            dialog.show();
                            DisplayMetrics metrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
                            Window window = dialog.getWindow();
                            WindowManager.LayoutParams params = window.getAttributes();
                            params.width = (metrics.widthPixels / 6) * 5;
                            params.height = metrics.heightPixels / 2;
                            window.setAttributes(params);
                        } else {
                            Toast.makeText(context, "当前已是最新版本", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                asyncTask.execute("https://api.github.com/repos/LGH1996/ADGORELEASE/releases/latest");
            }
        });
        authorChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openChat = new Intent(Intent.ACTION_VIEW, Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=2281442260"));
                if (openChat.resolveActivity(getPackageManager()) != null) {
                    startActivity(openChat);
                }
            }
        });
    }
}
