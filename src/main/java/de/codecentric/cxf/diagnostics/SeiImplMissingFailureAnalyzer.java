package de.codecentric.cxf.diagnostics;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * FailureAnalyzer to show custom Failure Message, if a SEI Implementation is missing
 * (which is mandatory for autodetection and instantiation of the CXF endpoint(s))
 *
 * @author jonashackt
 */
public class SeiImplMissingFailureAnalyzer extends AbstractFailureAnalyzer<SeiImplClassNotFoundException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, SeiImplClassNotFoundException cause) {
        return new FailureAnalysis(SeiImplClassNotFoundException.MESSAGE,
                String.format("Build a Class that implements your Service Endpoint Interface (SEI): '%s' and try again!", cause.getNotFoundClassName()), cause);
    }

}
