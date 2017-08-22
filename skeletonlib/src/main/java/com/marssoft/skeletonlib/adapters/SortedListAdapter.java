package com.marssoft.skeletonlib.adapters;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Comparator;
import java.util.List;

/**
 * Created by Alexey Sidorenko on 07-Sep-16.
 *
 * based on http://stackoverflow.com/questions/30398247/how-to-filter-a-recyclerview-with-a-searchview
 */
public abstract class SortedListAdapter<T> extends RecyclerView.Adapter<SortedListAdapter.ViewHolder<T>> {
    private static final String TAG = SortedListAdapter.class.getSimpleName();
    private final Comparator<T> mComparator;
    private final LayoutInflater mInflater;
    protected View.OnClickListener mOnClickListener;
    private SortedList<T> mData;
    private Context mContext;

    public SortedListAdapter(Context context, Class<T> itemClass, View.OnClickListener onClickListener, Comparator<T> comparator) {
        mOnClickListener = onClickListener;
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mComparator = comparator;
        mData = new SortedList<>(itemClass, new SortedList.Callback<T>() {
            @Override
            public int compare(T oldItem, T newItem) {
                return mComparator.compare(oldItem, newItem);
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(T oldItem, T newItem) {
                // return whether the items' visual representations are the same or not.
                return SortedListAdapter.this.areItemContentsTheSame(oldItem, newItem);
            }

            @Override
            public boolean areItemsTheSame(T oldItem, T newItem) {
                return SortedListAdapter.this.areItemsTheSame(oldItem, newItem);
            }
        });
    }

    protected abstract boolean areItemsTheSame(T oldItem, T newItem);

    protected abstract boolean areItemContentsTheSame(T oldItem, T newItem);

    @Override
    public ViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        return onCreateViewHolder(mInflater, parent, viewType);
    }

    protected abstract ViewHolder<T> onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(ViewHolder<T> holder, int position) {
        final T item = mData.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public abstract static class ViewHolder<T> extends RecyclerView.ViewHolder {

        private T mCurrentItem;

        public ViewHolder(View itemView) {
            super(itemView);
        }

        public final void bind(T item) {
            mCurrentItem = item;
            performBind(item);
        }

        protected abstract void performBind(T item);
        public final T getCurrentItem() {
            return mCurrentItem;
        }
    }


    public void clear() {
        mData.beginBatchedUpdates();
        //remove items at end, to avoid unnecessary array shifting
        while (mData.size() > 0) {
            mData.removeItemAt(mData.size() - 1);
        }
        mData.endBatchedUpdates();
    }

    public void replaceAll(List<T> newData) {
        mData.beginBatchedUpdates();
        // todo may be better to use iterator?
        for (int i = mData.size() - 1; i >= 0; i--) {
            final T model = mData.get(i);
            if (!newData.contains(model)) {
                mData.remove(model);
            }
        }
        mData.addAll(newData);
        mData.endBatchedUpdates();
    }

}

