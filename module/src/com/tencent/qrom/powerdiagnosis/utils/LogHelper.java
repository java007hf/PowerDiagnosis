package com.tencent.qrom.powerdiagnosis.utils;

import android.util.Log;

public final class LogHelper {
	private static final String TAG_DEFAULT = "powerdiagnosis";
	private static final int LOG_LEVEL_ERROR = 1;
	private static final int LOG_LEVEL_WARN = 2;
	private static final int LOG_LEVEL_INFO = 3;
	private static final int LOG_LEVEL_DEBUG = 4;
	private static final int LOG_LEVEL_VERBOS = 5;

	// Switch to control whether to enable log
	private static final boolean ENABLE_LOG = false;
	private static final int CURRENT_LOG_LEVEL = LOG_LEVEL_VERBOS;

	/**
	 * Send a {@link #VERBOSE} log message.
	 * 
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void v(String msg) {
		if (ENABLE_LOG && CURRENT_LOG_LEVEL >= LOG_LEVEL_VERBOS) {
			Log.v(TAG_DEFAULT, msg);
		}
	}

	/**
	 * Send a {@link #VERBOSE} log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void v(String tag, String msg) {
		if (ENABLE_LOG && CURRENT_LOG_LEVEL >= LOG_LEVEL_VERBOS) {
			Log.v(tag, msg);
		}
	}

	/**
	 * Send a {@link #VERBOSE} log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static void v(String tag, String msg, Throwable tr) {
		if (ENABLE_LOG && CURRENT_LOG_LEVEL >= LOG_LEVEL_VERBOS) {
			Log.v(tag, msg, tr);
		}
	}

	/**
	 * Send a {@link #DEBUG} log message.
	 * 
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void d(String msg) {
		if (ENABLE_LOG && CURRENT_LOG_LEVEL >= LOG_LEVEL_DEBUG) {
			Log.d(TAG_DEFAULT, msg);
		}
	}

	/**
	 * Send a {@link #DEBUG} log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void d(String tag, String msg) {
		if (ENABLE_LOG && CURRENT_LOG_LEVEL >= LOG_LEVEL_DEBUG) {
			Log.d(tag, msg);
		}
	}

	/**
	 * Send a {@link #DEBUG} log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static void d(String tag, String msg, Throwable tr) {
		if (ENABLE_LOG && CURRENT_LOG_LEVEL >= LOG_LEVEL_DEBUG) {
			Log.d(tag, msg, tr);
		}
	}

	/**
	 * Send a {@link #INFO} log message.
	 * 
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void i(String msg) {
		if (ENABLE_LOG && CURRENT_LOG_LEVEL >= LOG_LEVEL_INFO) {
			Log.i(TAG_DEFAULT, msg);
		}
	}

	/**
	 * Send an {@link #INFO} log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void i(String tag, String msg) {
		if (ENABLE_LOG && CURRENT_LOG_LEVEL >= LOG_LEVEL_INFO) {
			Log.i(tag, msg);
		}
	}

	/**
	 * Send a {@link #INFO} log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static void i(String tag, String msg, Throwable tr) {
		if (ENABLE_LOG && CURRENT_LOG_LEVEL >= LOG_LEVEL_INFO) {
			Log.i(tag, msg, tr);
		}
	}

	/**
	 * Send a {@link #WARN} log message.
	 * 
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void w(String msg) {
		if (ENABLE_LOG && CURRENT_LOG_LEVEL >= LOG_LEVEL_WARN) {
			Log.w(TAG_DEFAULT, msg);
		}
	}

	/**
	 * Send a {@link #WARN} log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void w(String tag, String msg) {
		if (ENABLE_LOG && CURRENT_LOG_LEVEL >= LOG_LEVEL_WARN) {
			Log.w(tag, msg);
		}
	}

	/**
	 * Send a {@link #WARN} log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static void w(String tag, String msg, Throwable tr) {
		if (ENABLE_LOG && CURRENT_LOG_LEVEL >= LOG_LEVEL_WARN) {
			Log.d(tag, msg, tr);
		}
	}

	/**
	 * Send an {@link #ERROR} log message.
	 * 
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void e(String msg) {
		if (ENABLE_LOG && CURRENT_LOG_LEVEL >= LOG_LEVEL_ERROR) {
			Log.e(TAG_DEFAULT, msg);
		}
	}

	/**
	 * Send an {@link #ERROR} log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void e(String tag, String msg) {
		if (ENABLE_LOG && CURRENT_LOG_LEVEL >= LOG_LEVEL_ERROR) {
			Log.e(tag, msg);
		}
	}

	/**
	 * Send a {@link #ERROR} log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static void e(String tag, String msg, Throwable tr) {
		if (ENABLE_LOG && CURRENT_LOG_LEVEL >= LOG_LEVEL_ERROR) {
			Log.e(tag, msg, tr);
		}
	}
}
