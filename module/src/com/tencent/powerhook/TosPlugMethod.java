package com.tencent.powerhook;

import java.lang.reflect.Member;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class TosPlugMethod {
	public static class MethodPlugParam {
		/** Description of the hooked method */
		public Member method;
		
		/** The <code>this</code> reference for an instance method, or null for static methods */
		public Object thisObject;
		//public Object orgMethodResult;
		/** Arguments to the method call */
		public Object[] args;
		
		public Object result = null;
		public Throwable throwable = null;

		public int invokeType;
		public Object xposedHookParam;
		
		/** Returns the result of the method call */
		public Object getResult() {
			return result;
		}
		/**
		 * Modify the result of the method call. In a "before-method-call"
		 * hook, prevents the call to the original method.
		 * You still need to "return" from the hook handler if required.
		 */
		public void setResult(Object result) {
			this.result = result;
			this.throwable = null;
			if (xposedHookParam != null) {
				MethodHookParam hookParam = (MethodHookParam)xposedHookParam;
				hookParam.setResult(result);
				hookParam.setThrowable(null);
			}
		}

		/** Returns the <code>Throwable</code> thrown by the method, or null */
		public Throwable getThrowable() {
			return throwable;
		}

		/** Returns true if an exception was thrown by the method */
		public boolean hasThrowable() {
			return throwable != null;
		}

		/**
		 * Modify the exception thrown of the method call. In a "before-method-call"
		 * hook, prevents the call to the original method.
		 * You still need to "return" from the hook handler if required.
		 */
		public void setThrowable(Throwable throwable) {
			this.throwable = throwable;
			this.result = null;
			if (xposedHookParam != null) {
				MethodHookParam hookParam = (MethodHookParam)xposedHookParam;
				hookParam.setResult(null);
				hookParam.setThrowable(throwable);
			}
		}

		/** Returns the result of the method call, or throws the Throwable caused by it */
		public Object getResultOrThrowable() throws Throwable {
			if (throwable != null)
				throw throwable;
			return result;
		}
		
	}

}
