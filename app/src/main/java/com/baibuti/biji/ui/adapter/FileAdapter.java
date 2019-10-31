package com.baibuti.biji.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.data.po.FileItem;
import com.baibuti.biji.R;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private List<FileItem> mFiles;

    final private static int UNCHECKED = 0;
    final private static int CHECKED = 1;

    static class ViewHolder extends RecyclerView.ViewHolder{

        CheckBox checkBox;
        ImageView imageView;
        TextView textView;

        public ViewHolder(View view){
            super(view);

            checkBox = (CheckBox) view.findViewById(R.id.id_adapter_fileitem_checkBox);
            imageView = (ImageView) view.findViewById(R.id.id_adapter_fileitem_imageView);
            textView = (TextView) view.findViewById(R.id.id_adapter_fileitem_textView);
        }
    }

    public FileAdapter(List<FileItem> files){
        mFiles = files;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType){
        //此处重用文档列表项view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_fileimportdialog_file, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        Log.i("TEST", "onCreateViewHolder: "+holder.getAdapterPosition());
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Toast.makeText(v.getContext(), "Click on file" + mFiles.get(position).getFileName(), Toast.LENGTH_LONG).show();
            }
        });
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton whichButton, boolean isChecked) {
                if(whichButton.isPressed()){
                    if(isChecked){
                        Toast.makeText(parent.getContext(), "Click on " + holder.getAdapterPosition(), Toast.LENGTH_LONG).show();
                        mFiles.get(holder.getAdapterPosition()).setTag(CHECKED);
                    }
                    else{
                        mFiles.get(holder.getAdapterPosition()).setTag(UNCHECKED);
                    }
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.i("TEST", "onBindViewHolder: "+holder.getAdapterPosition());
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
                    holder.imageView.setImageResource(R.drawable.xls);
                    break;
                case "txt":
                    holder.imageView.setImageResource(R.drawable.txt);
                    break;
                case "zip":
                    holder.imageView.setImageResource(R.drawable.zip);
                    break;
                default:
                    holder.imageView.setImageResource(R.drawable.unknown);
                    break;
            }
        }

        if(mFiles.get(position).getTag() == UNCHECKED)
            holder.checkBox.setChecked(false);
        else{
            holder.checkBox.setChecked(true);
        }

        holder.textView.setText(fileItem.getFileName());

    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public void addData(FileItem fileItem){
        mFiles.add(fileItem);
        notifyItemInserted(getItemCount());
    }
}
