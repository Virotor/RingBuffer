package com.lessons;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractRingBuffer<T> implements Collection<T> {
    protected List<T> buffer;

    private final AtomicInteger size;

    private final int capacity;
    protected AtomicInteger currentReadPointer;
    protected AtomicInteger currentWritePointer;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);


    protected AbstractRingBuffer(List<T> collection, int capacity) {
        buffer = collection;
        this.capacity = capacity;
        this.size = new AtomicInteger(0);
        currentReadPointer = new AtomicInteger(0);
        currentWritePointer = new AtomicInteger(0);
    }


    public T getNext() {
        var readLock = lock.readLock();
        readLock.lock();
        try {
            if (currentReadPointer.compareAndSet(currentWritePointer.get(), currentReadPointer.get())) {
                return null;
            }
            synchronized (this) {
                if (currentReadPointer.compareAndSet(currentWritePointer.get(), currentReadPointer.get())) {
                    return null;
                }
                return buffer.get(currentReadPointer.getAndUpdate(value -> (value + 1) % capacity));
            }
        } finally {
            readLock.unlock();
        }
    }


    @Override
    public boolean add(T element) {
        var writeLock = lock.writeLock();
        writeLock.lock();
        try {
            if ((currentWritePointer.get() + 1) == currentReadPointer.get()) {
                throw new IndexOutOfBoundsException("RingBuffer is overflow");
            }
            if (size.get() >= capacity) {
                buffer.set(currentWritePointer.getAndUpdate(value -> (value + 1) % capacity), element);
                return true;
            }
            size.getAndIncrement();
            currentWritePointer.getAndUpdate(value -> (value + 1) % capacity);
            buffer.add(element);
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public synchronized String toString() {
        var readLock = lock.readLock();
        readLock.lock();
        try {
            return buffer + " " + currentWritePointer + " " + currentReadPointer;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int size() {
        var readLock = lock.readLock();
        readLock.lock();
        try {
            return Math.abs(this.currentReadPointer.get() - this.currentWritePointer.get());
        } finally {
            readLock.unlock();
        }

    }

    @Override
    public boolean isEmpty() {
        return this.currentReadPointer.compareAndSet(currentWritePointer.get(), currentReadPointer.get());
    }

    @Override
    public boolean contains(Object o) {
        var readLock = lock.readLock();
        readLock.lock();
        try {
            return buffer.contains(o);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return currentReadPointer.compareAndSet(currentWritePointer.get(), currentReadPointer.get());
            }

            @Override
            public T next() {
                return getNext();
            }
        };
    }

    @Override
    public Object[] toArray() {
        var readLock = lock.readLock();
        readLock.lock();
        try {
            return buffer.toArray();
        } finally {
            readLock.unlock();
        }

    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        var readLock = lock.readLock();
        readLock.lock();
        try {
            return buffer.toArray(a);
        } finally {
            readLock.unlock();
        }
    }


    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Удаление из кольца не поддерживается");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        var readLock = lock.readLock();
        readLock.lock();
        try {
            return new HashSet<>(buffer).containsAll(c);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        var writeLock = lock.writeLock();
        writeLock.lock();
        try {
            return buffer.addAll(c);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Удаление из кольца не поддерживается");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Удаление из кольца не поддерживается");
    }

    @Override
    public void clear() {
        var writeLock = lock.writeLock();
        writeLock.lock();
        try {
            this.buffer.clear();
            this.currentWritePointer.set(0);
            this.currentReadPointer.set(0);
            this.size.set(0);
        } finally {
            writeLock.unlock();
        }

    }
}
