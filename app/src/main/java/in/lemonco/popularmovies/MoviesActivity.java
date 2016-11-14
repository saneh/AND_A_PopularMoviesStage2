package in.lemonco.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import in.lemonco.popularmovies.sync.MoviesSyncAdapter;

//MoviesActivity
public class MoviesActivity extends AppCompatActivity implements MoviesFragment.Callback {
    private static final String DETAILFRAGMENT_TAG = "DF_TAG";
    @NonNull
    private Boolean mTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
        } else {
            mTwoPane = false;
        }
        this.setTitle(getMovieActivityTitle());
        getSupportActionBar().setElevation(0f);

        //initialize SyncAdapter
        MoviesSyncAdapter.initializeSyncAdapter(this);

    }
    public void onItemSelected(int movie_id){
        if(mTwoPane){
            Bundle args = new Bundle();
            args.putSerializable(MovieDetailFragment.MOVIE_ID, (Integer) movie_id);
            MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
            movieDetailFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.movie_detail_container,movieDetailFragment,DETAILFRAGMENT_TAG).commit();

        }
        else{
            Intent intent = new Intent(this,MovieDetailActivity.class);
            intent.putExtra(MovieDetailActivity.MOVIE_ID, movie_id);
            startActivity(intent);
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        //Set the Movie Activity Title depending on sort criteria prefrence
        this.setTitle(getMovieActivityTitle());
    }
    @NonNull
    private String getMovieActivityTitle(){
        String sortCriteria = Utility.getSortCriteria(this);
        String title_movies_activity= "";
        if(sortCriteria.equals(this.getString(R.string.sort_order_popular))){
            title_movies_activity = getString(R.string.title_popular);
        }else if(sortCriteria.equals(this.getString(R.string.sort_order_toprated))){
            title_movies_activity = getString(R.string.title_top_rated);
        }else if(sortCriteria.equals(this.getString(R.string.sort_order_favorite))){
            title_movies_activity = getString(R.string.title_favourite);
        }
        return title_movies_activity;
    }


}
