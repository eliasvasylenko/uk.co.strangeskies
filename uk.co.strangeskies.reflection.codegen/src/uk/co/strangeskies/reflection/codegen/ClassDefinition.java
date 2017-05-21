/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.reflection.codegen.
 *
 * uk.co.strangeskies.reflection.codegen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.codegen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.codegen;

import static java.util.stream.Stream.concat;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Type.getInternalName;
import static uk.co.strangeskies.collection.stream.StreamUtilities.throwingMerger;
import static uk.co.strangeskies.reflection.codegen.CodeGenerationException.CODEGEN_PROPERTIES;

import javax.naming.OperationNotSupportedException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import uk.co.strangeskies.reflection.token.MethodMatcher;

/**
 * @author Elias N Vasylenko
 * 
 * @param <T>
 *          the intersection of the supertypes of the described class
 */
public class ClassDefinition<E, T> extends Definition<ClassDeclaration<E, T>> {
	private final String typeName;
	private final ClassRegister classSpace;

	protected ClassDefinition(ClassDeclaration<E, T> declaration, ClassRegister classSpace) {
		super(declaration);

		this.typeName = declaration.getSignature().getClassName();
		this.classSpace = classSpace;
	}

	/**
	 * @return the fully qualified class name
	 */
	public String getName() {
		return typeName;
	}

	public ClassRegister getRegister() {
		return classSpace;
	}

	public <U> ClassDefinition<E, T> defineConstructor(
			MethodMatcher<?, ? super U> methodMatcher,
			Block<? extends U> methodBody) {
		@SuppressWarnings("unchecked")
		MethodDeclaration<T, U> methodDeclaration = getDeclaration()
				.constructorDeclarations()
				.filter(m -> methodMatcher.match(m.getExecutableStub()))
				.reduce(throwingMerger())
				.map(m -> (MethodDeclaration<T, U>) m)
				.orElseThrow(
						() -> new CodeGenerationException(CODEGEN_PROPERTIES.cannotFindMethodOn(null, null)));

		MethodDefinition<T, U> definition = new MethodDefinition<>(methodDeclaration)
				.withBody(methodBody);

		return new ClassDefinition<>(
				getDeclaration(),
				classSpace.withMethodDefinition(methodDeclaration, definition));
	}

	public <U> ClassDefinition<E, T> defineMethod(
			MethodMatcher<?, ? super U> methodMatcher,
			Block<? extends U> methodBody) {
		@SuppressWarnings("unchecked")
		MethodDeclaration<T, U> methodDeclaration = concat(
				getDeclaration().methodDeclarations(),
				getDeclaration().staticMethodDeclarations())
						.filter(m -> methodMatcher.match(m.getExecutableStub()))
						.reduce(throwingMerger())
						.map(m -> (MethodDeclaration<T, U>) m)
						.orElseThrow(
								() -> new CodeGenerationException(
										CODEGEN_PROPERTIES.cannotFindMethodOn(null, null)));

		MethodDefinition<T, U> definition = new MethodDefinition<>(methodDeclaration)
				.withBody(methodBody);

		return new ClassDefinition<>(
				getDeclaration(),
				classSpace.withMethodDefinition(methodDeclaration, definition));
	}

	/**
	 * Derive a class definition which delegates to the given method intercepter
	 * object.
	 * <p>
	 * When multiple intercepters are specified for the same class definition,
	 * they will be attempted in the order they are given until one is found which
	 * is able to delegate.
	 * 
	 * @param intercepter
	 *          the intercepter
	 * @return the derived class definition
	 */
	public <U> ClassDefinition<E, T> delegate(MethodDelegation<? super T> intercepter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return getName();
	}

	public byte[] writeClass() {
		ClassReader stubClassReader = new ClassReader(getDeclaration().getStubClassBytes());
		ClassWriter classWriter = new ClassWriter(stubClassReader, COMPUTE_MAXS | COMPUTE_FRAMES);

		stubClassReader.accept(new ClassVisitor(ASM5, classWriter) {
			@Override
			public MethodVisitor visitMethod(
					int arg0,
					String arg1,
					String arg2,
					String arg3,
					String[] arg4) {
				MethodVisitor methodVisitor = super.visitMethod(arg0, arg1, arg2, arg3, arg4);

				methodVisitor.visitCode();
				methodVisitor.visitTypeInsn(NEW, getInternalName(OperationNotSupportedException.class));
				methodVisitor.visitInsn(DUP);
				try {
					methodVisitor.visitMethodInsn(
							INVOKESPECIAL,
							getInternalName(OperationNotSupportedException.class),
							"<init>",
							Type.getConstructorDescriptor(OperationNotSupportedException.class.getConstructor()),
							false);
				} catch (NoSuchMethodException | SecurityException e) {
					throw new AssertionError(e);
				}
				methodVisitor.visitInsn(ATHROW);
				methodVisitor.visitMaxs(0, 0);
				methodVisitor.visitEnd();

				return null;
			}
		}, 0);

		return classWriter.toByteArray();
	}

	@SuppressWarnings("unchecked")
	public Class<T> loadClass() {
		try {
			return (Class<T>) classSpace.loadClasses().loadClass(getName());
		} catch (ClassNotFoundException e) {
			throw new AssertionError(e);
		}
	}
}
