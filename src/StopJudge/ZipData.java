package StopJudge;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipData {
	
	private ZipFile zipfile = null;
	private Enumeration<? extends ZipEntry> entries = null;
	
	public ZipData(String zipPath) throws IOException
	{
		zipfile = new ZipFile(zipPath);
		entries = zipfile.entries();
	}
	 
	public InputStream getInStream(ZipEntry entry) throws IOException
	{
		return zipfile.getInputStream(entry);
	}
	
	public ZipEntry getNextEntry()
	{
		return entries.nextElement();
	}
	
	public boolean hasNextEntry()
	{
		return entries.hasMoreElements();
	}

	public void close() {
		try {
			zipfile.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
	}
}
