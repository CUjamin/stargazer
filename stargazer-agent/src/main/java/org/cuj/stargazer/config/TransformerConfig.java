package org.cuj.stargazer.config;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
public class TransformerConfig {
    // 被处理的方法列表
    private final Map<String, List<String>> methodMap = new HashMap<>();

    private boolean switchIsOpen = false;

    public void init() {
        List<String> methodNameList = lodaMethod();
        methodNameList.forEach(this::add);
        loadSwitch();
    }

    public void loadSwitch() {
        //1.创建一个timer实例
        Timer timer = new Timer();
        //2.创建一个MyTimerTask实例
        TimerTask myTimerTask = new TimerTask() {
            @Override
            public void run() {
                Properties properties = new Properties();
                // 使用ClassLoader加载properties配置文件生成对应的输入流
                InputStream in = null;
                try {
                    in = new FileInputStream("config/stargazer_switch.properties");
                    // 使用properties对象加载输入流
                    properties.load(in);
                    //获取key对应的value值
                    String switchStr = properties.getProperty("switch");
                    switchIsOpen = "open".equals(switchStr);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (null != in) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        //3.通过timer定时定频率调用mytimertask的业务逻辑

        //即第一次执行是在当前时间两秒之后，之后每隔10秒钟执行一次
        timer.schedule(myTimerTask, 2000L, 10*1000L);
    }


    private List<String> lodaMethod() {
        List<String> methodNameList = new ArrayList<>();
        SAXReader reader = new SAXReader();
        try {
            //2.加载xml
            Document document = reader.read(new File("config/stargazer_method.xml"));
            Element rootElement = document.getRootElement();
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

    public boolean getSwitchIsOpen() {
        return switchIsOpen;
    }

    public boolean containsClass(String className) {
        return methodMap.containsKey(className);
    }

    public List<String> getMethodList(String className) {
        return methodMap.get(className);
    }
}
