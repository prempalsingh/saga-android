package get.saga;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prempal on 19/2/15.
 */
public class LibraryFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private LibraryAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<SongInfo> songList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_library, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        for (int i = 0; i < 3; i++) {

            SongInfo song = new SongInfo();
            song.setTitle("Song " + i);
            songList.add(song);
        }

        mAdapter = new LibraryAdapter();
        mRecyclerView.setAdapter(mAdapter);
        return rootView;
    }

    public class SongInfo {
        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
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
            return 3;
        }
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {
        protected ImageView play;
        protected TextView title;

        public SongViewHolder(View view) {
            super(view);
            this.play = (ImageView) view.findViewById(R.id.icon);
            this.title = (TextView) view.findViewById(R.id.title);
        }
    }

}