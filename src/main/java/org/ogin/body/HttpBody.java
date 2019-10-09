package org.ogin.body;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Taken From : https://github.com/techery/janet-converters/blob/master/base-body/src/main/java/io/techery/janet/body/ActionBody.java
* */
public interface HttpBody {
    long length();
    InputStream getContent() throws IOException ;
    String mimeType();
    void writeContentTo(OutputStream outputStream) throws IOException;
}
