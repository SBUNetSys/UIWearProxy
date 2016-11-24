package edu.stonybrook.cs.netsys.uiwearlib.viewProtocol;

import static edu.stonybrook.cs.netsys.uiwearlib.viewProtocol.ViewUtil.renderView;
import static edu.stonybrook.cs.netsys.uiwearlib.viewProtocol.ViewUtil.setViewListener;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataNode;

public class WearableListAdapter extends WearableListView.Adapter {
    private Context mContext;
    private int mItemLayoutResId;
    private List<String> mPhoneItemViewIdList;
    private int[] mItemIds;
    private ArrayList<DataNode[]> mListDataNodes;

    public WearableListAdapter(Context context, int itemLayoutResId,
            List<String> phoneItemViewIdList,
            int[] itemIds, ArrayList<DataNode[]> listDataNodes) {
        mContext = context;
        mItemLayoutResId = itemLayoutResId;
        mPhoneItemViewIdList = phoneItemViewIdList;
        mItemIds = itemIds;
        mListDataNodes = listDataNodes;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
            int viewType) {
        return new WearableListView.ViewHolder(
                LayoutInflater.from(mContext).inflate(mItemLayoutResId, parent, false));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        DataNode[] dataNodes = mListDataNodes.get(position);
        for (DataNode node : dataNodes) {
            Logger.d("new item node: " + node);
            String phoneItemNodeViewId = node.getViewId();
            int index = mPhoneItemViewIdList.indexOf(phoneItemNodeViewId);

            View itemNodeView = holder.itemView.findViewById(mItemIds[index]);
            setViewListener(node, itemNodeView);
            renderView(mContext, node, itemNodeView);

        }

    }

    @Override
    public int getItemCount() {
        return mListDataNodes.size();
    }
}
