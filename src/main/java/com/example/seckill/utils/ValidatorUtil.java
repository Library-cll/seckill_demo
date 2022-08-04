package com.example.seckill.utils;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手机号码校验
 * 乐字节：专注线上IT培训
 * 答疑老师微信：lezijie
 *
 * @author zhoubin
 * @since 1.0.0
 */
public class ValidatorUtil {

	private static final Pattern mobile_pattern = Pattern.compile("[1]([3-9])[0-9]{9}$");

	public static boolean isMobile(String mobile){
		if (StringUtils.isEmpty(mobile)){
			return false;
		}
		Matcher matcher = mobile_pattern.matcher(mobile);
		return matcher.matches();
	}

}