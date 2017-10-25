package de.pecheur.colorbox.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import java.io.IOException;

import de.pecheur.colorbox.R;
import de.pecheur.colorbox.card.CardActivity;
import de.pecheur.colorbox.models.Unit;
import de.pecheur.colorbox.models.Word;
import de.pecheur.colorbox.editor.EditorActivity;
import de.pecheur.colorbox.unit.UnitEditorActivity;
import de.pecheur.colorbox.unit.UnitSelectionListener;


public class DetailFragment extends ListFragment implements
        OnQueryTextListener, OnActionExpandListener, MultiChoiceModeListener,
        UnitSelectionListener {

	private Unit mUnit;
	private WordCursorAdapter mAdapter;
	private Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();

		// Activate options menu & HomeAsUp-Button
		setHasOptionsMenu(true);

		// create & set word adapter
		mAdapter = new WordCursorAdapter(
                getActivity(),
                R.layout.word_list_item);

		setListAdapter(mAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_content, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// set multiple modal choice & fast scroll
		ListView listview = getListView();
		listview.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
		listview.setMultiChoiceModeListener(this);
		listview.setFastScrollEnabled(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Unit unit = getActivity().getIntent().getParcelableExtra(DetailActivity.EXTRA_UNIT);
		onUnitSelected(unit);
	}

	@Override
	public void onUnitSelected(Unit unit) {
		mUnit = unit;

		// adapter handles INVALID_ROW_ID itself.
		mAdapter.setUnit(unit);

		setEmptyText(unit == null ?
                R.string.word_list_no_unit_hint
				: R.string.word_list_empty_hint);

		// update actionbar
		getActivity().invalidateOptionsMenu();
	}

	@Override
	public void onListItemClick(ListView listview, View view, int pos, long id) {
		// edit card
		Intent intent = new Intent(getActivity(), EditorActivity.class);
		intent.putExtra(EditorActivity.EXTRA_WORD, (Word) listview.getItemAtPosition(pos));
		startActivity(intent);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.word_options, menu);

        // initialize addon action provider
        MenuItem item = menu.findItem(R.id.word_addon);
        AddonActionProvider aap = (AddonActionProvider) item.getActionProvider();
        aap.setActivity(getActivity());
        aap.setUnit(mUnit);
        item.setVisible(aap.hasSubMenu());

        // show menu items, if an unit is selected
        menu.setGroupVisible(R.id.word_group, mUnit != null);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
        case R.id.word_share:
            try {
                new AsyncShareTask(getActivity()).execute(mUnit);
            } catch(IOException e) {
                e.printStackTrace();
            }
            return true;

		case R.id.word_add: // add Item
			intent = new Intent(getActivity(), EditorActivity.class);
		    intent.putExtra(EditorActivity.EXTRA_UNIT, mUnit);
			startActivity(intent);
			return true;

		case R.id.word_search:
			// search view
			SearchView search = (SearchView) item.getActionView();
			item.setOnActionExpandListener(this);
			search.setOnQueryTextListener(this);
			return true;

		case R.id.word_query:
			intent = new Intent(getActivity(), CardActivity.class);
			intent.putExtra(CardActivity.EXTRA_UNIT, mUnit);
			startActivity(intent);
			return true;

		case R.id.unit_edit:
			// edit section
			intent = new Intent(getActivity(), UnitEditorActivity.class);
			intent.putExtra(DetailActivity.EXTRA_UNIT, mUnit);
			startActivity(intent);
			return true;
			
		case R.id.word_shortcut:
			Unit.addShortcut(getActivity(), mUnit);
			return true;
		}
		return false;
	}

	/**
	 * Called when SearchButton is collapsed
	 */
	@Override
	public boolean onMenuItemActionCollapse(MenuItem item) {
		mAdapter.filter(null);

		// reset empty text of listview
		setEmptyText(mUnit == null ? R.string.word_list_no_unit_hint
				: R.string.word_list_empty_hint);
		return true; // Return true to collapse action view
	}

	/**
	 * Called when SearchButton is expanded
	 */
	@Override
	public boolean onMenuItemActionExpand(MenuItem item) {
		setEmptyText(R.string.word_list_search_hint);
		return true;
	}

	/**
	 * Called when search query changed
	 */
	@Override
	public boolean onQueryTextChange(String filter) {
		mAdapter.filter(filter);
		return false;
	}

	/**
	 * Called when search query submit (we don't care about it)
	 */
	@Override
	public boolean onQueryTextSubmit(String query) {
		return true;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.getMenuInflater().inflate(R.menu.word_contextual, menu);
		return true;
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {

		int count = getListView().getCheckedItemCount();
		String title = getResources().getQuantityString(
				R.plurals.word_activity_menu_hint, count, count);
		mode.setTitle(title);
		return;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		ListView listview = getListView();
		long[] ids = listview.getCheckedItemIds();

		switch (item.getItemId()) {
		case R.id.word_move:
			WordDialog.MoveWordBuilder(getActivity(), mUnit, ids).show();
			mode.finish();
			break;

		case R.id.word_delete: // delete words
			WordDialog.DeleteWordsBuilder(getActivity(), ids).show();
			mode.finish();
			break;

		case R.id.word_undo: // clear words
			WordDialog.UndoWordBuilder(getActivity(), ids).show();
			mode.finish();
			break;

		case R.id.word_select_all: // select all words
			for (int i = 0; i < listview.getCount(); i++) {
				listview.setItemChecked(i, mAdapter.isEnabled(i));
			}
			break;

        // TODO: word_reverse unhandled
		case R.id.word_reverse: // reverse entries
            /*
			getActivity().getContentResolver().call(
					VocabularyProvider.TEXT_URI,
					VocabularyProvider.CALL_REVERSE,
					WordColumns._ID + " IN (" + SqliteHelper.convertIDs(ids)
							+ ")", null);
							*/
			break;

		default:
			return false;
		}

		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		return;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        AddonActionProvider.onActivityResult(
                getActivity(),
                requestCode,
                resultCode,
                data);
	}

	/**
	 * Workaround for the Issue 21742: Compatibility Library v4 ListFragment
	 * uses internal id for 'empty' TextView!
	 */
	private void setEmptyText(int resid) {
		((TextView) (getListView().getEmptyView())).setText(resid);
	}
}
