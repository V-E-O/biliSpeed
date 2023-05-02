package com.veo.hook.bili.speed;


import android.content.res.Resources;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class MainHook implements IXposedHookLoadPackage {
    public final static String hookPackage0 = "tv.danmaku.bili";
    public final static String hookPackage1 = "com.bilibili.app.in";
    public final static String hookPackage2 = "com.twitter.android";
    private static XC_MethodHook.Unhook first = null;
    private static XC_MethodHook.Unhook second = null;
    private static XC_MethodHook.Unhook third = null;
    private static Field twField = null;
    private static Method twMethod = null;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        boolean bili = false;
        boolean twitter = false;
        if (hookPackage0.equals(lpparam.packageName) || hookPackage1.equals(lpparam.packageName)) {
            bili = true;
            if (!hookPackage0.equals(lpparam.processName))
                return;
        } else if (hookPackage2.equals(lpparam.packageName)) {
            twitter = true;
            if (!hookPackage2.equals(lpparam.processName))
                return;
        }
        if (bili || twitter) {
            XSharedPreferences prefs = new XSharedPreferences("com.veo.hook.bili.speed", "speed");
            final float speedConfig = prefs.getFloat("speed", 1.5f);

            if (twitter) {
                first = XposedHelpers.findAndHookMethod(Resources.class, "getConfiguration", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                        if (stackTraceElements.length == 16) {
                            StackTraceElement stack = stackTraceElements[stackTraceElements.length - 1];
                            if ("android.os.HandlerThread".equals(stack.getClassName()) && "run".equals(stack.getMethodName()) && "onNext".equals(stackTraceElements[8].getMethodName()) && stackTraceElements[5].getClassName().equals(stackTraceElements[6].getClassName())) {
                                String className = stackTraceElements[5].getClassName();
                                String methodName = stackTraceElements[5].getMethodName();
                                Class<?> clz = XposedHelpers.findClass(stackTraceElements[5].getClassName(), lpparam.classLoader);
                                for (Method m : clz.getDeclaredMethods()) {
                                    if (methodName.equals(m.getName())) {
                                        XposedBridge.hookMethod(m, new XC_MethodHook() {
                                            @Override
                                            protected void afterHookedMethod(MethodHookParam param) throws IllegalAccessException, InvocationTargetException {
                                                if (twField == null) {
                                                    for (Field f : param.thisObject.getClass().getDeclaredFields()) {
                                                        if (Modifier.isVolatile(f.getModifiers())) {
                                                            twField = f;
                                                            XposedBridge.log("twField: " + f);
                                                            break;
                                                        }
                                                    }
                                                }
                                                Object c = twField.get(param.thisObject);
                                                if (twMethod == null) {
                                                    twMethod = XposedHelpers.findMethodsByExactParameters(c.getClass(), void.class, double.class)[0];
                                                    XposedBridge.log("twMethod: " + twMethod);
                                                }
                                                twMethod.invoke(c, speedConfig);
                                            }
                                        });

                                        first.unhook();
                                        XposedBridge.log("hooked " + className + "->" + methodName);

                                        // we just hook it, thus we need re-enter the method
                                        param.setThrowable(new Resources.NotFoundException());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });

            } else if (bili) {
                first = XposedHelpers.findAndHookMethod("tv.danmaku.ijk.media.player.AbstractMediaPlayer", lpparam.classLoader, "notifyOnPrepared", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws IllegalAccessException {
                        Field mOnPreparedListener = XposedHelpers.findField(param.thisObject.getClass(), "mOnPreparedListener");
                        Class<?> clz = mOnPreparedListener.get(param.thisObject).getClass();
                        XposedBridge.log("field found AbstractMediaPlayer->mOnPreparedListener");

                        second = XposedHelpers.findAndHookMethod(clz, "onPrepared", "tv.danmaku.ijk.media.player.IMediaPlayer", new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws IllegalAccessException {
                                Field[] fields = param.thisObject.getClass().getDeclaredFields();
                                XposedBridge.log("found fields t count: " + fields.length);
                                Field t = fields[0];
                                Object tObj = t.get(param.thisObject);
                                Class<?> clz = tObj.getClass();
                                XposedBridge.log("chosen fields t: " + clz);
                                Class<?> OnPreparedListener = XposedHelpers.findClass("tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener", lpparam.classLoader);
                                do {
                                    for (Field field : clz.getDeclaredFields()) {
                                        if (field.getType() == OnPreparedListener && !Modifier.isFinal(field.getModifiers())) {
                                            field.setAccessible(true);
                                            clz = field.get(tObj).getClass();
                                            XposedBridge.log("chosen fields p: " + clz);

                                            third = XposedHelpers.findAndHookMethod(clz, "onPrepared", "tv.danmaku.ijk.media.player.IMediaPlayer", new XC_MethodHook() {
                                                @Override
                                                protected void beforeHookedMethod(MethodHookParam param) throws IllegalAccessException {
                                                    Field[] fields = param.thisObject.getClass().getDeclaredFields();
                                                    XposedBridge.log("found fields s0 count: " + fields.length);

                                                    Class<?> s0 = param.thisObject.getClass().getDeclaredFields()[0].get(param.thisObject).getClass();
                                                    XposedBridge.log("chosen fields s0: " + s0);

                                                    for (Method method : s0.getDeclaredMethods()) {
                                                        if (void.class != method.getReturnType())
                                                            continue;
                                                        if (1 != method.getParameterCount())
                                                            continue;
                                                        if (float.class != method.getParameterTypes()[0])
                                                            continue;

                                                        XposedBridge.log("chosen b: " + method);
                                                        XposedBridge.hookMethod(method, new XC_MethodHook() {
                                                            @Override
                                                            protected void beforeHookedMethod(MethodHookParam param) {
                                                                float speed = (float) param.args[0];
                                                                if (speed == 1.0f) {
                                                                    StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                                                                    for (int i = 4; i <= 7 && i < stackTraceElements.length; i++) {
                                                                        // return on manually speed tweak
                                                                        if (stackTraceElements[i].getClassName().startsWith("com.bilibili.video."))
                                                                            return;
                                                                    }
                                                                    param.args[0] = speedConfig;
                                                                }
                                                            }
                                                        });
                                                        first.unhook();
                                                        second.unhook();
                                                        third.unhook();

                                                        XposedBridge.log("hooked s0->b");
                                                        break;
                                                    }
                                                }
                                            });
                                            XposedBridge.log("hooked p->onPrepared");

                                            break;
                                        }
                                    }
                                } while ((clz = clz.getSuperclass()) != null);
                            }
                        });
                        XposedBridge.log("hooked mOnPreparedListener->onPrepared");

                    }
                });
                XposedBridge.log("hooked AbstractMediaPlayer->notifyOnPrepared");
            }
        }
    }
}
