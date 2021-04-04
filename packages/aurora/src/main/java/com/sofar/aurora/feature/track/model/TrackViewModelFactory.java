package com.sofar.aurora.feature.track.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class TrackViewModelFactory implements ViewModelProvider.Factory {

  @NonNull
  @Override
  public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new TrackViewModel();
  }
}
