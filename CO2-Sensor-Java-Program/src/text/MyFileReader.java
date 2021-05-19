package text;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MyFileReader {

	public String readFile(File file) {
		if (!file.canRead() || !file.isFile())
			return null;
		FileReader fr = null;
		int c;
		StringBuffer buff = new StringBuffer();
		try {
			fr = new FileReader(file);
			while ((c = fr.read()) != -1)
				buff.append((char) c);
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buff.toString();
	}
}
