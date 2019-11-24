package com.baibuti.biji.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.baibuti.biji.model.vo.FileItem;
import com.baibuti.biji.R;

import java.util.List;

/**
 * FileImportDialog 文件列表项
 */
public class FileItemAdapter extends RecyclerView.Adapter<FileItemAdapter.ViewHolder> {

    private List<FileItem> fileItemList;

    final private static int UNCHECKED = 0;
    final private static int CHECKED = 1;

    // リコンストラクションしたくない
    public FileItemAdapter(List<FileItem> files) {
        fileItemList = files;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_file, parent, false);

        final ViewHolder holder = new ViewHolder(view);

        view.setOnClickListener((v) -> {
            holder.checkBox.setChecked(!holder.checkBox.isChecked());
            fileItemList.get(holder.getAdapterPosition()).setTag(holder.checkBox.isChecked() ? CHECKED : UNCHECKED);
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem fileItem = fileItemList.get(position);

        holder.textView.setText(fileItem.getFileName());
        if (fileItemList.get(position).getTag() == UNCHECKED)
            holder.checkBox.setChecked(false);
        else
            holder.checkBox.setChecked(true);

        if (fileItem.getFileType() != null) {
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
    }

    @Override
    public int getItemCount() {
        if (fileItemList == null)
            return 0;
        return fileItemList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkBox;
        ImageView imageView;
        TextView textView;

        ViewHolder(View view) {
            super(view);

            checkBox = view.findViewById(R.id.id_adapter_fileitem_checkBox);
            imageView = view.findViewById(R.id.id_adapter_fileitem_imageView);
            textView = view.findViewById(R.id.id_adapter_fileitem_textView);
        }
    }
}
