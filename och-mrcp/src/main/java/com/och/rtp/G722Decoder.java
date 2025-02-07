package com.och.rtp;

public class G722Decoder {

    // 状态变量：分别对低频（6位）和高频（2位）子带进行解码
    private int lowPredictor;    // 低频预测值
    private int lowStep;         // 低频步长
    private int highPredictor;   // 高频预测值
    private int highStep;        // 高频步长

    // 常量：对于6位编码，取中点 32；对于2位编码，取中点 2
    private static final int LOW_MIDPOINT = 32;   // 6位，取值范围 0～63
    private static final int HIGH_MIDPOINT = 2;   // 2位，取值范围 0～3

    /**
     * 构造函数，初始化状态变量。
     */
    public G722Decoder() {
        reset();
    }

    /**
     * 重置解码器状态（可在开始新的 RTP 流时调用）
     */
    public void reset() {
        lowPredictor = 0;
        // 初始步长值可根据实际实现进行调整，这里取一个示例值
        lowStep = 16;
        highPredictor = 0;
        highStep = 1;
    }

    /**
     * 解码给定的 G.722 编码数据，返回 PCM 16位样本数组。
     *
     * @param encodedData RTP 负载数据，每个字节均为 8 位 G.722 数据
     * @return 解码后的 PCM 样本（16位，每个样本对应一个输入字节）
     */
    public short[] decode(byte[] encodedData) {
        int n = encodedData.length;
        short[] pcm = new short[n];
        for (int i = 0; i < n; i++) {
            int octet = encodedData[i] & 0xFF;
            // 提取低频部分（高6位）和高频部分（低2位）
            int lowCode = (octet >> 2) & 0x3F;
            int highCode = octet & 0x03;

            // 低频部分：调用逆量化函数，更新预测值和步长
            int lowDiff = inverseQuantizeLow(lowCode, lowStep);
            lowPredictor = updatePredictor(lowPredictor, lowDiff);
            lowStep = updateStepLow(lowStep, lowCode);

            // 高频部分：调用逆量化函数，更新预测值和步长
            int highDiff = inverseQuantizeHigh(highCode, highStep);
            highPredictor = updatePredictor(highPredictor, highDiff);
            highStep = updateStepHigh(highStep, highCode);

            // 合成两个子带数据：
            // 根据 G.722 的原理，低频部分反映较大能量（约 6 位数据），高频部分反映较细节信息（2 位）。
            // 这里我们采用简单相加的方法，并对高频部分乘以 2（或左移1位）以适应幅度差异。
            int sample = lowPredictor + (highPredictor << 1);

            // 限幅到 16 位有符号范围
            if (sample > 32767) {
                sample = 32767;
            } else if (sample < -32768) {
                sample = -32768;
            }
            pcm[i] = (short) sample;
        }
        return pcm;
    }

    /**
     * 逆量化低频子带的量化码字
     *
     * @param code 6位编码值（0～63）
     * @param step 当前步长
     * @return 低频子带的差分值
     *
     * 注：此处采用简化线性公式： diff = (code - midpoint) * step
     */
    private int inverseQuantizeLow(int code, int step) {
        return (code - LOW_MIDPOINT) * step;
    }

    /**
     * 逆量化高频子带的量化码字
     *
     * @param code 2位编码值（0～3）
     * @param step 当前步长
     * @return 高频子带的差分值
     *
     * 注：同样采用简化线性公式： diff = (code - midpoint) * step
     */
    private int inverseQuantizeHigh(int code, int step) {
        return (code - HIGH_MIDPOINT) * step;
    }

    /**
     * 更新预测器值（简单的累加器模型）
     *
     * @param predictor 当前预测器值
     * @param diff 逆量化得到的差分值
     * @return 新的预测器值
     */
    private int updatePredictor(int predictor, int diff) {
        return predictor + diff;
    }

    /**
     * 更新低频子带的步长
     *
     * @param step 当前步长
     * @param code 6位编码值
     * @return 更新后的步长
     *
     * 注：真实实现中步长更新需要使用适应性调整表，此处仅作简单增减模拟
     */
    private int updateStepLow(int step, int code) {
        int delta = 1;
        if (code > LOW_MIDPOINT) {
            step += delta;
        } else if (code < LOW_MIDPOINT) {
            step -= delta;
        }
        if (step < 1) step = 1;
        if (step > 127) step = 127;
        return step;
    }

    /**
     * 更新高频子带的步长
     *
     * @param step 当前步长
     * @param code 2位编码值
     * @return 更新后的步长
     *
     * 注：真实实现中步长更新算法比较简单，由于 2 位码字变化有限，此处用简单模型
     */
    private int updateStepHigh(int step, int code) {
        int delta = 1;
        if (code > HIGH_MIDPOINT) {
            step += delta;
        } else if (code < HIGH_MIDPOINT) {
            step -= delta;
        }
        if (step < 1) step = 1;
        if (step > 7) step = 7;
        return step;
    }

    // 测试 main 方法：输入模拟的 G.722 编码数据
    public static void main(String[] args) {
        // 模拟一个 RTP 负载，每个包包含 160 字节（20ms）的 G.722 数据
        byte[] simulatedRtpPayload = new byte[160];
        // 此处填入实际数据，测试时可以使用固定数据
        for (int i = 0; i < simulatedRtpPayload.length; i++) {
            simulatedRtpPayload[i] = (byte) (Math.random() * 256);
        }

        G722Decoder decoder = new G722Decoder();
        short[] pcm = decoder.decode(simulatedRtpPayload);
        System.out.println("解码得到 PCM 样本数：" + pcm.length);
        // 可以将 PCM 数据写入文件或播放
    }
}
