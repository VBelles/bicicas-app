package com.tcn.bicicas.data.datasource.local

interface LocalStore<T> {
    fun get(): T?
    fun save(value: T)
    fun clear()
}