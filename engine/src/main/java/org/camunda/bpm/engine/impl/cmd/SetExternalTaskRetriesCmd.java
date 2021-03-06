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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * @author Thorben Lindhauer
 * @author Christopher Zell
 */
public class SetExternalTaskRetriesCmd extends ExternalTaskCmd {

  protected int retries;

  public SetExternalTaskRetriesCmd(String externalTaskId, int retries) {
    super(externalTaskId);
    this.retries = retries;
  }
  
  @Override
  protected void validateInput() {
    EnsureUtil.ensureGreaterThanOrEqual("retries", retries, 0);
  }

  @Override
  protected void execute(ExternalTaskEntity externalTask) {
    externalTask.setRetriesAndManageIncidents(retries);
  }
}
