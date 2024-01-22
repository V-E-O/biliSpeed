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
    public final static String hookPackageBili0 = "tv.danmaku.bili";
    public final static String hookPackageBili1 = "com.bilibili.app.in";
    public final static String hookPackageTw = "com.twitter.android";
    public final static String hookPackageDy0 = "com.ss.android.ugc.aweme";
    public final static String hookPackageDy1 = "com.ss.android.ugc.aweme.lite";
    public final static String hookPackageDy2 = "com.ss.android.ugc.live";
    private final static XSharedPreferences prefs = new XSharedPreferences("com.veo.hook.bili.speed", "speed");
    private static XC_MethodHook.Unhook first = null;
    private static XC_MethodHook.Unhook second = null;
    private static XC_MethodHook.Unhook third = null;
    private static Field twField = null;
    private static Method twMethod = null;

    private static float getSpeedConfig() {
        prefs.reload();
        return prefs.getFloat("speed", 1.5f);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        boolean bili = false;
        boolean twitter = false;
        boolean douyin = false;
        if (hookPackageBili0.equals(lpparam.packageName) || hookPackageBili1.equals(lpparam.packageName)) {
            bili = true;
            if (!hookPackageBili0.equals(lpparam.processName) && !hookPackageBili1.equals(lpparam.processName))
                return;
        } else if (hookPackageTw.equals(lpparam.packageName)) {
            twitter = true;
            if (!hookPackageTw.equals(lpparam.processName)) return;
        } else if (hookPackageDy0.equals(lpparam.packageName) || hookPackageDy1.equals(lpparam.packageName) || hookPackageDy2.equals(lpparam.packageName)) {
            douyin = true;
            if (!hookPackageDy0.equals(lpparam.processName) && !hookPackageDy1.equals(lpparam.processName) && !hookPackageDy2.equals(lpparam.processName))
                return;
        }
        if (bili || twitter || douyin) {
            if (twitter) {
                first = XposedHelpers.findAndHookMethod(Resources.class, "getConfiguration", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                        if (stackTraceElements.length >= 14 && stackTraceElements.length < 21) {
                            if ("android.os.HandlerThread".equals(stackTraceElements[stackTraceElements.length - 1].getClassName()) && "run".equals(stackTraceElements[stackTraceElements.length - 1].getMethodName())) {
                                for (int i = 0; i < stackTraceElements.length; i++) {
                                    if ("getConfiguration".equals(stackTraceElements[i].getMethodName())) {
                                        if (stackTraceElements[i + 1].getClassName().equals(stackTraceElements[i + 2].getClassName()) && "onNext".equals(stackTraceElements[i + 4].getMethodName())) {
                                            String className = stackTraceElements[i + 1].getClassName();
                                            String methodName = stackTraceElements[i + 1].getMethodName();
                                            Class<?> clz = XposedHelpers.findClass(stackTraceElements[i + 1].getClassName(), lpparam.classLoader);
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
                                                            twMethod.invoke(c, getSpeedConfig());
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
                                t.setAccessible(true);
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

                                                    Field field = param.thisObject.getClass().getDeclaredFields()[0];
                                                    field.setAccessible(true);
                                                    Class<?> s0 = field.get(param.thisObject).getClass();
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
                                                                    param.args[0] = getSpeedConfig();
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
            } else if (douyin) {
                XposedHelpers.findAndHookMethod("com.ss.android.ugc.aweme.video.simplayer.SimPlayer", lpparam.classLoader, "setSpeed", float.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        float speed = (float) param.args[0];
//                        XposedBridge.log("speed: " + speed);
                        if (speed == 1.0f) {
                            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                            for (int i = 16; i <= 19 && i < stackTraceElements.length; i++) {
                                // return on manually speed tweak
                                if (stackTraceElements[i].getMethodName().equals("dispatchTouchEvent")) {
//                                    XposedBridge.log("i: " + i);
                                    return;
                                }
                            }
                            param.args[0] = getSpeedConfig();
                        }
                    }
                });
                XposedBridge.log("hooked setSpeed");

                // 彩蛋：长按加速
//                XposedHelpers.findAndHookMethod("com.bytedance.ies.abmock.ABManager", lpparam.classLoader, "getIntValue", boolean.class, String.class, int.class, int.class, new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) {
//                            if ("long_press_fast_speed_enabled_scene".equals(param.args[1])) {
//                                param.setResult(Integer.valueOf(1));
//                            } else if ("long_press_fast_speed_screen_scale".equals(param.args[1])) {
//                                param.setResult(Integer.valueOf(40));
//                            }
//                        }
//                    }
//                );
            }
        }
    }
}
