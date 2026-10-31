package com.tbscg.doosan.magnolia.dxp.core.util;

import java.util.function.Function;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.TraceID;
import org.safehaus.uuid.Logger;

/**
 * Utility class used for Magnolia-based tracing.
 */
public class TracingUtil {
    public static String AWS_XRAY_SEGMENT_NAME = "Magnolia CMS";

    public static <R> R callWithTracing(Function<TraceID, R> function) {
        return callWithTracing(AWS_XRAY_SEGMENT_NAME, function);
    }

    public static <R> R callWithTracing(String segmentName, Function<TraceID, R> function) {
        AWSXRayRecorder globalRecorder = AWSXRay.getGlobalRecorder();
        Segment segment = globalRecorder.beginSegment(segmentName);
        try {
            Logger.logInfo(String.format("XRay recording started for segment %s", segmentName));
            return function.apply(segment.getTraceId());
        } catch (Exception ex) {
            Logger.logError(String.format("XRay recording error: %s", ex.getMessage()));
            segment.addException(ex);
            throw ex;
        } finally {
            Logger.logInfo(String.format("XRay recording ended for segment  %s", segmentName));
            globalRecorder.endSegment();
        }
    }
}