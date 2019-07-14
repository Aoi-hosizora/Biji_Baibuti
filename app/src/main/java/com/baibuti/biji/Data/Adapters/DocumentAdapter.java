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
                Toast.makeText(v.getContext(), "Click on document" + mDocuments.get(position).getDocumentName(), Toast.LENGTH_LONG).show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Document document = mDocuments.get(position);
        if(document.getDocumentType() != null) {
            Log.d("测试", "onBindViewHolder: 调用");
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
                    holder.imageView.setImageResource(R.drawable.excel);
                    break;
                default:
                    holder.imageView.setImageResource(R.drawable.other);
                    break;
            }
        }
        holder.textView.setText(document.getDocumentName());
    }

    @Override
    public int getItemCount() {
        return mDocuments.size();
    }
}
