package uk.co.strangeskies.p2;

import org.osgi.service.repository.Repository;

import aQute.bnd.service.Plugin;
import aQute.bnd.service.RemoteRepositoryPlugin;

public interface P2Repository
		extends RemoteRepositoryPlugin, Repository, Plugin {}
