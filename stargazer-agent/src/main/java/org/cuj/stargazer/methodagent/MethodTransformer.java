package org.cuj.stargazer.methodagent;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import org.cuj.stargazer.common.LogConstant;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.security.ProtectionDomain;
import java.util.*;


@Slf4j
public class MethodTransformer implements ClassFileTransformer {
    final static String prefix = "\nlong startTime = System.currentTimeMillis();\n";
    final static String postfix = "\nlong endTime = System.currentTimeMillis();\n";
    final static String ifStartFix = "\nif(org.cuj.stargazer.methodagent.MethodTransformer.getSwitchIsOpen()){\n";
    final static String ifEndFix = "\n}\n";

    // 被处理的方法列表
    final static Map<String, List<String>> methodMap = new HashMap<>();

    private static boolean switchIsOpen = false;

    public static boolean getSwitchIsOpen(){
        return switchIsOpen;
    }

    public MethodTransformer() {
        List<String> methodNameList = lodaMethod();
        methodNameList.forEach(this::add);
    }

    private List<String> lodaMethod() {
        List<String> methodNameList = new ArrayList<>();
        SAXReader reader = new SAXReader();
        try {
            //2.加载xml
            Document document = reader.read(new File("config/stargazer.xml"));
            Element rootElement = document.getRootElement();
            Element switchIsOpenElement = rootElement.element("switch");
            if(null!=switchIsOpenElement){
                switchIsOpen = "open".equals(switchIsOpenElement.getTextTrim());
            }

            List<Element> classMethodElementList = rootElement.elements("class");

            classMethodElementList.forEach(classMethodElement -> {
                String packageName = classMethodElement.attributeValue("package");
                String className = classMethodElement.attributeValue("class_name");
                List<Element> methodElementList = classMethodElement.elements("method");
                methodElementList.forEach(methodElement -> {
                    String methodName = packageName + "." + className + "." + methodElement.getTextTrim();
                    methodNameList.add(methodName);
                    log.info("methodName=" + methodName);
                });
            });
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return methodNameList;
    }

    private void add(String methodString) {
        String className = methodString.substring(0, methodString.lastIndexOf("."));
        String methodName = methodString.substring(methodString.lastIndexOf(".") + 1);
        List<String> list = methodMap.computeIfAbsent(className, k -> new ArrayList<>());
        list.add(methodName);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        className = className.replace("/", ".");
        if (methodMap.containsKey(className)) {// 判断加载的class的包路径是不是需要监控的类
            CtClass ctclass;
            try {
                ctclass = ClassPool.getDefault().get(className);// 使用全称,用于取得字节码类<使用javassist>
                for (String methodName : methodMap.get(className)) {
                    String outputStr = "\nlog.info(\"" + LogConstant.MONITOR + "this method " + methodName
                            + " cost:\" +(endTime - startTime) +\"ms.\");";

                    CtMethod ctmethod = ctclass.getDeclaredMethod(methodName);// 得到这方法实例
                    String newMethodName = methodName + "$old";// 新定义一个方法叫做比如sayHello$old
                    ctmethod.setName(newMethodName);// 将原来的方法名字修改

                    // 创建新的方法，复制原来的方法，名字为原来的名字
                    CtMethod newMethod = CtNewMethod.copy(ctmethod, methodName, ctclass, null);

                    // 构建新的方法体

                    String bodyStr = "{" +
                            prefix +
                            newMethodName + "($$);\n" +// 调用原有代码，类似于method();($$)表示所有的参数
                            postfix +
                            ifStartFix +
                            outputStr +
                            ifEndFix +
                            "}";
                    newMethod.setBody(bodyStr);// 替换新方法
                    ctclass.addMethod(newMethod);// 增加新方法
                }
                return ctclass.toBytecode();
            } catch (Exception e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }
}
