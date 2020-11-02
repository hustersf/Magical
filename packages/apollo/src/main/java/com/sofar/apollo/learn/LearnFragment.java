package com.sofar.apollo.learn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sofar.apollo.R;
import com.sofar.apollo.learn.viewbinder.LearnContext;
import com.sofar.apollo.learn.viewbinder.LearnCoreViewBinder;
import com.sofar.apollo.learn.viewbinder.LearnViewBinder;
import com.sofar.base.BaseFragment;
import com.sofar.base.viewbinder.ViewBinder;
import com.sofar.utility.ViewUtil;

public class LearnFragment extends BaseFragment {

  ViewBinder viewBinder;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return ViewUtil.inflate(container, R.layout.learn_fragment);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewBinder = new ViewBinder();
    viewBinder.addViewBinder(new LearnViewBinder());
    viewBinder.addViewBinder(new LearnCoreViewBinder());
    viewBinder.create(view);
    LearnContext learnContext = new LearnContext();
    viewBinder.bind(learnContext);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (viewBinder != null) {
      viewBinder.destroy();
    }
  }
}
