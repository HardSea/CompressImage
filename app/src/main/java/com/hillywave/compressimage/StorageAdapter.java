package com.hillywave.compressimage;

import android.content.Context;
import android.os.StatFs;
import android.view.View;
import android.widget.TextView;

public class StorageAdapter extends BaseListAdapter<String, StorageAdapter.ViewHolder> {
    public StorageAdapter(Context context) {
        super(context, R.layout.row_storage);
    }

    @Override
    protected ViewHolder viewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    protected void fillView(View rowView, ViewHolder viewHolder, String item) {
        viewHolder.name.setText(item);

        try {
            StatFs stat = new StatFs(item);
            long blockSize = (long) stat.getBlockSize();
            long totalSpace = blockSize * stat.getBlockCount();
            long availableSpace = blockSize * stat.getAvailableBlocks();

            SpaceFormatter spaceFormatter = new SpaceFormatter();

            String labelTotal = "Total";
            String total = spaceFormatter.format(totalSpace);
            String labelAvailable = "Aviable";
            String available = spaceFormatter.format(availableSpace);
            viewHolder.space.setText(String.format("%s: %s     %s: %s", labelTotal, total, labelAvailable, available));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static class ViewHolder {
        public final TextView name;
        public final TextView space;

        public ViewHolder(View view) {
            this.name = view.findViewById(R.id.name);
            this.space = view.findViewById(R.id.space);
        }
    }


}
