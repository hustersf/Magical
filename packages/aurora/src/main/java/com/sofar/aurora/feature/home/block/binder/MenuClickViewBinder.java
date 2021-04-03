package com.sofar.aurora.feature.home.block.binder;

import android.view.View;

import com.sofar.aurora.model.Menu;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class MenuClickViewBinder extends RecyclerViewBinder<Menu> {

  @Override
  protected void onBind(Menu data) {
    super.onBind(data);
    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

      }
    });
  }
}
