package in.lemonco.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import in.lemonco.popularmovies.data.MovieContract;

/**
 * Created by sanehyadav1 on 10/14/16.
 */
public class MovieAdapter extends CursorAdapter {
    public MovieAdapter(Context context,Cursor c, int flags){
        super(context,c,flags);
    }


    @Override
    public View newView(Context context,Cursor cursor,ViewGroup parent){
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_layout,parent,false);
        return view;
    }
    @Override
    public void bindView(View view,Context context,Cursor cursor){
        ImageView poster = (ImageView)view;
        Picasso.with(context).load(Utility.getPosterPath(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH)))).into(poster);

    }
}
