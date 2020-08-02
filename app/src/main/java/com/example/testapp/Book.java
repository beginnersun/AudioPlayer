package com.example.testapp;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {

    private String name;
    private int size;

    public Book(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public Book() {
    }


    protected Book(Parcel in) {
        name = in.readString();
        size = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(size);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
