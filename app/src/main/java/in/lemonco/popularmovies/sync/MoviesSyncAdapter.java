package in.lemonco.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import in.lemonco.popularmovies.BuildConfig;
import in.lemonco.popularmovies.R;
import in.lemonco.popularmovies.Utility;
import in.lemonco.popularmovies.data.MovieContract;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter {
    // Global variables
    // Define a variable to contain a content resolver instance
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MOVIE_STATUS_OK, MOVIE_STATUS_SERVER_DOWN, MOVIE_STATUS_SERVER_INVALID, MOVIE_STATUS_UNKNOWN})
            public @interface MovieServerStatus{}
    //Declare the constants
    public static final int MOVIE_STATUS_OK =0;
    public static final int MOVIE_STATUS_SERVER_DOWN =1;
    public static final int MOVIE_STATUS_SERVER_INVALID =2;
    public static final int MOVIE_STATUS_UNKNOWN =3;


    ContentResolver mContentResolver;
    private static final String LOG_TAG= MoviesSyncAdapter.class.getSimpleName();
    private static final String API_KEY = "api_key";
    public static final int SYNC_INTERVAL = 60*180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    /**
     * Set up the sync adapter
     */
    public MoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public MoviesSyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "On Perform Sync Called");
        String searchQuery = Utility.getSortCriteria(getContext());
        if (searchQuery == null|| searchQuery.equals("favorite")) {
            return;
        }
        //Loading both "popular" and "top_rated" movies, to avoid empty data in intial case of prefrence change and improve performace by fetching data in one go
        switch(1){
            case 1:
                fetchMovieData(getContext().getString(R.string.sort_order_popular));
            case 2:
                fetchMovieData(getContext().getString(R.string.sort_order_toprated));
                break;
        }
        return;
    }
    //fetch movie data from database
    private void fetchMovieData(String searchQuery){
        String moviesJsonStr = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.

        try {
            final String BASE_URL = "http://api.themoviedb.org/3/movie";
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendPath(searchQuery)
                    .appendQueryParameter(API_KEY, BuildConfig.MOVIEDB_API_KEY)
                    .build();
            URL url = new URL(builtUri.toString());
            Log.i(LOG_TAG + "search query", builtUri.toString());

            // Creates a request to TMDb, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                moviesJsonStr = null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                moviesJsonStr = null;
                setMovieStatus(getContext(), MOVIE_STATUS_SERVER_DOWN);
                return;
            }
            moviesJsonStr = buffer.toString();
            getMovieData(moviesJsonStr,searchQuery);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            setMovieStatus(getContext(), MOVIE_STATUS_SERVER_DOWN);
            moviesJsonStr = null;
        } catch (JSONException ex) {
            Log.i(LOG_TAG, "Error in Json parsing");
            ex.printStackTrace();
            setMovieStatus(getContext(), MOVIE_STATUS_SERVER_INVALID);
            return;
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }
    //get movie data from JsonString
    private void getMovieData(String moviesJsonStr,String sortCriteria) throws JSONException {
        if(moviesJsonStr!=null){
            final String TAG_MOVIE_ID ="id";
            final String TAG_RESULTS = "results";
            final String TAG_TITLE = "original_title";
            final String TAG_OVERVIEW = "overview";
            final String TAG_POSTER = "poster_path";
            final String TAG_RATING = "vote_average";
            final String TAG_RELEASEDATE = "release_date";
            final String TAG_BACKDROP = "backdrop_path";
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray movieArray = moviesJson.getJSONArray(TAG_RESULTS);
            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieArray.length());
            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movieJsonObj = movieArray.getJSONObject(i);
                int movie_id = movieJsonObj.getInt(TAG_MOVIE_ID);
                String title = movieJsonObj.getString(TAG_TITLE);
                String overview = movieJsonObj.getString(TAG_OVERVIEW);
                String poster = movieJsonObj.getString(TAG_POSTER);
                String rating = movieJsonObj.getString(TAG_RATING);
                String releaseDate = movieJsonObj.getString(TAG_RELEASEDATE);
                String backdrop = movieJsonObj.getString(TAG_BACKDROP);

                ContentValues movie_value = new ContentValues();
                movie_value.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID,movie_id);
                movie_value.put(MovieContract.MovieEntry.COLUMN_TITLE,title);
                movie_value.put(MovieContract.MovieEntry.COLUMN_OVERVIEW,overview);
                movie_value.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH,poster);
                movie_value.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,rating);
                movie_value.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE,releaseDate);
                movie_value.put(MovieContract.MovieEntry.COLUMN_SORT_CRITERIA,sortCriteria);
                movie_value.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH,backdrop);


                cVVector.add(movie_value);

            }
            //add to database
            if(cVVector.size()>0){
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI,cvArray);
            }
        }


    }

    /**
     * Helper method to have sync adapter sync immediately
     * @context the context used to access the account service
     */
    public static void onSyncImmediately(Context context){
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED,true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL,true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to create a fake account to be used with sync adapter or make a new account if the fake account does not exist
     */
    public static Account getSyncAccount(Context context){
        //Get instance of android account manager
        AccountManager accountManager = (AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE);

        //Create the account type and default account
        Account newAccount = new Account(context.getString(R.string.app_name),context.getString(R.string.sync_account_type));

        //if the password does not exist , the account doesn'r exist
        if(null ==accountManager.getPassword(newAccount)){

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount,context);
        }
        return newAccount;
    }
    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }
    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        onSyncImmediately(context);
    }
    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    static private void setMovieStatus(Context context, @MovieServerStatus int serverStatus){
       SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(context.getString(R.string.pref_movie_status_key),serverStatus);
        spe.commit();
    }

}