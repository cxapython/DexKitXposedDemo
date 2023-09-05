package com.example.dexkitxposeddemo;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedHelpers;
import io.luckypray.dexkit.DexKitBridge;
import io.luckypray.dexkit.builder.BatchFindArgs;
import io.luckypray.dexkit.descriptor.member.DexClassDescriptor;
import io.luckypray.dexkit.descriptor.member.DexMethodDescriptor;
import io.luckypray.dexkit.enums.MatchType;
import java.util.HashSet;

public class HookEntry implements IXposedHookLoadPackage{
    Set<String> check_info = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("method1234")));
    public static final String TAG= "cxa_dexkit";
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.processName.equals("com.example.cvc")) {
            return;
        }
        Class<?> c1=XposedHelpers.findClassIfExists(
                "com.example.cvc.TroopFileTransferManager",
                loadPackageParam.classLoader
        );
        Object obj1 = XposedHelpers.newInstance(c1);
        Log.d(TAG,String.valueOf(obj1));

        Class<?> c=XposedHelpers.findClassIfExists(
                "com.example.cvc.TroopFileTransferManager$Item",
                loadPackageParam.classLoader
        );
        Object obj = XposedHelpers.newInstance(c);
        Log.d(TAG,String.valueOf(obj));



//        vipHook(loadPackageParam);

    }

    public void vipHook(XC_LoadPackage.LoadPackageParam loadPackageParam) throws NoSuchMethodException {
        System.loadLibrary("dexkit");
        String apkPath = loadPackageParam.appInfo.sourceDir;
        Set<String> check_info_class = new HashSet<>();
        check_info_class.add("myClass");
        check_info_class.add("thisis");
        try (DexKitBridge bridge = DexKitBridge.create(apkPath)) {
            if (bridge == null) {
                return;
            }
            Map<String, List<DexMethodDescriptor>> resultMap =
                    bridge.batchFindMethodsUsingStrings(
                            BatchFindArgs.builder()
                                    .addQuery("VipCheckUtil_method",check_info)
                                    .matchType(MatchType.CONTAINS)
                                    .build()
                    );
            Map<String, List<DexClassDescriptor>> resultMap2 =  bridge.batchFindClassesUsingStrings(
                            BatchFindArgs.builder()
                                    .addQuery("VipCheckUtil_class",check_info_class)
                                    .matchType(MatchType.SIMILAR_REGEX)
                                    .build()
                    );

            List<DexMethodDescriptor> result = Objects.requireNonNull(resultMap.get("VipCheckUtil_method"));
            Log.d(TAG,String.format("VipCheckUtil_method结果数:%d",result.size()));
            assert result.size() == 1;

            for (DexMethodDescriptor descriptor : result) {
                Method isVipMethod = descriptor.getMethodInstance(loadPackageParam.classLoader);
                Log.d(TAG,String.format("发现方法:%s",isVipMethod));
                XposedBridge.hookMethod(isVipMethod, XC_MethodReplacement.returnConstant(true));
            }

            List<DexClassDescriptor> resultClass = Objects.requireNonNull(resultMap2.get("VipCheckUtil_class"));
            Log.d(TAG,String.format("VipCheckUtil_class结果数:%d",result.size()));
            assert resultClass.size() == 1;

            for (DexClassDescriptor descriptor : resultClass) {
                Log.d(TAG,String.format("发现类:%s",descriptor.getName()));
            }



        }
    }



}
