package com.citen.sajeer.tokenmaster;

import android.content.ClipData;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.citen.sajeer.tokenmaster.helper.ItemTouchHelperAdapter;
import com.citen.sajeer.tokenmaster.helper.ItemTouchHelperViewHolder;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Simple RecyclerView.Adapter that implements {@link ItemTouchHelperAdapter} to respond to move and
 * dismiss events from a {@link android.support.v7.widget.helper.ItemTouchHelper}.
 *
 * @author Paul Burke (ipaulpro)
 */
public class AdSpaceRecyclerListAdapter extends RecyclerView.Adapter<AdSpaceRecyclerListAdapter.ItemViewHolder>
        implements ItemTouchHelperAdapter {

    /**
     * Listener for manual initiation of a drag.
     */
    public interface OnStartDragListener {

        /**
         * Called when a view is requesting a start of a drag.
         *
         * @param viewHolder The holder of the view to drag.
         */
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private static final String[] STRINGS = new String[]{};

    private List<AdData> mItems;
    List<AdData> adsToDelete = new ArrayList<AdData>();

    private final OnStartDragListener mDragStartListener;
    private final OnListChangeToDbListner dbToChangeListner;
    CoordinatorLayout coordinatorLayout;

    public AdSpaceRecyclerListAdapter(OnStartDragListener dragStartListener, OnListChangeToDbListner dbToChangeListner, List<AdData> adList, CoordinatorLayout coordinatorLayout) {
        this.mDragStartListener = dragStartListener;
        this.mItems = new ArrayList<>(adList);
        this.dbToChangeListner = dbToChangeListner;
        this.coordinatorLayout = coordinatorLayout;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        ItemViewHolder itemViewHolder = new ItemViewHolder(view);
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        holder.fileName.setText(mItems.get(position).getDisplayName());
        Bitmap thumbnailImage = loadImageFromStorage(mItems.get(position).getDirectoryPath(), (mItems.get(position).getAdSpaceId()==4)?"letter_t.png":mItems.get(position).getFileName());
        if(thumbnailImage != null)
            holder.thumbImage.setImageBitmap(thumbnailImage);
        // Start a drag whenever the handle view it touched
        holder.handleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {

        public final TextView fileName;
        public final ImageView handleView;
        public final CircleImageView thumbImage;

        public ItemViewHolder(View itemView) {
            super(itemView);
            fileName = (TextView) itemView.findViewById(R.id.file_name);
            handleView = (ImageView) itemView.findViewById(R.id.handle);
            thumbImage = (CircleImageView) itemView.findViewById(R.id.thumb_image);
        }

        @Override
        public void onItemSelected() {
            //itemView.setBackgroundColor(Color.RED);
        }

        @Override
        public void onItemClear() {
            //itemView.setBackgroundColor(0);
        }
    }

    @Override
    public void onItemDismiss(final int position) {
        final int adapterPosition = position;
        final AdData adData = mItems.get(position);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "ITEM REMOVED", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mItems.add(adapterPosition, adData);
                        notifyItemInserted(adapterPosition);
                        adsToDelete.remove(adData);
                    }
                });
        snackbar.show();

        mItems.remove(position);
        notifyItemRemoved(position);
        adsToDelete.add(adData);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * Simple example of a view holder that implements {@link ItemTouchHelperViewHolder} and has a
     * "handle" view that initiates a drag event when touched.
     */

    public void updateAdList(List<AdData> adList) {
        this.mItems = adList;
        notifyDataSetChanged();
    }

    public void appendToAdList(AdData newFile){
        this.mItems.add(newFile);
        notifyItemChanged(this.mItems.size() - 1);
    }

    List<AdData> getAdListItems(){
        return this.mItems;
    }

    private Bitmap loadImageFromStorage(String path, String fileName)
    {
        Bitmap b = null;
        try {
            File f = new File(path, fileName);
            b = BitmapFactory.decodeStream(new FileInputStream(f));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return b;
    }

}