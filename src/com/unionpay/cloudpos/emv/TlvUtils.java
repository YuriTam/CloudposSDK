
package com.unionpay.cloudpos.emv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

/**
 * 将字符串转换为TLV对象
 */
abstract class TlvUtils {

    /**
     * 将16进制字符串转换为TLV对象列表
     * 
     * @param hexString
     * @return
     */
    static List<Tlv> builderTlvList(String hexString) {
        List<Tlv> tlvs = new ArrayList<Tlv>();

        int position = 0;
        while (position != hexString.length()) {
            String _hexTag = getTag(hexString, position);
            position += _hexTag.length();

            LPositon l_position = getLengthAndPosition(hexString, position);
            int _vl = l_position.get_vL();

            position = l_position.get_position();

            String _value = hexString.substring(position, position + _vl * 2);

            position = position + _value.length();

            tlvs.add(new Tlv(_hexTag, _vl, _value));
        }
        return tlvs;
    }

    /**
     * 将16进制字符串转换为TLV对象MAP
     * 
     * @param hexString
     * @return
     */
    static Map<Integer, byte[]> builderTlvMap(String hexString) {

        Map<Integer, byte[]> tlvs = new HashMap<Integer, byte[]>();

        int position = 0;
        while (position != hexString.length()) {
            String _hexTag = getTag(hexString, position);
            position += _hexTag.length();
            int OutLenStart = position;

            LPositon l_position = getLengthAndPosition(hexString, position);
            String _OutTagLen = hexString.substring(OutLenStart, l_position.get_position());
            int _vl = l_position.get_vL();
            position = l_position.get_position();
            String _value = hexString.substring(position, position + _vl * 2);
            position = position + _value.length();
            Integer integer;
            try {
                // integer=new Integer(_hexTag);
                integer = Integer.valueOf(_hexTag, 16).intValue();
            } catch (Exception e) {
                Log.e("TLV", "TAG ERROR,tag=" + _hexTag);
                return null;
            }
            tlvs.put(integer, StringUtil.hexString2bytes(_value));
        }
        return tlvs;
    }

    /**
     * 返回最后的Value的长度
     * 
     * @param hexString
     * @param position
     * @return
     */
    private static LPositon getLengthAndPosition(String hexString, int position) {
        String firstByteString = hexString.substring(position, position + 2);
        int i = Integer.parseInt(firstByteString, 16);
        String hexLength = "";

        if (((i >>> 7) & 1) == 0) {
            hexLength = hexString.substring(position, position + 2);
            position = position + 2;
        } else {
            // 当最左侧的bit位为1的时候，取得后7bit的值，
            int _L_Len = i & 127;
            position = position + 2;
            hexLength = hexString.substring(position, position + _L_Len * 2);
            // position表示第一个字节，后面的表示有多少个字节来表示后面的Value值
            position = position + _L_Len * 2;
        }
        return new LPositon(Integer.parseInt(hexLength, 16), position);

    }

    /**
     * 取得子域Tag标签
     * 
     * @param hexString
     * @param position
     * @return
     */
    private static String getTag(String hexString, int position) {
        String firstByte = hexString.substring(position, position + 2);
        int i = Integer.parseInt(firstByte, 16);
        if ((i & 0x1f) == 0x1f) {
            return hexString.substring(position, position + 4);

        } else {
            return hexString.substring(position, position + 2);
        }
    }
}
