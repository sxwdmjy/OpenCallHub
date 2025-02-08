package com.och.rtp;

public class G711Decoder {

    private static final short[] ALAW_DECODE_TABLE = new short[256];

    static {
        for (int i = 0; i < 256; i++) {
            int input = i ^ 0x55; // A-law uses a bit inversion
            int sign = (input & 0x80) == 0 ? 1 : -1;
            int exponent = (input & 0x70) >> 4;
            int mantissa = input & 0x0F;
            int value = (mantissa << 4) + 8;

            if (exponent != 0) {
                value += 0x100;
                value <<= (exponent - 1);
            }

            ALAW_DECODE_TABLE[i] = (short) (sign * value);
        }
    }

    public static short[] aDecode(byte[] alawData) {
        short[] pcmData = new short[alawData.length];
        for (int i = 0; i < alawData.length; i++) {
            pcmData[i] = ALAW_DECODE_TABLE[alawData[i] & 0xFF];
        }
        return pcmData;
    }

    public static short[] uDecode(byte[] g711Data) {
        short[] pcmData = new short[g711Data.length];
        for (int i = 0; i < g711Data.length; i++) {
            pcmData[i] = decodeG711Sample(g711Data[i]);
        }
        return pcmData;
    }

    private static short decodeG711Sample(byte sample) {
        int sign = (sample & 0x80) == 0 ? 1 : -1;
        int magnitude = ~sample & 0x7F;
        int step = ((magnitude & 0xF) << 1) + 33;
        int value = (step << ((magnitude >> 4) & 0x7)) - 33;
        return (short) (sign * value);
    }
}
