package com.nsc;

import java.io.Closeable;
import java.io.InputStream;

public interface RemoteStorage extends Closeable {

    void upload(InputStream srcInputStream, String destPath);
    void mkdirs(String destPath) throws Exception;
}
