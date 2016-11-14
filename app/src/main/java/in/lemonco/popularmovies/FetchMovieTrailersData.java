package in.lemonco.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * AsyncTaskLoader for fetching trailer data
 */
public class FetchMovieTrailersData extends AsyncTaskLoader<ArrayList<String>> {
    private static final String API_KEY = "api_key";
    private ConnectivityManager mConnectivityManager;
    private static final String TRAILERS = "videos";
    private Context mcontext;
    private static final String LOG_TAG = FetchMovieTrailersData.class.getSimpleName();
    private String mMovie_id;
    ArrayList<String> trailerIds;

    public FetchMovieTrailersData(Context context,ArrayList<String> trailerIds,String movie_id) {
        super(context);
        mcontext = context;
        this.trailerIds = trailerIds;
        this.mMovie_id = movie_id;

    }

    /*
    * loadInBackground method for fetching youtube video ids , json string
    */
    @Override
    public ArrayList<String> loadInBackground(){
        if (isNetworkConnected() == false) {
            // Cancel request.
            Log.i(getClass().getName(), "Not connected to the internet");
            Toast.makeText(mcontext, "Please Check Internet connectivity", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (mMovie_id == null) {
            return null;
        }
        String trailersJsonStr = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        // Will contain the raw JSON response as a string.
        try {
            final String BASE_URL = "http://api.themoviedb.org/3/movie/";
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendPath(mMovie_id)
                    .appendPath(TRAILERS)
                    .appendQueryParameter(API_KEY, BuildConfig.MOVIEDB_API_KEY)
                    .build();
            URL url = new URL(builtUri.toString());
            Log.i(LOG_TAG + "video trailer query", builtUri.toString());

            // Creates a request to TMDb, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                trailersJsonStr = null;
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
                trailersJsonStr = null;
            }
            trailersJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in parsing
            trailersJsonStr = null;
        } finally {
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
        Log.i("result", trailersJsonStr);

        try {
            //returns the movies arrayList
            ArrayList<String> reviews = getTrailersData(trailersJsonStr);
            if(reviews.size()==0){
                return null;
            }else
                return reviews;
        } catch (JSONException ex) {
            Log.i(LOG_TAG, "Error in Json parsing");
            ex.printStackTrace();
            return null;
        }

    }
    //get trailer data ,youtube video ids, from JsonString
    private ArrayList<String> getTrailersData(String trailerJsonStr) throws JSONException {
        ArrayList<String> videos = new ArrayList<>();
        if (trailerJsonStr != null) {
            final String TAG_RESULTS = "results";
            final String TAG_VIDEO_KEY = "key";
            JSONObject trailerJson = new JSONObject(trailerJsonStr);
            JSONArray trailerArray = trailerJson.getJSONArray(TAG_RESULTS);
            for (int i = 0; i < trailerArray.length(); i++) {
                JSONObject trailerJsonObj = trailerArray.getJSONObject(i);
                String video_key = trailerJsonObj.getString(TAG_VIDEO_KEY);
                videos.add(video_key);
            }
        }
        return videos;
    }
    //check for network connectivity
    protected boolean isNetworkConnected() {

        // Instantiate mConnectivityManager if necessary
        if (mConnectivityManager == null) {
            mConnectivityManager = (ConnectivityManager) mcontext.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        // Is device connected to the Internet?
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }
}
