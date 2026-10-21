package telegramdecoder;

import org.telegram.tgnet.SerializedData;

/**
 * SerializedData that fails instead of returning garbage when a read goes past the end of the data: upstream
 * reads -1 from the stream at the end and returns a bogus value, and readData allocates the requested size
 * unchecked. The checks apply only when {@code exception} is true, keeping the lenient upstream behavior otherwise.
 */
final class StrictSerializedData extends SerializedData {

    StrictSerializedData(byte[] data) {
        super(data);
    }

    private void require(int bytes, boolean exception) {
        if (exception && (bytes < 0 || remaining() < bytes)) {
            throw new IllegalStateException("read past end of data: need " + bytes + ", remaining " + remaining());
        }
    }

    @Override
    public int readInt32(boolean exception) {
        require(4, exception);
        return super.readInt32(exception);
    }

    @Override
    public long readInt64(boolean exception) {
        require(8, exception);
        return super.readInt64(exception);
    }

    @Override
    public byte readByte(boolean exception) {
        require(1, exception);
        return super.readByte(exception);
    }

    @Override
    public void readBytes(byte[] b, boolean exception) {
        require(b.length, exception);
        super.readBytes(b, exception);
    }

    @Override
    public byte[] readData(int count, boolean exception) {
        require(count, exception);
        return super.readData(count, exception);
    }
}
