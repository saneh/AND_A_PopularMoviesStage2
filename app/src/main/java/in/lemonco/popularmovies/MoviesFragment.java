package in.lemonco.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import in.lemonco.popularmovies.data.MovieContract;
import in.lemonco.popularmovies.sync.MoviesSyncAdapter;

/**
 * MoviesFragment, Replaces container in MainActivity
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> ,SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int MOVIES_LOADER = 0;
    private GridView mGridView;
    private int mPosition = GridView.INVALID_POSITION;
    private TextView mEmptyTextView;
    private static final String SELECTED_KEY = "selected_position";
    private static final String[] MOVIES_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.TABLE_NAME+"."+MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE
    };
    static final int COL_ID = 0;
    static final int COL_MOVIE_ID = 1;
    static final int COL_TITLE = 2;
    static final int COL_OVERVIEW = 3;
    static final int COL_POSTER_PATH = 4;
    static final int COL_RELEASE_DATE = 5;
    static final int COL_VOTE_AVERAGE = 6;


    private static final String LOG_TAG = MoviesFragment.class.getSimpleName();
    private MovieAdapter mMovieAdapter;
    Cursor cursor;
    private static Boolean isPrefChanged = false;

    interface Callback {
        public void onItemSelected(int movie_id);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        mEmptyTextView = (TextView)rootView.findViewById(R.id.empty_grid_textView);
        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);
        mGridView = (GridView) rootView.findViewById(R.id.movie_grid);
        mGridView.setEmptyView(mEmptyTextView);
        mGridView.setAdapter(mMovieAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) mMovieAdapter.getItem(position);
                if (cursor != null) {
                    ((Callback) getActivity()).onItemSelected(cursor.getInt(COL_MOVIE_ID));
                }
                mPosition = position;

            }
        });
        //If there is a savedInstanceState mine it for the position of selected movie
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //When tablet rotates the current selectd position will be saved in Bundle. Check for INVALID_POSITION
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isPrefChanged) {
            getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        sp.registerOnSharedPreferenceChangeListener(this);

    }
    @Override
    public void onPause(){
        super.onPause();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        sp.unregisterOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);

    }

    public static void setPrefChange(Boolean value) {
        isPrefChanged = value;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.i(LOG_TAG, "In on create loader");
        String sortCriteria = Utility.getSortCriteria(getActivity());
        if (sortCriteria != getString(R.string.sort_order_favorite)) {
            String selection = MovieContract.MovieEntry.COLUMN_SORT_CRITERIA + " = ? ";
            String[] selectionArgs = {sortCriteria};
            return new CursorLoader(getActivity(), MovieContract.MovieEntry.CONTENT_URI, MOVIES_COLUMNS, selection, selectionArgs, null);
        } else {

            return new CursorLoader(getActivity(), MovieContract.FavoriteMovieEntry.CONTENT_URI, MOVIES_COLUMNS, null, null, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            mMovieAdapter.swapCursor(cursor);
        if(Utility.getSortCriteria(getActivity())==getString(R.string.sort_order_favorite)&& mMovieAdapter.getCount()==0){
            Toast.makeText(getContext(),getString(R.string.error_msg_null_fav),Toast.LENGTH_SHORT).show();
        }
        if (mPosition != GridView.INVALID_POSITION) {
            Log.i(LOG_TAG, "Smooth Scrolling");
            mGridView.smoothScrollToPosition(mPosition);

        }
        updateEmptyView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mMovieAdapter.swapCursor(null);
    }

    //Displays appropriate message in case of empty MovieAdapter
    public void updateEmptyView() {
        if (mMovieAdapter.getCount() == 0) {
            if (mEmptyTextView != null) {
                int message = R.string.empty_text;
                @MoviesSyncAdapter.MovieServerStatus int movies_status = Utility.getMoviesStatus(getContext());
                switch (movies_status) {
                    case MoviesSyncAdapter.MOVIE_STATUS_SERVER_DOWN:
                        message = R.string.server_down;
                        break;
                    case MoviesSyncAdapter.MOVIE_STATUS_SERVER_INVALID:
                        message = R.string.server_invalid;
                        break;
                    default:
                        if (!Utility.checkNetworkState(getContext())) {
                            message = R.string.connectionError;
                        }
                }
                mEmptyTextView.setText(message);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_movie_status_key))){
            updateEmptyView();
        }

    }
}

