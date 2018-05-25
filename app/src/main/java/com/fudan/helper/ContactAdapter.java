package com.fudan.helper;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.fudan.callingu.R;
import com.fudan.callingu.ReadContacts;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by FanJin on 2017/10/18.
 */

public class ContactAdapter extends BaseAdapter {
    // 填充数据的list
    private ArrayList<String> list;
    // 用来控制CheckBox的选中状况
    private static HashMap<Integer, Boolean> isSelected;
    private Context context;
    private LayoutInflater inflater = null;
    public ContactAdapter(ArrayList<String> list, Context context) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
        isSelected = new HashMap<Integer, Boolean>();

        initDate();
    }

    private void initDate() {
        for (int i = 0; i < list.size(); i++) {
            getIsSelected().put(i, false);
        }
    }
    @Override
    public int getCount() {
        return list.size();
    }
    @Override
    public Object getItem(int position) {
        return list.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            // 获得ViewHolder对象
            holder = new ViewHolder();
            // 导入布局并赋值给convertview
            convertView = inflater.inflate(R.layout.listviewitem, null);
            holder.tv = (TextView) convertView.findViewById(R.id.item_tv);
            holder.cb = (CheckBox) convertView.findViewById(R.id.item_cb);
            // 为view设置标签
            convertView.setTag(holder);
        } else {
            // 取出holder
            holder = (ViewHolder) convertView.getTag();
        }
        // 设置list中TextView的显示
        holder.tv.setText(list.get(position));
        // 根据isSelected来设置checkbox的选中状况
        holder.cb.setOnCheckedChangeListener(null);
        holder.cb.setChecked(getIsSelected().get(position));
        holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //点击操作
                try{
                    getIsSelected().put(position, isChecked);
                    Log.e("eer","------------0----------------");
                    if (isChecked){
                        ReadContacts.checkNum++;
                        Log.e("eer","------------1----------------"+ReadContacts.checkNum);
                    }else {
                        ReadContacts.checkNum--;
                        Log.e("eer","-------------2---------------"+ReadContacts.checkNum);
                    }
                    ReadContacts.tv_show.setText(" 已选中" + ReadContacts.checkNum + "项");
                    ReadContacts.fff[position]=-ReadContacts.fff[position];
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

        });
        //holder.cb.setChecked(getIsSelected().get(position));
        return convertView;
    }


    public static HashMap<Integer, Boolean> getIsSelected() {
        return isSelected;
    }
    public static void setIsSelected(HashMap<Integer, Boolean> isSelected) {
        ContactAdapter.isSelected = isSelected;
    }
    public static class ViewHolder {
        TextView tv;
        CheckBox cb;
    }
}

