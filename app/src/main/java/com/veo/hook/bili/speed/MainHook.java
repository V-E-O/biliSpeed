package com.veo.hook.bili.speed;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class MainHook implements IXposedHookLoadPackage {
    public final static String hookPackage = "tv.danmaku.bili";
    private static XC_MethodHook.Unhook first = null;
    private static XC_MethodHook.Unhook second = null;
    private static XC_MethodHook.Unhook third = null;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (hookPackage.equals(lpparam.packageName)) {
            boolean isMainProcess = hookPackage.equals(lpparam.processName);
            if (!isMainProcess)
                return;

            XSharedPreferences prefs = new XSharedPreferences("com.veo.hook.bili.speed", "speed");
            final float speedConfig = prefs.getFloat("speed", 1.5f);

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
