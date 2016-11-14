package in.lemonco.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * MovieContract: defines the database structure of "MovieEntry" and "FavoriteMovieEntry" for movies and favorite movie respectively
 */
public class MovieContract {
   //CONTENT_AUTHORITY is the name for the entire content provider
    public static final String CONTENT_AUTHORITY = "in.lemonco.popularmovies";

    //Use CONTENT_AUTHORITY to create the base URI which apps will use to connect with the contact provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);

    //Possible paths. To be appended with BASE_CONTENT_URI
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_FAV_MOVIES = "fav_movies";

    public static final class MovieEntry implements BaseColumns{
        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_SORT_CRITERIA = "sort_criteria";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";

        public static final Uri CONTENT_URI= BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +"/"+CONTENT_AUTHORITY+"/"+PATH_MOVIES;

        public static Uri buildMovieUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
        public static Uri buildUriWithMovieid(int movieId){
            return CONTENT_URI.buildUpon().appendPath(((Integer)movieId).toString()).build();
        }



    }
    public static final class FavoriteMovieEntry implements BaseColumns{
        public static final String TABLE_NAME = "favorite_movies";
        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendEncodedPath(PATH_FAV_MOVIES).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_FAV_MOVIES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_FAV_MOVIES;

        public static Uri buildFavoriteMovieUri(Long _id){
            return ContentUris.withAppendedId(CONTENT_URI,_id);
        }


    }
}
