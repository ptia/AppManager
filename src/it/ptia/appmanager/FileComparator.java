package it.ptia.appmanager;

import java.io.File;

public class FileComparator {
    public static class FileNameComparator implements java.util.Comparator<File> {
		public FileNameComparator() {}
        public final int compare(File file1, File file2) {
            String fileName1=file1.getName();
            String fileName2=file2.getName();
			return(fileName1.compareTo(fileName2));
        }
    }
	public static class FileDateComparator implements java.util.Comparator<File> {
		public final int compare(File file1, File file2) {
			long date1=file1.lastModified();
			long date2=file2.lastModified();
			if(date1>date2) {
				return -1;
			}
			else if (date1<date2) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}
}
