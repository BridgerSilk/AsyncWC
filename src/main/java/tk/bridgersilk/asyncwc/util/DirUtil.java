package tk.bridgersilk.asyncwc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class DirUtil {
	public static void copyDirectory(File src, File dst) {
		if (src == null || !src.exists()) return;
		if (!dst.exists() && !dst.mkdirs()) throw new RuntimeException("Failed to create " + dst);
		File[] files = src.listFiles();
		if (files == null) return;
		for (File f : files) {
			File target = new File(dst, f.getName());
			if (f.isDirectory()) copyDirectory(f, target);
			else copyFile(f, target);
		}
	}

	public static void copyFile(File src, File dst) {
		try (FileChannel in = new FileInputStream(src).getChannel();
			 FileChannel out = new FileOutputStream(dst).getChannel()) {
			out.transferFrom(in, 0, in.size());
		} catch (IOException e) { throw new RuntimeException(e); }
	}

	public static void deleteSilently(File f) {
		if (f == null || !f.exists()) return;
		if (f.isDirectory()) {
			File[] files = f.listFiles();
			if (files != null) for (File c : files) deleteSilently(c);
		}
		//noinspection ResultOfMethodCallIgnored
		f.delete();
	}
}