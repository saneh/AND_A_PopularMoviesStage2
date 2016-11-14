package in.lemonco.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLOpenHelper class
 */
public class MovieDbHelper extends SQLiteOpenHelper {
    //Increment the database version whenever there is a change in database
    public static final int DATABASE_VERSION=10;

    public static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase){
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " ("+
                MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL , "+
                MovieContract.MovieEntry.COLUMN_TITLE + " TEXT NOT NULL , "+
                MovieContract.MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL , "+
                MovieContract.MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL , "+
                MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " TEXT NOT NULL , "+
                MovieContract.MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL , "+
                MovieContract.MovieEntry.COLUMN_SORT_CRITERIA + " TEXT NOT NULL , "+
                MovieContract.MovieEntry.COLUMN_BACKDROP_PATH + " TEXT NOT NULL , "+
                "UNIQUE("+ MovieContract.MovieEntry.COLUMN_TITLE+" , "+ MovieContract.MovieEntry.COLUMN_SORT_CRITERIA+") ON CONFLICT REPLACE );";

        final String SQL_CREATE_FAV_MOVIE_TABLE = "CREATE TABLE " + MovieContract.FavoriteMovieEntry.TABLE_NAME + " ("+
                MovieContract.FavoriteMovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                MovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL , " +
                " UNIQUE("+ MovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE "+ ");";


        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FAV_MOVIE_TABLE);

    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase,int oldVersion,int newVersion){
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ MovieContract.MovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ MovieContract.FavoriteMovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
