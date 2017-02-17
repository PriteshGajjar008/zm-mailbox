/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2009, 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.client;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.methods.GetMethod;

/**
 * Wraps a HTTPClient <tt>GetMethod</tt> and automatically releases resources
 * when the stream is closed.
 */
public class GetMethodInputStream extends InputStream {

    private GetMethod mGetMethod;
    private InputStream mIn;
    
    public GetMethodInputStream(GetMethod getMethod)
    throws IOException {
        mGetMethod = getMethod;
        mIn = getMethod.getResponseBodyAsStream();
    }
    
    @Override
    public int read() throws IOException {
        return mIn.read();
    }

    @Override
    public int available() throws IOException {
        return mIn.available();
    }

    @Override
    public void close() throws IOException {
        mIn.close();
        mGetMethod.releaseConnection();
    }

    @Override
    public synchronized void mark(int readlimit) {
        mIn.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return mIn.markSupported();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return mIn.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return mIn.read(b);
    }

    @Override
    public synchronized void reset() throws IOException {
        mIn.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return mIn.skip(n);
    }
}
