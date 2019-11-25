package com.baibuti.biji.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baibuti.biji.R;
import com.baibuti.biji.model.po.DownloadItem;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class DownloadItemAdapter extends RecyclerView.Adapter<DownloadItemAdapter.ViewHolder>
    implements View.OnClickListener, View.OnLongClickListener {

    private Context context;

    @Getter @Setter
    private List<DownloadItem> downloadItemList;

    public DownloadItemAdapter(Context context) {
        this.context = context;
    }

    //////

    public interface OnDownloadItemClickListener {
        void OnClick(DownloadItem document);
    }

    public interface OnDownloadItemLongClickListener {
        void OnLongClick(DownloadItem document);
    }

    private OnDownloadItemClickListener onDownloadItemClickListener;
    private OnDownloadItemLongClickListener onDownloadItemLongClickListener;

    public void setOnDownloadItemClickListener(OnDownloadItemClickListener onDownloadItemClickListener){
        this.onDownloadItemClickListener = onDownloadItemClickListener;
    }

    public void setOnDownloadItemLongClickListener(OnDownloadItemLongClickListener onDownloadItemLongClickListener){
        this.onDownloadItemLongClickListener = onDownloadItemLongClickListener;
    }

    //////

    @Override
    public int getItemCount() {
        if (downloadItemList == null)
            return 0;
        return downloadItemList.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context)
            .inflate(R.layout.adapter_document, parent, false);

        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        return new ViewHolder(view);
    }

    @Override
    public void onClick(View v) {
        DownloadItem downloadItem = (DownloadItem) v.getTag();
        if (null != onDownloadItemClickListener)
            onDownloadItemClickListener.OnClick(downloadItem);
    }

    @Override
    public boolean onLongClick(View v) {
        DownloadItem downloadItem = (DownloadItem) v.getTag();
        if (null != onDownloadItemLongClickListener)
            onDownloadItemLongClickListener.OnLongClick(downloadItem);
        return true;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DownloadItem downloadItem = downloadItemList.get(position);
        holder.itemView.setTag(downloadItem);

        holder.textView.setText(downloadItem.getBaseFilename());
        switch (downloadItem.getFileExtension()) {
            case "pdf":
                holder.imageView.setImageResource(R.drawable.pdf);
                break;
            case "ppt":
            case "pptx":
                holder.imageView.setImageResource(R.drawable.ppt);
                break;
            case "doc":
            case "docx":
                holder.imageView.setImageResource(R.drawable.doc);
                break;
            case "xls":
                holder.imageView.setImageResource(R.drawable.xls);
                break;
            case "txt":
                holder.imageView.setImageResource(R.drawable.txt);
                break;
            case "zip":
            case "rar":
                holder.imageView.setImageResource(R.drawable.zip);
                break;
            default:
                holder.imageView.setImageResource(R.drawable.unknown);
                break;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView;

        ViewHolder(View view) {
            super(view);

            imageView = view.findViewById(R.id.id_adapter_document_imageView);
            textView = view.findViewById(R.id.id_adapter_document_textView);
        }
    }
}
