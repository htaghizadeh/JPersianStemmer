package global.utils;

import global.utils.PatriciaTrie.KeyAnalyzer;

/**
 *
 * @author htaghizadeh
 */
public class IntegerKeyCreator implements KeyAnalyzer<Integer> {

    public static int[] createIntBitMask(final int bitCount) {
        int[] bits = new int[bitCount];
        for (int i = 0; i < bitCount; i++) {
            bits[i] = 1 << (bitCount - i - 1);
        }
        return bits;
    }

    private static final int[] BITS = createIntBitMask(32);

    @Override
    public int length(Integer key) {
        return 32;
    }

    @Override
    public boolean isBitSet(Integer key, int keyLength, int bitIndex) {
        return (key & BITS[bitIndex]) != 0;
    }

    @Override
    public int bitIndex(Integer key,   int keyOff, int keyLength,
                        Integer found, int foundOff, int foundKeyLength) {
        if (found == null)
            found = 0;

        if(keyOff != 0 || foundOff != 0)
            throw new IllegalArgumentException("offsets must be 0 for fixed-size keys");

        boolean allNull = true;

        int length = Math.max(keyLength, foundKeyLength);

        for (int i = 0; i < length; i++) {
            int a = key & BITS[i];
            int b = found & BITS[i];

            if (allNull && a != 0) {
                allNull = false;
            }

            if (a != b) {
                return i;
            }
        }

        if (allNull) {
            return KeyAnalyzer.NULL_BIT_KEY;
        }

        return KeyAnalyzer.EQUAL_BIT_KEY;
    }

    @Override
    public int compare(Integer o1, Integer o2) {
        return o1.compareTo(o2);
    }

    @Override
    public int bitsPerElement() {
        return 1;
    }

    @Override
    public boolean isPrefix(Integer prefix, int offset, int length, Integer key) {
        int addr1 = prefix.intValue();
        int addr2 = key.intValue();
        addr1 = addr1 << offset;

        int mask = 0;
        for(int i = 0; i < length; i++) {
            mask |= (0x1 << i);
        }

        addr1 &= mask;
        addr2 &= mask;

        return addr1 == addr2;
    }
}

