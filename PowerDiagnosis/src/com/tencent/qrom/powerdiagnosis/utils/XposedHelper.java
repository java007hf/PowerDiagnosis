package com.tencent.qrom.powerdiagnosis.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import android.util.Log;

public class XposedHelper {
	private static final String TAG = "XposedHelper";
	public static final String TMP_FOLDER = "/data/local/tmp/systemlog";
	
	public static void preCreateTmpFolder() {
		runAsRoot("mkdir " + TMP_FOLDER,
				"chmod 777 " + TMP_FOLDER,
				"cp /system/framework/services.jar " + TMP_FOLDER,
				"chmod 777 " + TMP_FOLDER + "/services.jar");
	}
	
	public static boolean hook(String pkgName, String targetProcess, String modulePath, String className) {
		String cmd = null;
		if (targetProcess != null && !targetProcess.isEmpty()) {
			cmd = "xloader" + " -p " + targetProcess + " " + pkgName  + " " + modulePath;
		} else {
			cmd = "xloader" + " " + pkgName  + " " + modulePath;
		}
		
		LogHelper.d("cmd = " + cmd);
		
		String moduleDir = modulePath.substring(0, modulePath.lastIndexOf("/"));
		String ret = runAsRoot("getenforce");
		
		if (ret != null && ret.equals("Enforcing")) {
			//selinux Enforcing
			runAsRoot("setenforce 0",
					"mkdir /data/powersave/",
					"chmod -R 777 /data/powersave/",
					"export CLASSPATH=" + modulePath,
					"app_process " + moduleDir + " " + className,
					cmd); //"setenforce 1"
		} else {
			//selinux Permissive or Disabled
			runAsRoot("mkdir /data/powersave/",
					"chmod -R 777 /data/powersave/",
					"export CLASSPATH=" + modulePath,
					"app_process " + moduleDir + " " + className,
					cmd);
		}
		return true;
	}
	
	public static boolean isHooked(int pid) {
		String path = "/proc/" + pid + "/maps";
		String resultString = runCommand("cat " + path + " | grep libclient.so");
		LogHelper.d("isHooked " + resultString);
		return !resultString.equals("");
	}
	
	private static String runAsRoot(String... cmds) {
        Process process = null;
        DataOutputStream os = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            for(String cmd : cmds) {
            	os.writeBytes(cmd + "\n");
            }
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            br = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024);
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line != null) {
                	sb.append(line);
                }
            }
        } catch (Exception e) {
        	Log.d(TAG, "(error)run cmd : " + Arrays.toString(cmds));
            e.printStackTrace();
            return "Error!";
        } finally {
            try {
                if (os != null) {
                    os.close();  
                }
                if (br != null) {
                	br.close();  
                }
                process.destroy();
            } catch (Exception e) {
            }  
        }
        Log.d(TAG, "Result(" + Arrays.toString(cmds) + "):" + sb.toString());
        return sb.toString();
	}
	
	private static String runCommand(String command) {
		try {
			Process p = Runtime.getRuntime().exec(
					new String[] {
						"su",
						"-c",
						command
					});
			
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = stdout.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			while ((line = stderr.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			stdout.close();
			return sb.toString();
			
		} catch (IOException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			return sw.toString();
		}
	}
}
