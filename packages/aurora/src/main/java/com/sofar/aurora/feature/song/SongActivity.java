package com.sofar.aurora.feature.song;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sofar.aurora.R;
import com.sofar.aurora.retrofit.ExceptionHandler;
import com.sofar.base.BaseActivity;
import com.sofar.utility.statusbar.StatusBarUtil;

public abstract class SongActivity extends BaseActivity {

  public static final String KEY_ID = "id";

  @NonNull
  protected String id;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.song_activty);
    StatusBarUtil.setLightMode(this);
    if (getIntent() != null && getIntent().getStringExtra(KEY_ID) instanceof String) {
      id = getIntent().getStringExtra(KEY_ID);
    } else {
      finish();
      return;
    }
  }


  protected void onSuccess() {
    replaceSongFragment();
  }

  protected void onFailed(Throwable throwable) {
    ExceptionHandler.handleException(throwable);
  }

  protected void replaceSongFragment() {
    SongFragment fragment = SongFragment.create(id);
    getSupportFragmentManager().beginTransaction()
      .replace(R.id.fragment_container, fragment, "song")
      .commitAllowingStateLoss();
  }
}
