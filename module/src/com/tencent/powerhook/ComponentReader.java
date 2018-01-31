package com.tencent.powerhook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.text.TextUtils;
import android.util.Xml;

import com.tencent.powerhook.Component.ComponentMethod;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

public class ComponentReader {
	private static final int DEFAULT_MAX_VERSION = 100;
	private static final int DEFAULT_MIN_VERSION = 9;
	
	private static Map<String,String>  mAndroidVersions = new HashMap<String,String>();
	
	static{
		mAndroidVersions.put("4.4.2", "4.4.2/register.xml");
		mAndroidVersions.put("4.4.4", "4.4.4/register.xml");
		mAndroidVersions.put("5.1.1", "5.1.1/register.xml"); 
	}
	
	/*
	 * 和register.xml中的标签一一对应
	 */
   private static final String COMPONENT_TAG_NAME="component";
	
	private static final String COMPONENT_TARGET_METHOD="targetMethod";
	
	private static final String COMPONENT_TARGET_CLASS_NAME="targetClassName";
	
	private static final String COMPONENT_IMP_CLASS_NAME="impClassName";
	
	private static final String COMPONENT_METHOD_NAME="name";
	
	private static final String COMPONENT_INVOKE_TYPE="invokeType";
	
	private static final String COMPONENT_TARGET_METHOD_ARGS="methodArg";
	
	private static final String COMPONENT_METHOD_IS_CONSTRUCTOR="constructor";
	
	private static final String COMPONENT_METHOD_MIN_VERSION="minSdkVersion";
	
	private static final String COMPONENT_METHOD_MAX_VERSION="maxSdkVersion";
	
	private static StringBuffer mBuffer = new StringBuffer();
	
	private static ComponentReader mInstance;
	
	public static List<Component> mComponents = new ArrayList<Component>();
	
	public static  List<String> mHookClasses = new ArrayList<String>();
	public String resPath;
	
	public synchronized static ComponentReader getInstance(){
		
		if(mInstance == null){
			mInstance = new ComponentReader();
		}
		return mInstance;
	}
	
	/**
	 * 读取register.xml
	 */
	public void readRegXmlAndParse(Context context) {
		Context hookContext;
		String version = PlugUtils.getCurAndroidVersion();
		LogHelper.d("readRegXmlAndParse android version = " + version);
		String path = mAndroidVersions.get(version);
		
		InputStream is = null;
		try {
			hookContext = context.createPackageContext(ProcessHook.HOOK_PACKAGENAME,
					Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
			
			if (path == null) path = "5.1.1/register.xml";
			is = hookContext.getAssets().open(path);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if (is == null) {
			LogHelper.d(path + " not found in the APK!");
			return;
		} else {
			LogHelper.d(path + " read sucess!");
		}
		
		//bug, fredy need modify
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			String component = null;
			while ((component = br.readLine()) != null) {
				mBuffer.append(component);
			}
			parseXmlFile();
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} finally {
			//warning, fredy modify
			//if (br != null) {
			//if (is != null) {
				try {
					//fredy modify
					//br.close();
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				//fredy del
				//br = null;
			//}
		}
		
		registerComponents(mComponents);
	}
	
	private void parseXmlFile() {
		String xml = mBuffer.toString();
		if(TextUtils.isEmpty(xml)){
			return;
		}
		Component cpt = null;
		ComponentMethod method = null;
		
        StringReader strReader = new StringReader(xml);
		
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(strReader);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		
		try {
			while(parser.next()!=XmlPullParser.END_DOCUMENT){
			   switch (parser.getEventType()) {
				case XmlPullParser.START_TAG:
					if(COMPONENT_TAG_NAME.equals(parser.getName())){
						cpt = new Component();
						String targetCls = parser.getAttributeValue(null, COMPONENT_TARGET_CLASS_NAME);
						cpt.setTargetClassName(targetCls);
						String impCls = parser.getAttributeValue(null, COMPONENT_IMP_CLASS_NAME);
						cpt.setImpClassName(impCls);
						
					}else if(COMPONENT_TARGET_METHOD.equals(parser.getName())){
						method = new ComponentMethod();
						String methodName = parser.getAttributeValue(null, COMPONENT_METHOD_NAME);
						int invokeType = Integer.parseInt(parser.getAttributeValue(null, COMPONENT_INVOKE_TYPE));
						String isCons = parser.getAttributeValue(null, COMPONENT_METHOD_IS_CONSTRUCTOR);
						String minSdkVerStr = parser.getAttributeValue(null, COMPONENT_METHOD_MIN_VERSION);
						String maxSdkVerStr = parser.getAttributeValue(null, COMPONENT_METHOD_MAX_VERSION);
						int minVersion = DEFAULT_MIN_VERSION;
						int maxVersion = DEFAULT_MAX_VERSION;
						if(!TextUtils.isEmpty(minSdkVerStr)){
							minVersion = Integer.parseInt(minSdkVerStr);
						}
						if(!TextUtils.isEmpty(maxSdkVerStr)){
							maxVersion = Integer.parseInt(maxSdkVerStr);
						}
						
						method.setSdkRange(minVersion, maxVersion);
						boolean isConstructor = false;
						if(!TextUtils.isEmpty(isCons)){
							isConstructor = Boolean.parseBoolean(isCons);
						}
						method.setConstructor(isConstructor);
						method.setInvokeType(invokeType);
						method.setMethodName(methodName);
					}else if(COMPONENT_TARGET_METHOD_ARGS.equals(parser.getName())){
						if(method != null){
							String arg = parser.nextText();
							method.addMethodArgType(arg);
						}
					}
					break;
				case XmlPullParser.END_TAG:
					if(COMPONENT_TAG_NAME.equals(parser.getName())){
						mComponents.add(cpt);
					
						cpt = null;
					}else if(COMPONENT_TARGET_METHOD.equals(parser.getName())){
						cpt.addMethod(method);
						method = null;
					}
					break;
	
				default:
					break;
				}
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * 将从register.xml中读取到的数据保存起来
	 * @param components 所有在register.xml注册的组件
	 */
	private void registerComponents(List<Component> components) {
		if(components == null || components.size()==0){
			return;
		}
		
		for(int i = 0;i<components.size();i++){
			Component component = components.get(i);
			if(component == null) {
				return;
			}
			String targetClassName = component.getTargetClassName();
			String impClassName = component.getImpClassName();
			List<ComponentMethod> methods = component.getMethods();
			mHookClasses.add(impClassName);
			if(methods == null || methods.size() == 0){
				return;
			}
			for(ComponentMethod method:methods){
				int invokeType = method.getInvokeType();
				if(invokeType==0){
					return;
				}
				String methodName = method.getMethodName();
				boolean isConstructor = method.isConstructor();
				if(TextUtils.isEmpty(methodName)){
					return;
				}
				int minVersion = method.getMinSdkVersion();
				int maxVersion = method.getMaxSdkVersion();
				List<Class<?>> args = method.getArgsTypes();
				Class<?>[] argsTypes = new Class<?>[args.size()];
				args.toArray(argsTypes);
				/*
				 * 注册hook的component
				 */
				PlugRegister.getInstance().register(minVersion,maxVersion,
						   invokeType, 
							targetClassName+"",
							methodName+"", 
							impClassName+"", 
							isConstructor,
							argsTypes);
				
			}
		}
		
	}
}
