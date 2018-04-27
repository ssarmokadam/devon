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
package com.devonfw.module.winauthad.common.impl.security;

import java.util.Properties;

import javax.inject.Named;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.ldap.LdapAuthenticationProviderConfigurer;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.devonfw.module.winauthad.common.api.AuthenticationSource;
import com.devonfw.module.winauthad.common.api.accesscontrol.ActiveDirectory;

/**
 * Implementation of {@link AuthenticationSource}
 *
 * @author jhcore
 */
@ConfigurationProperties(prefix = "devon.winauth.ad")
@Named
public class AuthenticationSourceADImpl implements AuthenticationSource {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationSourceADImpl.class);

  /**
   * Instance of the ActiveDirectory class. We need it to do the query.
   */
  private ActiveDirectory activeDirectory;

  /**
   * User name of the server authentication
   */
  private String username = "";

  /**
   * Password of the server authentication
   */
  private String password;

  /**
   * Server domain
   */
  private String domain = "";

  private String userSearchFilter = "(uid={0})";

  private String userSearchBase = "";

  private String searchBy = "";

  private String rolePrefix = "";

  private String url = "";

  private StandardPBEStringEncryptor encryptor;

  private String keyPass;

  private boolean encrypt = false;

  /**
   * @return searchBy
   */
  public String getSearchBy() {

    return this.searchBy;
  }

  /**
   * @param searchBy new value of searchBy.
   */
  public void setSearchBy(String searchBy) {

    this.searchBy = searchBy;
  }

  /**
   * The constructor.
   */
  public AuthenticationSourceADImpl() {
    super();
    this.activeDirectory = new ActiveDirectory();
    if (this.searchBy == null || this.searchBy.equals("")) {
      this.searchBy = "samaccountname";
    }

  }

  @Override
  public LdapAuthenticationProviderConfigurer<AuthenticationManagerBuilder> getLdapAuthenticationProviderConfigurer() {

    LdapAuthenticationProviderConfigurer<AuthenticationManagerBuilder> ldap =
        new LdapAuthenticationProviderConfigurer<>();

    String pass = this.password;

    if (this.encrypt) {
      this.encryptor = new StandardPBEStringEncryptor();
      this.encryptor.setPassword(this.keyPass);

      Properties props = new EncryptableProperties(this.encryptor);
      props.setProperty("password", this.password);
      pass = props.getProperty("password");
    }

    ldap.userSearchBase(this.userSearchBase).userSearchFilter(this.userSearchFilter).rolePrefix(this.rolePrefix)
        .contextSource().managerDn(this.username).managerPassword(pass).url(this.url);

    return ldap;
  };

  /**
   * @return keyPass
   */
  public String getKeyPass() {

    return this.keyPass;
  }

  /**
   * @param keyPass new value of keyPass.
   */
  public void setKeyPass(String keyPass) {

    this.keyPass = keyPass;
  }

  /**
   * @return encrypt
   */
  public boolean isEncrypt() {

    return this.encrypt;
  }

  /**
   * @param encrypt new value of encrypt.
   */
  public void setEncrypt(boolean encrypt) {

    this.encrypt = encrypt;
  }

  /**
   * The constructor.
   *
   * @param username
   * @param password
   * @param domain
   */
  @SuppressWarnings("javadoc")
  public AuthenticationSourceADImpl(String username, String password, String domain) {
    super();
  }

  /**
   * @param searchValue - the value of the user name we are searching
   * @return attributes of the user
   */
  @Override
  public Attributes searchUserByUsername(String searchValue) {

    NamingEnumeration<SearchResult> result;
    try {
      String pass = this.password;
      if (this.encrypt) {
        this.encryptor = new StandardPBEStringEncryptor();
        this.encryptor.setPassword(this.keyPass);

        Properties props = new EncryptableProperties(this.encryptor);
        props.setProperty("password", this.password);
        pass = props.getProperty("password");
      }
      this.activeDirectory.connect(this.username, pass, this.domain);

      result = this.activeDirectory.searchUser(searchValue, this.searchBy, this.domain);
    } finally {
      this.activeDirectory.closeLdapConnection();
    }

    try {
      Attributes attrs = result.next().getAttributes();
      return attrs;
    } catch (NamingException e) {
      e.printStackTrace();
      UsernameNotFoundException exception = new UsernameNotFoundException("Authentication failed.", e);
      LOG.error("Failed to get user {}.", this.username, exception);
      throw exception;
    }
  }

  /**
   * @return activeDirectory
   */
  public ActiveDirectory getActiveDirectory() {

    return this.activeDirectory;
  }

  /**
   * @param activeDirectory new value of {@link ActiveDirectory}.
   */
  public void setActiveDirectory(ActiveDirectory activeDirectory) {

    this.activeDirectory = activeDirectory;
  }

  /**
   * @return username
   */
  @Override
  public String getUsername() {

    return this.username;
  }

  /**
   * @param username new value of username.
   */
  @Override
  public void setUsername(String username) {

    this.username = username;
  }

  /**
   * @return password
   */
  @Override
  public String getPassword() {

    return this.password;
  }

  /**
   * @param password new value of password.
   */
  @Override
  public void setPassword(String password) {

    this.password = password;
  }

  /**
   * @return domain
   */
  @Override
  public String getDomain() {

    return this.domain;
  }

  /**
   * @param domain new value of domain.
   */
  @Override
  public void setDomain(String domain) {

    this.domain = domain;
  }

  /**
   * @return userSearchFilter
   */
  @Override
  public String getUserSearchFilter() {

    return this.userSearchFilter;
  }

  /**
   * @param userSearchFiler new value of userSearchFilter.
   */
  @Override
  public void setUserSearchFilter(String userSearchFiler) {

    this.userSearchFilter = userSearchFiler;
  }

  /**
   * @return userSearchBase
   */
  @Override
  public String getUserSearchBase() {

    return this.userSearchBase;
  }

  /**
   * @param userSearchBase new value of userSearchBase.
   */
  @Override
  public void setUserSearchBase(String userSearchBase) {

    this.userSearchBase = userSearchBase;
  }

  /**
   * @return rolePrefix
   */
  @Override
  public String getRolePrefix() {

    return this.rolePrefix;
  }

  /**
   * @param rolePrefix new value of rolePrefix.
   */
  @Override
  public void setRolePrefix(String rolePrefix) {

    this.rolePrefix = rolePrefix;
  }

  /**
   * @return url
   */
  @Override
  public String getUrl() {

    return this.url;
  }

  /**
   * @param url new value of url.
   */
  @Override
  public void setUrl(String url) {

    this.url = url;
  }
}
