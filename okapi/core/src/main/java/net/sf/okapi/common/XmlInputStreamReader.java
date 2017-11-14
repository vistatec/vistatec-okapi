package net.sf.okapi.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class XmlInputStreamReader extends InputStreamReader {

    public final static int MAX_UNICODE_CHAR = 0x10FFFF;

    public XmlInputStreamReader(InputStream in) {
        super(in);
    }

    public XmlInputStreamReader(InputStream in, String charsetName) throws UnsupportedEncodingException {
        super(in, charsetName);
    }

    public XmlInputStreamReader(InputStream in, Charset cs) {
        super(in, cs);
    }

    public XmlInputStreamReader(InputStream in, CharsetDecoder dec) {
        super(in, dec);
    }

    private char[] cbuf = new char[1024];
    private int nextChar = 0;
    private int max = 0;

    private int fillBuffer() throws IOException {
        int remaining = max - nextChar;
        System.arraycopy(cbuf, nextChar, cbuf, 0, remaining);
        int i = super.read(cbuf, remaining, cbuf.length - remaining);
        max = remaining + Math.max(0, i);
        nextChar = 0;
        return i;
    }

    private int _read() throws IOException {
        if (nextChar >= max) {
            int i = fillBuffer();
            if (i == -1) {
                return -1;
            }
        }
        return cbuf[nextChar++]; 
    }

    private int ensureAvailableChars(int count) throws IOException {
        if (nextChar + count > max) {
            fillBuffer();
        }
        return (max - nextChar);
    }

    @Override
    public int read() throws IOException {
        for (int c = _read(); c != -1; c = _read()) {
            if (c == '&') {
                int available = ensureAvailableChars(8);
                if (available == -1) return '&'; // XXX needed?
                Entity entity = resolveEntity(cbuf, nextChar, max);
                if (entity == null || !entity.invalid) {
                    return '&';
                }
                nextChar += entity.size;
            }
            else if (validateChar(c)) {
                return c;
            }
        }
        return -1;
    }

    @Override
    public int read(char cbuf[], int offset, int length) throws IOException {
        int i = 0;
        for ( ; i < length; i++) {
            int c = read();
            if (c == -1) {
                return (i == 0) ? -1 : i;
            }
            cbuf[offset + i] = (char)c;
        }
        return i;
    }

    public static boolean validateChar(int value) {
        if (value >= 0xD800) {
            if (value < 0xE000) {
                return false;
            }
            if (value > 0xFFFF) {
                if (value > MAX_UNICODE_CHAR) {
                    return false;
                }
            } else if (value >= 0xFFFE) {
                return false;
            }
        } else if (value < 32) {
            if (value == 0) {
                return false;
            }
            if (value != 0x9 && value != 0xA && value != 0xD) {
                return false;
            }
        }
        return true;
    }

    public static class Entity {
        int value;
        int size;
        boolean invalid;
        Entity(int value, int size, boolean invalid) {
            this.value = value;
            this.size = size;
            this.invalid = invalid ? true : !validateChar(value);
        }
    }

    // This assumes that the first character '&' has already been read
    public static Entity resolveEntity(char[] buf, int ptr, int size) {
        int initialPtr = ptr;
        if (ptr >= size - 2)
            return null;
        char c = buf[ptr++];
        boolean invalid = false;

        // Numeric reference?
        if (c == '#') {
            c = buf[ptr++];
            int value = 0;
            if (c == 'x') { // hex
                while (c != ';') {
                    if (ptr >= size) {
                        break;
                    }
                    c = buf[ptr++];
                    if (c == ';') {
                        return new Entity(value, ptr-initialPtr, invalid);
                    }
                    if (!invalid) {
                        int hexVal = Character.digit(c, 16);
                        if (hexVal == -1) {
                            // Unterminated entity
                            return null;
                        }
                        value = (value << 4) + hexVal;
                        invalid = (value > MAX_UNICODE_CHAR);
                    }
                }
            } else { // numeric (decimal)
                while (c != ';') {
                    if (!invalid) {
                        int decVal = Character.digit(c, 10);
                        if (decVal == -1) {
                            // Unterminated entity
                            return null;
                        }
                        value = (value * 10) + decVal;
                        invalid = (value > MAX_UNICODE_CHAR);
                    }
                    if (ptr >= size) {
                        break;
                    }
                    c = buf[ptr++];
                }
                ptr++;
            }
            if (c == ';') { // got the full thing
                return new Entity(value, ptr-initialPtr-1, invalid);
            }

        } else {
            if (c == 'a') { // amp or apos?
                c = buf[ptr++];

                if (c == 'm') { // amp?
                    if (buf[ptr++] == 'p' && buf[ptr++] == ';') {
                        return new Entity('&', ptr-initialPtr, invalid);
                    }
                } else if (c == 'p') { // apos?
                    if (buf[ptr++] == 'o' && buf[ptr++] == 's' && buf[ptr++] == ';') {
                        return new Entity('\'', ptr-initialPtr, invalid);
                    }
                }
            } else if (c == 'g') { // gt?
                if (buf[ptr++] == 't' && buf[ptr++] == ';') {
                    return new Entity('>', ptr-initialPtr, invalid);
                }
            } else if (c == 'l') { // lt?
                if (buf[ptr++] == 't' && buf[ptr++] == ';') {
                    return new Entity('<', ptr-initialPtr, invalid);
                }
            } else if (c == 'q') { // quot?
                if (buf[ptr++] == 'u' && buf[ptr++] == 'o' && buf[ptr++] == 't' && buf[ptr++] == ';') {
                    return new Entity('"', ptr-initialPtr, invalid);
                }
            }
        }
        return null;
    }
}
