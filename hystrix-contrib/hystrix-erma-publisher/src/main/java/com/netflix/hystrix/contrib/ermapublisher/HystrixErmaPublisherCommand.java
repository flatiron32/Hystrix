package com.netflix.hystrix.contrib.ermapublisher;

import java.util.Timer;
import java.util.TimerTask;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCommand;
import com.netflix.hystrix.util.HystrixRollingNumberEvent;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.monitor.ValueMonitor;

/**
 * Implementation of {@link HystrixMetricsPublisherCommand} using Orbitz Erma.
 * (https://github.com/erma/erma)
 */
public class HystrixErmaPublisherCommand implements
		HystrixMetricsPublisherCommand {
	private final HystrixCommandKey key;
	private final HystrixCommandGroupKey commandGroupKey;
	private final HystrixCommandMetrics metrics;
	private final HystrixCircuitBreaker circuitBreaker;
	private final HystrixCommandProperties properties;
	private static final String METRIC_GROUP = "HystrixCommand";

	public HystrixErmaPublisherCommand(HystrixCommandKey commandKey,
			HystrixCommandGroupKey commandGroupKey,
			HystrixCommandMetrics metrics,
			HystrixCircuitBreaker circuitBreaker,
			HystrixCommandProperties properties) {
		this.key = commandKey;
		this.commandGroupKey = commandGroupKey;
		this.metrics = metrics;
		this.circuitBreaker = circuitBreaker;
		this.properties = properties;
	}

	@Override
	public void initialize() {
		new Timer(true).scheduleAtFixedRate(new HystrixCommandTimerTask(),
				0, 60 * 1000);
	}

	private final class HystrixCommandTimerTask extends TimerTask {

		@Override
		public void run() {
			if (circuitBreaker.isOpen()) {
				new EventMonitor(createMetricName("circuitBreakerOpen")).fire();
			}
			if (properties.circuitBreakerForceOpen().get()) {
				new EventMonitor(createMetricName("propertyValue.circuitBreakerForceOpen")).fire();
			}
			if (properties.circuitBreakerForceClosed().get()) {
				new EventMonitor(createMetricName("propertyValue.circuitBreakerForceClosed")).fire();
			}

			// the number of executionSemaphorePermits in use right now
			new ValueMonitor(createMetricName("executionSemaphorePermitsInUse"), metrics.getCurrentConcurrentExecutionCount()).fire();
			new ValueMonitor(createMetricName("errorCount"),                     metrics.getHealthCounts().getErrorCount()).fire();
			new ValueMonitor(createMetricName("errorPercentage"),                metrics.getHealthCounts().getErrorPercentage()).fire();
			new ValueMonitor(createMetricName("totalRequests"),                  metrics.getHealthCounts().getTotalRequests()).fire();
			new ValueMonitor(createMetricName("latencyExecute.mean"),            metrics.getExecutionTimeMean()).fire();
			new ValueMonitor(createMetricName("latencyExecute.percentile_5"),    metrics.getExecutionTimePercentile(5)).fire();
			new ValueMonitor(createMetricName("latencyExecute.percentile_25"),   metrics.getExecutionTimePercentile(25)).fire();
			new ValueMonitor(createMetricName("latencyExecute.percentile_50"),   metrics.getExecutionTimePercentile(50)).fire();
			new ValueMonitor(createMetricName("latencyExecute.percentile_75"),   metrics.getExecutionTimePercentile(75)).fire();
			new ValueMonitor(createMetricName("latencyExecute.percentile_90"),   metrics.getExecutionTimePercentile(90)).fire();
			new ValueMonitor(createMetricName("latencyExecute.percentile_99"),   metrics.getExecutionTimePercentile(99)).fire();
			new ValueMonitor(createMetricName("latencyExecute.percentile_99_5"), metrics.getExecutionTimePercentile(99.5)).fire();
			new ValueMonitor(createMetricName("latencyTotal.mean"),              metrics.getTotalTimeMean()).fire();
			new ValueMonitor(createMetricName("latencyTotal.percentile_5"),      metrics.getTotalTimePercentile(5)).fire();
			new ValueMonitor(createMetricName("latencyTotal.percentile_25"),     metrics.getTotalTimePercentile(25)).fire();
			new ValueMonitor(createMetricName("latencyTotal.percentile_50"),     metrics.getTotalTimePercentile(50)).fire();
			new ValueMonitor(createMetricName("latencyTotal.percentile_75"),     metrics.getTotalTimePercentile(75)).fire();
			new ValueMonitor(createMetricName("latencyTotal.percentile_90"),     metrics.getTotalTimePercentile(90)).fire();
			new ValueMonitor(createMetricName("latencyTotal.percentile_99"),     metrics.getTotalTimePercentile(99)).fire();
			new ValueMonitor(createMetricName("latencyTotal.percentile_99_5"),   metrics.getTotalTimePercentile(99.5)).fire();
			
			new ValueMonitor(createMetricName("propertyValue.rollingStatisticalWindowInMilliseconds"),  
					properties.metricsRollingStatisticalWindowInMilliseconds().get()).fire();
			new ValueMonitor(createMetricName("propertyValue.circuitBreakerRequestVolumeThreshold"),    
					properties.circuitBreakerRequestVolumeThreshold().get()).fire();
			new ValueMonitor(createMetricName("propertyValue.circuitBreakerSleepWindowInMilliseconds"), 
					properties.circuitBreakerSleepWindowInMilliseconds().get()).fire();
			new ValueMonitor(createMetricName("propertyValue.circuitBreakerErrorThresholdPercentage"),  
					properties.circuitBreakerErrorThresholdPercentage().get()).fire();
			new ValueMonitor(createMetricName("propertyValue.executionIsolationThreadTimeoutInMilliseconds"),  
					properties.executionIsolationThreadTimeoutInMilliseconds().get()).fire();
			new ValueMonitor(createMetricName("propertyValue.executionIsolationSemaphoreMaxConcurrentRequests"),  
					properties.executionIsolationSemaphoreMaxConcurrentRequests().get()).fire();
			new ValueMonitor(createMetricName("propertyValue.fallbackIsolationSemaphoreMaxConcurrentRequests"),  
					properties.fallbackIsolationSemaphoreMaxConcurrentRequests().get()).fire();
			new ValueMonitor(createMetricName("propertyValue.circuitBreakerForceOpen"),  
					properties.fallbackIsolationSemaphoreMaxConcurrentRequests().get()).fire();
			
			if (properties.metricsRollingPercentileEnabled().get()) {
				new EventMonitor(createMetricName("propertyValue.metricsRollingPercentileEnabled")).fire();
			}
			if (properties.requestCacheEnabled().get()) {
				new EventMonitor(createMetricName("propertyValue.requestCacheEnabled")).fire();
			}
			if (properties.requestLogEnabled().get()) {
				new EventMonitor(createMetricName("propertyValue.requestLogEnabled")).fire();
			}

			// cumulative counts
			createCumulativeCountForEvent("countCollapsedRequests",
					HystrixRollingNumberEvent.COLLAPSED);
			createCumulativeCountForEvent("countExceptionsThrown",
					HystrixRollingNumberEvent.EXCEPTION_THROWN);
			createCumulativeCountForEvent("countFailure",
					HystrixRollingNumberEvent.FAILURE);
			createCumulativeCountForEvent("countFallbackFailure",
					HystrixRollingNumberEvent.FALLBACK_FAILURE);
			createCumulativeCountForEvent("countFallbackRejection",
					HystrixRollingNumberEvent.FALLBACK_REJECTION);
			createCumulativeCountForEvent("countFallbackSuccess",
					HystrixRollingNumberEvent.FALLBACK_SUCCESS);
			createCumulativeCountForEvent("countResponsesFromCache",
					HystrixRollingNumberEvent.RESPONSE_FROM_CACHE);
			createCumulativeCountForEvent("countSemaphoreRejected",
					HystrixRollingNumberEvent.SEMAPHORE_REJECTED);
			createCumulativeCountForEvent("countShortCircuited",
					HystrixRollingNumberEvent.SHORT_CIRCUITED);
			createCumulativeCountForEvent("countSuccess",
					HystrixRollingNumberEvent.SUCCESS);
			createCumulativeCountForEvent("countThreadPoolRejected",
					HystrixRollingNumberEvent.THREAD_POOL_REJECTED);
			createCumulativeCountForEvent("countTimeout",
					HystrixRollingNumberEvent.TIMEOUT);

			// rolling counts
			createRollingCountForEvent("rollingCountCollapsedRequests",
					HystrixRollingNumberEvent.COLLAPSED);
			createRollingCountForEvent("rollingCountExceptionsThrown",
					HystrixRollingNumberEvent.EXCEPTION_THROWN);
			createRollingCountForEvent("rollingCountFailure",
					HystrixRollingNumberEvent.FAILURE);
			createRollingCountForEvent("rollingCountFallbackFailure",
					HystrixRollingNumberEvent.FALLBACK_FAILURE);
			createRollingCountForEvent("rollingCountFallbackRejection",
					HystrixRollingNumberEvent.FALLBACK_REJECTION);
			createRollingCountForEvent("rollingCountFallbackSuccess",
					HystrixRollingNumberEvent.FALLBACK_SUCCESS);
			createRollingCountForEvent("rollingCountResponsesFromCache",
					HystrixRollingNumberEvent.RESPONSE_FROM_CACHE);
			createRollingCountForEvent("rollingCountSemaphoreRejected",
					HystrixRollingNumberEvent.SEMAPHORE_REJECTED);
			createRollingCountForEvent("rollingCountShortCircuited",
					HystrixRollingNumberEvent.SHORT_CIRCUITED);
			createRollingCountForEvent("rollingCountSuccess",
					HystrixRollingNumberEvent.SUCCESS);
			createRollingCountForEvent("rollingCountThreadPoolRejected",
					HystrixRollingNumberEvent.THREAD_POOL_REJECTED);
			createRollingCountForEvent("rollingCountTimeout",
					HystrixRollingNumberEvent.TIMEOUT);
		}

		private String createMetricName(String string) {
			return METRIC_GROUP + "." + commandGroupKey.name() + "." + key.name() + "." + string;
		}
		
		protected void createRollingCountForEvent(String name,
				final HystrixRollingNumberEvent event) {
			new ValueMonitor(createMetricName(name), metrics.getRollingCount(event)).fire();
		}
		
		protected void createCumulativeCountForEvent(String name,
				final HystrixRollingNumberEvent event) {
			new ValueMonitor(createMetricName(name), metrics.getCumulativeCount(event)).fire();
		}

	}
}
