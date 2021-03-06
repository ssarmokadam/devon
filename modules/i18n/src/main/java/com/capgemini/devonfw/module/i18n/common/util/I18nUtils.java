/*******************************************************************************
 * Copyright 2015-2018 Capgemini SE.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package com.capgemini.devonfw.module.i18n.common.util;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import net.sf.mmm.util.nls.base.ResourceBundleControlUtf8WithNlsBundleSupport;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.capgemini.devonfw.module.i18n.common.I18nConstants;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * TODO vkiran This type ...
 *
 * @author vkiran
 */
public class I18nUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(I18nUtils.class);

  /**
   * @param resource is the bundle of message resources
   * @return map containing key value pairs of message resources
   */
  public static Map<String, String> convertResourceBundleToMap(ResourceBundle resource) {

    Map<String, String> map = new HashMap<>();

    Enumeration<String> keys = resource.getKeys();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      map.put(key, resource.getString(key));
    }
    return map;
  }

  /**
   * @param objJSON JSON String which is the representation of key value pairs in message resources property file
   * @param filter passed from the service
   * @param strJSONKey JSON Key
   * @param count used to traverse the JSON
   * @return JSON String that is matched against the filter
   * @throws JSONException thrown by getWithDotNotation
   */
  public static String getWithDotNotation(JsonObject objJSON, String filter, String strJSONKey, int count)
      throws JSONException {

    String closingBraces = I18nConstants.EMPTY_STRING;
    String strJSON = I18nConstants.EMPTY_STRING;
    if (filter.contains(I18nConstants.DOT)) {
      int indexOfDot = filter.indexOf(I18nConstants.DOT);
      String subKey = filter.substring(0, indexOfDot);
      JsonObject jsonObject = (JsonObject) objJSON.get(subKey);
      count++;
      strJSONKey = strJSONKey + "{\"" + subKey + "\":";
      if (jsonObject == null) {
        throw new JSONException(subKey + " is null");
      }
      try {
        return getWithDotNotation(jsonObject, filter.substring(indexOfDot + 1), strJSONKey, count);
      } catch (JSONException e) {
        throw new JSONException(subKey + I18nConstants.DOT + e.getMessage());
      }
    } else {
      for (int i = 0; i <= count; i++) {
        closingBraces = closingBraces + I18nConstants.CLOSING_BRACE;
      }

      if (objJSON.get(filter) == null)
        strJSON = I18nConstants.OPENING_BRACE + I18nConstants.CLOSING_BRACE;
      else
        strJSON = strJSONKey + "{\"" + filter + "\":" + objJSON.get(filter).toString() + closingBraces;
    }
    return strJSON;
  }

  /**
   * @param objJSON is the JSON representation of the resource file
   * @param filter is the filter supplied from service
   * @return JSON object filtered by the filter
   * @throws Throwable thrown by getJsonObj
   */

  public static String getJsonObj(JsonObject objJSON, String filter) throws Throwable {

    String strJSON = null;
    String strJSONKey = I18nConstants.EMPTY_STRING;
    int count = I18nConstants.ZERO;
    if (filter == null) {
      throw new JSONException("Null key.");
    }
    if (objJSON != null) {
      strJSON = getWithDotNotation(objJSON, filter, strJSONKey, count);
    }
    return strJSON;
  }

  /**
   * @param resourceMap contains the resources in the form of key value pairs
   * @param filter received from service
   * @return resources as JSON String
   * @throws Throwable thrown by getResourcesAsJSON
   */
  public static String getResourcesAsJSON(HashMap<String, String> resourceMap, String filter) throws Throwable {

    JsonParser jsonParser = new JsonParser();

    final java.lang.reflect.Type mapType = new TypeToken<Map<String, String>>() {
    }.getType();

    String strJSON =
        new GsonBuilder().registerTypeAdapter(mapType, new BundleMapSerializer()).create().toJson(resourceMap, mapType);

    if (filter != null && !filter.isEmpty()) {
      JsonObject obj = (JsonObject) jsonParser.parse(strJSON);
      strJSON = I18nUtils.getJsonObj(obj, filter);
    }
    return strJSON;
  }

  /**
   * @param locale received from service
   * @return resources generated by MMM as Map
   * @throws Throwable thrown by getResourcesGeneratedFromMMMAsMap
   */

  public static Map<String, String> getResourcesGeneratedFromMMMAsMap(Locale locale) throws Throwable {

    HashMap<String, String> resourceMap = new HashMap<>();
    ResourceBundle resBundle = null;
    File objResourcesFile = null;
    try {
      String language = locale.getLanguage();

      String resourcePath = I18nConstants.NLS_BUNDLE_INTF_NAME + I18nConstants.UNDER_SCORE + language
          + I18nConstants.DOT + I18nConstants.PROPERTIES;
      URL url = I18nUtils.class.getClassLoader().getResource(resourcePath);
      if (url != null) {
        objResourcesFile = new File(url.getPath());
      }

      if (objResourcesFile != null && objResourcesFile.exists() && !language.equals(I18nConstants.en)) {
        resBundle = ResourceBundle.getBundle(
            I18nConstants.NLS_BUNDLE_INTF_QUAL_NAME + I18nConstants.UNDER_SCORE + new Locale(locale.getLanguage()),
            locale, ResourceBundleControlUtf8WithNlsBundleSupport.INSTANCE);
      } /*
         * else if (language.equals(I18nConstants.en)) { resBundle = ResourceBundle.getBundle(
         * I18nConstants.NLS_BUNDLE_INTF_QUAL_NAME + I18nConstants.UNDER_SCORE + I18nConstants.en, new
         * Locale(locale.getLanguage()), ResourceBundleControlUtf8WithNlsBundleSupport.INSTANCE); }
         */else {
        resBundle = ResourceBundle.getBundle(I18nConstants.NLS_BUNDLE_INTF_QUAL_NAME,
            ResourceBundleControlUtf8WithNlsBundleSupport.INSTANCE);
      }
      resourceMap = (HashMap<String, String>) I18nUtils.convertResourceBundleToMap(resBundle);
    } catch (Throwable t) {
      LOGGER.error("Exception in getResourcesGeneratedFromMMMAsMap ", t);
      throw t;
    }
    return resourceMap;
  }

  /**
   * @param locale received from service
   * @param objLocale Locale object created out of locale received from service
   * @return resources generated by Default Implementation as Map
   * @throws Throwable thrown by getResourcesGeneratedFromMMMAsMap
   */
  public static Map<String, String> getResourcesGeneratedFromDefaultImplAsMap(String locale, Locale objLocale)
      throws Throwable {

    HashMap<String, String> resourceMap = new HashMap<>();
    ResourceBundle resBundle = null;
    try {
      resBundle =
          ResourceBundle.getBundle(I18nConstants.RESOURCE_BASENAME + I18nConstants.UNDER_SCORE + locale, objLocale);
      resourceMap = (HashMap<String, String>) I18nUtils.convertResourceBundleToMap(resBundle);
    } catch (Throwable t) {
      LOGGER.error("Exception in getResourcesGeneratedFromDefaultImplAsMap ", t);
      throw t;
    }
    return resourceMap;
  }

  /**
   * @param locale String that contains language and country code separated by hyphen
   * @return Locale Object
   */
  public static Locale getLocale(String locale) {

    String[] arrLocale = locale.split(I18nConstants.UNDER_SCORE);
    return new Locale(arrLocale[0], arrLocale[1]);
  }
}
