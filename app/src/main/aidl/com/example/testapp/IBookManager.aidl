// IBookManager.aidl
package com.example.testapp;

// Declare any non-default types here with import statements
import com.example.testapp.Book;
interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);
}
