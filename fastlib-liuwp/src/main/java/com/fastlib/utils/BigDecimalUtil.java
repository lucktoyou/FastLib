package com.fastlib.utils;

import java.math.BigDecimal;

/**
 * Created by liuwp on 2019/4/2.
 *
 * BigDecimal对包含小数的值进行精确计算（尤其是涉及到金钱）。
 *
 * 我们平常使用float或者double进行计算时会出现精度丢失的情况，例如：
 *      system.out.println(0.06-0.01)
 * 得到的结果为0.049999999999999996。
 *
 * 为什么会出现这种情况呢？
 * 原因在于我们的计算机是二进制的。浮点数没有办法使用二进制进行精确表示。如：2.4的二进制表示并非就是精确的2.4。反
 * 而最为接近的二进制表示是 2.3999999999999999。浮点数的值实际上是由一个特定的数学公式计算得到的。
 *
 * BigDecimal提供了add(),subtract(),multiply()和divide()四种方法，分别为加减乘除，一般计算包含小数的用法:
 * BigDecimal b1 = new BigDecimal(a1);
 * BigDecimal b2 = new BigDecimal(a2);
 * System.out.println(b1.subtract(b2).doubleValue());
 * 其中a1和a2的类型可以为String,Double,int,long等等，我在实际用的时候发现了一个问题，当a1和a2的类型为Double的时候，得到的结果仍然丢失了精度，例如：
 *
 * BigDecimal b1 = new BigDecimal(0.06);
 * BigDecimal b2 = new BigDecimal(0.01);
 * System.out.println(b1.subtract(b2).doubleValue());
 * 得到的结果仍然为0.049999999999999996
 * 可是如果参数类型为String,结果就是正确的，就像这样：
 *
 * BigDecimal b1 = new BigDecimal("0.06");
 * BigDecimal b2 = new BigDecimal("0.01");
 * System.out.println(b1.subtract(b2).doubleValue());
 * 得到的结果为0.05。
 *
 * 所以如果大家要对小数进行精确计算的话，new BigDecimal()的参数一定要用String类型的。
 * ---------------------
 * 作者：kolechez
 * 来源：CSDN
 * 原文：https://blog.csdn.net/a_kevin/article/details/75310516
 * 版权声明：本文为博主原创文章，转载请附上博文链接！
 */
public class BigDecimalUtil {

    /**
     * 提供精确的加法运算
     *
     * @param v1 被加数
     * @param v2 加数
     * @param scale 保留scale位小数
     * @return 两个参数的和
     */
    public static String add(String v1, String v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("保留的小数位数必须大于零");
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.add(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 提供精确的减法运算
     *
     * @param v1 被减数
     * @param v2 减数
     * @param scale 保留scale位小数
     * @return 两个参数的差
     */
    public static String subtract(String v1, String v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("保留的小数位数必须大于零");
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.subtract(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 提供精确的乘法运算
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @param scale 保留scale位小数
     * @return 两个参数的积
     */
    public static String multiply(String v1, String v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("保留的小数位数必须大于零");
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.multiply(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 提供精确的除法运算。当发生除不尽的情况时，由scale参数指定精度，以后的数字四舍五入
     *
     * @param v1 被除数
     * @param v2 除数
     * @param scale 表示需要精确到小数点以后几位
     * @return 两个参数的商
     */
    public static String divide(String v1, String v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("保留的小数位数必须大于零");
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 提供精确的小数位四舍五入处理
     *
     * @param v 需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("保留的小数位数必须大于零");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        return b.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 提供精确的小数位四舍五入处理
     *
     * @param v 需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static String round(String v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("保留的小数位数必须大于零");
        }
        BigDecimal b = new BigDecimal(v);
        return b.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 取余数
     *
     * @param v1 被除数
     * @param v2 除数
     * @param scale 小数点后保留几位
     * @return 余数
     */
    public static String remainder(String v1, String v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("保留的小数位数必须大于零");
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.remainder(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 比较大小
     *
     * @param v1 被比较数
     * @param v2 比较数
     * @return 如果v1大于v2则返回true 否则false
     */
    public static boolean compare(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        int bj = b1.compareTo(b2);
        return bj > 0;
    }
}
