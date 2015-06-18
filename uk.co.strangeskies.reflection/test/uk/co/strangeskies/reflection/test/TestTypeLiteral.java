/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.reflection.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.test;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.testng.collections.Objects.ToStringHelper;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.InferenceVariable;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.ParameterizedTypes;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeToken.Capture;
import uk.co.strangeskies.reflection.TypeToken.Infer;
import uk.co.strangeskies.reflection.TypeToken.Preserve;
import uk.co.strangeskies.reflection.TypeToken.Wildcards;
import uk.co.strangeskies.utilities.Self;

/**
 * Informal series of tests...
 * 
 * @author eli
 *
 */
public class TestTypeLiteral {
	static class A<T> {
		public class B {}
	}

	static class B {
		public <T extends Number> void method999(T a, T b) {}

		public <T> void method(@SuppressWarnings("unchecked") Collection<T>... a) {}

		public <T> void bethod(Collection<T> a) {}

		public <T extends Number, U extends List<? super T>> Map<T, U> method2(
				List<T> a, U b) {
			return null;
		}

		public <T extends Number, U extends List<? super T>> U method(
				Collection<? extends T> a, U b) {
			return null;
		}

		public void moothod(Integer integer, Number number) {}

		public void moothod(Number integer, Integer number) {}

		public void moothod(Number integer, Number number) {}

		public <T extends Number, U extends List<? super T>> U method4(
				Collection<? extends T> a, U b) {
			return null;
		}

		public <T, R> void accept(Set<Invokable<T, R>> set) {}

		public strictfp <T extends Comparable<? super T>, U extends Collection<? extends Comparable<? super T>>> void bothways(
				T t, U u) {}

		public <U, R> Invokable<U, ? extends R> okay(
				Set<? extends Invokable<U, ? extends R>> candidates,
				List<? extends Type> parameters) {
			return null;
		}

		public static <T> T testeroonie(Class<T> t, String s) {
			return null;
		}
	}

	@SuppressWarnings("javadoc")
	public static void main(String... args) {
		test1();
		test2();
	}

	static void test1() {
		System.out.println(new TypeToken<SchemaNode.Effective<?, ?>>() {}
				.resolveSupertypeParameters(SchemaNode.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<HashSet<String>>() {}
				.resolveSupertypeParameters(Set.class));
		System.out.println();
		System.out.println();

		System.out.println("List with T = String: " + listOf(String.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<Collection<? super String>>() {}
				.resolveSubtypeParameters(HashSet.class));
		System.out.println();
		System.out.println();

		new TypeToken<Outer<Serializable>.Inner<String, HashSet<Serializable>, Set<String>>>() {}
				.getResolver();
		System.out.println();
		System.out.println();

		System.out
				.println(new TypeToken<Outer<Serializable>.Inner<String, HashSet<Serializable>, Set<String>>>() {}
						.resolveSubtypeParameters(Outer2.Inner3.class));
		System.out.println();
		System.out.println();

		System.out
				.println(new TypeToken<Outer2<Serializable, String>.Inner3<HashSet<Serializable>>>() {}
						.resolveSupertypeParameters(Outer.Inner.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<Outer<String>.Inner2<Double>>() {}
				.resolveSupertypeParameters(Outer.Inner.class));
		System.out.println();
		System.out.println();

		System.out.println("type test: "
				+ new TypeToken<String>() {}
						.resolveSupertypeParameters(Comparable.class));
		System.out.println();
		System.out.println();

		class SM<YO> {}
		class NM<V extends Number> extends SM<V> {}
		System.out.println(new TypeToken<NM<?>>() {});
		System.out.println(new TypeToken<NM<?>>() {}
				.resolveSupertypeParameters(SM.class));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken.over(new TypeToken<Nest<?>>() {}.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken.over(new TypeToken<C2<?>>() {}.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken.over(new TypeToken<C1<?>>() {}.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken.over(new TypeToken<Base<LeftN, RightN>>() {}
				.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken.over(new TypeToken<RightN>() {}
				.resolveSupertypeParameters(Base.class).getType()));
		System.out.println();
		System.out.println();

		System.out.println("TYPELITTEST: " + new TT<String>() {});
		System.out.println("TYPELITTEST-2: " + new YY<String>() {});
		System.out.println("TYPELITTEST-3: " + new G() {});
		System.out.println("TYPELITTEST-4: "
				+ new YY<Integer>() {}.resolveSupertypeParameters(Collection.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<Self<?>>() {}
				.isAssignableFrom(new TypeToken<Nest<?>>() {}));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken.over(new TypeToken<C1<? extends C1<?>>>() {}
				.getType()));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<SchemaNode.Effective<?, ?>>() {}
				.resolveSupertypeParameters(SchemaNode.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<Gurn<Integer>>() {}.getMethods()
				.iterator().next().infer());
		System.out.println();
		System.out.println();

		TypeToken<?> receiver = new TypeToken<BindingState>() {};
		System.out.println("RESOLVE 1:");
		System.out
				.println(receiver.resolveMethodOverload("bindingNode", int.class));
		System.out.println();
		System.out.println();

		receiver = new TypeToken<SchemaNodeConfigurator<?, ?>>() {};
		System.out.println("RESOLVE 2:");
		System.out.println(TypeToken.over(receiver.getType())
				.resolveMethodOverload("name", String.class));
		System.out.println();
		System.out.println();

		receiver = new TypeToken<ChildNodeConfigurator<?, ?>>() {};
		System.out.println("RESOLVE 3:");
		System.out.println(TypeToken.over(receiver.getType())
				.resolveMethodOverload("name", String.class));
		System.out.println();
		System.out.println();

		receiver = new TypeToken<DataBindingType.Effective<?>>() {};
		System.out.println("RESOLVE 4:");
		System.out.println(TypeToken.over(receiver.getType())
				.resolveMethodOverload("child", String.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<IncludeTarget>() {}.resolveMethodOverload(
				"includer", Model.class, Collection.class));
		System.out.println();
		System.out.println();
	}

	static <H extends C2<H>> void test2() {
		System.out
				.println("<T extends Number, U extends List<? super T>> U method4(Collection<? extends T> a, U b)");
		System.out
				.println("((B) null).method4((Collection<? extends Integer>) null, (List<? super Number>) null)");
		System.out.println(TypeToken
				.over(B.class)
				.resolveMethodOverload("method4",
						new TypeToken<Collection<? extends Integer>>() {}.getType(),
						new TypeToken<List<? super Number>>() {}.getType()).infer());
		System.out.println();

		System.out.println(new TypeParameter<H>() {});
		System.out.println(new TypeToken<List<String>>() {});
		System.out.println();

		System.out.println(ParameterizedTypes.from(HashSet.class,
				Arrays.asList(new InferenceVariable())).resolveSupertypeParameters(
				Collection.class));
		System.out.println();

		System.out.println(TypeToken
				.over(B.class)
				.resolveMethodOverload("bothways", String.class,
						new TypeToken<List<String>>() {}.getType()).infer());
		System.out.println();

		System.out.println(TypeToken.over(B.class).resolveMethodOverload("moothod",
				Integer.class, Number.class));
		System.out.println();

		System.out.println(TypeToken.over(B.class).resolveMethodOverload("moothod",
				Number.class, Integer.class));
		System.out.println();

		System.out.println(TypeToken.over(B.class).resolveMethodOverload("moothod",
				Number.class, Number.class));
		System.out.println();

		/*-
		System.out.println(TypeToken.of(B.class).resolveMethodOverload(
				"moothod", Integer.class, Integer.class));
		System.out.println();
		 */

		System.out.println(new TypeToken<List<? extends Number>>() {}.getType());
		System.out.println(TypeToken.over(Arrays.class)
				.resolveMethodOverload("asList", int.class, double.class)
				.withTargetType(new TypeToken<List<? extends Number>>() {}).infer());
		System.out.println();

		System.out.println(TypeToken
				.over(Arrays.class)
				.resolveMethodOverload("asList", int.class, double.class)
				.withTargetType(
						new TypeToken<List<? super Comparable<? extends Number>>>() {}));
		System.out.println();

		System.out
				.println(TypeToken
						.over(Arrays.class)
						.resolveMethodOverload("asList", int.class, double.class)
						.withTargetType(
								new TypeToken<@Infer List<? super Comparable<? extends Number>>>() {})
						.infer());
		System.out.println();

		System.out.println(TypeToken.over(B.class).resolveMethodOverload("method",
				new TypeToken<List<Integer>>() {}, new TypeToken<List<Number>>() {}));
		System.out.println();

		System.out.println(TypeToken.over(B.class).resolveMethodOverload("method2",
				new TypeToken<List<Integer>>() {},
				new TypeToken<List<Comparable<Integer>>>() {}));
		System.out.println();

		System.out.println(TypeToken
				.over(B.class)
				.resolveMethodOverload("method",
						new TypeToken<Collection<? super Integer>>() {}).infer());
		System.out.println();

		System.out.println(new TypeToken<B>() {}.resolveMethodOverload("okay",
				new TypeToken<Set<Invokable<H, ?>>>() {},
				new TypeToken<List<? extends Type>>() {}));
		System.out.println();

		System.out.println(new TypeToken<Collection<H>>() {}
				.resolveSubtypeParameters(HashSet.class));
		System.out.println();

		System.out.println(new TypeToken<B>() {}.resolveMethodOverload(
				"testeroonie", new TypeToken<Class<?>>() {}.getType(), String.class)
				.infer());
		System.out.println();

		TypeToken<?> targetClass = new TypeToken<List<?>>() {};
		TypeToken<?> resultClass = new TypeToken<Iterable<String>>() {};
		System.out.println(resultClass.isContainedBy(targetClass
				.resolveSupertypeParameters(resultClass.getRawType())));
		System.out.println();

		System.out.println(new TypeToken<List<?>>() {}
				.getExtending(Wildcards.CAPTURE));
		System.out.println();

		System.out.println(new TypeToken<List<?>>() {}.getExtending(
				Wildcards.CAPTURE).resolveSupertypeParameters(Collection.class));
		System.out.println();

		System.out.println(new TypeToken<List<BigInteger>>() {}.getExtending(
				Wildcards.INFER).resolveSupertypeParameters(Collection.class));
		System.out.println();

		System.out.println(new TypeToken<HashSet<?>>() {}.getResolver());
		System.out.println();

		System.out.println(new TypeToken<Collection<? extends String>>() {}
				.getExtending(Wildcards.INFER)
				.withUpperBound(new TypeToken<ArrayList<?>>() {}.getType())
				.getResolver().getBounds());
		System.out.println();

		System.out.println(new TypeToken<Collection<? extends String>>() {}
				.getExtending(Wildcards.INFER)
				.withUpperBound(new TypeToken<ArrayList<?>>() {}.getType()).infer());
		System.out.println();

		System.out.println(new TypeToken<List<? super Number>>() {}
				.getExtending(Wildcards.INFER)
				.resolveMethodOverload("add", Integer.class).getReceiverType().infer());
		System.out.println();

		System.out.println(new TypeToken<@Infer List<@Infer ? extends Number>>() {}
				.getResolver().getBounds());
		System.out.println();

		System.out.println(new TypeToken<@Infer List<? extends Number>>() {}
				.getExtending(Wildcards.INFER)
				.resolveMethodOverload("add", Integer.class).getReceiverType()
				.resolveMethodOverload("add", Double.class).infer().getReceiverType());
		System.out.println();

		System.out
				.println(new TypeToken<@Infer Collection<? extends String>>() {}
						.getExtending(Wildcards.INFER)
						.withUpperBound(
								new TypeToken<ArrayList<? super String>>() {}.getType())
						.infer().resolveSupertypeParameters(Iterable.class));
		System.out.println();

		System.out.println(new TypeToken<@Infer List<? extends Number>>() {}
				.getExtending(Wildcards.INFER)
				.resolveMethodOverload("add", Integer.class).getReceiverType()
				.resolveMethodOverload("add", Double.class).getReceiverType().infer());
		System.out.println();

		System.out
				.println(new TypeToken<@Infer ArrayList<? super Integer>>() {}
						.getExtending(Wildcards.INFER).resolveConstructorOverload()
						.withTargetType(new TypeToken<Iterable<? extends Number>>() {})
						.infer());
		System.out.println();

		System.out.println(new TypeToken<@Infer HashMap<?, ?>>() {}
				.getExtending(Wildcards.INFER)
				.resolveConstructorOverload()
				.withTargetType(
						new TypeToken<Map<? extends String, ? extends Number>>() {})
				.infer());
		System.out.println();

		System.out.println(new TypeToken<HashMap<String, Number>>() {}
				.getExtending(Wildcards.INFER).resolveConstructorOverload()
				.withTargetType(new TypeToken<@Infer Map<?, ?>>() {}).infer());
		System.out.println();

		System.out.println(new TypeToken<Set<String>>() {}.resolveMethodOverload(
				"addAll", new TypeToken<@Infer List<?>>() {}).inferParameterTypes());
		System.out.println();

		System.out.println(new TypeToken<@Infer Set<?>>() {}.resolveMethodOverload(
				"addAll", new TypeToken<List<Stream<?>>>() {}).inferParameterTypes());
		System.out.println();

		System.out.println(new TypeToken<ChildNode<?, ?>>() {}.getType()
				+ " + + + "
				+ new TypeToken<ChildNode<?, ?>>() {}
						.isAssignableTo(new TypeToken<ChildNode<?, ?>>() {}));
		System.out.println();

		System.out.println(new TypeToken<ChildNode<?, ?>>() {}.getType()
				+ " ~ = ~ " + new TypeToken<ChildNode<?, ?>>() {}.resolve());
		System.out.println();

		System.out.println(getIteratorType(new TypeToken<String>() {}));
		System.out.println();

		System.out.println(getIteratorType2(String.class));
		System.out.println();

		System.out.println(getIteratorType3(new TypeToken<String>() {}));
		System.out.println();

		System.out
				.println(getIteratorExtending(new TypeToken<@Infer List<? extends String>>() {}));
		System.out.println();

		System.out.println(getIteratorExtending(
				new TypeToken<@Infer List<? extends String>>() {}).infer());
		System.out.println();

		Invokable<?, ?> blurner = new TypeToken<@Infer Blurn<? extends List<? extends Number>>>() {}
				.resolveMethodOverload("blurn").withReceiverType(
						new TypeToken<Gurn<Integer>>() {});
		System.out.println(blurner);
		System.out.println();

		try {
			System.out.println(Invokable.over(Blurn.class.getMethod("blurn"),
					new TypeToken<Blurn<Long>>() {}));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		System.out.println();

		System.out.println(new TypeToken<SchemaNode<?, ?>>() {}
				.resolveMethodOverload("children").withTargetType(
						getIteratorExtending(new TypeToken<@Infer ChildNode<?, ?>>() {})));
		System.out.println();

		System.out.println(new TypeToken<ChoiceNode>() {}.resolveMethodOverload(
				"getName", new ArrayList<>()));
		System.out.println();

		System.out
				.println(new TypeToken<@Infer LinkedHashSet<?>>() {}
						.resolveMethodOverload("add", new TypeToken<StringBuffer>() {})
						.infer());
		System.out.println();

		System.out.println(new TypeToken<@Infer Set<?>>() {}.resolveMethodOverload(
				"addAll",
				new TypeToken<@Infer ArrayList<? super Integer>>() {}
						.resolveConstructorOverload().getReturnType()).infer());
		System.out.println();

		System.out.println(new TypeToken<@Infer HashSet<? super Double>>() {}
				.resolveConstructorOverload(
						new TypeToken<@Infer ArrayList<? super Integer>>() {}
								.resolveConstructorOverload().getReturnType()).getReturnType()
				.infer());
		System.out.println();

		TypeToken<?> bball = new TypeToken<@Infer HashSet<? super Double>>() {}
				.resolveConstructorOverload(
						new TypeToken<@Infer ArrayList<? super Integer>>() {}
								.resolveConstructorOverload().getReturnType()).getReturnType();
		System.out.println(bball.getResolver().getBounds());
		System.out.println(bball.deepCopy().getResolver().getBounds());
		System.out.println();

		TypeToken<?> eqselente = new TypeToken<@Infer SchemaNode<?, ?>>() {};
		System.out.println(eqselente);
		System.out.println(eqselente.getResolver().getBounds());
		eqselente = eqselente.withUpperBound(eqselente.deepCopy());
		System.out.println(eqselente.getResolver().getBounds());
		System.out.println(eqselente.infer());
		System.out.println();

		System.out.println(new TypeToken<@Infer List<? extends String>>() {});
		System.out.println();

		System.out.println(new TypeToken<@Infer List<? extends String>>() {});
		System.out.println();

		System.out.println(new TypeToken<@Infer List<? extends String>>() {});
		System.out.println();

		System.out.println(new TypeToken<@Infer List<? extends String>>() {});
		System.out.println();

		System.out.println(new TypeToken<List<? extends Long>>() {}
				.resolveSupertypeParameters(Iterable.class).getType());
		System.out.println();

		System.out.println(new TypeToken<List<? extends Long>>() {}
				.resolveSupertypeParameters(Iterable.class)
				.resubstituteCapturedWildcards().getType());
		System.out.println();

		System.out.println(new TypeToken<List<? extends @Infer Set<?>>>() {});
		System.out.println();

		System.out.println(new TypeToken<C1<C2<String>>>() {}
				.isAssignableFrom(new TypeToken<C2<String>>() {}));
		System.out.println();

		Imports imports = new Imports().withImports(Capture.class, Preserve.class,
				Test2.class, List.class, Type.class);
		String annotationString = AnnotatedTypes
				.toString(
						new TypeToken<@Test(thisIsTest = "yeah!", wat = 2.5) List<@Test2(idk = "helo", wat = 2) ? extends @Preserve Number> @Capture [] @Infer []>() {}
								.getAnnotatedDeclaration(), imports);
		System.out.println(annotationString);
		System.out.println();

		System.out.println(AnnotatedTypes.fromString("java.lang.reflect.Type"));
		System.out.println();

		System.out.println(AnnotatedTypes.fromString("Type", imports));
		System.out.println();

		System.out.println(Annotations.getParser(imports).getAnnotation()
				.parse("@Capture"));
		System.out.println();

		System.out.println(Annotations.getParser(imports).getAnnotationList()
				.parse("@Capture"));
		System.out.println();

		System.out.println(Annotations.getParser(imports).getAnnotationList()
				.parse("@Capture @Preserve()"));
		System.out.println();

		System.out.println(AnnotatedTypes.fromString(
				"@Capture java.lang.reflect.Type", imports));
		System.out.println();

		imports = imports.withImport(Test.class);

		System.out.println(Annotations.getParser(imports).getProperty()
				.parse("thisIsTest = \"yeah!\""));
		System.out.println();

		System.out.println(Annotations.getParser(imports).getProperty()
				.parse("stupid = 5"));
		System.out.println();

		System.out.println(Annotations.getParser(imports).getPropertyMap()
				.parse("wat = 2.5"));
		System.out.println();

		System.out.println(AnnotatedTypes.fromString(
				"@Capture() java.lang.reflect.Type", imports));
		System.out.println();

		System.out.println(Annotations.getParser(imports).getPropertyMap()
				.parse("thisIsTest = \"yeah!\", wat = 2.5"));
		System.out.println();

		System.out.println(Annotations.fromString(
				"@Test(thisIsTest = \"yeah!\", wat = 2.5)", imports));
		System.out.println();

		System.out.println(AnnotatedTypes.fromString(
				"java.util.ArrayList<java.lang.String>", imports));
		System.out.println();

		System.out
				.println(AnnotatedTypes
						.fromString(
								"@Test(thisIsTest = \"yeah!\", wat = 2.5) java.util.ArrayList<java.lang.String>",
								imports));
		System.out.println();

		System.out
				.println(AnnotatedTypes
						.fromString(
								"@Test(thisIsTest = \"yeah!\", wat = .2) java.util.ArrayList<@Capture java.lang.String>",
								imports));
		System.out.println();

		System.out.println(Annotations.getParser(imports).getAnnotation()
				.parse("@Test2(idk = \"helo\", wat = 2)"));

		System.out.println(AnnotatedTypes.fromString(annotationString, imports));
		System.out.println();

		System.out.println(new TypeToken<@Infer TreeSet<? extends C2<?>>>() {});
		System.out.println();

		System.out.println(new TypeToken<SchemaNode<?, ?>>() {}
				.isAssignableTo(new TypeToken<SchemaNode<?, ?>>() {}));
		System.out.println();

		System.out.println(new TypeToken<C1<? extends C2<String>>>() {});
		System.out.println();

		System.out.println(new TypeToken<C1<? extends C2<?>>>() {});
		System.out.println();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	@interface Test2 {
		String idk();

		long wat();
	};

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	@interface Test {
		String thisIsTest();

		double wat();
	};

	static class C1<T extends C1<T>> {}

	static class C2<U> extends C1<C2<U>> {}

	static class C22<U> extends C1<C22<U>> {}

	static class C3<T extends C3<?>> {}

	static class C4<V, W> extends C3<C4<V, W>> {}

	static class C5<X extends C3<X>> {}

	static class C6<Y, Z> extends C3<C6<Z, Y>> {}

	private static <U> TypeToken<Iterable<? extends U>> getIteratorExtending(
			TypeToken<U> type) {
		return new TypeToken<Iterable<? extends U>>() {}.withTypeArgument(
				new TypeParameter<U>() {}, type);
	}

	static <T> TypeToken<List<T>> listOf(Class<T> sub) {
		return new TypeToken<List<T>>() {}.withTypeArgument(
				new TypeParameter<T>() {}, sub);
	}

	static <T> TypeToken<List<T>> listOf(TypeToken<T> sub) {
		return new TypeToken<List<T>>() {}.withTypeArgument(
				new TypeParameter<T>() {}, sub);
	}

	static <U> TypeToken<Iterable<U>> getIteratorType(TypeToken<U> type) {
		return new TypeToken<Iterable<U>>() {}.withTypeArgument(
				new TypeParameter<U>() {}, type);
	}

	static <U> TypeToken<Iterable<U>> getIteratorType2(Class<U> type) {
		return new TypeToken<Iterable<U>>() {}.withTypeArgument(
				new TypeParameter<U>() {}, type);
	}

	static <U> TypeToken<Iterable<? extends U>> getIteratorType3(TypeToken<U> type) {
		return new TypeToken<Iterable<? extends U>>() {}.withTypeArgument(
				new TypeParameter<U>() {}, type);
	}
}

class TT<TTT> extends TypeToken<TTT> {}

class YY<YT> extends TT<Set<YT>> {}

class G extends YY<List<String>> {}

class Outer<T> {
	public class Inner<N extends T, J extends Collection<? extends T>, P> {}

	public class Inner2<M extends Number & Comparable<?>> extends
			Outer<Comparable<?>>.Inner<M, List<Integer>, T> {}
}

class Outer2<F, Z extends F> {
	public class Inner3<X extends Set<F>> extends Outer<F>.Inner<Z, X, Set<Z>> {
		Inner3() {
			new Outer<F>() {}.super();
		}
	}
}

interface IncludeTarget {
	<T> void include(Model<T> model, T object);

	<T> void include(Model<T> model, Collection<? extends T> objects);

	void includer(Model<?> model, Object object);

	void includer(Model<?> model, Collection<?> objects);
}

interface Model<T> {}

interface BindingState {
	SchemaNode.Effective<?, ?> bindingNode(int parent);
}

interface SchemaNode<S extends SchemaNode<S, E>, E extends SchemaNode.Effective<S, E>> {
	interface Effective<S extends SchemaNode<S, E>, E extends Effective<S, E>>
			extends SchemaNode<S, E> {}

	String getName();

	ChildNode<?, ?> child(String name);

	List<? extends ChildNode<?, ?>> children();
}

interface ChildNode<S extends ChildNode<S, E>, E extends ChildNode.Effective<S, E>>
		extends SchemaNode<S, E> {
	interface Effective<S extends ChildNode<S, E>, E extends Effective<S, E>>
			extends ChildNode<S, E>, SchemaNode.Effective<S, E> {}
}

interface ChoiceNode extends ChildNode<ChoiceNode, ChoiceNode.Effective> {
	interface Effective extends ChoiceNode,
			ChildNode.Effective<ChoiceNode, ChoiceNode.Effective> {}
}

interface SchemaNodeConfigurator<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<N, ?>> {
	public S name(String name);
}

interface ChildNodeConfigurator<S extends ChildNodeConfigurator<S, N>, N extends ChildNode<N, ?>>
		extends SchemaNodeConfigurator<S, N> {}

interface DataBindingType<T> extends
		BindingNode<T, DataBindingType<T>, DataBindingType.Effective<T>> {
	interface Effective<T> extends DataBindingType<T>,
			BindingNode.Effective<T, DataBindingType<T>, Effective<T>> {}

	List<? extends Model<? super T>> baseModel();
}

interface BindingNode<T, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
		extends SchemaNode<S, E> {
	interface Effective<T, S extends BindingNode<T, S, E>, E extends Effective<T, S, E>>
			extends BindingNode<T, S, E>, SchemaNode.Effective<S, E> {}
}

class Nest<T extends Set<Nest<T>>> implements Self<Nest<T>> {
	@Override
	public Nest<T> copy() {
		return null;
	}
}

interface Blurn<T> {
	Set<T> blurn();
}

interface Gurn<X> extends Blurn<List<X>> {
	@Override
	HashSet<List<X>> blurn();
}

class Base<T extends Base<U, T>, U extends Base<T, U>> {}

class LeftN extends Base<RightN, LeftN> {}

class RightN extends Base<LeftN, RightN> {}
