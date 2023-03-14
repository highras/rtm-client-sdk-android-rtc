package com.highras.videoudp;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author fengzi
 * @date 2022/3/16 10:50
 */
public class Constants {

    public static List<String> LANGUAGE = Arrays.asList("中文简体","英语","日语","泰语","印尼语","西语","越南语","印地语","阿语","马来语","法语","葡语");
    public static List<String> LANGUAGE_VALUE = Arrays.asList("zh","en","ja","th","id","es","vi","hi","ar","ms","fr","pt");
    public static List<String> LANGUAGE_SHOW = Arrays.asList("中文简体","英语","日语","泰语","印尼语","西语","越南语","印地语","阿语","马来语","法语","葡语");
    public static HashMap<String, String> LANGUAGE_SHOW_MAP = new HashMap<String, String>() {
        {
            put("zh", "data_zh.json");
            put("en", "data_en.json");
            put("ja", "data_ja.json");
            put("th", "data_th.json");
            put("id", "data_id.json");
            put("es", "data_en.json");
            put("vi", "data_en.json");
            put("hi", "data_en.json");
            put("ar", "data_en.json");
            put("ms", "data_en.json");
            put("fr", "data_en.json");
            put("pt", "data_en.json");
        }
    };
    public static JSONObject languageObj = new JSONObject();
}
