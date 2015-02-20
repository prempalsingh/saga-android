package get.saga;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import get.saga.ui.DividerItemDecoration;

/**
 * Created by prempal on 19/2/15.
 */
public class LibraryFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private LibraryAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<SongInfo> songList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSongList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_library, container, false);
        LinearLayout emptyView = (LinearLayout) rootView.findViewById(R.id.emptyList);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new LibraryAdapter();
        mRecyclerView.setAdapter(mAdapter);
        if(mAdapter.getItemCount()==0){
            emptyView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }

        return rootView;
    }

    public void getSongList() {
        String dirPath= Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.d("dir", dirPath);
        String selection = MediaStore.Audio.Media.DATA +" like ?";
        String[] selectionArgs={dirPath+"/saga/%"};
        ContentResolver musicResolver = getActivity().getContentResolver();
        Cursor musicCursor = musicResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                selection,
                selectionArgs,
                MediaStore.Audio.Media.TITLE + " ASC");
        if(musicCursor!=null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            do {
                long id = musicCursor.getLong(idColumn);
                String title = musicCursor.getString(titleColumn);
                songList.add(new SongInfo(id, title));
            }
            while (musicCursor.moveToNext());
        }
    }

    public class SongInfo {

        private String mTitle;
        private long mId;

        public SongInfo(long id,String title){
            mId = id;
            mTitle = title;
        }

        public long getID(){return mId;}
        public String getTitle(){return mTitle;}
    }

    public class LibraryAdapter extends RecyclerView.Adapter<SongViewHolder>{

        @Override
        public SongViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_library, null);
            SongViewHolder vh = new SongViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(SongViewHolder holder, int i) {
            SongInfo song = songList.get(i);
            holder.title.setText(song.getTitle());
        }

        @Override
        public int getItemCount() {
            return songList.size();
        }
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {
        protected TextView title;

        public SongViewHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.title);
        }
    }

}