package com.jetopto.newsapp;

/**
 * Created by lalalilalat on 2016/12/22.
 */

public interface AsyncTaskResult<T extends Object>
{
    // T是執行結果的物件型態

    public void taskFinish( T result );
}