package get.saga;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
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
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    public static boolean newSongAdded = false;
    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList((ArrayList<SongInfo>) songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSongList();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_library, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        final SwipeRefreshLayout refresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                repopulateList();
                refresh.setRefreshing(false);
            }
        });

        mAdapter = new LibraryAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    public void getSongList() {
        Cursor musicCursor = null;
        try {
            String dirPath = Utils.getStoragePath(getActivity());
            String selection = MediaStore.Audio.Media.DATA + " like ?";
            String[] selectionArgs = {dirPath + "/%"};
            ContentResolver musicResolver = getActivity().getContentResolver();
            musicCursor = musicResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    MediaStore.Audio.Media.TITLE + " ASC");
            if (musicCursor != null && musicCursor.moveToFirst()) {
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
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error fetching song list", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            if (musicCursor != null) {
                musicCursor.close();
            }
        }

    }

    @Override
    public void onDestroy() {
        getActivity().stopService(playIntent);
        musicSrv = null;
        super.onDestroy();
    }


    public class SongInfo {

        private String mTitle;
        private long mId;
        private String mDuration;

        public SongInfo(long id, String title) {
            mId = id;
            mTitle = title;
        }

        public long getID() {
            return mId;
        }

        public String getTitle() {
            return mTitle;
        }
    }

    public class LibraryAdapter extends RecyclerView.Adapter<SongViewHolder> {

        @Override
        public SongViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_library, null);
            SongViewHolder vh = new SongViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final SongViewHolder holder, final int i) {
            SongInfo song = songList.get(i);
            holder.title.setText(song.getTitle());
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    musicSrv.setSong(i);
                    musicSrv.playSong();
                }
            });
        }

        @Override
        public int getItemCount() {
            return songList.size();
        }
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {
        protected TextView title;
        protected View view;

        public SongViewHolder(View view) {
            super(view);
            this.view = view;
            this.title = (TextView) view.findViewById(R.id.songNameListView);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && newSongAdded){
            repopulateList();
            newSongAdded = false;
        }
    }
    private void repopulateList(){
        songList.clear();
        getSongList();
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }
}