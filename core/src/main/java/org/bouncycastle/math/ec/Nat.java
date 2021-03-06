package org.bouncycastle.math.ec;

import java.math.BigInteger;

import org.bouncycastle.crypto.util.Pack;

public abstract class Nat
{
    private static final long M = 0xFFFFFFFFL;

    public static int add(int len, int[] x, int[] y, int[] z)
    {
        long c = 0;
        for (int i = 0; i < len; ++i)
        {
            c += (x[i] & M) + (y[i] & M);
            z[i] = (int)c;
            c >>>= 32;
        }
        return (int)c;
    }

    public static int addBothTo(int len, int[] x, int[] y, int[] z)
    {
        long c = 0;
        for (int i = 0; i < len; ++i)
        {
            c += (x[i] & M) + (y[i] & M) + (z[i] & M);
            z[i] = (int)c;
            c >>>= 32;
        }
        return (int)c;
    }

    public static int addBothTo(int len, int[] x, int xOff, int[] y, int yOff, int[] z, int zOff)
    {
        long c = 0;
        for (int i = 0; i < len; ++i)
        {
            c += (x[xOff + i] & M) + (y[yOff + i] & M) + (z[zOff + i] & M);
            z[zOff + i] = (int)c;
            c >>>= 32;
        }
        return (int)c;
    }

    // TODO Re-write to allow full range for x?
    public static int addDWord(int len, long x, int[] z, int zOff)
    {
        // assert zOff <= (len - 2);
        long c = x;
        c += (z[zOff + 0] & M);
        z[zOff + 0] = (int)c;
        c >>>= 32;
        c += (z[zOff + 1] & M);
        z[zOff + 1] = (int)c;
        c >>>= 32;
        return c == 0 ? 0 : inc(len, z, zOff + 2);
    }

    public static int addTo(int len, int[] x, int xOff, int[] z, int zOff)
    {
        long c = 0;
        for (int i = 0; i < len; ++i)
        {
            c += (x[xOff + i] & M) + (z[zOff + i] & M);
            z[zOff + i] = (int)c;
            c >>>= 32;
        }
        return (int)c;
    }

    public static int addWord(int len, int x, int[] z, int zOff)
    {
        // assert zOff < len;
        long c = (x & M) + (z[zOff + 0] & M);
        z[zOff + 0] = (int)c;
        c >>>= 32;
        return c == 0 ? 0 : inc(len, z, zOff + 1);
    }

    public static int addWordExt(int len, int x, int[] zz, int zzOff)
    {
        int extLen = len << 1;
        // assert zzOff < extLen;
        long c = (x & M) + (zz[zzOff + 0] & M);
        zz[zzOff + 0] = (int)c;
        c >>>= 32;
        return c == 0 ? 0 : inc(extLen, zz, zzOff + 1);
    }

    public static int[] copy(int len, int[] x)
    {
        int[] z = new int[len];
        System.arraycopy(x, 0, z, 0, len);
        return z;
    }

    public static void copy(int len, int[] x, int[] z)
    {
        System.arraycopy(x, 0, z, 0, len);
    }

    public static int[] create(int len)
    {
        return new int[len];
    }

    public static int dec(int len, int[] z, int zOff)
    {
        // assert zOff <= len;
        for (int i = zOff; i < len; ++i)
        {
            if (--z[i] != -1)
            {
                return 0;
            }
        }
        return -1;
    }

    public static boolean eq(int len, int[] x, int[] y)
    {
        for (int i = len - 1; i >= 0; --i)
        {
            if (x[i] != y[i])
            {
                return false;
            }
        }
        return true;
    }

    public static int[] fromBigInteger(int bits, BigInteger x)
    {
        if (x.signum() < 0 || x.bitLength() > bits)
        {
            throw new IllegalArgumentException();
        }

        int len = (bits + 31) >> 5;
        int[] z = create(len);
        int i = 0;
        while (x.signum() != 0)
        {
            z[i++] = x.intValue();
            x = x.shiftRight(32);
        }
        return z;
    }

    public static int getBit(int[] x, int bit)
    {
        if (bit == 0)
        {
            return x[0] & 1;
        }
        int w = bit >> 5;
        if (w < 0 || w >= x.length)
        {
            return 0;
        }
        int b = bit & 31;
        return (x[w] >>> b) & 1;
    }

    public static boolean gte(int len, int[] x, int[] y)
    {
        for (int i = len - 1; i >= 0; --i)
        {
            int x_i = x[i] ^ Integer.MIN_VALUE;
            int y_i = y[i] ^ Integer.MIN_VALUE;
            if (x_i < y_i)
                return false;
            if (x_i > y_i)
                return true;
        }
        return true;
    }

    public static int inc(int len, int[] z, int zOff)
    {
        // assert zOff <= len;
        for (int i = zOff; i < len; ++i)
        {
            if (++z[i] != 0)
            {
                return 0;
            }
        }
        return 1;
    }

    public static boolean isOne(int len, int[] x)
    {
        if (x[0] != 1)
        {
            return false;
        }
        for (int i = 1; i < len; ++i)
        {
            if (x[i] != 0)
            {
                return false;
            }
        }
        return true;
    }

    public static boolean isZero(int len, int[] x)
    {
        for (int i = 0; i < len; ++i)
        {
            if (x[i] != 0)
            {
                return false;
            }
        }
        return true;
    }

    public static void mul(int len, int[] x, int[] y, int[] zz)
    {
        zz[len] = mulWord(len, x[0], y, zz);

        for (int i = 1; i < len; ++i)
        {
            zz[i + len] = mulWordAddTo(len, x[i], y, 0, zz, i);
        }
    }

    public static void mul(int len, int[] x, int xOff, int[] y, int yOff, int[] zz, int zzOff)
    {
        zz[zzOff + len] = mulWord(len, x[xOff + 0], y, yOff, zz, zzOff);

        for (int i = 1; i < len; ++i)
        {
            zz[zzOff + i + len] = mulWordAddTo(len, x[xOff + i], y, yOff, zz, zzOff + i);
        }
    }

    public static int mul31BothAdd(int len, int a, int[] x, int b, int[] y, int[] z, int zOff)
    {
        long c = 0, aVal = a & M, bVal = b & M;
        int i = 0;
        do
        {
            c += aVal * (x[i] & M) + bVal * (y[i] & M) + (z[zOff + i] & M);
            z[zOff + i] = (int)c;
            c >>>= 32;
        }
        while (++i < len);
        return (int)c;
    }

    public static int mulWord(int len, int x, int[] y, int[] z)
    {
        long c = 0, xVal = x & M;
        int i = 0;
        do
        {
            c += xVal * (y[i] & M);
            z[i] = (int)c;
            c >>>= 32;
        }
        while (++i < len);
        return (int)c;
    }

    public static int mulWord(int len, int x, int[] y, int yOff, int[] z, int zOff)
    {
        long c = 0, xVal = x & M;
        int i = 0;
        do
        {
            c += xVal * (y[yOff + i] & M);
            z[zOff + i] = (int)c;
            c >>>= 32;
        }
        while (++i < len);
        return (int)c;
    }

    public static int mulWordAddTo(int len, int x, int[] y, int yOff, int[] z, int zOff)
    {
        long c = 0, xVal = x & M;
        int i = 0;
        do
        {
            c += xVal * (y[yOff + i] & M) + (z[zOff + i] & M);
            z[zOff + i] = (int)c;
            c >>>= 32;
        }
        while (++i < len);
        return (int)c;
    }

    public static int mulWordDwordAdd(int len, int x, long y, int[] z, int zOff)
    {
        // assert zOff <= (len - 3);
        long c = 0, xVal = x & M;
        c += xVal * (y & M) + (z[zOff + 0] & M);
        z[zOff + 0] = (int)c;
        c >>>= 32;
        c += xVal * (y >>> 32) + (z[zOff + 1] & M);
        z[zOff + 1] = (int)c;
        c >>>= 32;
        c += (z[zOff + 2] & M);
        z[zOff + 2] = (int)c;
        c >>>= 32;
        return c == 0 ? 0 : inc(len, z, zOff + 3);
    }

    public static int shiftDownBit(int len, int[] z, int c)
    {
        int i = len;
        while (--i >= 0)
        {
            int next = z[i];
            z[i] = (next >>> 1) | (c << 31);
            c = next;
        }
        return c << 31;
    }

    public static int shiftDownBit(int len, int[] x, int c, int[] z)
    {
        int i = len;
        while (--i >= 0)
        {
            int next = x[i];
            z[i] = (next >>> 1) | (c << 31);
            c = next;
        }
        return c << 31;
    }

    public static int shiftDownBits(int len, int[] z, int bits, int c)
    {
//        assert bits > 0 && bits < 32;
        int i = len;
        while (--i >= 0)
        {
            int next = z[i];
            z[i] = (next >>> bits) | (c << -bits);
            c = next;
        }
        return c << -bits;
    }

    public static int shiftDownBits(int len, int[] x, int xOff, int bits, int c, int[] z)
    {
//        assert bits > 0 && bits < 32;
        int i = len;
        while (--i >= 0)
        {
            int next = x[xOff + i];
            z[i] = (next >>> bits) | (c << -bits);
            c = next;
        }
        return c << -bits;
    }

    public static int shiftDownWord(int len, int[] z, int c)
    {
        int i = len;
        while (--i >= 0)
        {
            int next = z[i];
            z[i] = c;
            c = next;
        }
        return c;
    }

    public static int shiftUpBit(int len, int[] z, int c)
    {
        for (int i = 0; i < len; ++i)
        {
            int next = z[i];
            z[i] = (next << 1) | (c >>> 31);
            c = next;
        }
        return c >>> 31;
    }

    public static int shiftUpBit(int len, int[] z, int zOff, int c)
    {
        for (int i = 0; i < len; ++i)
        {
            int next = z[zOff + i];
            z[zOff + i] = (next << 1) | (c >>> 31);
            c = next;
        }
        return c >>> 31;
    }

    public static int shiftUpBit(int len, int[] x, int c, int[] z)
    {
        for (int i = 0; i < len; ++i)
        {
            int next = x[i];
            z[i] = (next << 1) | (c >>> 31);
            c = next;
        }
        return c >>> 31;
    }

    public static int shiftUpBit(int len, int[] x, int xOff, int c, int[] z)
    {
        for (int i = 0; i < len; ++i)
        {
            int next = x[xOff + i];
            z[i] = (next << 1) | (c >>> 31);
            c = next;
        }
        return c >>> 31;
    }

    public static int shiftUpBits(int len, int[] z, int bits, int c)
    {
//        assert bits > 0 && bits < 32;
        for (int i = 0; i < len; ++i)
        {
            int next = z[i];
            z[i] = (next << bits) | (c >>> -bits);
            c = next;
        }
        return c >>> -bits;
    }

    public static int shiftUpBits(int len, int[] x, int bits, int c, int[] z)
    {
//        assert bits > 0 && bits < 32;
        for (int i = 0; i < len; ++i)
        {
            int next = x[i];
            z[i] = (next << bits) | (c >>> -bits);
            c = next;
        }
        return c >>> -bits;
    }

    public static void square(int len, int[] x, int[] zz)
    {
        int extLen = len << 1;
        int c = 0;
        int j = len, k = extLen;
        do
        {
            long xVal = (x[--j] & M);
            long p = xVal * xVal;
            zz[--k] = (c << 31) | (int)(p >>> 33);
            zz[--k] = (int)(p >>> 1);
            c = (int)p;
        }
        while (j > 0);

        for (int i = 1; i < len; ++i)
        {
            c = squareWordAdd(x, i, zz);
            addWordExt(len, c, zz, i << 1);
        }

        shiftUpBit(extLen, zz, x[0] << 31);
    }

    public static int squareWordAdd(int[] x, int xPos, int[] z)
    {
        long c = 0, xVal = x[xPos] & M;
        int i = 0;
        do
        {
            c += xVal * (x[i] & M) + (z[xPos + i] & M);
            z[xPos + i] = (int)c;
            c >>>= 32;
        }
        while (++i < xPos);
        return (int)c;
    }

    public static int sub(int len, int[] x, int[] y, int[] z)
    {
        long c = 0;
        for (int i = 0; i < len; ++i)
        {
            c += (x[i] & M) - (y[i] & M);
            z[i] = (int)c;
            c >>= 32;
        }
        return (int)c;
    }

    public static int subBothFrom(int len, int[] x, int[] y, int[] z)
    {
        long c = 0;
        for (int i = 0; i < len; ++i)
        {
            c += (z[i] & M) - (x[i] & M) - (y[i] & M);
            z[i] = (int)c;
            c >>= 32;
        }
        return (int)c;
    }

    public static int subBothFrom(int len, int[] x, int xOff, int[] y, int yOff, int[] z, int zOff)
    {
        long c = 0;
        for (int i = 0; i < len; ++i)
        {
            c += (z[zOff + i] & M) - (x[xOff + i] & M) - (y[yOff + i] & M);
            z[zOff + i] = (int)c;
            c >>= 32;
        }
        return (int)c;
    }

    // TODO Re-write to allow full range for x?
    public static int subDWord(int len, long x, int[] z)
    {
        // assert 0 <= (len - 2);
        long c = -x;
        c += (z[0] & M);
        z[0] = (int)c;
        c >>= 32;
        c += (z[1] & M);
        z[1] = (int)c;
        c >>= 32;
        return c == 0 ? 0 : dec(len, z, 2);
    }

    public static int subFrom(int len, int[] x, int xOff, int[] z, int zOff)
    {
        long c = 0;
        for (int i = 0; i < len; ++i)
        {
            c += (z[zOff + i] & M) - (x[xOff + i] & M);
            z[zOff + i] = (int)c;
            c >>= 32;
        }
        return (int)c;
    }

    public static BigInteger toBigInteger(int len, int[] x)
    {
        byte[] bs = new byte[len << 2];
        for (int i = 0; i < len; ++i)
        {
            int x_i = x[i];
            if (x_i != 0)
            {
                Pack.intToBigEndian(x_i, bs, (len - 1 - i) << 2);
            }
        }
        return new BigInteger(1, bs);
    }

    public static void zero(int len, int[] z)
    {
        for (int i = 0; i < len; ++i)
        {
            z[i] = 0;
        }
    }
}
