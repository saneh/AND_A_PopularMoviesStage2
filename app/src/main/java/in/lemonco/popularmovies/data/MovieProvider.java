package in.lemonco.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;


/**
 * Content Provider for movies
 */
public class MovieProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;
    static final int MOVIES_BY_SORT_CRITERIA = 100;
    static final int FAVORITE_MOVIES = 200;
    static final int FAV_MOVIE = 300;

    private static final SQLiteQueryBuilder sMovieQueryBuilder;
    private static final SQLiteQueryBuilder sFavoriteMoviesQueryBuilder;
    //private static final SQLiteQueryBuilder sFavouriteMovie;

    static {
        //query builder for the movie table
        sMovieQueryBuilder = new SQLiteQueryBuilder();
        sMovieQueryBuilder.setTables(MovieContract.MovieEntry.TABLE_NAME);

        //query builder for the join between movie and favourite_movie table
        sFavoriteMoviesQueryBuilder = new SQLiteQueryBuilder();
        sFavoriteMoviesQueryBuilder.setTables(MovieContract.MovieEntry.TABLE_NAME + " INNER JOIN "
        + MovieContract.FavoriteMovieEntry.TABLE_NAME + " ON "+
                MovieContract.MovieEntry.TABLE_NAME + "."+ MovieContract.MovieEntry.COLUMN_MOVIE_ID+
        " = "+ MovieContract.FavoriteMovieEntry.TABLE_NAME + "." + MovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher newUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        newUriMatcher.addURI(authority, MovieContract.PATH_MOVIES, MOVIES_BY_SORT_CRITERIA);
        newUriMatcher.addURI(authority, MovieContract.PATH_FAV_MOVIES, FAVORITE_MOVIES);
        newUriMatcher.addURI(authority,MovieContract.PATH_FAV_MOVIES+"/#",FAV_MOVIE);

        return newUriMatcher;
    }

    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES_BY_SORT_CRITERIA:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case FAVORITE_MOVIES:
                return MovieContract.FavoriteMovieEntry.CONTENT_TYPE;
            case FAV_MOVIE:
                return MovieContract.FavoriteMovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case MOVIES_BY_SORT_CRITERIA:
                retCursor = sMovieQueryBuilder.query(mOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, null);
                break;
            case FAVORITE_MOVIES:
                retCursor = sFavoriteMoviesQueryBuilder.query(mOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, null);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    // implement insert,update, delete
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri retUri;
        long _id;
        switch (match) {
            case MOVIES_BY_SORT_CRITERIA:
//                normalizeDate(values);
                _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    retUri = MovieContract.MovieEntry.buildMovieUri(_id);
                } else
                    throw new android.database.SQLException("Failed to insert row into:" + uri);
                break;
            case FAVORITE_MOVIES:
                _id = db.insert(MovieContract.FavoriteMovieEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    retUri = MovieContract.FavoriteMovieEntry.buildFavoriteMovieUri(_id);
                } else
                    throw new android.database.SQLException("Failed to insert row into:" + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return retUri;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case MOVIES_BY_SORT_CRITERIA:
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);

        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        db.close();
        return rowsUpdated;
    }

    //Delete method
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (null == selection) selection = "1";
        switch (match) {
            case MOVIES_BY_SORT_CRITERIA:
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FAVORITE_MOVIES:
                rowsDeleted = db.delete(MovieContract.FavoriteMovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        db.close();
        return rowsDeleted;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES_BY_SORT_CRITERIA:
                int rowsInserted = 0;
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return rowsInserted;
            default:
                return super.bulkInsert(uri, values);

        }
    }


}
