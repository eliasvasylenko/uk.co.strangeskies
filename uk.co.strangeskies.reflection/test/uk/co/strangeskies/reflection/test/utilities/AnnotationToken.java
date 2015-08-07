package uk.co.strangeskies.reflection.test.utilities;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class AnnotationToken {
	private String stringRepresentation;
	private Set<Package> packages;

	protected AnnotationToken(String stringRepresentation) {
		this(stringRepresentation, new String[0]);
	}

	protected AnnotationToken(String stringRepresentation,
			String... importedPackages) {
		this.stringRepresentation = stringRepresentation;

		// Make sure current classloader is aware of all annotation packages
		getAnnotations();

		packages = new HashSet<>();
		for (String importedPackage : importedPackages)
			packages.add(Package.getPackage(importedPackage));
	}

	public String getStringRepresentation() {
		return stringRepresentation;
	}

	public Annotation[] getAnnotations() {
		return getClass().getAnnotatedSuperclass().getAnnotations();
	}

	public Set<Package> getPackages() {
		return new HashSet<>(packages);
	}
}
