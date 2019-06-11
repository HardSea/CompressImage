package com.hillywave.compressimage;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class FolderAdapter extends BaseListAdapter<FileInfo, FolderAdapter.ViewHolder> {
    private int itemsSelected = 0;
    private final ThumbnailLoader thumbnailLoader;

    FolderAdapter(Context context) {
        super(context, R.layout.row_file);

        this.thumbnailLoader = new ThumbnailLoader(context.getResources());
    }

    @Override
    protected ViewHolder viewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    protected void fillView(View rowView, ViewHolder viewHolder, FileInfo fileInfo) {
        viewHolder.name.setText(fileInfo.name());

        if (fileInfo.isDirectory()) {
            int numberOfChildren = fileInfo.numberOfChildren();

            viewHolder.size.setText(getContext().getResources().getQuantityString(R.plurals.itemAmount, numberOfChildren, numberOfChildren));

            viewHolder.icon.setImageResource(R.drawable.ic_folder);
            viewHolder.extension.setText(null);
            viewHolder.extension.setBackgroundResource(android.R.color.transparent);
        } else {
            if (fileInfo.isImage()) {
                viewHolder.size.setText(fileInfo.getImageFormat());
                viewHolder.size.append(" ");
                viewHolder.size.append(fileInfo.size());
            } else {
                viewHolder.size.setText(fileInfo.size());
            }

            if (fileInfo.isImage()) {
                thumbnailLoader.load(fileInfo, viewHolder.icon);
                viewHolder.extension.setText(null);
                viewHolder.extension.setBackgroundResource(android.R.color.transparent);
            } else if (fileInfo.isPdf()) {
                viewHolder.icon.setImageResource(R.drawable.ic_pdf);
                viewHolder.extension.setText(null);
                viewHolder.extension.setBackgroundResource(android.R.color.transparent);
            } else if (fileInfo.isAudio()) {
                viewHolder.icon.setImageResource(R.drawable.ic_audio);
                viewHolder.extension.setText(null);
                viewHolder.extension.setBackgroundResource(android.R.color.transparent);
            } else if (fileInfo.isVideo()) {
                viewHolder.icon.setImageResource(R.drawable.ic_video);
                viewHolder.extension.setText(null);
                viewHolder.extension.setBackgroundResource(android.R.color.transparent);
            } else {
                viewHolder.icon.setImageResource(R.drawable.ic_file);

                String extension = fileInfo.extension();

                if (!extension.isEmpty()) {
                    viewHolder.extension.setText(extension);
                    viewHolder.extension.setBackgroundResource(R.drawable.extension_border);

                    if (extension.length() <= 3) {
                        viewHolder.extension.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
                    } else {
                        viewHolder.extension.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                    }
                } else {
                    viewHolder.extension.setText(null);
                    viewHolder.extension.setBackgroundResource(android.R.color.transparent);
                }
            }
        }

        if (fileInfo.isSelected()) {
            rowView.setBackgroundColor(Color.GRAY);
        } else {
            rowView.setBackgroundColor(0);
        }
    }

    void updateSelection(boolean itemAdded) {
        notifyDataSetChanged();

        itemsSelected += itemAdded ? 1 : -1;
    }

    void setData(List<FileInfo> list) {
        update(list);
        unselectAll();
    }

    void unselectAll() {
        for (int i = 0; i < getCount(); i++) {
            FileInfo fileInfo = getItem(i);

            if (fileInfo != null) {
                fileInfo.select(false);
            }
        }

        itemsSelected = 0;
        notifyDataSetChanged();
    }


    boolean isSelectionMode() {
        return itemsSelected > 0;
    }


    protected static class ViewHolder {
        public final TextView name;
        final TextView size;
        final TextView extension;
        final ImageView icon;

        ViewHolder(View view) {
            this.name = view.findViewById(R.id.name);
            this.size = view.findViewById(R.id.size);
            this.extension = view.findViewById(R.id.extension);
            this.icon = view.findViewById(R.id.icon);
        }
    }
}
