package com.example.hsu.bt_4_mega2560;


import java.util.Arrays;

public class obd_ring_buffer<T> {

    private final static int DEFAULT_SIZE = 1024;
    private Object[] buffer;
    private int head = 0;
    private int tail = 0;
    private int bufferSize;

    public obd_ring_buffer() {
        this.bufferSize = DEFAULT_SIZE;
        this.buffer = new Object[bufferSize];
    }

    public obd_ring_buffer(int initSize) {
        this.bufferSize = initSize;
        this.buffer = new Object[bufferSize];
    }

    public Boolean empty() {
        return head == tail;
    }

    public Boolean full() {
        return (tail + 1) % bufferSize == head;
    }

    public void clear() {
        Arrays.fill(buffer, null);
        this.head = 0;
        this.tail = 0;
    }

    public Boolean put(ble_receiver_data v) {
        if (full()) {
            return false;
        }
        buffer[tail] = v;
        tail = (tail + 1) % bufferSize;
        return true;
    }

    public Object get() {
        if (empty()) {
            return null;
        }
        Object result = buffer[head];
        head = (head + 1) % bufferSize;
        return result;
    }

    public Object[] getAll() {
        if (empty()) {
            return new Object[0];
        }
        int copyTail = tail;
        int cnt = head < copyTail ? copyTail - head : bufferSize - head + copyTail;
        Object[] result = new String[cnt];
        if (head < copyTail) {
            for (int i = head; i < copyTail; i++) {
                result[i - head] = buffer[i];
            }
        } else {
            for (int i = head; i < bufferSize; i++) {
                result[i - head] = buffer[i];
            }
            for (int i = 0; i < copyTail; i++) {
                result[bufferSize - head + i] = buffer[i];
            }
        }
        head = copyTail;
        return result;
    }
}


