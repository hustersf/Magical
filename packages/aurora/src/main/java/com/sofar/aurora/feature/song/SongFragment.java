package com.sofar.aurora.feature.song;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sofar.aurora.model.Song;
import com.sofar.base.page.PageList;
import com.sofar.base.page.SimplePageList;
import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.recycler.RecyclerFragment;
import com.sofar.utility.CollectionUtil;

/**
 * 歌曲列表
 */
public class SongFragment extends RecyclerFragment<Song> {

  public static final String KEY_ID = "id";
  private List<Song> songs;

  @Override
  protected RecyclerAdapter<Song> onCreateAdapter() {
    return new SongAdapter();
  }

  @Override
  protected PageList<?, Song> onCreatePageList() {
    if (songs == null) {
      songs = new ArrayList<>();
    }
    return new SimplePageList<>(songs);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    String id = getArguments().getString(KEY_ID);
    songs = SongDataManager.get().get(id);
    if (CollectionUtil.isEmpty(songs)) {
      getActivity().finish();
      return;
    }

    super.onViewCreated(view, savedInstanceState);
    setRefreshEnable(false);
  }

  public static SongFragment create(@NonNull String id) {
    SongFragment fragment = new SongFragment();
    Bundle b = new Bundle();
    b.putString(KEY_ID, id);
    fragment.setArguments(b);
    return fragment;
  }
}
