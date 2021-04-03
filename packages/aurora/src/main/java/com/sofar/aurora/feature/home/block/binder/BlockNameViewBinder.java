package com.sofar.aurora.feature.home.block.binder;

import android.widget.TextView;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.model.HomeBlock;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class BlockNameViewBinder extends RecyclerViewBinder<HomeBlock> {

  TextView nameTv;

  @Override
  protected void onCreate() {
    super.onCreate();
    nameTv = bindView(R.id.block_name);
  }

  @Override
  protected void onBind(HomeBlock data) {
    super.onBind(data);
    nameTv.setText(data.name);
  }
}
