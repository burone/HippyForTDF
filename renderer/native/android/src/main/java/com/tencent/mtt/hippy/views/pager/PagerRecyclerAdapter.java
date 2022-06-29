package com.tencent.mtt.hippy.views.pager;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.tencent.mtt.hippy.uimanager.RenderNode;
import com.tencent.mtt.hippy.views.hippylist.HippyRecyclerListAdapter;
import com.tencent.mtt.hippy.views.hippylist.HippyRecyclerView;

class PagerRecyclerAdapter extends HippyRecyclerListAdapter {

    public PagerRecyclerAdapter(HippyRecyclerView hippyRecyclerView) {
        super(hippyRecyclerView);
    }

    @Override
    protected void setLayoutParams(View itemView, int position) {
        RecyclerView.LayoutParams childLp = getLayoutParams(itemView);
        RenderNode childNode = getChildNodeByAdapterPosition(position);
        childLp.height = childNode.getHeight();
        childLp.width = childNode.getWidth();
        itemView.setLayoutParams(childLp);
    }

    public boolean canCircular() {
        return false;
    }

    public int getNormalizedPosition(int position) {
        return position;
    }

    public int getNormalizedItemCount() {
        return getRenderNodeCount();
    }

}
