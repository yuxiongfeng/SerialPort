/* Copyright 2011-2013 Google Inc.
 * Copyright 2013 mike wakerly <opensource@hoho.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * Project home page: https://github.com/mik3y/usb-serial-for-android
 */

package com.hoho.android.usbserial.util;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.wms.logger.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Utility class which services a {@link UsbSerialPort} in its {@link #run()} method.
 *
 * @author mike wakerly (opensource@hoho.com)
 */
public class SerialInputOutputManager implements Runnable {

    private static final String TAG = SerialInputOutputManager.class.getSimpleName() + "  ";
    private static final boolean DEBUG = true;
    private static final int BUFSIZ = 4096;

    /**
     * default read timeout is infinite, to avoid data loss with bulkTransfer API
     */
    private int mReadTimeout = 0;
    private int mWriteTimeout = 0;

    private final ByteBuffer mReadBuffer = ByteBuffer.allocate(BUFSIZ);
    private final ByteBuffer mWriteBuffer = ByteBuffer.allocate(BUFSIZ); // Synchronized by 'mWriteBuffer'

    public enum State {
        STOPPED,
        RUNNING,
        STOPPING
    }

    private State mState = State.STOPPED; // Synchronized by 'this'
    private Listener mListener; // Synchronized by 'this'
    private final UsbSerialPort mSerialPort;

    public interface Listener {
        /**
         * Called when new incoming data is available.
         */
        public void onNewData(byte[] data);

        /**
         * Called when {@link SerialInputOutputManager#run()} aborts due to an error.
         */
        public void onRunError(Exception e);
    }

    public SerialInputOutputManager(UsbSerialPort serialPort) {
        mSerialPort = serialPort;
    }

    public SerialInputOutputManager(UsbSerialPort serialPort, Listener listener) {
        mSerialPort = serialPort;
        mListener = listener;
    }

    public synchronized void setListener(Listener listener) {
        mListener = listener;
    }

    public synchronized Listener getListener() {
        return mListener;
    }

    public void setReadTimeout(int timeout) {
        // when set if already running, read already blocks and the new value will not become effective now
        if (mReadTimeout == 0 && timeout != 0 && mState != State.STOPPED)
            throw new IllegalStateException("Set readTimeout before SerialInputOutputManager is started");
        mReadTimeout = timeout;
    }

    public int getReadTimeout() {
        return mReadTimeout;
    }

    public void setWriteTimeout(int timeout) {
        mWriteTimeout = timeout;
    }

    public int getWriteTimeout() {
        return mWriteTimeout;
    }

    /*
     * when writeAsync is used, it is recommended to use readTimeout != 0,
     * else the write will be delayed until read data is available
     */
    public void writeAsync(byte[] data) {
        synchronized (mWriteBuffer) {
            mWriteBuffer.put(data);
        }
    }

    public synchronized void stop() {
        if (getState() == State.RUNNING) {
//            Log.i(TAG, "Stop requested");
            Logger.i(TAG, "Stop requested");
            mState = State.STOPPING;
        }
    }

    public synchronized State getState() {
        return mState;
    }

    /**
     * Continuously services the read and write buffers until {@link #stop()} is
     * called, or until a driver exception is raised.
     * <p>
     * NOTE(mikey): Uses inefficient read/write-with-timeout.
     */

    @Override
    public void run() {
        synchronized (this) {
            if (getState() != State.STOPPED) {
                throw new IllegalStateException("Already running");
            }
            mState = State.RUNNING;
        }

        Logger.i(TAG, "Running ...");
        try {
            while (true) {
                if (getState() != State.RUNNING) {
                    Logger.i(TAG, "Stopping mState=State.RUNNING");
                    break;
                } else if (getState() == State.STOPPED) {
                    Logger.i(TAG, "Stopping mState=STOPPED");
                } else if (getState() == State.STOPPING) {
                    Logger.i(TAG, "Stopping mState=STOPPING");
                }
                step();
            }
        } catch (Exception e) {
            Logger.w(TAG, "Run ending due to exception: " + e.getMessage(), e);
            final Listener listener = getListener();
            if (listener != null) {
                listener.onRunError(e);
            }
        } finally {
            synchronized (this) {
                mState = State.STOPPED;
                Logger.i(TAG, "Stopped");
            }
        }
    }

    private long lastTime = 0;

    private void step() {

        long currentTime = System.currentTimeMillis();
        Logger.w("interval time is :", currentTime - lastTime);
        lastTime = currentTime;

        // Handle incoming data.
        int len = 0;
        try {
            len = mSerialPort.read(mReadBuffer.array(), mReadTimeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (len > 0) {
//            if (DEBUG) Log.d(TAG, "Read data len=" + len);
            if (DEBUG) Logger.d(TAG, "Read data len=" + len);
            final Listener listener = getListener();
            if (listener != null) {
                final byte[] data = new byte[len];
                mReadBuffer.get(data, 0, len);
                listener.onNewData(data);
            }
            mReadBuffer.clear();
        }

        // Handle outgoing data.
        byte[] outBuff = null;
        synchronized (mWriteBuffer) {
            len = mWriteBuffer.position();
            if (len > 0) {
                outBuff = new byte[len];
                mWriteBuffer.rewind();
                mWriteBuffer.get(outBuff, 0, len);
                mWriteBuffer.clear();
            }
        }
        if (outBuff != null) {
            if (DEBUG) {
                Logger.d(TAG, "Writing data len = " + len);
            }
            try {
                mSerialPort.write(outBuff, mWriteTimeout);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
