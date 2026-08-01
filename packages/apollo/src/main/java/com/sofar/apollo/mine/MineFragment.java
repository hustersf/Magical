package com.sofar.apollo.mine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sofar.apollo.R;
import com.sofar.apollo.SofarApp;
import com.sofar.apollo.mine.viewbinder.LogoutViewBinder;
import com.sofar.apollo.mine.viewbinder.UserInfoViewBinder;
import androidx.fragment.app.Fragment;
import com.sofar.base.viewbinder.ViewBinder;

public class MineFragment extends Fragment {

  ViewBinder binder = new ViewBinder();

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.mine_fragment, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    binder.addViewBinder(new UserInfoViewBinder());
    binder.addViewBinder(new LogoutViewBinder());
    binder.create(view);
    binder.bind(SofarApp.ME);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    binder.destroy();
  }
}
