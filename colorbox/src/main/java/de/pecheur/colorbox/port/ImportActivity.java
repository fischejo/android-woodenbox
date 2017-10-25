package de.pecheur.colorbox.port;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import de.pecheur.colorbox.R;

public class ImportActivity extends ListActivity implements OnNavigationListener, LoaderCallbacks<List<UnitMap>> {
    private static final String BUNDLE_URI = "uri";
    private static final int LOADER_ID = 1;

    private WordListAdapter mAdapter;
    private NavigationAdapter unitAdapter;
    private List<UnitMap> mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getActionBar().setTitle("Importieren");
        //getActionBar().setIcon(android.R.color.transparent);


        Intent intent = getIntent();
        String action = intent.getAction();
        Uri uri = intent.getData();
        String type = intent.getType();
        String path = uri.getScheme().equals("file") ? uri.getPath() : null;

        Bundle args = new Bundle();
        args.putParcelable(BUNDLE_URI, uri);


        if (Intent.ACTION_VIEW.equals(action) && (
                (type != null && type.endsWith("zip")) ||
                (path != null && path.endsWith(".zip")) )) {

        }else{
            Toast.makeText(this, "kann nicht geöffnet werden!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        // list adapter
        mAdapter = new WordListAdapter(this, R.layout.word_list_item_checked);
        setListAdapter(mAdapter);

        // asnyc xml loader
        getLoaderManager().initLoader(LOADER_ID, args, this);

        ListView listview = getListView();
        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }



    @Override
    public boolean onNavigationItemSelected(int position, long arg1) {
        mAdapter.clear();
        mAdapter.addAll( mContent.get(position).getContent());
        return false;
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.import_options, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.import_button);
        item.setVisible(mContent != null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.import_button:
                onImport();
        }

        return super.onOptionsItemSelected(item);
    }

    private void onImport() {
        UnitMap[] array = mContent.toArray(new UnitMap[mContent.size()]);
        new AsyncImportTask(this).execute(array);
    }





    @Override
    public Loader<List<UnitMap>> onCreateLoader(int id, Bundle args) {
       return new AsyncZipLoader(this, (Uri) args.getParcelable(BUNDLE_URI));
    }


    @Override
    public void onLoadFinished(Loader<List<UnitMap>> loader, List<UnitMap> content) {
        mAdapter.clear();
        mContent = content;

        if(content == null) {
            // error handling -> exit
            Log.d("import", "content == null");
            return;
        }

        if(content.isEmpty()) {
            Log.d("import", "content is empty");
            // empty handling -> exit
            return;
        }

        invalidateOptionsMenu();

        ActionBar ab = getActionBar();
        if(content.size() > 1) {
            unitAdapter = new NavigationAdapter(ab.getThemedContext(), content);
            ab.setListNavigationCallbacks(unitAdapter, this);
            ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            ab.setTitle("");

        } else {
            mAdapter.addAll(content.get(0).getContent());
            ab.setTitle(content.get(0).getTitle());
        }
    }


    @Override
    public void onLoaderReset(Loader<List<UnitMap>> loader) {
        mAdapter.clear();
    }


}