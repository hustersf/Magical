package com.sofar.apollo.book;

import android.widget.TextView;

import com.sofar.apollo.R;
import com.sofar.apollo.book.model.Book;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class BookViewBinder extends RecyclerViewBinder<Book> {

  TextView nameTv;
  TextView desTv;

  @Override
  protected void onCreate() {
    super.onCreate();
    nameTv = view.findViewById(R.id.book_name);
    desTv = view.findViewById(R.id.book_des);
  }

  @Override
  protected void onBind(Book data) {
    super.onBind(data);
    nameTv.setText(data.title);
    desTv.setText(data.description);
  }

}
