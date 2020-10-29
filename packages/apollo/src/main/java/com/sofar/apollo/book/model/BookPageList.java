package com.sofar.apollo.book.model;

import com.sofar.base.page.retrofit.SofarRetrofitPageList;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class BookPageList extends SofarRetrofitPageList<BookResponse, Book> {

  @Override
  protected Observable<BookResponse> onCreateRequest() {
    //todo 修改为服务端获取数据
    BookResponse response = new BookResponse();
    response.books = getMockBooks();
    return Observable.just(response);
  }

  private List<Book> getMockBooks() {
    List<Book> books = new ArrayList<>();
    books.add(buildBook("星火雅思英语", "星火权威版合作社词汇"));
    books.add(buildBook("考研基础词汇", "涵盖研究生考试大纲规定的所有基础词汇"));
    books.add(buildBook("李明的生词本", "实时记录我的生词汇"));
    return books;
  }

  private Book buildBook(String title, String description) {
    Book book = new Book();
    book.title = title;
    book.description = description;
    return book;
  }

}
