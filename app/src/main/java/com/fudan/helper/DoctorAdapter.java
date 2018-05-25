package com.fudan.helper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fudan.callingu.R;

import java.util.List;

/**
 * Created by FanJin on 2017/10/9.
 * DoctorAdapter is an adapter which is used to adapt the DoctorList to the RecyclerView
 */

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {
    private List<SaveObject> mDoctorList;
    private Activity mActivity;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView doctorInfo;
        ImageButton doctorPhone;
        View doctorView;

        public ViewHolder(View view){
            super(view);
            doctorView = view;
            doctorInfo = view.findViewById(R.id.info_doctor);
            doctorPhone = view.findViewById(R.id.phone_doctor);
        }
    }

    public DoctorAdapter(List<SaveObject> doctorList, Activity activity){
        mDoctorList = doctorList;
        mActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.doctor_item,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        holder.doctorPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                SaveObject doctor = mDoctorList.get(position);
                //dial
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+doctor.address));
                mActivity.startActivity(intent);

            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SaveObject doctor = mDoctorList.get(position);
        holder.doctorInfo.setText(doctor.title+" "+doctor.address);
    }

    @Override
    public int getItemCount() {
        return mDoctorList.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
}
