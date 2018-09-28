package com.huawei.smart.server.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.CollectionActivity;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CollectFileListAdapter extends BaseListItemAdapter<File, CollectFileListAdapter.CollectFileItemViewHolder> {

    public CollectFileListAdapter(Context context, List<File> items) {
        super(context, items);
    }

    @Override
    public CollectFileItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new CollectFileItemViewHolder(LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.list_collect_file_item, null));
    }

    @Override
    public void onBindViewHolder(CollectFileItemViewHolder holder, int position) {
        holder.initialize(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void resetItems(List<File> items) {
        this.items.clear();
        Collections.sort(items, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
               return t1.getName().compareTo(file.getName());
            }
        });
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public class CollectFileItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private File file;
        private TextView filename;
        private View shareButton;
        private View deleteButton;

        private CollectFileItemViewHolder(View itemView) {
            super(itemView);
            filename = itemView.findViewById(R.id.filename);
            shareButton = itemView.findViewById(R.id.share_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
            shareButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
        }


        public void initialize(File file) {
            this.file = file;
            filename.setText(file.getName());
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.share_button) {
                CollectionActivity.share(context, file.getAbsolutePath(), "application/gzip");
            } else if (view.getId() == R.id.delete_button) {
                new MaterialDialog.Builder(context)
                    .content(R.string.collect_prompt_delete_file)
                    .positiveText(R.string.button_done)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            items.remove(file);
                            file.delete();
                            notifyDataSetChanged();
                        }
                    })
                    .show();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }

    }

}
