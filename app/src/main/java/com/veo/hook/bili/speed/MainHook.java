package com.veo.hook.bili.speed;


import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;

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
    public final static String hookPackageDy3 = "com.ss.android.ugc.aweme.mobile";
    public final static String hookPackageRed = "com.xingin.xhs";
    public final static String hookPackageWb = "com.sina.weibo";
    public final static String hookPackageIg0 = "com.instagram.android";
    public final static String hookPackageIg1 = "com.instander.android";
    public final static String hookPackageTg = "org.telegram.messenger";
    public final static String hookPackageWx = "com.tencent.mm";
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
    private static boolean hasSpeedConfigChanged() {
        return prefs.hasFileChanged();
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        boolean bili = false;
        boolean twitter = false;
        boolean douyin = false;
        boolean red = false;
        boolean wb = false;
        boolean ig = false;
        boolean tg = false;
        boolean wx = false;

        if (hookPackageBili0.equals(lpparam.packageName) || hookPackageBili1.equals(lpparam.packageName)) {
            bili = true;
            if (!hookPackageBili0.equals(lpparam.processName) && !hookPackageBili1.equals(lpparam.processName))
                return;
        } else if (hookPackageTw.equals(lpparam.packageName)) {
            twitter = true;
            if (!hookPackageTw.equals(lpparam.processName)) return;
        } else if (hookPackageDy0.equals(lpparam.packageName) || hookPackageDy1.equals(lpparam.packageName) || hookPackageDy2.equals(lpparam.packageName) || hookPackageDy3.equals(lpparam.packageName)) {
            douyin = true;
            if (!hookPackageDy0.equals(lpparam.processName) && !hookPackageDy1.equals(lpparam.processName) && !hookPackageDy2.equals(lpparam.processName) && !hookPackageDy3.equals(lpparam.processName))
                return;
        } else if (hookPackageRed.equals(lpparam.packageName)) {
            red = true;
            if (!hookPackageRed.equals(lpparam.processName))
                return;
        } else if (hookPackageWb.equals(lpparam.packageName)) {
            wb = true;
            if (!hookPackageWb.equals(lpparam.processName))
                return;
        } else if (hookPackageIg0.equals(lpparam.packageName) || hookPackageIg1.equals(lpparam.packageName)) {
            ig = true;
            if (!hookPackageIg0.equals(lpparam.processName) && !hookPackageIg1.equals(lpparam.processName))
                return;
        } else if (hookPackageTg.equals(lpparam.packageName)) {
            tg = true;
            if (!hookPackageTg.equals(lpparam.processName))
                return;
        } else if (hookPackageWx.equals(lpparam.packageName)) {
            wx = true;
            if (!hookPackageWx.equals(lpparam.processName))
                return;
        }
        if (bili || twitter || douyin || red || wb || ig || tg || wx) {
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
                // bilibili configured to tv.danmaku.ijk.media.player.IjkMediaPlayer
//                float[] speedConfig = {getSpeedConfig()};
//
//                XposedHelpers.findAndHookMethod("tv.danmaku.ijk.media.player.IjkMediaPlayer", lpparam.classLoader, "start", new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) {
//                        Object thisObject = param.thisObject;
//                        if (hasSpeedConfigChanged()) {
//                            speedConfig[0] = getSpeedConfig();
//                        }
//                        XposedHelpers.callMethod(thisObject, "setSpeed", speedConfig[0]);
//                        XposedBridge.log("bili setSpeed: " + speedConfig[0]);
//                    }
//                });
//
//                XposedHelpers.findAndHookMethod("tv.danmaku.ijk.media.player.IjkMediaPlayer", lpparam.classLoader, "setSpeed", float.class, new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) {
//                        XposedBridge.log(String.valueOf(param.args[0]));
//                        XposedBridge.log(Log.getStackTraceString(new Throwable()));
//
//                        if ((float) param.args[0] != speedConfig[0]) {
//                            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
//                            for (int i = 4; i < stackTraceElements.length; i++) {
//                                // return on manually speed tweak
//                                if (stackTraceElements[i].getClassName().equals("com.bilibili.player.tangram.basic.PlaySpeedManagerImpl")) {
//                                    XposedBridge.log("bili manual speed: " + param.args[0]);
//                                    speedConfig[0] = (float) param.args[0];
//                                    return;
//                                }
//                            }
//                        }
//                    }
//                });

//                XposedHelpers.findAndHookMethod("tv.danmaku.ijk.media.player.IjkMediaPlayer$EventHandler", lpparam.classLoader, "handleMessage", Message.class, new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) {
//                        Message msg = (Message) param.args[0];
//                        XposedBridge.log(String.valueOf(msg.what));
//                    }
//                });
//                float[] speedConfig = {getSpeedConfig()};
//
//                Class<?> clz = XposedHelpers.findClass("tv.danmaku.ijk.media.player.IjkMediaPlayer", lpparam.classLoader);
//                Method method = XposedHelpers.findMethodExact(clz, "setSpeed", float.class);
//                XposedBridge.hookMethod(method, new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) {
//                        XposedBridge.log(Log.getStackTraceString(new Throwable()));
//
//                        if ((float) param.args[0] != speedConfig[0]) {
//                            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
//                            for (int i = 4; i < stackTraceElements.length; i++) {
//                                // return on manually speed tweak
//                                if (stackTraceElements[i].getClassName().equals("com.bilibili.player.tangram.basic.PlaySpeedManagerImpl")) {
//                                    XposedBridge.log("bili manual speed: " + param.args[0]);
//                                    speedConfig[0] = (float) param.args[0];
//                                    return;
//                                }
//                            }
//                        }
//                    }
//                });
//
//                XposedBridge.log("bili hooked setSpeed");

//                XposedHelpers.findAndHookMethod("tv.danmaku.ijk.media.player.IjkMediaPlayer", lpparam.classLoader, "start", new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) {
//                        Object thisObject = param.thisObject;
//                        if (hasSpeedConfigChanged()) {
//                            speedConfig[0] = getSpeedConfig();
//                        }
//                        try {
//                            method.invoke(thisObject,  speedConfig[0]);
//                        } catch (IllegalAccessException e) {
//                            // should not happen
//                            XposedBridge.log(e);
//                            throw new IllegalAccessError(e.getMessage());
//                        } catch (InvocationTargetException e) {
//                            throw new RuntimeException(e);
//                        }
//                        XposedBridge.log("bili setSpeed: " + speedConfig[0]);
//                    }
//                });
//                XposedBridge.log("bili hooked resume");

                first = XposedHelpers.findAndHookMethod("tv.danmaku.ijk.media.player.AbstractMediaPlayer", lpparam.classLoader, "notifyOnInfo", int.class, int.class, Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws IllegalAccessException {
                        if (second == null) {
                            Field mOnPreparedListener = XposedHelpers.findField(param.thisObject.getClass(), "mOnPreparedListener");
                            Class<?> clz = mOnPreparedListener.get(param.thisObject).getClass();
                            XposedBridge.log("field found AbstractMediaPlayer->mOnPreparedListener");

                            second = XposedHelpers.findAndHookMethod(clz, "onPrepared", "tv.danmaku.ijk.media.player.IMediaPlayer", new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws IllegalAccessException {
                                    if (third == null ) {
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

                                                                float[] speedConfig = {getSpeedConfig()};

                                                                XposedBridge.hookMethod(method, new XC_MethodHook() {
                                                                    @Override
                                                                    protected void afterHookedMethod(MethodHookParam param) {
                                                                        if ((float) param.args[0] != speedConfig[0]) {
                                                                            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                                                                            for (int i = 4; i < stackTraceElements.length; i++) {
                                                                                // return on manually speed tweak
                                                                                if (stackTraceElements[i].getClassName().equals("com.bilibili.player.tangram.basic.PlaySpeedManagerImpl")) {
                                                                                    XposedBridge.log("bili manual speed: " + param.args[0]);
                                                                                    speedConfig[0] = (float) param.args[0];
                                                                                    return;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                });
                                                                XposedBridge.log("bili hooked setSpeed");

                                                                XposedHelpers.findAndHookMethod(s0, "resume", new XC_MethodHook() {
                                                                    @Override
                                                                    protected void afterHookedMethod(MethodHookParam param) {
                                                                        Object thisObject = param.thisObject;
                                                                        if (hasSpeedConfigChanged()) {
                                                                            speedConfig[0] = getSpeedConfig();
                                                                        }
                                                                        try {
                                                                            method.invoke(thisObject, speedConfig[0]);
                                                                        } catch (
                                                                                IllegalAccessException e) {
                                                                            // should not happen
                                                                            XposedBridge.log(e);
                                                                            throw new IllegalAccessError(e.getMessage());
                                                                        } catch (
                                                                                InvocationTargetException e) {
                                                                            throw new RuntimeException(e);
                                                                        }
                                                                        XposedBridge.log("bili setSpeed: " + speedConfig[0]);
                                                                    }
                                                                });
                                                                XposedBridge.log("bili hooked resume");

                                                                first.unhook();
                                                                second.unhook();
                                                                third.unhook();

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
                                }
                            });
                            XposedBridge.log("hooked mOnPreparedListener->onPrepared");
                        }
                    }
                });
                XposedBridge.log("hooked AbstractMediaPlayer->notifyOnInfo");
            } else if (douyin) {
                try {
                    float[] speedConfig = {getSpeedConfig()};
                    Boolean[] resumeHooked = {false};

                    XposedHelpers.findAndHookMethod("com.ss.android.ugc.aweme.video.simplayer.SimPlayer", lpparam.classLoader, "prepare", "com.ss.android.ugc.aweme.video.PlayRequest", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Object thisObject = param.thisObject;

                            if (!resumeHooked[0]) {
                                StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                                for (int i = 4; i <= 7 && i < stackTraceElements.length; i++) {
                                    if (stackTraceElements[i].getMethodName().equals("prepare")) {
                                        Class<?> parentClass = XposedHelpers.findClass(stackTraceElements[i + 1].getClassName(), lpparam.classLoader);
                                        Object[] parameters = {"com.ss.android.ugc.aweme.feed.model.Video", "com.ss.android.ugc.aweme.player.sdk.api.OnUIPlayListener", int.class, String.class, boolean.class, String.class, boolean.class, boolean.class, int.class, int.class, boolean.class, boolean.class, Bundle.class};
                                        Class<?>[] parameter = (Class<?>[]) XposedHelpers.callStaticMethod(XposedHelpers.class, "getParameterClasses", lpparam.classLoader, parameters);
                                        Method[] methods = XposedHelpers.findMethodsByExactParameters(parentClass, String.class, parameter);
                                        if (methods.length == 1) {
                                            XC_MethodHook mh = new XC_MethodHook() {
                                                @Override
                                                protected void afterHookedMethod(MethodHookParam param) {
                                                    if (hasSpeedConfigChanged()) {
                                                        speedConfig[0] = getSpeedConfig();
                                                    }
                                                    XposedHelpers.callMethod(thisObject, "setSpeed", speedConfig[0]);
                                                    XposedBridge.log("douyin setSpeed1: " + speedConfig[0]);
                                                }
                                            };
                                            XposedBridge.hookMethod(methods[0], mh);
                                            XposedHelpers.findAndHookMethod("com.ss.android.ugc.aweme.video.simplayer.SimPlayer", lpparam.classLoader, "resume", mh);
                                            XposedBridge.log("hooked douyin resume");
                                        }
                                    }
                                }
                                resumeHooked[0] = true;
                            }

                            if (hasSpeedConfigChanged()) {
                                speedConfig[0] = getSpeedConfig();
                            }
                            XposedHelpers.callMethod(thisObject, "setSpeed", speedConfig[0]);
                            XposedBridge.log("douyin setSpeed0: " + speedConfig[0]);
                        }
                    });

                    XposedHelpers.findAndHookMethod("com.ss.android.ugc.aweme.video.simplayer.SimPlayer", lpparam.classLoader, "setSpeed", float.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            if ((float) param.args[0] != speedConfig[0]) {
                                StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                                if (stackTraceElements.length <= 24) {
                                    // ignore old version set speed == 1
                                } else if (stackTraceElements.length <= 40) {
                                    for (int i = 4; i <= 7 && i < stackTraceElements.length; i++) {
                                        // manually speed tweak
                                        if (stackTraceElements[i].getMethodName().equals("setSpeed") &&
                                                (stackTraceElements[i+2].getMethodName().contains("onChanged") ||
                                                        stackTraceElements[i+3].getMethodName().contains("onChanged"))) {
                                            XposedBridge.log("douyin manual speed0: " + param.args[0]);
                                            speedConfig[0] = (float) param.args[0];
                                            return;
                                        }
                                    }
                                } else {
                                    for (int i = 16; i <= 24 && i < stackTraceElements.length; i++) {
                                        // manually speed tweak
                                        if (stackTraceElements[i].getMethodName().equals("dispatchTouchEvent")) {
                                            XposedBridge.log("douyin manual speed1: " + param.args[0]);
                                            speedConfig[0] = (float) param.args[0];
                                            return;
                                        }
                                    }
                                }
                                param.args[0] = speedConfig[0];
                                XposedBridge.log("douyin setSpeed2: " + speedConfig[0]);
                            }
                        }
                    });
                } catch (XposedHelpers.ClassNotFoundError cnfe) {
                    XposedBridge.log("douyin hook error0: " + cnfe);
                }

                try {
                    XposedHelpers.findAndHookMethod("com.ss.android.ugc.aweme.player.sdk.impl.TTPlayer", lpparam.classLoader, "setPlaySpeed", float.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            float speed = (float) param.args[0];
//                        XposedBridge.log("speed: " + speed);
                            if (speed == 1.0f) {
                                StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                                for (int i = 12; i <= 17 && i < stackTraceElements.length; i++) {
                                    // return on manually speed tweak
                                    if (stackTraceElements[i].getMethodName().equals("dispatchingValue")) {
//                                    XposedBridge.log("i: " + i);
                                        return;
                                    }
                                }
                                param.args[0] = getSpeedConfig();
                            }
                        }
                    });
                } catch (XposedHelpers.ClassNotFoundError cnfe) {
                    XposedBridge.log("douyin hook error1: " + cnfe);
                }

                XposedBridge.log("hooked douyin setSpeed");

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
            } else if (red) {
                float[] speedConfig = {getSpeedConfig()};

                XC_MethodHook hookInit = new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Object thisObject = param.thisObject;
//                        float currentSpeed = (float) XposedHelpers.callMethod(thisObject, "getSpeed", 0.0f);
                        if (hasSpeedConfigChanged()) {
                            speedConfig[0] = getSpeedConfig();
                        }
                        XposedHelpers.callMethod(thisObject, "setSpeed", speedConfig[0]);
                        XposedBridge.log("red setSpeed: " + speedConfig[0]);
                    }
                };

                XC_MethodHook hookSetSpeed = new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if ((float) param.args[0] != speedConfig[0]) {
                            XposedBridge.log(Log.getStackTraceString(new Throwable()));

                            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                            for (int i = 4; i < stackTraceElements.length; i++) {
                                // manual set
                                if (stackTraceElements[i].getMethodName().contains("Click")) {
                                    XposedBridge.log("red manual speed: " + param.args[0]);
                                    speedConfig[0] = (float) param.args[0];
                                    return;
                                }
                            }
                            param.args[0] = speedConfig[0];
                        }
                    }
                };

                try {
                    XposedHelpers.findAndHookMethod("tv.danmaku.ijk.media.player.IjkMediaPlayer", lpparam.classLoader, "initPlayer", "android.content.Context", "tv.danmaku.ijk.media.player.IjkLibLoader", hookInit);
                    XposedHelpers.findAndHookMethod("tv.danmaku.ijk.media.player.IjkMediaPlayer", lpparam.classLoader, "setSpeed", float.class, hookSetSpeed);
                    XposedBridge.log("hooked red IjkMediaPlayer");
                } catch (XposedHelpers.ClassNotFoundError e) {
                    XposedBridge.log("red IjkMediaPlayer not found");
                }

                try {
                    XposedHelpers.findAndHookMethod("com.xingin.redplayercore.RedPlayerCore", lpparam.classLoader, "initPlayer", Context.class, "com.xingin.redplayercore.RedLibLoader", hookInit);
                    XposedHelpers.findAndHookMethod("com.xingin.redplayercore.RedPlayerCore", lpparam.classLoader, "setSpeed", float.class, hookSetSpeed);
                    XposedBridge.log("hooked red RedPlayerCore");
                } catch (XposedHelpers.ClassNotFoundError e) {
                    XposedBridge.log("red RedPlayerCore not found");
                }

                first = XposedHelpers.findAndHookMethod("java.util.concurrent.ConcurrentHashMap", lpparam.classLoader, "get", Object.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
                        if ("followfeed_doubleline".equals(param.args[0])) {
                            ConcurrentHashMap hashMap = (ConcurrentHashMap) param.thisObject;
                            hashMap.compute("followfeed_doubleline", (k, v) -> {
                                if (v == null) {
                                    return v;
                                } else {
                                    try {
                                        Method method = XposedHelpers.findMethodExact(v.getClass(), "setValue", String.class);
                                        method.invoke(v, "0");
                                        XposedBridge.log("hooked red doubleline config");
                                        first.unhook();
                                        return v;
                                    } catch (InvocationTargetException e) {
                                        throw new RuntimeException(e);
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                        }
                    }
                });

            } else if (wb) {
                float[] speedConfig = {getSpeedConfig()};

                XposedHelpers.findAndHookMethod("com.sina.weibo.mc.MagicCubePlayer", lpparam.classLoader, "onInfo", int.class, int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
//                        XposedBridge.log("setSpeed arg0: " + (int)param.args[0] + " arg1: " + (int)param.args[1]);
                        if ((int)param.args[0] == 13 && (int)param.args[1] == 0) {
                            Object thisObject = param.thisObject;
                            if (hasSpeedConfigChanged()) {
                                speedConfig[0] = getSpeedConfig();
                            }
                            XposedHelpers.callMethod(thisObject, "setSpeed", speedConfig[0]);
                            XposedBridge.log("weibo setSpeed0: " + speedConfig[0]);
                        }
                    }
                });
                XposedHelpers.findAndHookMethod("com.sina.weibo.mc.MagicCubePlayer", lpparam.classLoader, "setSpeed", float.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if ((float) param.args[0] != speedConfig[0]) {
                            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                            for (int i = 4; i < stackTraceElements.length; i++) {
                                // manual set
                                if (stackTraceElements[i].getMethodName().contains("Click")) {
                                    XposedBridge.log("weibo manual speed0: " + param.args[0]);
                                    speedConfig[0] = (float) param.args[0];
                                    return;
                                }
                            }
                            param.args[0] = speedConfig[0];
                        }
                    }
                });

                XposedHelpers.findAndHookMethod("tv.danmaku.ijk.media.player.IjkMediaPlayer", lpparam.classLoader, "start", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Object thisObject = param.thisObject;
                        if (hasSpeedConfigChanged()) {
                            speedConfig[0] = getSpeedConfig();
                        }
                        XposedHelpers.callMethod(thisObject, "setSpeed", speedConfig[0]);
                        XposedBridge.log("weibo setSpeed1: " + speedConfig[0]);
                    }
                });

                XposedHelpers.findAndHookMethod("tv.danmaku.ijk.media.player.IjkMediaPlayer", lpparam.classLoader, "setSpeed", float.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if ((float) param.args[0] != speedConfig[0]) {
                            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                            for (int i = 4; i < stackTraceElements.length; i++) {
                                // manual set
                                if (stackTraceElements[i].getMethodName().contains("Click")) {
                                    XposedBridge.log("weibo manual speed0: " + param.args[0]);
                                    speedConfig[0] = (float) param.args[0];
                                    return;
                                }
                            }
                            param.args[0] = speedConfig[0];
                        }
                    }
                });

                XposedBridge.log("hooked weibo");
            } else if (ig) {
                first = XposedHelpers.findAndHookMethod("com.facebook.breakpad.BreakpadManager", lpparam.classLoader,"setCustomData", String.class, String.class, Object[].class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if ("last_video".equals(param.args[0])) {
                            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                            for (int i = 2; i <= 7 && i < stackTraceElements.length; i++) {
                                if ("setCustomData".equals(stackTraceElements[i].getMethodName())) {
                                    XposedHelpers.findAndHookMethod(stackTraceElements[i + 1].getClassName(), lpparam.classLoader, "handleMessage", Message.class, new XC_MethodHook() {
                                        @Override
                                        protected void beforeHookedMethod(MethodHookParam param) {
                                            Message msg = (Message) param.args[0];
                                            if (msg.what == 6) {
                                                Message speedMsg = new Message();
                                                speedMsg.what = 27;
                                                speedMsg.obj = getSpeedConfig();
                                                XposedHelpers.callMethod(param.thisObject, "handleMessage", speedMsg);
                                            }
                                        }
                                    });

                                    XposedBridge.log("hooked ig handleMessage");
                                    first.unhook();
                                    return;
                                }
                            }
                        }
                    }
                });
                XposedBridge.log("hooked ig BreakpadManager");
            } else if (tg) {
                XposedHelpers.findAndHookMethod("org.telegram.ui.PhotoViewer", lpparam.classLoader, "preparePlayer", "android.net.Uri", boolean.class, boolean.class, "org.telegram.messenger.MediaController$SavedFilterState", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object thisObject = param.thisObject;
                        XposedHelpers.setObjectField(thisObject, "currentVideoSpeed", getSpeedConfig());
                        XposedBridge.log("tg speed set");
                    }
                });
                XposedBridge.log("hooked tg setPlaybackSpeed");
            } else if (wx) {
                XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.finder.video.FinderThumbPlayerProxy", lpparam.classLoader, "setPlaySpeed", float.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        float speed = (float) param.args[0];
                        if (speed == 1.0f) {
//                            XposedBridge.log(Log.getStackTraceString(new Throwable()));
                            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                            for (int i = 4; i <= 10 && i < stackTraceElements.length; i++) {
                                if ("com.tencent.mm.plugin.finder.video.FinderVideoLayout".equals(stackTraceElements[i].getClassName())) {
                                    param.args[0] = getSpeedConfig();
                                    XposedBridge.log("wx speed set");
                                    return;
                                }
                            }
                        }
                    }
                });
                XposedBridge.log("hooked wx setPlaySpeed");
            }
        }
    }
}
