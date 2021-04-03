package com.sofar.aurora.feature.home.block.binder;

import android.view.View;
import android.widget.TextView;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.model.HomeBlock;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class BlockMoreViewBinder extends RecyclerViewBinder<HomeBlock> {

  TextView moreTv;

  @Override
  protected void onCreate() {
    super.onCreate();
    moreTv = bindView(R.id.block_more);
  }

  @Override
  protected void onBind(HomeBlock data) {
    super.onBind(data);
    moreTv.setText("更多");
    moreTv.setVisibility(data.haveMore == 0 ? View.GONE : View.VISIBLE);
    moreTv.setOnClickListener(v -> {

    });
  }

}
