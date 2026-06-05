package android.util;
import java.util.Arrays;

public class SparseArray<E> {
    private int[] mKeys;
    private Object[] mValues;
    private int mSize;

    public SparseArray() {
        this(10);
    }

    public SparseArray(int initialCapacity) {
        mKeys = new int[initialCapacity];
        mValues = new Object[initialCapacity];
        mSize = 0;
    }

    public void put(int key, E value) {
        int i = binarySearch(key);
        if (i >= 0) {
            mValues[i] = value;
        } else {
            i = ~i; // Insertion point
            if (mSize >= mKeys.length) {
                int newSize = mSize + 1;
                mKeys = Arrays.copyOf(mKeys, newSize);
                mValues = Arrays.copyOf(mValues, newSize);
            }
            if (mSize - i != 0) {
                System.arraycopy(mKeys, i, mKeys, i + 1, mSize - i);
                System.arraycopy(mValues, i, mValues, i + 1, mSize - i);
            }
            mKeys[i] = key;
            mValues[i] = value;
            mSize++;
        }
    }

    @SuppressWarnings("unchecked")
    public E get(int key) {
        int i = binarySearch(key);
        return (i < 0) ? null : (E) mValues[i];
    }

    @SuppressWarnings("unchecked")
    public E get(int key, E valueIfKeyNotFound) {
        int i = binarySearch(key);
        return (i < 0) ? valueIfKeyNotFound : (E) mValues[i];
    }

    public void remove(int key) {
        int i = binarySearch(key);
        if (i >= 0) {
            removeAt(i);
        }
    }

    public void removeAt(int index) {
        if (index < mSize - 1) {
            System.arraycopy(mKeys, index + 1, mKeys, index, mSize - index - 1);
            System.arraycopy(mValues, index + 1, mValues, index, mSize - index - 1);
        }
        mValues[mSize - 1] = null;
        mSize--;
    }

    public int size() {
        return mSize;
    }

    public int keyAt(int index) {
        return mKeys[index];
    }

    @SuppressWarnings("unchecked")
    public E valueAt(int index) {
        return (E) mValues[index];
    }

    private int binarySearch(int key) {
        int low = 0;
        int high = mSize - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = mKeys[mid];
            if (midVal < key) low = mid + 1;
            else if (midVal > key) high = mid - 1;
            else return mid;
        }
        return ~low;
    }
}