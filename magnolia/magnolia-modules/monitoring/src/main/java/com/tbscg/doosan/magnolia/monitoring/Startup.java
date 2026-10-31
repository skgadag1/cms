package com.tbscg.doosan.magnolia.monitoring;

import java.net.URL;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.plugins.EC2Plugin;
import com.amazonaws.xray.plugins.EKSPlugin;
import com.amazonaws.xray.slf4j.SLF4JSegmentListener;
import com.amazonaws.xray.strategy.LogErrorContextMissingStrategy;
import com.amazonaws.xray.strategy.sampling.LocalizedSamplingStrategy;

public class Startup implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		AWSXRayRecorderBuilder builder = AWSXRayRecorderBuilder.standard()
				.withSegmentListener(new SLF4JSegmentListener())
				.withContextMissingStrategy(new LogErrorContextMissingStrategy())
				.withPlugin(new EC2Plugin())
				.withPlugin(new EKSPlugin("db-dxp-eks"));

		URL ruleFile = Startup.class.getResource("/sampling-rules.json");
		builder.withSamplingStrategy(new LocalizedSamplingStrategy(ruleFile));

		AWSXRay.setGlobalRecorder(builder.build());
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}
}
