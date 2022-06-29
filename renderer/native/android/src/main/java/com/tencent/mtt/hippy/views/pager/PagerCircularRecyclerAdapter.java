package com.tencent.mtt.hippy.views.pager;

import androidx.annotation.NonNull;
import com.tencent.mtt.hippy.views.hippylist.HippyRecyclerView;
import com.tencent.mtt.hippy.views.hippylist.HippyRecyclerViewHolder;

class PagerCircularRecyclerAdapter extends PagerRecyclerAdapter {

    PagerCircularRecyclerAdapter(HippyRecyclerView rv) {
        super(rv);
    }

    @Override
    public boolean canCircular() {
       return true;
    }

    @Override
    public int getNormalizedPosition(int position) {
        return position % getRenderNodeCount();
    }

    @Override
    public int getItemCount() {
        return getRenderNodeCount() > 0 ? Integer.MAX_VALUE : 0;
    }

    @Override
    public int getItemViewType(int position) {
        setPositionToCreate(getNormalizedPosition(position));
        return 0;
    }

    @Override
    public void onBindViewHolder(@NonNull HippyRecyclerViewHolder holder, int position) {
        super.onBindViewHolder(holder, getNormalizedPosition(position));
    }


}
