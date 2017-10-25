package de.pecheur.colorbox.unit;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import de.pecheur.colorbox.database.VocabularyProvider;
import de.pecheur.colorbox.R;


public class UnitListFragment extends ListFragment implements ActionBar.OnNavigationListener {
    private static final String ARGUMENTS_UNIT = "unit";
    private static final int LOADER_ID = 0;

    private UnitSelectionListener mCallback;
    private boolean dualPaneMode;
    private UnitListAdapter mAdapter;
    private LoaderManager loaderManager;
    private ContentResolver contentResolver;
    private NavigationAdapter navigationAdapter;
    private Context mContext;



    /**
     * additional observer for observing the word table. This is necessary
     * because the subtitle message is depend on words.
     */
    private ContentObserver wordObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            getLoaderManager().restartLoader(LOADER_ID, null, mAdapter);
        }
    };

    /**
     * observer for observing the selection after an async load.
     */
    private DataSetObserver selectionObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            int position = getListView().getCheckedItemPosition();
            if (position != ListView.INVALID_POSITION && mAdapter.getCount() > position) {
                mCallback.onUnitSelected(mAdapter.getItem(position));
            } else {
                mCallback.onUnitSelected(null);
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mAdapter = new UnitListAdapter(getActivity(), R.layout.unit_list_item);
        setListAdapter(mAdapter);

        dualPaneMode = getResources().getBoolean(R.bool.dualPaneMode);
        loaderManager = getLoaderManager();



        ActionBar actionBar = getActivity().getActionBar();
        navigationAdapter = new NavigationAdapter(actionBar.getThemedContext());
        if(!navigationAdapter.isEmpty()) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            actionBar.setListNavigationCallbacks(navigationAdapter, this);
        }

    }


    @Override
    public void onStart() {
        super.onStart();

        // register selection observer
        if (dualPaneMode) mAdapter.registerDataSetObserver(selectionObserver);


        // register word observer
        contentResolver.registerContentObserver(
                VocabularyProvider.WORD_URI,
                true,
                wordObserver);

        // init or reload unit list
        if (loaderManager.getLoader(LOADER_ID) == null) {
            loaderManager.initLoader(LOADER_ID, null, mAdapter);
        } else {
            loaderManager.restartLoader(LOADER_ID, null, mAdapter);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        // unregister selection observer
        if (dualPaneMode) mAdapter.unregisterDataSetObserver(selectionObserver);

        // unregister word observer
        contentResolver.unregisterContentObserver(wordObserver);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        contentResolver = getActivity().getContentResolver();

        // This makes sure that the container activity has implemented
        // the callback interface.
        try {
            mCallback = (UnitSelectionListener) activity;
            mContext = activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement UnitSelectionListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_content, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set empty text
        setEmptyText(R.string.unit_list_empty_hint);

        // When in two-pane layout, set the listview to highlight the selected list item
        getListView().setChoiceMode(dualPaneMode ?
                ListView.CHOICE_MODE_SINGLE :
                ListView.CHOICE_MODE_NONE);
    }


    @Override
    public void onListItemClick(ListView listview, View v, int pos, long id) {
        // notify activity about the changed selection.
        listview.setItemChecked(pos, true);
        mCallback.onUnitSelected(mAdapter.getItem(pos));
    }


    @Override
    public void onCreateOptionsMenu(Menu nMenu, MenuInflater inflater) {
        inflater.inflate(R.menu.unit_fragment_options, nMenu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.unit_add:
                // start Dialog for adding item
                Intent intent = new Intent(getActivity(), UnitEditorActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Workaround for the Issue 21742: 	Compatibility Library v4 ListFragment
     * uses internal id for 'empty' TextView!
     */
    private void setEmptyText(int resid) {
        ((TextView) (getListView().getEmptyView())).setText(resid);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {

        mAdapter.code = navigationAdapter.getItem(itemPosition);
        getLoaderManager().restartLoader(LOADER_ID, null, mAdapter);
        return false;
    }
}