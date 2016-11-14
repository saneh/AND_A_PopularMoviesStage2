package in.lemonco.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import in.lemonco.popularmovies.sync.MoviesSyncAdapter;

/**
 * Created by sanehyadav1 on 10/14/16.
 */
public class Utility {
    public static String getSortCriteria(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key),context.getString(R.string.sort_order_default));
    }
    public static String getPosterPath(String poster_path){
        final String BASE_URL = "http://image.tmdb.org/t/p/";
        final String SIZE = "w185";
        return BASE_URL+SIZE+"/"+poster_path ;
    }
    public static String getBackdropPath(String backdrop_path){
        final String BASE_URL = "http://image.tmdb.org/t/p/";
        final String SIZE = "w500";
        return BASE_URL+SIZE+"/"+backdrop_path ;
    }
    public static void setListViewLayoutParams(ListView listView){
        ListAdapter listAdapter = listView.getAdapter();
        if(listAdapter==null){
            return;
        }
        //set list adapter in loop for getting the final size
        int totalHeight =0;
        for(int size=0;size<listAdapter.getCount();size++){
            View listItem = listAdapter.getView(size,null,listView);
            listItem.measure(0,0);
            totalHeight += listItem.getMeasuredHeight();
        }
        //setting listview item in adapter
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight()*(listAdapter.getCount()-1));
        listView.setLayoutParams(params);
        //print height of adapter in the log
        Log.i("Adapter Height:",String.valueOf(totalHeight));
    }
    public static String getYoutubeUri(String trailer_id){
        String BASE_URL = "https://www.youtube.com/watch?v=";
        return BASE_URL+trailer_id;
    }
    public static Boolean checkNetworkState(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo!=null && networkInfo.isConnected();
    }
    public static int getMoviesStatus(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(context.getString(R.string.pref_movie_status_key),MoviesSyncAdapter.MOVIE_STATUS_UNKNOWN);
    }
}
