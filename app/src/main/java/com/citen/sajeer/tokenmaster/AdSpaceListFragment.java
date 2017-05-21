package com.citen.sajeer.tokenmaster;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.citen.sajeer.tokenmaster.helper.SimpleItemTouchHelperCallback;

import java.util.ArrayList;


public class AdSpaceListFragment extends Fragment{
    View v;

    public AdSpaceListFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_ad_space_list, container, false);
        return v;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        RecyclerView rv = (RecyclerView)v.findViewById(R.id.ad_space_list_view);
        rv.setHasFixedSize(true);
        rv.setClickable(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        RVAdapter rvAdapter = new RVAdapter();
        rv.setAdapter(rvAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        OnAdSpaceSelectionChangeListner listener = (OnAdSpaceSelectionChangeListner) getActivity();
        listener.changeTitle(0);

    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.AdSpaceViewHolder>{

        ArrayList<AdSpaceData> adSpaceDatas = new ArrayList<>();

        public RVAdapter() {
            this.adSpaceDatas.add(new AdSpaceData("Ad Space 1", "Top Right Corner Space (Images)", R.drawable.image2_tn));
            this.adSpaceDatas.add(new AdSpaceData("Ad Space 2", "Centre Right Space (Images)", R.drawable.image4_tn));
            this.adSpaceDatas.add(new AdSpaceData("Ad Space 3", "Bottom Right Corner Space (Video)", R.drawable.image5_tn));
            this.adSpaceDatas.add(new AdSpaceData("Ad Space 4", "Main Content Area (Video)", R.drawable.image6_tn));
            this.adSpaceDatas.add(new AdSpaceData("Ad Space 5", "Bottom Scroll Area (Text)", R.drawable.image7_tn));

        }

        @Override
        public AdSpaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_space_cardview, parent, false);
            AdSpaceViewHolder adSpaceViewHolder = new AdSpaceViewHolder(v);
            return adSpaceViewHolder;
        }

        @Override
        public void onBindViewHolder(AdSpaceViewHolder holder, int position) {
            holder.adSpaceName.setText(adSpaceDatas.get(position).getAdSpaceName());
            holder.adSpaceSummary.setText(adSpaceDatas.get(position).getAdSpaceDiscription());
            holder.adSpacePhoto.setImageResource(adSpaceDatas.get(position).getAdSpaceImageID());
            holder.adSpacePhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        @Override
        public int getItemCount() {
            return adSpaceDatas.size();
        }

        public class AdSpaceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            CardView cv;
            TextView adSpaceName;
            TextView adSpaceSummary;
            ImageView adSpacePhoto;

            AdSpaceViewHolder(View itemView) {
                super(itemView);
                cv = (CardView)itemView.findViewById(R.id.cv);
                adSpaceName = (TextView)itemView.findViewById(R.id.adspace_name);
                adSpaceSummary = (TextView)itemView.findViewById(R.id.adspace_summary);
                adSpacePhoto = (ImageView)itemView.findViewById(R.id.adspace_photo);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                OnAdSpaceSelectionChangeListner listener = (OnAdSpaceSelectionChangeListner) getActivity();
                Log.d("Click", Integer.toString(getAdapterPosition()));
                listener.onSelectionChanged(getAdapterPosition());
            }
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

    }


}
