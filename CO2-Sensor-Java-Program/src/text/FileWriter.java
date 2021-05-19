package text;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class FileWriter {

	public FileWriter(String path, String fileName, String text) {
		System.out.println(fileName);
		try {
			File file = new File(path + fileName);
			if (!file.exists()) {
				new File(path).mkdirs();
				file.createNewFile();
			}
			java.io.FileWriter fileWriter = new java.io.FileWriter(path + fileName, true);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			printWriter.println(text);
			printWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
