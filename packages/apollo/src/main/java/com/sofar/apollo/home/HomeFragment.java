package com.sofar.apollo.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sofar.apollo.R;
import com.sofar.apollo.home.viewbinder.HomeAuthorViewBinder;
import com.sofar.apollo.home.viewbinder.HomeContext;
import com.sofar.apollo.home.viewbinder.HomeLearnViewBinder;
import com.sofar.apollo.home.viewbinder.HomeReviewViewBinder;
import com.sofar.apollo.home.viewbinder.HomeTabViewBinder;
import com.sofar.apollo.home.viewbinder.HomeViewBinder;
import androidx.fragment.app.Fragment;
import com.sofar.base.viewbinder.ViewBinder;

public class HomeFragment extends Fragment {

  ViewBinder viewBinder;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.home_fragment, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewBinder = new ViewBinder();
    viewBinder.addViewBinder(new HomeViewBinder());
    viewBinder.addViewBinder(new HomeTabViewBinder());
    viewBinder.addViewBinder(new HomeLearnViewBinder());
    viewBinder.addViewBinder(new HomeReviewViewBinder());
    viewBinder.addViewBinder(new HomeAuthorViewBinder());
    HomeContext homeContext = new HomeContext();
    viewBinder.create(view);
    viewBinder.bind(homeContext);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (viewBinder != null) {
      viewBinder.destroy();
    }
  }
}
