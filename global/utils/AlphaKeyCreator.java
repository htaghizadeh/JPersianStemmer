package global.utils;

import global.utils.PatriciaTrie.KeyAnalyzer;

/**
 *
 * @author htaghizadeh
 */
public class AlphaKeyCreator implements KeyAnalyzer<Character> {

    public static int[] createIntBitMask(final int bitCount) {
        int[] bits = new int[bitCount];
        for (int i = 0; i < bitCount; i++) {
            bits[i] = 1 << (bitCount - i - 1);
        }
        return bits;
    }

    private static final int[] BITS = createIntBitMask(16);

    @Override
    public int length(Character key) {
        return 16;
    }

    @Override
    public boolean isBitSet(Character key, int keyLength, int bitIndex) {
        return (key & BITS[bitIndex]) != 0;
    }

    @Override
    public int bitIndex(Character key,   int keyOff, int keyLength,
                        Character found, int foundOff, int foundKeyLength) {
        if (found == null)
            found = (char)0;

        if(keyOff != 0 || foundOff != 0)
            throw new IllegalArgumentException("offsets must be 0 for fixed-size keys");

        int length = Math.max(keyLength, foundKeyLength);

        boolean allNull = true;
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
    public int compare(Character o1, Character o2) {
        return o1.compareTo(o2);
    }

    @Override
    public int bitsPerElement() {
        return 1;
    }

    @Override
    public boolean isPrefix(Character prefix, int offset, int length, Character key) {
        int addr1 = prefix;
        int addr2 = key;
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