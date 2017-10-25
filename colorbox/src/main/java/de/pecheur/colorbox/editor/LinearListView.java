package de.pecheur.colorbox.editor;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

public class LinearListView extends LinearLayout {
    private View mEmptyView;
    private ListAdapter mAdapter;
    private SparseBooleanArray mCheckStates;


    private DataSetObserver mDataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            setupChildren();
        }

        @Override
        public void onInvalidated() {
            setupChildren();
        }
    };

    public LinearListView(Context context) {
        this(context, null);
    }

    public LinearListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCheckStates = new SparseBooleanArray();
    }


    public ListAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * Sets the data behind this LinearListView.
     *
     * @param adapter The ListAdapter which is responsible for maintaining the data
     *                backing this list and for producing a view to represent an
     *                item in that data set.
     * @see #getAdapter()
     */
    public void setAdapter(ListAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataObserver);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(mDataObserver);
        }

        setupChildren();
    }


    /**
     * Sets the view to show if the adapter is empty
     */
    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;
        final ListAdapter adapter = getAdapter();
        final boolean empty = ((adapter == null) || adapter.isEmpty());
        updateEmptyStatus(empty);
    }

    /**
     * When the current adapter is empty, the LinearListView can display a special
     * view call the empty view. The empty view is used to provide feedback to
     * the user that no data is available in this LinearListView.
     *
     * @return The view to show if the adapter is empty.
     */
    public View getEmptyView() {
        return mEmptyView;
    }

    /**
     * Update the status of the list based on the empty parameter. If empty is
     * true and we have an empty view, display it. In all the other cases, make
     * sure that the layout is VISIBLE and that the empty view is GONE (if
     * it's not null).
     */
    private void updateEmptyStatus(boolean empty) {
        if (empty) {
            if (mEmptyView != null) {
                mEmptyView.setVisibility(View.VISIBLE);
                setVisibility(View.GONE);
            } else {
                setVisibility(View.VISIBLE);
            }
        } else {
            if (mEmptyView != null)
                mEmptyView.setVisibility(View.GONE);
            setVisibility(View.VISIBLE);
        }
    }

    private void setupChildren() {
        removeAllViews();
        mCheckStates.clear();

        updateEmptyStatus((mAdapter == null) || mAdapter.isEmpty());

        if (mAdapter == null) {
            return;
        }
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View child = mAdapter.getView(i, null, this);
            addViewInLayout(child, -1, child.getLayoutParams(), true);

            if(child instanceof Checkable) {
                ((Checkable) child).setChecked(mCheckStates.get(i));
            }


        }
    }
}