/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.authorization.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Assert;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * @author Thorben Lindhauer
 *
 */
public class AuthorizationTestRule extends TestWatcher {

  protected ProcessEngineRule engineRule;

  protected AuthorizationExceptionInterceptor interceptor;
  protected CommandExecutor replacedCommandExecutor;

  protected AuthorizationScenarioInstance scenarioInstance;

  protected List<User> users = new ArrayList<User>();
  protected List<Group> groups = new ArrayList<Group>();

  public AuthorizationTestRule(ProcessEngineRule engineRule) {
    this.engineRule = engineRule;
    this.interceptor = new AuthorizationExceptionInterceptor();
  }

  public void start(AuthorizationScenario scenario) {
    start(scenario, new HashMap<String, String>());
  }

  public void start(AuthorizationScenario scenario, Map<String, String> resourceBindings) {
    Assert.assertNull(interceptor.getLastException());
    scenarioInstance = new AuthorizationScenarioInstance(scenario, engineRule.getAuthorizationService(), resourceBindings);
    enableAuthorization();
    interceptor.activate();
  }

  public void enableAuthorization() {
    engineRule.getProcessEngine().getProcessEngineConfiguration().setAuthorizationEnabled(true);
  }

  public void assertScenario(AuthorizationScenario scenario) {

    interceptor.deactivate();
    disableAuthorization();
    scenarioInstance.tearDown(engineRule.getAuthorizationService());
    scenarioInstance.assertAuthorizationException(interceptor.getLastException());
    scenarioInstance = null;
  }

  /**
   * No exception was expected and no was thrown
   */
  public boolean scenarioSuceeded() {
    return interceptor.getLastException() == null;
  }

  public boolean scenarioFailed() {
    return interceptor.getLastException() != null;
  }

  public void disableAuthorization() {

    engineRule.getProcessEngine().getProcessEngineConfiguration().setAuthorizationEnabled(false);
  }

  protected void starting(Description description) {
    ProcessEngineConfigurationImpl engineConfiguration =
        (ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration();

    interceptor.reset();
    engineConfiguration.getCommandInterceptorsTxRequired().get(0).setNext(interceptor);
    interceptor.setNext(engineConfiguration.getCommandInterceptorsTxRequired().get(1));

    super.starting(description);
  }

  protected void finished(Description description) {
    engineRule.getIdentityService().clearAuthentication();

    ProcessEngineConfigurationImpl engineConfiguration =
        (ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration();

    engineConfiguration.getCommandInterceptorsTxRequired().get(0).setNext(interceptor.getNext());
    interceptor.setNext(null);

    super.finished(description);
  }

  public static Collection<AuthorizationScenario[]> asParameters(AuthorizationScenario... scenarios) {
    List<AuthorizationScenario[]> scenarioList = new ArrayList<AuthorizationScenario[]>();
    for (AuthorizationScenario scenario : scenarios) {
      scenarioList.add(new AuthorizationScenario[]{ scenario });
    }

    return scenarioList;
  }

  public void createUserAndGroup(String userId, String groupId) {

    User user = engineRule.getIdentityService().newUser(userId);
    engineRule.getIdentityService().saveUser(user);
    users.add(user);

    Group group = engineRule.getIdentityService().newGroup(groupId);
    engineRule.getIdentityService().saveGroup(group);
    groups.add(group);
  }

  public void deleteUsersAndGroups() {
    for (User user : users) {
      engineRule.getIdentityService().deleteUser(user.getId());
    }
    users.clear();

    for (Group group : groups) {
      engineRule.getIdentityService().deleteGroup(group.getId());
    }
    groups.clear();
  }

  public AuthorizationScenarioInstanceBuilder init(AuthorizationScenario scenario) {
    AuthorizationScenarioInstanceBuilder builder = new AuthorizationScenarioInstanceBuilder();
    builder.scenario = scenario;
    builder.rule = this;
    return builder;
  }

  public static class AuthorizationScenarioInstanceBuilder {
    protected AuthorizationScenario scenario;
    protected AuthorizationTestRule rule;
    protected Map<String, String> resourceBindings = new HashMap<String, String>();

    public AuthorizationScenarioInstanceBuilder bindResource(String key, String value) {
      resourceBindings.put(key, value);
      return this;
    }

    public void start() {
      rule.start(scenario, resourceBindings);
    }
  }

}
