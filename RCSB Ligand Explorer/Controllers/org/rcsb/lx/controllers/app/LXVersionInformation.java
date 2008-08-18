package org.rcsb.lx.controllers.app;

import org.rcsb.mbt.controllers.app.VersionInformation;

public class LXVersionInformation extends VersionInformation
{
	/**
	 * LX specific version - deliberately hides base class
	 * 
	 * @return - version string
	 */
	public static String version() { return "3.0"; }
}
