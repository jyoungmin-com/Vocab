package jyoungmin.vocabcommons.logging;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * {@link HttpServletRequest} wrapper that caches the request body.
 * Allows the request body to be read multiple times.
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    /**
     * Caches the original request's input stream.
     *
     * @param request The original {@link HttpServletRequest}.
     * @throws IOException if an I/O error occurs.
     */
    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
    }

    /**
     * Returns an input stream from the cached body.
     *
     * @return A {@link ServletInputStream} from the cached body.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CachedBodyServletInputStream(this.cachedBody);
    }

    /**
     * Returns a reader from the cached body.
     *
     * @return A {@link BufferedReader} from the cached body.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    /**
     * Returns the cached request body.
     *
     * @return The cached body as a byte array.
     */
    public byte[] getCachedBody() {
        return cachedBody;
    }

    /**
     * {@link ServletInputStream} implementation for reading from a byte array.
     */
    private static class CachedBodyServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream inputStream;

        /**
         * Creates a new stream from a byte array.
         *
         * @param cachedBody The byte array content.
         */
        public CachedBodyServletInputStream(byte[] cachedBody) {
            this.inputStream = new ByteArrayInputStream(cachedBody);
        }

        /**
         * Checks if the stream is at its end.
         *
         * @return True if no more bytes are available.
         */
        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        /**
         * Always ready for reading.
         *
         * @return True.
         */
        @Override
        public boolean isReady() {
            return true;
        }

        /**
         * Not supported.
         *
         * @throws UnsupportedOperationException always.
         */
        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException();
        }

        /**
         * Reads the next byte.
         *
         * @return The next byte, or -1 if end of stream.
         * @throws IOException if an I/O error occurs.
         */
        @Override
        public int read() throws IOException {
            return inputStream.read();
        }
    }
}
