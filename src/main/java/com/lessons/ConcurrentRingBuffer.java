package com.lessons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConcurrentRingBuffer<T> extends  AbstractRingBuffer<T>{
    protected ConcurrentRingBuffer(int size) {
        super(new ArrayList<>(), size);
    }
}
