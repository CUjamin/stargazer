package org.cuj.stargazer.mainagent;

import lombok.extern.log4j.Log4j2;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

@Log4j2
public class PreMainAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        log.info("agentArgs : {}", agentArgs);
        inst.addTransformer(new DefineTransformer(), true);
    }

    static class DefineTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            log.info("premain load Class:{}", className);
            return classfileBuffer;
        }
    }
}
