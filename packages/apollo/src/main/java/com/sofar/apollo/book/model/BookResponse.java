package com.sofar.apollo.book.model;

import com.sofar.base.page.response.ListResponse;

import java.util.List;

public class BookResponse implements ListResponse<Book> {

  public List<Book> books;

  public boolean hasMore = true;

  @Override
  public boolean hasMore() {
    return hasMore;
  }

  @Override
  public List<Book> getItems() {
    return books;
  }

}
