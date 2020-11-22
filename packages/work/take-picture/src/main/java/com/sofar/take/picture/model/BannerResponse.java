package com.sofar.take.picture.model;

import com.sofar.base.page.response.ListResponse;

import java.util.List;

public class BannerResponse implements ListResponse<Banner> {

  public List<Banner> items;

  @Override
  public boolean hasMore() {
    return false;
  }

  @Override
  public List<Banner> getItems() {
    return items;
  }
}
