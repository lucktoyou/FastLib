package com.fastlib.utils;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liuwp 2017/12/20
 * 验证格式工具类
 */
public class VerifyUtil {

    private VerifyUtil(){
        //不实例化
    }

    /**
     * 判断格式是否为手机号
     * @param mobile 手机号码
     * @return
     */
    public static boolean isPhone(String mobile){
        /*
        移动：134、135、136、137、138、139、147、150、151、152、157（TD）、158、159、178、182、183、184、187、188、198
        联通：130、131、132、155、156、176、185、186、166
        电信：133、153、180、181、189、177、173、199
        总结起来就是第一位必定为1，第二位必定为3、4、5、6、7、8、9，其他位置可以为0-9.
         */
        String telRegex = "[1][3456789]\\d{9}";//"[1]"代表第1位为数字1，"[3456789]"代表第二位可以为其中的一个，"\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobile)){
            return false;
        }  else{
            return mobile.matches(telRegex);
        }
    }

    /**
     * 判断格式是否为email
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        if (TextUtils.isEmpty(email)){
            return false;
        } else{
            return m.matches();
        }
    }

    /**
     * 校验银行卡卡号
     *
     * @param cardId
     * @return
     */
    public static boolean isBankCard(String cardId) {
        char bit = getBankCardCheckCode(cardId.substring(0, cardId.length() - 1));
        if(bit == 'N'){
            return false;
        }
        return cardId.charAt(cardId.length() - 1) == bit;
    }

    //从不含校验位的银行卡卡号采用 Luhm 校验算法获得校验位
    private static char getBankCardCheckCode(String nonCheckCodeCardId){
        if(nonCheckCodeCardId == null || nonCheckCodeCardId.trim().length() == 0 || !nonCheckCodeCardId.matches("\\d+")) {
            //如果传的不是数据返回N
            return 'N';
        }
        char[] chs = nonCheckCodeCardId.trim().toCharArray();
        int luhmSum = 0;
        for(int i = chs.length - 1, j = 0; i >= 0; i--, j++) {
            int k = chs[i] - '0';
            if(j % 2 == 0) {
                k *= 2;
                k = k / 10 + k % 10;
            }
            luhmSum += k;
        }
        return (luhmSum % 10 == 0) ? '0' : (char)((10 - luhmSum % 10) + '0');
    }
}
