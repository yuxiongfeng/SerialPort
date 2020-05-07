package com.hoho.android.usbserial.examples;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * @Description:
 * @Author: yxf
 * @CreateDate: 2020/5/7 15:53
 * @UpdateUser: yxf
 * @UpdateDate: 2020/5/7 15:53
 */
public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.VH> {

    private Context context;
    private List<String> deviceList;

    public ScanAdapter(Context context, List<String> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(context).inflate(R.layout.item_device, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.txtMac.setText(deviceList.get(position));
        holder.btnConnect.setOnClickListener(v -> {
            if (listener != null) {
                listener.itemClickListener(deviceList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public void setListener(ItemClickListener listener) {
        this.listener = listener;
    }

    private ItemClickListener listener;

    interface ItemClickListener {
        void itemClickListener(String mac);
    }

    class VH extends RecyclerView.ViewHolder {
        TextView txtMac;
        Button btnConnect;

        public VH(@NonNull View itemView) {
            super(itemView);
            txtMac = itemView.findViewById(R.id.id_device_mac);
            btnConnect = itemView.findViewById(R.id.id_connect);
        }
    }
}
