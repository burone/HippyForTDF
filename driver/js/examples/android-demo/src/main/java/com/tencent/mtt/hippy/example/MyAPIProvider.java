package com.tencent.mtt.hippy.example;

import com.tencent.mtt.hippy.HippyAPIProvider;
import com.tencent.mtt.hippy.HippyEngineContext;
import com.tencent.mtt.hippy.common.Provider;
import com.tencent.mtt.hippy.example.module.MyModule;
import com.tencent.mtt.hippy.example.module.TestModule;
import com.tencent.mtt.hippy.example.module.turbo.DemoJavaTurboModule;
import com.tencent.mtt.hippy.modules.javascriptmodules.HippyJavaScriptModule;
import com.tencent.mtt.hippy.modules.nativemodules.HippyNativeModuleBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAPIProvider implements HippyAPIProvider
{
	/**
	 * 接口：用来让JavaScript调用Java层的接口
	 */
	@Override
	public Map<Class<? extends HippyNativeModuleBase>, Provider<? extends HippyNativeModuleBase>> getNativeModules(final HippyEngineContext context)
	{
		Map<Class<? extends HippyNativeModuleBase>, Provider<? extends HippyNativeModuleBase>> modules = new HashMap<>();

		//regist the MyModule
		modules.put(MyModule.class, new Provider<HippyNativeModuleBase>()
		{
			@Override
			public HippyNativeModuleBase get()
			{
				return new MyModule(context);
			}
		});
		modules.put(TestModule.class, new Provider<HippyNativeModuleBase>()
		{
			@Override
			public HippyNativeModuleBase get()
			{
				return new TestModule(context);
			}
		});

    // TurboModule
    modules.put(DemoJavaTurboModule.class, new Provider<HippyNativeModuleBase>() {
      @Override
      public HippyNativeModuleBase get() {
        return new DemoJavaTurboModule(context);
      }
    });

		return modules;
	}

	/**
	 * 接口：Java层用来调用JavaScript里的同名接口
	 */
	@Override
	public List<Class<? extends HippyJavaScriptModule>> getJavaScriptModules()
	{
		return null;
	}

}
