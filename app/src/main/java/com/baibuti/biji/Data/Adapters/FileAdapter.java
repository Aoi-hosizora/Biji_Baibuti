package com.baibuti.biji.Data.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.Data.Models.FileItem;
import com.baibuti.biji.R;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private List<FileItem> mFiles;

    static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView textView;

        public ViewHolder(View view){
            super(view);

            imageView = (ImageView) view.findViewById(R.id.id_adapter_document_imageView);
            textView = (TextView) view.findViewById(R.id.id_adapter_document_textView);        }
    }

    public FileAdapter(List<FileItem> files){
        mFiles = files;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        //此处重用文档列表项view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.modulelayout_filefrag_documentlistitem, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                //file可以为目录，也可为文件
                Toast.makeText(v.getContext(), "Click on file" + mFiles.get(position).getFileName(), Toast.LENGTH_LONG).show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem fileItem = mFiles.get(position);
        if(fileItem.getFileType() != null) {
            Log.d("测试", "onBindViewHolder: 调用");
            switch (fileItem.getFileType()) {
                case "pdf":
                    holder.imageView.setImageResource(R.drawable.pdf);
                    break;
                case "ppt":
                    holder.imageView.setImageResource(R.drawable.ppt);
                    break;
                case "doc":
                    holder.imageView.setImageResource(R.drawable.doc);
                    break;
                case "xls":
                    holder.imageView.setImageResource(R.drawable.excel);
                    break;
                default:
                    holder.imageView.setImageResource(R.drawable.other);
                    break;
            }
        }
        holder.textView.setText(fileItem.getFileName());
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }
}
