package com.tencent.powerhook;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.robv.android.xposed.XposedHelpers;
//import de.robv.android.xposed.XposedBridge;
    
/**
 *对应/ssets/register.xml中的component,
 *标识了一个hook组件，在该组件中定义需要hook的目标类，
 *目标方法以及替代方法的实现类 
 *
 */
public class Component {
	
	private String mTargetClassName;
	
	private String mImpClassName;

	public static final ClassLoader SYSTEMCLASSLOADER = ClassLoader.getSystemClassLoader();
	
	private List<ComponentMethod> mMethods = new ArrayList<ComponentMethod>();
	
	private static HashMap<String,Class<?>> mSimpleClasses = new HashMap<String,Class<?>>();
	
	static{
		mSimpleClasses.put("void", void.class);
		mSimpleClasses.put("int", int.class);
		mSimpleClasses.put("float", float.class);
		mSimpleClasses.put("double", double.class);
		mSimpleClasses.put("long", long.class);
		mSimpleClasses.put("boolean", boolean.class);
		mSimpleClasses.put("char", char.class);
		mSimpleClasses.put("short", short.class);
		mSimpleClasses.put("int[]", int[].class);
		mSimpleClasses.put("float[]", float[].class);
		mSimpleClasses.put("double[]", double[].class);
		mSimpleClasses.put("long[]", long[].class);
		mSimpleClasses.put("boolean[]", boolean[].class);
		mSimpleClasses.put("char[]", char[].class);
		mSimpleClasses.put("short[]", short[].class);
	}
	
	
	public void setTargetClassName(String name){
		this.mTargetClassName = name;
	}
	
	
	public String getTargetClassName(){
	
		return mTargetClassName;
	}
	
	public void setImpClassName(String name){
		this.mImpClassName = name;
	}
	
	public String getImpClassName(){
		return mImpClassName;
	}
	
	public void addMethod(ComponentMethod method){
		mMethods.add(method);
	}
	
	public List<ComponentMethod> getMethods(){
		
		return mMethods;
	}
	
	
	
	public static class ComponentMethod{
		/**
		 * 方法的名称
		 */
		private String mMethodName;
		/**
		 * 方法的调用顺序
		 */
		private int mInvokeType;
		/**
		 * 该List用于存储方法的参数类型列表
		 */
		private List<Class<?>> mArgsTypes = new ArrayList<Class<?>>();
		
		private boolean isConstructor ;
		
		private int mMaxSdkVersion;
		
		private int mMinSdkVersion;
		
		public int getMinSdkVersion() {
			return mMinSdkVersion;
		}

		public void setSdkRange(int min, int max) {
			this.mMinSdkVersion = min;
			this.mMaxSdkVersion = max;
		}

		public int getMaxSdkVersion() {
			return mMaxSdkVersion;
		}
		
		public void setConstructor(boolean yes){
			this.isConstructor = yes;
		}
		public boolean isConstructor(){
			return isConstructor;
		}
		public void setInvokeType(int type){
			this.mInvokeType = type;
		}
		public int getInvokeType(){
			return mInvokeType;
		}
		public void setMethodName(String name){
			this.mMethodName = name;
		}
		public String getMethodName(){
			return mMethodName;
		}
		public List<Class<?>> getArgsTypes(){
			return mArgsTypes;
		}
		/**
		 * 记录方法参数列表
		 * @param type
		 */
		public void addMethodArgType(String type){
			Class<?> cls = null;
			try{
				if(mSimpleClasses.containsKey(type)) {
					cls = mSimpleClasses.get(type);
				} else {
					if(type.endsWith("[]")){
						Object array = Array.newInstance(Class.forName(type.substring(0,type.length()-2)), 0);
						cls = array.getClass();
					}else{
						//fredy modify 2015.05.22
						cls = XposedHelpers.findClass(type, SYSTEMCLASSLOADER);
					}
				
				}
				 if(cls != null){
					 mArgsTypes.add(cls);
				 }
			}catch(Exception e){
				
			}
			
		}
		
		/**
		 * 判断注册的方法是否重复，方法重复的规则和方法重载的规则是一样的
		 * @param method
		 * @return
		 */
		public boolean equals(ComponentMethod method) {
			// TODO Auto-generated method stub
			if(method == null){
				return false;
			}
			if(method.getArgsTypes().size() != this.mArgsTypes.size()){
				return false;
			}
			if(method.getInvokeType() == this.mInvokeType
					&&method.getMethodName().equals(this.mMethodName)
				&&compareArgs(method.getArgsTypes(), this.mArgsTypes)){
				return true;
			}
			return false;
		}
		
		/**
		 * 比较方法的参数列表是否相同
		 * @param a
		 * @param b
		 * @return
		 */
		private boolean compareArgs(List<Class<?>> a,List<Class<?>> b){
			if (a.size() != b.size())
				return false;
			for (int i = 0; i < a.size(); i++) {
				if (!a.get(i).getSimpleName().equals(b.get(i).getSimpleName()))
					return false;
			}
			return true;
		}
		
	}
	
	
	
	
	
	
	
	
	
}
