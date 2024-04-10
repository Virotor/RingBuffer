package com.lessons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SyncRingBuffer<T> extends  AbstractRingBuffer<T>{


    protected SyncRingBuffer( List<T> collection,int size) {
        super(collection, size);
/*        super(initClass,size);*/
    }
}