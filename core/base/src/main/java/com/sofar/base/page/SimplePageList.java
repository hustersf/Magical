package com.sofar.base.page;

import java.util.List;

import com.sofar.base.page.response.CursorResponse;
import com.sofar.base.page.retrofit.SofarRetrofitPageList;

import io.reactivex.Observable;

public class SimplePageList<MODEL>
  extends SofarRetrofitPageList<SimplePageList.SimplePage<MODEL>, MODEL> {

  private SimplePage<MODEL> page;

  public SimplePageList(List<MODEL> list) {
    super();
    page = new SimplePage<>(list);
    addAll(list);
    setLatestPage(page);
    setHasMore(false);
  }

  @Override
  protected Observable<SimplePage<MODEL>> onCreateRequest() {
    return Observable.just(page);
  }

  static class SimplePage<MODEL> implements CursorResponse<MODEL> {

    List<MODEL> list;

    SimplePage(List<MODEL> list) {
      this.list = list;
    }

    @Override
    public String getCursor() {
      return null;
    }

    @Override
    public boolean hasMore() {
      return false;
    }

    @Override
    public List<MODEL> getItems() {
      return list;
    }
  }
}
