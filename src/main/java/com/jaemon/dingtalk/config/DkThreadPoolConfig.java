/*
 * Copyright 2015-2020 Jaemon(answer_ljm@163.com)
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
package com.jaemon.dingtalk.config;

import com.jaemon.dingtalk.entity.DkThreadPoolProperties;
import com.jaemon.dingtalk.support.AsyncCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static com.jaemon.dingtalk.constant.DkConstant.DINGTALK_EXECUTOR;

/**
 * DINGTALK线程池配置类
 *
 * @author Jaemon@answer_ljm@163.com
 * @version 1.0
 */
@Configuration
@Conditional(AsyncCondition.class)
@ConditionalOnMissingBean(name = DINGTALK_EXECUTOR)
@EnableConfigurationProperties({DkThreadPoolProperties.class})
public class DkThreadPoolConfig {


    @Bean(name = DINGTALK_EXECUTOR)
    public Executor dingTalkExecutor(DkThreadPoolProperties threadPoolProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(threadPoolProperties.getCoreSize());
        // 最大线程数
        executor.setMaxPoolSize(threadPoolProperties.getMaxSize());
        // 线程最大空闲时间
        executor.setKeepAliveSeconds(threadPoolProperties.getKeepAliveSeconds());
        // 队列大小
        executor.setQueueCapacity(threadPoolProperties.getQueueCapacity());
        // 指定用于新创建的线程名称的前缀
        executor.setThreadNamePrefix(threadPoolProperties.getThreadNamePrefix());

        // 使用自定义拒绝策略, 直接抛出异常
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        // 等待任务完成时再关闭线程池--表明等待所有线程执行完
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 初始化
        executor.initialize();
        return executor;
    }

}