package uk.co.strangeskies.p2bnd;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import aQute.bnd.service.RemoteRepositoryPlugin;
import aQute.bnd.service.ResourceHandle;
import aQute.bnd.service.Strategy;
import aQute.bnd.version.Version;

public class P2BndPlugin implements RemoteRepositoryPlugin {
	@Override
	public PutResult put(InputStream stream, PutOptions options) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File get(String bsn, Version version, Map<String, String> properties, DownloadListener... listeners)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canWrite() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> list(String pattern) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<Version> versions(String bsn) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceHandle getHandle(String bsn, String version, Strategy strategy, Map<String, String> properties)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getCacheDirectory() {
		// TODO Auto-generated method stub
		return null;
	}
}
