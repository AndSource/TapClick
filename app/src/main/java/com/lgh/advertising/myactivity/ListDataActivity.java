package com.lgh.advertising.myactivity;

import androidx.annotation.NonNull;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lgh.advertising.going.MyAccessibilityService;
import com.lgh.advertising.going.MyAccessibilityServiceNoGesture;
import com.lgh.advertising.going.R;
import com.lgh.advertising.myclass.AppDescribe;
import com.lgh.advertising.myclass.DataDao;
import com.lgh.advertising.myclass.MyApplication;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

public class ListDataActivity extends BaseActivity {

    Context context;
    DataDao dataDao;
    PackageManager packageManager;
    LayoutInflater inflater;
    Map<String, AppDescribe> appDescribeMap;
    List<AppDescribe> appDescribeList;
    List<AppDescribeAndIcon> appDescribeAndIconList;
    List<AppDescribeAndIcon> appDescribeAndIconFilterList;
    BaseAdapter baseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data);
        context = getApplicationContext();
        dataDao = MyApplication.dataDao;
        packageManager = getPackageManager();
        inflater = LayoutInflater.from(context);
        appDescribeAndIconList = new ArrayList<>();
        appDescribeAndIconFilterList = new ArrayList<>();

        final ListView listView = findViewById(R.id.listView);
        final ProgressBar progressBar = findViewById(R.id.progress);
        listView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if (MyAccessibilityService.mainFunction == null && MyAccessibilityServiceNoGesture.mainFunction == null) {
            Toast.makeText(context, "无障碍服务未开启", Toast.LENGTH_SHORT).show();
        } else if (MyAccessibilityService.mainFunction != null && MyAccessibilityServiceNoGesture.mainFunction != null) {
            Toast.makeText(context, "无障碍服务冲突", Toast.LENGTH_SHORT).show();
        } else {
            if (MyAccessibilityService.mainFunction != null) {
                appDescribeMap = MyAccessibilityService.mainFunction.getAppDescribeMap();
            }
            if (MyAccessibilityServiceNoGesture.mainFunction != null) {
                appDescribeMap = MyAccessibilityServiceNoGesture.mainFunction.getAppDescribeMap();
            }
        }

        View searchView = inflater.inflate(R.layout.view_search, null);
        EditText searchBox = searchView.findViewById(R.id.searchBox);
        final Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                appDescribeAndIconFilterList.clear();
                if (constraint.equals("@开启")) {
                    for (AppDescribeAndIcon e : appDescribeAndIconList) {
                        if (e.appDescribe.on_off) {
                            appDescribeAndIconFilterList.add(e);
                        }
                    }
                    return null;
                }
                if (constraint.equals("@关闭")) {
                    for (AppDescribeAndIcon e : appDescribeAndIconList) {
                        if (!e.appDescribe.on_off) {
                            appDescribeAndIconFilterList.add(e);
                        }
                    }
                    return null;
                }
                if (constraint.equals("@已创建规则")) {
                    for (AppDescribeAndIcon e : appDescribeAndIconList) {
                        if (!e.appDescribe.coordinateMap.isEmpty() || !e.appDescribe.widgetSetMap.isEmpty()) {
                            appDescribeAndIconFilterList.add(e);
                        }
                    }
                    return null;
                }
                if (constraint.equals("@未创建规则")) {
                    for (AppDescribeAndIcon e : appDescribeAndIconList) {
                        if (e.appDescribe.coordinateMap.isEmpty() && e.appDescribe.widgetSetMap.isEmpty()) {
                            appDescribeAndIconFilterList.add(e);
                        }
                    }
                    return null;
                }

                for (AppDescribeAndIcon e : appDescribeAndIconList) {
                    if (e.appDescribe.appName.contains(constraint)) {
                        appDescribeAndIconFilterList.add(e);
                    }
                }
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                baseAdapter.notifyDataSetChanged();
            }
        };
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter.filter(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        listView.addHeaderView(searchView);


        baseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return appDescribeAndIconFilterList.size();
            }

            @Override
            public Object getItem(int position) {
                return position;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ListDataActivity.ViewHolder holder;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.view_list_item, null);
                    holder = new ListDataActivity.ViewHolder(convertView);
                    convertView.setTag(holder);
                } else {
                    holder = (ListDataActivity.ViewHolder) convertView.getTag();
                }
                final AppDescribeAndIcon tem = appDescribeAndIconFilterList.get(position);
                holder.textName.setText(tem.appDescribe.appName);
                holder.textOnOff.setText(tem.appDescribe.on_off ? "开启" : "关闭");
                holder.imageView.setImageDrawable(tem.icon);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MyApplication.appDescribe = tem.appDescribe;
                        startActivity(new Intent(context, EditDataActivity.class));
                    }
                });
                return convertView;
            }
        };
        listView.setAdapter(baseAdapter);
        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 0x00:
                        baseAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        break;
                }
                return true;
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (appDescribeMap != null) {
                    appDescribeList = new ArrayList<>(appDescribeMap.values());
                } else {
                    appDescribeList = dataDao.getAllAppDescribes();
                    for (AppDescribe e : appDescribeList) {
                        e.getOtherFieldsFromDatabase(dataDao);
                    }
                }
                Collections.sort(appDescribeList, new Comparator<AppDescribe>() {
                    @Override
                    public int compare(AppDescribe o1, AppDescribe o2) {
                        return Collator.getInstance(Locale.CHINESE).compare(o1.appName, o2.appName);
                    }
                });
                ListIterator<AppDescribe> iterator = appDescribeList.listIterator();
                while (iterator.hasNext()) {
                    try {
                        AppDescribe e = iterator.next();
                        Drawable icon = packageManager.getApplicationIcon(e.appPackage);
                        appDescribeAndIconList.add(new AppDescribeAndIcon(e, icon));
                    } catch (PackageManager.NameNotFoundException nameNotFoundException) {
                        iterator.remove();
//                        nameNotFoundException.printStackTrace();
                    }
                }
                appDescribeAndIconFilterList.addAll(appDescribeAndIconList);
                handler.sendEmptyMessage(0x00);
            }
        }).start();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        baseAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MyAccessibilityService.mainFunction != null) {
            for (AppDescribe e : MyAccessibilityService.mainFunction.getAppDescribeMap().values()) {
                e.getOtherFieldsFromDatabase(dataDao);
            }
        }
        if (MyAccessibilityServiceNoGesture.mainFunction != null) {
            for (AppDescribe e : MyAccessibilityServiceNoGesture.mainFunction.getAppDescribeMap().values()) {
                e.getOtherFieldsFromDatabase(dataDao);
            }
        }
    }

    static class ViewHolder {
        TextView textName;
        TextView textOnOff;
        ImageView imageView;

        public ViewHolder(View v) {
            textName = v.findViewById(R.id.name);
            textOnOff = v.findViewById(R.id.on_off);
            imageView = v.findViewById(R.id.img);
        }
    }

    static class AppDescribeAndIcon {
        AppDescribe appDescribe;
        Drawable icon;

        public AppDescribeAndIcon(AppDescribe appDescribe, Drawable icon) {
            this.appDescribe = appDescribe;
            this.icon = icon;
        }
    }
}