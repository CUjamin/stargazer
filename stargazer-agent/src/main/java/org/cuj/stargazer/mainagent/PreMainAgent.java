package org.cuj.stargazer.mainagent;

import lombok.extern.log4j.Log4j2;
import org.cuj.stargazer.common.LogConstant;
import org.cuj.stargazer.transformer.MethodTransformer;
import java.lang.instrument.Instrumentation;

@Log4j2
public class PreMainAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        log.info(LogConstant.MONITOR+"agentArgs : {}", agentArgs);
        inst.addTransformer(new MethodTransformer());
    }
}
