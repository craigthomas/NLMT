/**
 * Copyright 2015 Craig Thomas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nlmt.commandline;

/**
 * Commands represent some form of action that the java program can execute.
 * As a minimum, each command must have an <code>execute</code> function that
 * is responsible for actually performing the command specified.
 *
 * @author thomas
 */
public abstract class Command {

    /**
     * Runs the specified command.
     */
    public abstract void execute();
}
