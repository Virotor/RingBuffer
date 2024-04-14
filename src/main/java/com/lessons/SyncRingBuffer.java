package com.lessons;

import java.util.List;

public class SyncRingBuffer<T> extends AbstractRingBuffer<T> {


    public SyncRingBuffer(List<T> collection, int size) {
        super(collection, size);
    }
}