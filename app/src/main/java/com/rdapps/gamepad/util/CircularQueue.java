package com.rdapps.gamepad.util;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class CircularQueue<E> {

    private static final int DEFAULT_MAX_CAPACITY = 10;

    private E[] underlyingArray;
    private Integer front;
    private Integer rear;
    private int size;
    private int modCount;

    public CircularQueue() {
        this(DEFAULT_MAX_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public CircularQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity has to be bigger than 0.");
        }
        underlyingArray = (E[]) new Object[capacity];
        clear();
        modCount = 0;
    }


    public void ends() {
        System.out.println("Front: " + front + " Rear: " + rear);
    }

    public synchronized boolean addFirst(E obj) {
        if (isFull()) {
            return false;
        } else if (isEmpty()) {
            addToEmptyList(obj);
        } else {
            size++;
            front = rollOverIfNeeded(front - 1);
            underlyingArray[front] = obj;
        }
        modCount++;
        return true;
    }

    public synchronized boolean addFirstOverride(E obj) {
        if (isFull()) {
            removeLast();
        }
        addFirst(obj);
        return true;
    }

    public synchronized boolean addLast(E obj) {
        if (isFull()) {
            return false;
        } else if (isEmpty()) {
            addToEmptyList(obj);
        } else {
            size++;
            rear = rollOverIfNeeded(rear + 1);
            underlyingArray[rear] = obj;
        }
        modCount++;
        return true;
    }

    private synchronized void addToEmptyList(E obj) {
        underlyingArray[0] = obj;
        rear = 0;
        front = 0;
        size = 1;
    }

    public synchronized E removeFirst() {
        if (isEmpty()) {
            return null;
        } else {
            size--;
            final E result = underlyingArray[front];
            underlyingArray[front] = null;
            if (isEmpty()) {
                clear();
            } else {
                front = rollOverIfNeeded(front + 1);
            }
            modCount++;
            return result;
        }
    }

    public synchronized E removeLast() {
        if (isEmpty()) {
            return null;
        } else {
            size--;
            final E result = underlyingArray[rear];
            underlyingArray[rear] = null;
            if (isEmpty()) {
                clear();
            } else {
                rear = rollOverIfNeeded(rear - 1);
            }
            modCount++;
            return result;
        }
    }

    public synchronized E remove(E obj) {
        int indexToRemove = findIndex(obj);
        if (indexToRemove >= 0) {
            final E result = underlyingArray[indexToRemove];
            size--;
            for (int i = indexToRemove; i != rear; ) {
                int nextIndex = rollOverIfNeeded(i + 1);
                underlyingArray[i] = underlyingArray[nextIndex];
                i = nextIndex;
            }

            underlyingArray[rear] = null;
            if (isEmpty()) {
                clear();
            } else {
                rear = rollOverIfNeeded(rear - 1);
            }

            modCount++;
            return result;
        } else {
            return null;
        }
    }

    public synchronized E peekFirst() {
        if (isEmpty()) {
            return null;
        } else {
            return underlyingArray[front];
        }
    }

    public synchronized E peekLast() {
        if (isEmpty()) {
            return null;
        } else {
            return underlyingArray[rear];
        }
    }

    public synchronized boolean contains(E obj) {
        return findIndex(obj) >= 0;
    }

    public synchronized E find(E obj) {
        int index = findIndex(obj);
        if (index >= 0) {
            return underlyingArray[index];
        } else {
            return null;
        }
    }

    private synchronized int findIndex(E obj) {
        if (!isEmpty()) {
            for (int i = 0; i < size; i++) {
                int index = rollOverIfNeeded(front + i);
                if (Objects.equals(underlyingArray[index], obj)) {
                    return index;
                }
            }
        }
        return -1;
    }

    public synchronized void clear() {
        modCount++;
        front = null;
        rear = null;
        size = 0;
    }

    public synchronized boolean isEmpty() {
        return size() == 0;
    }

    public synchronized boolean isFull() {
        return size() == underlyingArray.length;
    }

    public synchronized int size() {
        return size;
    }

    public synchronized Iterator<E> iterator() {
        return new ArrayLinearListIterator();
    }

    public synchronized List<E> removeAll() {
        List<E> list = new ArrayList<>();
        while (!isEmpty()) {
            list.add(removeLast());
        }
        return list;
    }

    private class ArrayLinearListIterator implements Iterator<E> {
        private int index;
        private int exceptedModCount;
        private int lastRetrieved;

        public ArrayLinearListIterator() {
            this.index = 0;
            this.lastRetrieved = -1;
            this.exceptedModCount = modCount;
        }

        public boolean hasNext() {
            return index < size();
        }

        public E next() {
            if (exceptedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            lastRetrieved = rollOverIfNeeded(front + index);
            E result = underlyingArray[lastRetrieved];
            index++;
            return result;
        }

        public void remove() {
            if (lastRetrieved < 0) {
                throw new IllegalStateException("Next is not called");
            }

            final int indexToRemove = lastRetrieved;
            lastRetrieved = -1;
            size--;
            modCount++;
            exceptedModCount = modCount;
            for (int i = indexToRemove; i != rear; ) {
                int nextIndex = rollOverIfNeeded(i + 1);
                underlyingArray[i] = underlyingArray[nextIndex];
                i = nextIndex;
            }
            underlyingArray[rear] = null;
            if (isEmpty()) {
                clear();
            } else {
                rear = rollOverIfNeeded(rear - 1);
            }
            this.index--;
        }
    }

    private int rollOverIfNeeded(int index) {
        if (index < 0) {
            //index went over the left
            return underlyingArray.length + index;
        } else if (index >= underlyingArray.length) {
            //Index went over the right
            return index - underlyingArray.length;
        } else {
            //No rollOver Needed
            return index;
        }
    }
}