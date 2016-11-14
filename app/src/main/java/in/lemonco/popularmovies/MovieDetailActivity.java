package in.lemonco.popularmovies;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {

    public static final String MOVIE_ID ="movie_id";
    private static final String DETAILFRAGMENT_TAG = "DF_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.movies_detail_container,movieDetailFragment,DETAILFRAGMENT_TAG).commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //enable the back button in detail activity
        MoviesFragment.setPrefChange(false);
    }

}
