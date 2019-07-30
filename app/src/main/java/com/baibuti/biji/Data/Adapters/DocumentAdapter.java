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

import com.baibuti.biji.Data.Models.Document;
import com.baibuti.biji.R;

import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    private List<Document> mDocuments;
    private OnDocumentClickListener onDocumentClickListener;
    private OnDocumentLongClickListener onDocumentLongClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView;

        public ViewHolder(View view){
            super(view);

            imageView = (ImageView) view.findViewById(R.id.id_adapter_document_imageView);
            textView = (TextView) view.findViewById(R.id.id_adapter_document_textView);        }
    }

    public DocumentAdapter(List<Document> documents){
        mDocuments = documents;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.modulelayout_filefrag_documentlistitem, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if(null != onDocumentClickListener)
                    onDocumentClickListener.OnDocumentClick(mDocuments.get(position).getDocumentPath());
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = holder.getAdapterPosition();
                if(null != onDocumentLongClickListener) {
                    onDocumentLongClickListener.OnDocumentLongClick(position);
                }
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Document document = mDocuments.get(position);
        if(document.getDocumentType() != null) {
            switch (document.getDocumentType()) {
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
        holder.textView.setText(document.getDocumentName());
    }

    @Override
    public int getItemCount() {
        return mDocuments.size();
    }

    public void setOnDocumentClickListener(OnDocumentClickListener onDocumentClickListener){
        this.onDocumentClickListener = onDocumentClickListener;
    }

    public void setOnDocumentLongClickListener(OnDocumentLongClickListener onDocumentLongClickListener){
        this.onDocumentLongClickListener = onDocumentLongClickListener;
    }

    public interface OnDocumentClickListener{
        public void OnDocumentClick(String path);
    }

    public interface OnDocumentLongClickListener{
        public void OnDocumentLongClick(int position);
    }
}
