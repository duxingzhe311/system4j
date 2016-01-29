package com.dw.grid;

import java.io.File;

import com.dw.system.xmldata.XmlDataWithFile;

public interface IXmlDataWithFileRecvedHandler {
	public void onXmlDataWithFileRecved(XmlDataWithFile.CombinedFileHead cfh,
			File combined_file) throws Exception;
}
