package nz.govt.natlib.ndha.viewers.epubviewer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//import org.apache.log4j.Logger;

/**
 * Service class for streaming the epub file and unzip it on server. This also
 * deletes the old content which are the exloded epub contents that are old
 * (created atleast a day before).
 * 
 * Created by : Lithil Kuriakose
 * Modified by: Mathachan Kulathinal/Preeti Badle [03.Jul.2014]
 */
public class EpubExploder {

	//private static Logger logger = Logger.getLogger(EpubExploder.class);

	public static final String FILESEPARATOR = System.getProperty("file.separator");
	public static final String EPUBCONTENTFOLDER = "epubContent";
	public static final String DELETELOGFILENAME = "deleteContentsLog.txt";
	public static final String EPUB_FILE_EXTENSION = ".epub";
	public static final String ZIP_CONTENT_SEPARATOR = "/";

	
	public EpubExploder() {
		super();
	}
	
	/**
	 * This method creates a folder named "epubContent" on the server if not
	 * already existing. This is the folder where the exploded epub files are
	 * located.
	 * 
	 * It calls the deleteOldFiles method which deletes old exploded epub
	 * contents of "epubContent" folder older than today.
	 * 
	 * Subsequently, it streams the file from
	 * the filepath which is an epub file(packaged files and folders) and
	 * explodes the contents and write them on to the server location under
	 * folder epubContent.
	 * 
	 * @param realPath
	 * @param filePid
	 * @return the epub file name
	 */
	public String explodeFile(String epubFilePath, String realPath, String filePid) throws Exception {

		String epubFileName = null;
	
		try {
			
			// Create the epubContent folder if it does not exist
			File epubContentDir = new File(realPath + EPUBCONTENTFOLDER);
			if (!epubContentDir.exists()) {
				if (!epubContentDir.mkdir()) {
					// Report error - unable to create the epubContent folder
					throw new Exception("Unable to create the sub-folder:[" + realPath + EPUBCONTENTFOLDER + "] while trying to unzip the epub file");
				}
			}
			
			// Delete the old content
			deleteOldFiles(realPath, filePid);
			
			epubFileName = epubFilePath.substring(epubFilePath.lastIndexOf(FILESEPARATOR)+1).replace(".epub", "");
			
			String epubFileDirName = realPath + EPUBCONTENTFOLDER + FILESEPARATOR + epubFileName;
			ZipFile zipFile = new ZipFile(new File(epubFilePath));
			
			// Explode the contents of the epub file
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				
				// Ensure the base epubContent folder is created
				File epubFileDir = new File(epubFileDirName); 
				if (!(epubFileDir.exists())) {
					if (!epubFileDir.mkdirs()) {
						// Report error - unable to create the epubContent folder
						throw new Exception("Unable to create the sub-folder:[" + epubFileDirName + "] while trying to unzip the epub file");
					}
				}
				
				String fileName = entry.getName();
				String parentToSubDir = epubFileDirName;
				while (fileName.indexOf(ZIP_CONTENT_SEPARATOR) > 0) {
					String subDirName = fileName.substring(0, fileName.indexOf(ZIP_CONTENT_SEPARATOR));
					// Hold the folder trail if creating more than one sub-directory
					parentToSubDir +=  FILESEPARATOR + subDirName;
					File subDir = new File(parentToSubDir);
					
					if (!subDir.exists()) {
						if (!subDir.mkdir()) {
							// Report error - genuine error in exploding the content as unable to make the sub directory
							throw new Exception("Unable to create the sub-folder:[" + parentToSubDir + "] while trying to unzip the epub file");
						}
						//logger.info("Sub directory created:[" + parentToSubDir + "]");
					}
					fileName = fileName.substring(fileName.indexOf(ZIP_CONTENT_SEPARATOR) + 1);
				}
				
				// Ensure that there is a valid file to be exploded
				if (!fileName.isEmpty()) {
					// Unzip the files by reading it into filestream and write it out into the correct location
					InputStream fis = zipFile.getInputStream(entry);
					FileOutputStream fos = new FileOutputStream(epubFileDirName + FILESEPARATOR + entry.getName());
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					byte[] buffer = new byte[1024];
					int readSize = -1;
					while ((readSize = fis.read(buffer)) != -1) {
						bos.write(buffer, 0, readSize);
					}
					bos.close();
					fis.close();
					fos.flush();
					fos.close();
				}
			}

		} catch (Exception ex) {
			//logger.error("Exception occured while trying to unzip the epub file. " + ex.getMessage());
			throw new Exception("Error occurred while unzipping the epub file", ex);
		}
		
		return epubFileName;
	}

	/**
	 * This method deletes the old epub contents which are quite old created
	 * atleast a day before. It creates a log file which has the date, on which
	 * the contents are deleted last.
	 * 
	 * @param realPath
	 * @param filePid
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public void deleteOldFiles(String realPath, String filePid) throws ParseException, IOException {
		// When the servlet is called for the first time, check for any files
		// created before the (current date - 1 day) and delete them all.
		// Create a log file (if not already existing), otherwise read the log
		// file and see whether this call is the first call for current date.
		// If so proceed further to delete the contents
		
		// Check if files have been already deleted for today
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(new Date());
		Date previousDate = sdf.parse(dateStr);
		Date todaysDate = sdf.parse(dateStr);
		File deleteContentsLogFile =  new File(realPath + EPUBCONTENTFOLDER + FILESEPARATOR + DELETELOGFILENAME);
		if (deleteContentsLogFile.exists()) {
			// Open the file and get the date
			BufferedReader br = new BufferedReader(new FileReader(deleteContentsLogFile));
			String line = br.readLine();
			while (line != null) {
				previousDate = sdf.parse(line);
				line = br.readLine();
			}
			br.close();
		}
		
		// Check if we have run the delete files for today
		if (!todaysDate.equals(previousDate)) {
			// Delete old files
			String contents[] = new File(realPath + EPUBCONTENTFOLDER).list();
			for(String tempcontent : contents) {
				String fileOrFolderNameToDelete = realPath + EPUBCONTENTFOLDER + FILESEPARATOR + tempcontent;
				//logger.info("Deleting old content. Folder:[" + fileOrFolderNameToDelete + "]");
				File fileToDelete = new File(fileOrFolderNameToDelete);
				// Check if the folder was created earlier than 1 day & delete if YES
				if ((System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000)) > (fileToDelete.lastModified())) {
					deleteFile(fileToDelete, fileOrFolderNameToDelete);
				}
			}
		}
		// Update the date in the log file for next run comparison
		BufferedWriter bw = new BufferedWriter(new FileWriter(deleteContentsLogFile));
		bw.write(sdf.format(todaysDate));
		bw.close();
	}
	
	/**
	 * This method deletes directories and files by calling it recursively to
	 * delete the sub directories and files inside them
	 * 
	 * @param filetoDelete
	 * @param fileNameToDelete
	 */
	public void deleteFile(File filetoDelete, String fileNameToDelete) {
		
		if (filetoDelete.isDirectory()) {
			String contentsToDelete[] = filetoDelete.list();
			for (String tempStr : contentsToDelete) {
				String tempBaseFolderToDelete = fileNameToDelete + FILESEPARATOR + tempStr;
				File tempFile = new File(tempBaseFolderToDelete);
				if (tempFile.isDirectory()) {
					String tempFileContents[] = tempFile.list();
					if (tempFileContents == null || (tempFileContents != null && tempFileContents.length == 0)) {
						tempFile.delete();
					}
					deleteFile(tempFile, tempBaseFolderToDelete);
				} else {
					tempFile.delete();
				}
			}
		} else {
			filetoDelete.delete();
		}
		// Delete any empty epub file directory (based on the file PID) after deleting all the contents
		if (filetoDelete.exists() && filetoDelete.isDirectory() 
				&& (filetoDelete.list() == null || (filetoDelete.list() != null && filetoDelete.list().length == 0))) {
			filetoDelete.delete();
		}
	}
}