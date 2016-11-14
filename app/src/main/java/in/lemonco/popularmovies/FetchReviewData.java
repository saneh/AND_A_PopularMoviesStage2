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
 * AsyncTaskLoader for fetching movie review data
 */
public class FetchReviewData extends AsyncTaskLoader<ArrayList<String>> {
    private static final String API_KEY = "api_key";
    private ConnectivityManager mConnectivityManager;
    private static final String REVIEWS = "reviews";
    private Context mcontext;
    private String mMovie_id;
    private static final String LOG_TAG = FetchReviewData.class.getSimpleName();
    ArrayList<String> reviews;

    public FetchReviewData(Context context, String movie_id) {
        super(context);
        mcontext = context;
        this.mMovie_id = movie_id;
    }

    @Override
    public ArrayList<String> loadInBackground() {
        if (isNetworkConnected() == false) {
            // Cancel request.
            Log.i(getClass().getName(), "Not connected to the internet");
            Toast.makeText(mcontext, "Please Check Internet connectivity", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (mMovie_id == null) {
            return null;
        }
        String reviewJsonStr = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        // Will contain the raw JSON response as a string.
        try {
            final String BASE_URL = "http://api.themoviedb.org/3/movie/";
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendPath(mMovie_id)
                    .appendPath(REVIEWS)
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
                reviewJsonStr = null;
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
                reviewJsonStr = null;
            }
            reviewJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in parsing
            reviewJsonStr = null;
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
        Log.i("result", reviewJsonStr);

        try {
            //returns the movies arrayList
            ArrayList<String> reviews = getReviewData(reviewJsonStr);
            if (reviews.size() == 0) {
                return null;

            }
            return reviews;

        } catch (JSONException ex) {
            Log.i(LOG_TAG, "Error in Json parsing");
            ex.printStackTrace();
            return null;
        }
    }

    //get review data from JsonString
    private ArrayList<String> getReviewData(String reviewJsonStr) throws JSONException {
        ArrayList<String> reviews = new ArrayList<>();
        if (reviewJsonStr != null) {
            final String TAG_RESULTS = "results";
            final String TAG_CONTENT = "content";
            final String TAG_AUTHOR = "author";
            JSONObject reviewJson = new JSONObject(reviewJsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray(TAG_RESULTS);
            for (int i = 0; i < reviewArray.length(); i++) {
                JSONObject reviewJsonObj = reviewArray.getJSONObject(i);
                String content = reviewJsonObj.getString(TAG_CONTENT);
                String author = reviewJsonObj.getString(TAG_AUTHOR);
                reviews.add(content + "\n*********************************************" + author);
            }
        }
        return reviews;
    }

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
