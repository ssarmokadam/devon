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
package com.capgemini.devonfw.module.moduleconfig.common.impl;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.capgemini.devonfw.module.moduleconfig.common.api.ModuleConfig;

/**
 * Basic implementation of the {@link ModuleConfig} interface. returns configured property values without changes
 *
 * @author ivanderk
 * @since 1.1
 */
@Named("lowercasemoduleConfig")
public class ModuleConfigImpl implements ModuleConfig {

  /**
   * Configured properties
   */
  @Inject
  private ModuleConfigProperties props;

  /**
   * @return props
   */
  public ModuleConfigProperties getProps() {

    return this.props;
  }

  /**
   * @param props new value of props.
   */
  public void setProps(ModuleConfigProperties props) {

    this.props = props;
  }

  @Override
  public String baz() {

    return this.props.getBaz();
  }

  @Override
  public Map<String, String> bar() {

    return this.props.getBar();
  }

}
