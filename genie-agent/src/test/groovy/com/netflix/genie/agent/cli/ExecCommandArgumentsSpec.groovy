/*
 *
 *  Copyright 2018 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.genie.agent.cli

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import com.netflix.genie.agent.execution.CleanupStrategy
import spock.lang.Specification

class ExecCommandArgumentsSpec extends Specification {

    ExecCommand.ExecCommandArguments options
    JCommander jCommander
    ArgumentDelegates.ServerArguments serverArguments
    ArgumentDelegates.CacheArguments cacheArguments
    ArgumentDelegates.JobRequestArguments jobRequestArguments
    ArgumentDelegates.CleanupArguments cleanupArguments
    MainCommandArguments mainCommandArguments


    void setup() {
        serverArguments = new ServerArgumentsImpl()
        cacheArguments = new CacheArgumentsImpl()
        mainCommandArguments = Mock(MainCommandArguments)
        jobRequestArguments = new JobRequestArgumentsImpl(mainCommandArguments)
        cleanupArguments = new CleanupArgumentsImpl()
        options = new ExecCommand.ExecCommandArguments(serverArguments, cacheArguments, jobRequestArguments, cleanupArguments)
        jCommander = new JCommander(options)
    }

    void cleanup() {
    }

    def "Defaults (sample of delegates)"() {
        when:
        jCommander.parse()

        then:
        CacheArgumentsImpl.DEFAULT_CACHE_PATH == options.getCacheArguments().getCacheDirectory().getAbsolutePath()
        null == options.getJobRequestArguments().getJobId()
        !options.getJobRequestArguments().isInteractive()
        options.getJobRequestArguments().getJobTags().isEmpty()
        options.getCleanupArguments().getCleanupStrategy() == CleanupStrategy.DEPENDENCIES_CLEANUP
    }

    def "Parse"() {
        when:
        jCommander.parse(
            "--serverHost", "server.com",
            "--serverPort", "1234",
            "--cacheDirectory", "/tmp/foo",
            "--clusterCriterion", "NAME=prod",
            "--clusterCriterion", "NAME=test",
            "--no-cleanup"
        )

        then:
        "server.com" == options.getServerArguments().getServerHost()
        1234 == options.getServerArguments().getServerPort()
        "/tmp/foo" == options.getCacheArguments().getCacheDirectory().getAbsolutePath()
        2 == options.getJobRequestArguments().getClusterCriteria().size()
        options.getCleanupArguments().getCleanupStrategy() == CleanupStrategy.NO_CLEANUP
    }

    def "InvalidRequestId"() {
        when:
        jCommander.parse(
            "--commandCriterion", "/",
        )

        then:
        thrown(ParameterException)
    }
}
