package com.devpulse.starter;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;

/**
 * Custom Logback appender that ships ERROR logs to DevPulse.
 *
 * WHY A LOGBACK APPENDER IN ADDITION TO THE EXCEPTION HANDLER:
 * The exception handler catches unhandled exceptions at the
 * HTTP layer — anything thrown from a @RestController.
 *
 * But some errors happen outside HTTP request scope:
 * - Scheduled tasks (@Scheduled)
 * - Async methods (@Async)
 * - Kafka consumers
 * - Background threads
 *
 * These never hit the exception handler. But they DO log.
 * The Logback appender catches ERROR-level log events
 * regardless of where in the app they originate.
 *
 * Together: exception handler + logback appender = complete coverage.
 *
 * HOW IT WORKS:
 * Logback calls append() for every log event.
 * We filter for ERROR level only, then ship to DevPulse.
 * The consuming app configures this in logback-spring.xml.
 */
public class DevPulseLogbackAppender  extends AppenderBase<ILoggingEvent> {

    private DevPulseClient devPulseClient;
    public void setDevPulseClient(DevPulseClient devPulseClient) {
        this.devPulseClient = devPulseClient;
    }

    @Override
    protected void append(ILoggingEvent event) { // ILoggingEvent receives all the logs info, warm , error , etc i
        // Only process ERROR level logs , WARM INFO all are ignored
        if(!Level.ERROR.equals(event.getLevel())){
            return ;
        }

        /*checking if client exists
        * Because if appender initializes before Spring injects: we will get NullPointerException inside logger*/
        if(devPulseClient == null){
            return;
        }

        String errorMessage = event.getFormattedMessage();
        String stackTrace = extractStackTrace(event);

        devPulseClient.reportError(errorMessage,stackTrace,"ERROR");
    }

    private String extractStackTrace(ILoggingEvent event){

        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if(throwableProxy == null) return "";
        return ThrowableProxyUtil.asString(throwableProxy);

    }
}
