package com.baibuti.biji.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.R;

import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder>
    implements View.OnClickListener, View.OnLongClickListener {

    private Context context;
    private List<Document> documentList;

    public DocumentAdapter(Context context) {
        this.context = context;
    }

    public void setDocumentList(List<Document> documentList) {
        this.documentList = documentList;
    }

    //////

    public interface OnDocumentClickListener {
        void OnDocumentClick(Document document);
    }

    public interface OnDocumentLongClickListener {
        void OnDocumentLongClick(Document document);
    }

    private OnDocumentClickListener onDocumentClickListener;
    private OnDocumentLongClickListener onDocumentLongClickListener;

    public void setOnDocumentClickListener(OnDocumentClickListener onDocumentClickListener){
        this.onDocumentClickListener = onDocumentClickListener;
    }

    public void setOnDocumentLongClickListener(OnDocumentLongClickListener onDocumentLongClickListener){
        this.onDocumentLongClickListener = onDocumentLongClickListener;
    }

    //////

    @Override
    public int getItemCount() {
        if (documentList == null)
            return 0;
        return documentList.size();
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
        Document document = (Document) v.getTag();
        if (null != onDocumentClickListener)
            onDocumentClickListener.OnDocumentClick(document);
    }

    @Override
    public boolean onLongClick(View v) {
        Document document = (Document) v.getTag();
        if (null != onDocumentLongClickListener)
            onDocumentLongClickListener.OnDocumentLongClick(document);
        return true;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Document document = documentList.get(position);
        holder.itemView.setTag(document);

        holder.textView.setText(document.getBaseFilename());
        switch (document.getFileExtension()) {
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
