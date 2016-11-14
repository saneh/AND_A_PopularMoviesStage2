package in.lemonco.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import in.lemonco.popularmovies.data.MovieContract;

/**
 * MovieDetailFragment
 */
public class MovieDetailFragment extends Fragment{
    public static final String MOVIE_ID = "movie_id";
    private static final int MOVIE_DETAIL_LOADER = 1;
    private static final int MOVIE_TRAILER_LOADER =2;
    private static final int MOVIE_REVIEW_LOADER =3;
    private static final int MOVIE_FAV_LOADER =4;
    private LoaderManager.LoaderCallbacks<Cursor> mCursorLoaderListener;
    private LoaderManager.LoaderCallbacks<Cursor> mCursorLoaderListener_FavMovie;
    private LoaderManager.LoaderCallbacks<ArrayList<String>> mTrailersLoaderListener;
    private LoaderManager.LoaderCallbacks<ArrayList<String>> mReviewLoaderListener;
    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    private ToggleButton mFavButton;
    private ArrayList<String> trailerIds;
    private int movie_id;
    private static final String[] MOVIES_COLUMNS ={
            MovieContract.MovieEntry.TABLE_NAME+"."+ MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_BACKDROP_PATH
    };
    static final int COL_ID = 0;
    static final int COL_MOVIE_ID=1;
    static final int COL_TITLE = 2;
    static final int COL_OVERVIEW = 3;
    static final int COL_POSTER_PATH = 4;
    static final int COL_RELEASE_DATE = 5;
    static final int COL_VOTE_AVERAGE = 6;
    static final int COL_BACKDROP_PATH = 7;

    public void MovieDetailFragment(){}

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_movie_detail, container, false);

        Bundle arguments = getArguments();
        Intent intent = getActivity().getIntent();
        if (arguments != null) {
            movie_id = (Integer)arguments.getSerializable(MOVIE_ID);
        } else if (intent != null) {
            movie_id = intent.getIntExtra(MOVIE_ID,-1);
        }
        //Favourite Toggle Button
        mFavButton = (ToggleButton)rootView.findViewById(R.id.favorite_button);
        mFavButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isPressed()){
                    if (isChecked) {
                        mFavButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favourite_button_on));
                        ContentValues values = new ContentValues();
                        values.put(MovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID, movie_id);
                        Uri row = getContext().getContentResolver().insert(MovieContract.FavoriteMovieEntry.CONTENT_URI, values);
                        Toast.makeText(getActivity(),getString(R.string.fav_movie_addition),Toast.LENGTH_SHORT).show();
                        if (row != null) {
                            Log.i(LOG_TAG, "Movie successfully inserted in favorite list" + movie_id);
                        }
                    } else {
                        mFavButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favourite_button_off));
                        String selection = MovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID + " = ? ";
                        String[] selectionArgs = new String[]{String.valueOf(movie_id)};
                        int rows_deleted = getContext().getContentResolver().delete(MovieContract.FavoriteMovieEntry.CONTENT_URI, selection, selectionArgs);
                        Toast.makeText(getActivity(),getString(R.string.fav_movie_deletion),Toast.LENGTH_SHORT).show();
                        if (rows_deleted != 0) {
                            Log.i(LOG_TAG, "Movie successfully deleted from favorite list" + movie_id);
                        }
                    }
                }

            }
        });

        Button showReview = (Button)rootView.findViewById(R.id.reviews);
        showReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLoaderManager().initLoader(MOVIE_REVIEW_LOADER,null,mReviewLoaderListener).forceLoad();

            }
        });
        //Implementation of CursorLoader Listener (For movie details)
        mCursorLoaderListener = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String selection = MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";
                String[] selectionArgs = {String.valueOf(movie_id)};
                return new CursorLoader(getActivity(), MovieContract.MovieEntry.CONTENT_URI,MOVIES_COLUMNS,selection,selectionArgs,null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                if(cursor.moveToFirst()) {
                    ImageView movieBackdrop = (ImageView) getView().findViewById(R.id.movie_backdrop);
                    Picasso.with(getActivity()).load(Utility.getBackdropPath(cursor.getString(COL_BACKDROP_PATH))).into(movieBackdrop);

                    TextView movieTitle = (TextView) getView().findViewById(R.id.movie_title);
                    movieTitle.setText(cursor.getString(COL_TITLE));

                    TextView movieRating = (TextView) getView().findViewById(R.id.movie_rating);
                    movieRating.setText(cursor.getString(COL_VOTE_AVERAGE)+ getActivity().getString(R.string.movie_rating_text));

                    TextView releaseDate = (TextView) getView().findViewById(R.id.release_date);
                    releaseDate.setText(getActivity().getString(R.string.release_date_text)+cursor.getString(COL_RELEASE_DATE));

                    TextView overview = (TextView) getView().findViewById(R.id.movie_overview);
                    overview.setText(cursor.getString(COL_OVERVIEW));

                    getActivity().setTitle(getString(R.string.title_activity_movie_detail));
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
        //Cursor loader to set the favourite_button background
        mCursorLoaderListener_FavMovie = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String selection = MovieContract.MovieEntry.TABLE_NAME+"."+MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";
                String[] selectionArgs = {String.valueOf(movie_id)};
                return new CursorLoader(getActivity(), MovieContract.FavoriteMovieEntry.CONTENT_URI,new String[]{MovieContract.MovieEntry.TABLE_NAME+"."+MovieContract.MovieEntry.COLUMN_MOVIE_ID},selection,selectionArgs,null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                if(cursor!= null && cursor.moveToFirst()){
                    mFavButton.setChecked(true);
                    mFavButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favourite_button_on));
                }else
                {
                    mFavButton.setChecked(false);
                    mFavButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favourite_button_off));
                }

            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
        //Implementation of AsyncTaskLoader Listener (For Trailer ids)
        mTrailersLoaderListener = new LoaderManager.LoaderCallbacks<ArrayList<String>>() {
            @Override
            public AsyncTaskLoader<ArrayList<String>> onCreateLoader(int id, Bundle args) {
               return new FetchMovieTrailersData(getContext(),trailerIds,String.valueOf(movie_id));
            }
            @Override
            public void onLoadFinished(Loader<ArrayList<String>> loader, ArrayList<String> data) {
                if(data!=null) {
                    RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.trailer_list);
                    recyclerView.setHasFixedSize(true);
                    //to use RecycleView, you need a layout manager. default is LinearLayoutManager
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    TrailerRecyclerAdapter adapter = new TrailerRecyclerAdapter(getActivity(), data);
                    Log.i(LOG_TAG + "Trailer id", data.get(0));
                    recyclerView.setAdapter(adapter);
                }else
                {
                    Toast.makeText(getContext(),getString(R.string.error_no_trailers),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<String>> loader) {

            }
        };
        //Implementation of AsyncTaskLoader Listener (For reviews)
        mReviewLoaderListener = new LoaderManager.LoaderCallbacks<ArrayList<String>>() {
            @Override
            public Loader<ArrayList<String>> onCreateLoader(int id, Bundle args) {
                return new FetchReviewData(getContext(),String.valueOf(movie_id));
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<String>> loader, final ArrayList<String> reviews) {
                if (reviews != null) {
                    int i = 0;
                    while (i < reviews.size()) {
                        TextView review = new TextView(getActivity());
                        review.setText(reviews.get(i));
                        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        review.setLayoutParams(layoutParams);
                        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.outer_layout);
                        layout.addView(review);
                        i++;
                        Log.i(LOG_TAG, "Adding review");

                    }
                }else{
                    Toast.makeText(getContext(), "No Reviews Available", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<String>> loader) {

            }
        };
        return rootView;


    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, mCursorLoaderListener);
        getLoaderManager().initLoader(MOVIE_FAV_LOADER,null,mCursorLoaderListener_FavMovie).forceLoad();
        getLoaderManager().initLoader(MOVIE_TRAILER_LOADER, null, mTrailersLoaderListener).forceLoad();
        super.onActivityCreated(savedInstanceState);

    }
}
