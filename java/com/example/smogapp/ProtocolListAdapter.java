package com.example.smogapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProtocolListAdapter extends RecyclerView.Adapter<ProtocolListAdapter.ProtocolViewHolder> {

    private final List<String> protocolList;
    private final RecyclerItemClickListener.OnItemClickListener listener;


    public ProtocolListAdapter(List<String> protocolList, RecyclerItemClickListener.OnItemClickListener listener) {
        this.protocolList = protocolList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProtocolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_protocol, parent, false);
        return new ProtocolViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProtocolViewHolder holder, int position) {
        String protocol = protocolList.get(position);
        holder.bind(protocol);
    }

    public void updateData(List<String> newProtocols) {
        protocolList.clear();
        protocolList.addAll(newProtocols);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return protocolList.size();
    }

    class ProtocolViewHolder extends RecyclerView.ViewHolder {
        TextView protocolText;

        ProtocolViewHolder(@NonNull View itemView) {
            super(itemView);
            protocolText = itemView.findViewById(R.id.protocolText);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(itemView, position);
                }
            });
        }

        void bind(String protocol) {
            protocolText.setText(protocol);
        }
    }
}
