package com.netflix.hystrix.contrib.ermapublisher;

import java.util.Timer;
import java.util.TimerTask;

import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;
import com.orbitz.monitoring.api.monitor.ValueMonitor;

/**
 * Implementation of {@link HystrixMetricsPublisherThreadPool} using Orbitz
 * ERMA (https://github.com/erma/erma)
 */
public class HystrixErmaPublisherThreadPool implements
		HystrixMetricsPublisherThreadPool {
	private final HystrixThreadPoolKey key;
	private final HystrixThreadPoolMetrics metrics;
	private final HystrixThreadPoolProperties properties;
	private static final String METRIC_GROUP = "HystrixThreadPool";

	public HystrixErmaPublisherThreadPool(HystrixThreadPoolKey threadPoolKey,
			HystrixThreadPoolMetrics metrics,
			HystrixThreadPoolProperties properties) {
		this.key = threadPoolKey;
		this.metrics = metrics;
		this.properties = properties;
	}

	@Override
	public void initialize() {
		new Timer(true).scheduleAtFixedRate(new HystrixThreadPoolTimerTask(),
				0, 60 * 1000);
	}

	private final class HystrixThreadPoolTimerTask extends TimerTask {
		public void run() {
			new ValueMonitor(createMetricName("threadActiveCount"), metrics.getCurrentActiveCount().intValue()).fire();
			new ValueMonitor(createMetricName("completedTaskCount"),
					metrics.getCurrentCompletedTaskCount().intValue()).fire();
			new ValueMonitor(createMetricName("largestPoolSize"),
					metrics.getCurrentLargestPoolSize().intValue()).fire();
			new ValueMonitor(createMetricName("totalTaskCount"), metrics.getCurrentTaskCount().intValue()).fire();
			new ValueMonitor(createMetricName("queueSize"), metrics.getCurrentQueueSize().intValue()).fire();
			new ValueMonitor(createMetricName("rollingMaxActiveThreads"),
					metrics.getRollingMaxActiveThreads()).fire();
			new ValueMonitor(createMetricName("countThreadsExecuted"),
					metrics.getCumulativeCountThreadsExecuted()).fire();
			new ValueMonitor(createMetricName("rollingCountThreadsExecuted"),
					metrics.getRollingCountThreadsExecuted()).fire();
			new ValueMonitor(createMetricName("propertyValue_corePoolSize"), properties.coreSize().get()).fire();
			new ValueMonitor(createMetricName("propertyValue_keepAliveTimeInMinutes"),
					properties.keepAliveTimeMinutes().get()).fire();
			new ValueMonitor(createMetricName("propertyValue_queueSizeRejectionThreshold"),
					properties.queueSizeRejectionThreshold().get()).fire();
			new ValueMonitor(createMetricName("propertyValue_maxQueueSize"),
					properties.maxQueueSize().get()).fire();
		}

		private String createMetricName(String string) {
			return METRIC_GROUP + "." + key.name() + "." + string;
		}

	}

}
