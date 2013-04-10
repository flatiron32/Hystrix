package com.netflix.hystrix.contrib.ermapublisher;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCommand;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;

/**
 * Orbitz ERMA (https://erma.github.com) implementation of {@link HystrixMetricsPublisher}.
 */
public class HystrixErmaPublisher extends HystrixMetricsPublisher {

    @Override
    public HystrixMetricsPublisherCommand getMetricsPublisherForCommand(HystrixCommandKey commandKey, 
    		HystrixCommandGroupKey commandGroupKey, HystrixCommandMetrics metrics, HystrixCircuitBreaker circuitBreaker, 
    		HystrixCommandProperties properties) {
        return new HystrixErmaPublisherCommand(commandKey, commandGroupKey, metrics, circuitBreaker, properties);
    }

    @Override
    public HystrixMetricsPublisherThreadPool getMetricsPublisherForThreadPool(HystrixThreadPoolKey threadPoolKey, 
    		HystrixThreadPoolMetrics metrics, HystrixThreadPoolProperties properties) {
        return new HystrixErmaPublisherThreadPool(threadPoolKey, metrics, properties);
    }
}
