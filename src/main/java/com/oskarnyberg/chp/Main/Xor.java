package com.oskarnyberg.chp.Main;

/**
 * Created with IntelliJ IDEA.
 * User: Oskar
 * Date: 2013-10-20
 * Time: 17:58
 * To change this template use File | Settings | File Templates.
 */
public class Xor {

	public static String encryptString(String str) {
		StringBuffer sb = new StringBuffer(str);

		int lenStr = str.length();
		int lenKey = getKey().length();

		for (int i=0, j=0; i<lenStr; i++, j++) {
			if (j >= lenKey) {
				j = 0;
			}
			sb.setCharAt(i, (char)(str.charAt(i) ^ getKey().charAt(j)));
		}

		return sb.toString();
	}

	public static String decryptString(String str) {
		return encryptString(str);
	}

	private static String getKey() {
		return "kvoyFtdQwjxr03pVo24HMCwncuIprKEbunMDwjMZ";
	}
}