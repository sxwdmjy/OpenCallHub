package com.och.utils;

public class G711Utils {

    private static final short BIAS = 0x84;   // 定义u-law量化偏移量
    private static final int MAX = 32635;     // 最大量化输入绝对值（避免溢出）

    /**
     * 将16位PCM小端序字节数组转换为G.711 μ-law字节数组
     * @param pcmData 输入PCM数据（16位小端序）
     * @return μ-law编码后的字节数组
     */
    public static byte[] pcmToULaw(byte[] pcmData) {
        byte[] ulawData = new byte[pcmData.length / 2];
        for (int i = 0; i < ulawData.length; i++) {
            // 小端序转换（低字节在前）
            short sample = (short) ((pcmData[2*i + 1] << 8) | (pcmData[2*i] & 0xFF));
            ulawData[i] = linearToULaw(sample);
        }
        return ulawData;
    }

    private static byte linearToULaw(short sample) {
        int sign = (sample < 0) ? 0x80 : 0x00;
        int absSample = Math.min(Math.abs(sample), 32767);
        absSample += 132; // μ-law偏移量
        int exponent = 7 - Integer.numberOfLeadingZeros(absSample) / 4;
        exponent = Math.max(0, Math.min(exponent, 7));
        int mantissa = (absSample >> (exponent + 3)) & 0x0F;
        return (byte) (sign | (exponent << 4) | mantissa);
    }


    /**
     * PCM转G.711 A-law
     * 算法参考ITU-T G.711标准
     */
    public static byte[] pcmToALaw(byte[] pcmData) {
        byte[] alawData = new byte[pcmData.length / 2];
        for (int i = 0; i < alawData.length; i++) {
            short sample = (short) ((pcmData[2*i + 1] << 8) | (pcmData[2*i] & 0xFF));
            alawData[i] = linearToALaw(sample);
        }
        return alawData;
    }

    private static byte linearToALaw(short pcmSample) {
        int sign = (pcmSample < 0) ? 0x00 : 0x80;
        int absSample = Math.min(Math.abs(pcmSample), 0x7FFF);
        int quantization = (absSample < 0x20) ? 0 : 7 - Integer.numberOfLeadingZeros(absSample >> 4);
        quantization = Math.max(0, Math.min(quantization, 7));
        int mantissa = (absSample >> (quantization + 3)) & 0x0F;
        return (byte) (sign | (quantization << 4) | mantissa);
    }
}
