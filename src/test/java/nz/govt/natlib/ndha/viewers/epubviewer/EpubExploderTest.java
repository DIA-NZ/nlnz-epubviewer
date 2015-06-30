package nz.govt.natlib.ndha.viewers.epubviewer;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EpubExploderTest {

	public static final String FILESEPARATOR = System.getProperty("file.separator");
	EpubExploder exploder = new EpubExploder();

	String epubFilePath;
	String realPath;
	String filePid;

	@Before
	public void setUp() {
		filePid = "1598501";
		epubFilePath = "src" + FILESEPARATOR + "test" + FILESEPARATOR + "resources" + FILESEPARATOR + "epubFile" + FILESEPARATOR + filePid + ".epub";
		realPath = "target" + FILESEPARATOR;
	}

	@After
	public void tearDownCreatedDirectories(){
		String filePath = realPath + "epubContent" + FILESEPARATOR;
		File file = new File(filePath);
		if(file.exists()){
			exploder.deleteFile(file, filePath);
		}
		else {
			file.delete();
		}
	}

	@Test
	public void testExplodeFile() throws Exception {
		String epubfileName = exploder.explodeFile(epubFilePath, realPath, filePid);
		assertEquals("The epub file name returned after exploding the file is as expected...", "1598501", epubfileName);
		
		File testFile = new File(realPath + "epubContent");
		assertTrue("Base directory epubContent is created", testFile.exists());
	}
	
	@Test
	public void testExplodeFileErrorWhenEpubfileIsNotAccessible() {
		String epubFilePath = "src" +  FILESEPARATOR + "test" +  FILESEPARATOR + "resources" +  FILESEPARATOR + "epubFile" +  FILESEPARATOR + "inaccessableLocation"+ FILESEPARATOR + "1598501.epub";
		String epubFileName = null;
		try {
			epubFileName = exploder.explodeFile(epubFilePath, realPath, filePid);
			fail("Exception occured while trying to unzip the epub file.");
		} catch (Exception e) {
			assertThat(e.getMessage(), is("Error occurred while unzipping the epub file"));
		}
		 assertNull("The epub file name returned after exploding the file should be null", epubFileName);
		 
		File testFile = new File(realPath + "epubContent" + FILESEPARATOR + epubFileName);
		assertFalse("Epub file directory is not created", testFile.exists());
	}
	
}