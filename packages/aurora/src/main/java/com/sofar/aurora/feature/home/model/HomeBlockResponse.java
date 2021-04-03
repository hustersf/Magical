package com.sofar.aurora.feature.home.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.sofar.aurora.model.BaseResponse;
import com.sofar.base.page.response.ListResponse;

public class HomeBlockResponse extends BaseResponse implements ListResponse<HomeBlock> {

  @SerializedName("data")
  public List<HomeBlock> blocks;

  @Override
  public boolean hasMore() {
    return false;
  }

  @Override
  public List<HomeBlock> getItems() {
    return blocks;
  }
}
