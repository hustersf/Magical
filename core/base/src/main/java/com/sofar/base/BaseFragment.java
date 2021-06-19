package com.sofar.base;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {

  private static final String TAG = "BaseFragment";

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate=" + this.getClass().getSimpleName());
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume=" + this.getClass().getSimpleName());
  }

  @Override
  public void onPause() {
    super.onPause();
    Log.d(TAG, "onPause=" + this.getClass().getSimpleName());
  }
}
