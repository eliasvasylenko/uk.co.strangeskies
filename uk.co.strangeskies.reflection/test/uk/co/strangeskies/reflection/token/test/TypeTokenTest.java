/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.token.test;

import static uk.co.strangeskies.reflection.token.ExecutableToken.getStaticMethods;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.ParameterizedType;
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

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.AnnotatedWildcardTypes;
import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeParameter;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypeToken.Capture;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;
import uk.co.strangeskies.reflection.token.TypeToken.Preserve;
import uk.co.strangeskies.reflection.token.TypeToken.Wildcards;
import uk.co.strangeskies.utilities.Self;

/**
 * Tests for {@link TypeToken} class.
 * 
 * @author Elias N Vasylenko
 */
@SuppressWarnings("javadoc")
public class TypeTokenTest {
	static class A<T> {
		public class B {}
	}

	static class B {
		public <T extends Number> void method999(T a, T b) {}

		public <T> void method(@SuppressWarnings("unchecked") Collection<T>... a) {}

		public <T> void bethod(Collection<T> a) {}

		public <T extends Number, U extends List<? super T>> Map<T, U> method2(List<T> a, U b) {
			return null;
		}

		public <T extends Number, U extends List<? super T>> U method(Collection<? extends T> a, U b) {
			return null;
		}

		public void moothod(Integer integer, Number number) {}

		public void moothod(Number integer, Integer number) {}

		public void moothod(Number integer, Number number) {}

		public <T extends Number, U extends List<? super T>> U method4(Collection<? extends T> a, U b) {
			return null;
		}

		public <T, R> void accept(Set<ExecutableToken<T, R>> set) {}

		public strictfp <T extends Comparable<? super T>, U extends Collection<? extends Comparable<? super T>>> void bothways(
				T t, U u) {}

		public <U, R> ExecutableToken<U, ? extends R> okay(Set<? extends ExecutableToken<U, ? extends R>> candidates,
				List<? extends Type> parameters) {
			return null;
		}

		public static <T> T testeroonie(Class<T> t, String s) {
			return null;
		}
	}

	@Test
	public void nullTypeTokenTest() {
		TypeToken<?> nullType = TypeToken.overNull();
		TypeToken<String> stringType = new TypeToken<String>() {};

		Assert.assertTrue(nullType.isAssignableTo(String.class));
		Assert.assertTrue(nullType.isAssignableFrom(String.class));

		Assert.assertTrue(nullType.isAssignableTo(stringType));
		Assert.assertTrue(nullType.isAssignableFrom(stringType));

		Assert.assertTrue(stringType.isAssignableTo(nullType));
		Assert.assertTrue(stringType.isAssignableFrom(nullType));
	}

	@Test
	public void makeTestsFromThese() {
		System.out.println("#" + Types.leastUpperBound(Integer.class, Double.class,
				new TypeToken<Comparable<? extends Number>>() {}.getType()));

		System.out.println("#" + Types.leastUpperBound(new TypeToken<Comparable<? extends Number>>() {}.getType(),
				Integer.class, Double.class));

		List<ParameterizedType> bestTypes = Arrays.asList(
				(ParameterizedType) new TypeToken<Comparable<? extends Number>>() {}.getType(),
				(ParameterizedType) new TypeToken<Comparable<Double>>() {}.getType(),
				(ParameterizedType) new TypeToken<Comparable<Integer>>() {}.getType());
		Type bestType = ((ParameterizedType) Types.best(Comparable.class, bestTypes)).getActualTypeArguments()[0];
		System.out.println("!  " + bestType);

		List<ParameterizedType> bestTypes2 = Arrays.asList(
				(ParameterizedType) new TypeToken<Comparable<Double>>() {}.getType(),
				(ParameterizedType) new TypeToken<Comparable<Integer>>() {}.getType(),
				(ParameterizedType) new TypeToken<Comparable<? extends Number>>() {}.getType());
		Type bestType2 = ((ParameterizedType) Types.best(Comparable.class, bestTypes2)).getActualTypeArguments()[0];
		System.out.println("!  " + bestType2);

		List<ParameterizedType> bestTypes3 = Arrays.asList(
				(ParameterizedType) new TypeToken<Comparable<Double>>() {}.getType(),
				(ParameterizedType) new TypeToken<Comparable<Integer>>() {}.getType());
		Type bestType3 = ((ParameterizedType) Types.best(Comparable.class, bestTypes3)).getActualTypeArguments()[0];
		System.out.println("!  " + bestType3);

		System.out.println();
		System.out.println(Types.leastContainingArgument(bestType, bestType3));
	}

	// @Test
	public void supertypeParameterTest() {
		Assert.assertEquals("uk.co.strangeskies.reflection.test.SchemaNode<?, ?>",
				new TypeToken<SchemaNode.Effective<?, ?>>() {}.resolveSupertypeParameters(SchemaNode.class)
						.resubstituteCapturedWildcards().toString());
	}

	@Test
	public void hugeTest1() {
		System.out.println(new TypeToken<HashSet<String>>() {}.resolveSupertypeParameters(Set.class));
		System.out.println();
		System.out.println();

		System.out.println("List with T = String: " + listOf(String.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<Collection<? super String>>() {}.resolveSubtypeParameters(HashSet.class));
		System.out.println();
		System.out.println();

		new TypeToken<Outer1<Serializable>.Inner<String, HashSet<Serializable>, Set<String>>>() {}.getResolver();
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<Outer1<Serializable>.Inner<String, HashSet<Serializable>, Set<String>>>() {}
				.resolveSubtypeParameters(Outer2.Inner3.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<Outer2<Serializable, String>.Inner3<HashSet<Serializable>>>() {}
				.resolveSupertypeParameters(Outer1.Inner.class));
		System.out.println();
		System.out.println();

		System.out
				.println(new TypeToken<Outer1<String>.Inner2<Double>>() {}.resolveSupertypeParameters(Outer1.Inner.class));
		System.out.println();
		System.out.println();

		System.out.println("type test: " + new TypeToken<String>() {}.resolveSupertypeParameters(Comparable.class));
		System.out.println();
		System.out.println();

		class SM<YO> {}
		class NM<V extends Number> extends SM<V> {}
		System.out.println(new TypeToken<NM<?>>() {});
		System.out.println(new TypeToken<NM<?>>() {}.resolveSupertypeParameters(SM.class));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken.overType(new TypeToken<Nest<?>>() {}.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken.overType(new TypeToken<C2<?>>() {}.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken.overType(new TypeToken<C1<?>>() {}.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken.overType(new TypeToken<Base<LeftN, RightN>>() {}.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken.overType(new TypeToken<RightN>() {}.resolveSupertypeParameters(Base.class).getType()));
		System.out.println();
		System.out.println();

		System.out.println("TYPELITTEST: " + new TT<String>() {});
		System.out.println("TYPELITTEST-2: " + new YY<String>() {});
		System.out.println("TYPELITTEST-3: " + new G() {});
		System.out.println("TYPELITTEST-4: " + new YY<Integer>() {}.resolveSupertypeParameters(Collection.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<Self<?>>() {}.isAssignableFrom(new TypeToken<Nest<?>>() {}));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken.overType(new TypeToken<C1<? extends C1<?>>>() {}.getType()));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<SchemaNode.Effective<?, ?>>() {}.resolveSupertypeParameters(SchemaNode.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<Gurn<Integer>>() {}.getMethods().findAny().get().infer());
		System.out.println();
		System.out.println();

		TypeToken<?> receiver = new TypeToken<BindingState>() {};
		System.out.println("RESOLVE 1:");
		System.out.println(receiver.getMethods().named("bindingNode").resolveOverload(int.class));
		System.out.println();
		System.out.println();

		receiver = new TypeToken<SchemaNodeConfigurator<?, ?>>() {};
		System.out.println("RESOLVE 2:");
		System.out.println(TypeToken.overType(receiver.getType()).getMethods().named("name").resolveOverload(String.class));
		System.out.println();
		System.out.println();

		receiver = new TypeToken<ChildNodeConfigurator<?, ?>>() {};
		System.out.println("RESOLVE 3:");
		System.out.println(TypeToken.overType(receiver.getType()).getMethods().named("name").resolveOverload(String.class));
		System.out.println();
		System.out.println();

		receiver = new TypeToken<DataBindingType.Effective<?>>() {};
		System.out.println("RESOLVE 4:");
		System.out
				.println(TypeToken.overType(receiver.getType()).getMethods().named("child").resolveOverload(String.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<IncludeTarget>() {}.getMethods().named("includer").resolveOverload(Model.class,
				Collection.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<Map<String, ?>>() {}.getThisType());
		System.out.println();
		System.out.println();
	}

	// @Test
	public <H extends C2<H>> void huge2Test() {
		System.out.println("<T extends Number, U extends List<? super T>> U method4(Collection<? extends T> a, U b)");
		System.out.println("((B) null).method4((Collection<? extends Integer>) null, (List<? super Number>) null)");
		System.out.println(TypeToken.overType(B.class).getMethods().named("method4")
				.resolveOverload(new TypeToken<Collection<? extends Integer>>() {}.getType(),
						new TypeToken<List<? super Number>>() {}.getType())
				.infer());
		System.out.println();

		System.out.println(new TypeParameter<H>() {});
		System.out.println(new TypeToken<List<String>>() {});
		System.out.println();

		System.out.println(TypeToken.overType(B.class).getMethods().named("bothways")
				.resolveOverload(String.class, new TypeToken<List<String>>() {}.getType()).infer());
		System.out.println();

		System.out.println(
				TypeToken.overType(B.class).getMethods().named("moothod").resolveOverload(Integer.class, Number.class));
		System.out.println();

		System.out.println(
				TypeToken.overType(B.class).getMethods().named("moothod").resolveOverload(Number.class, Integer.class));
		System.out.println();

		System.out
				.println(TypeToken.overType(B.class).getMethods().named("moothod").resolveOverload(Number.class, Number.class));
		System.out.println();

		/*-
		System.out.println(TypeToken.of(B.class).getMethods(
				"moothod", Integer.class, Integer.class));
		System.out.println();
		 */

		System.out.println(new TypeToken<List<? extends Number>>() {}.getType());
		System.out.println(getStaticMethods(Arrays.class).named("asList").resolveOverload(int.class, double.class)
				.withTargetType(new TypeToken<List<? extends Number>>() {}).infer());
		System.out.println();

		System.out.println(getStaticMethods(Arrays.class).named("asList").resolveOverload(int.class, double.class)
				.withTargetType(new TypeToken<List<? super Comparable<? extends Number>>>() {}));
		System.out.println();

		System.out.println(getStaticMethods(Arrays.class).named("asList").resolveOverload(int.class, double.class)
				.getResolver().getBounds());
		System.out.println();

		System.out.println(getStaticMethods(Arrays.class).named("asList").resolveOverload(int.class, double.class)
				.withTargetType(new TypeToken<List<? super Comparable<? extends Number>>>() {}).infer());
		System.out.println();

		System.out.println(TypeToken.overType(B.class).getMethods().named("method")
				.resolveOverload(new TypeToken<List<Integer>>() {}, new TypeToken<List<Number>>() {}));
		System.out.println();

		System.out.println(new TypeToken<B>() {}.getMethods().named("method2")
				.resolveOverload(new TypeToken<List<Integer>>() {}, new TypeToken<List<Comparable<Integer>>>() {}));
		System.out.println();

		System.out.println(TypeToken.overType(B.class).getMethods().named("method")
				.resolveOverload(new TypeToken<Collection<? super Integer>>() {}).infer());
		System.out.println();

		System.out.println(new TypeToken<B>() {}.getMethods().named("okay")
				.resolveOverload(new TypeToken<Set<ExecutableToken<H, ?>>>() {}, new TypeToken<List<? extends Type>>() {}));
		System.out.println();

		System.out.println(new TypeToken<Collection<H>>() {}.resolveSubtypeParameters(HashSet.class));
		System.out.println();

		System.out.println(getStaticMethods(B.class).named("testeroonie")
				.resolveOverload(new TypeToken<Class<?>>() {}.getType(), String.class).infer());
		System.out.println();

		TypeToken<?> targetClass = new TypeToken<List<?>>() {};
		TypeToken<?> resultClass = new TypeToken<Iterable<String>>() {};
		System.out.println(resultClass.isContainedBy(targetClass.resolveSupertypeParameters(resultClass.getRawType())));
		System.out.println();

		System.out.println(new TypeToken<List<?>>() {}.getExtending(Wildcards.CAPTURE));
		System.out.println();

		System.out.println(
				new TypeToken<List<?>>() {}.getExtending(Wildcards.CAPTURE).resolveSupertypeParameters(Collection.class));
		System.out.println();

		System.out.println(new TypeToken<List<BigInteger>>() {}.getExtending(Wildcards.INFER)
				.resolveSupertypeParameters(Collection.class));
		System.out.println();

		System.out.println(new TypeToken<HashSet<?>>() {}.getResolver());
		System.out.println();

		System.out.println(new TypeToken<Collection<? extends String>>() {}.getExtending(Wildcards.INFER)
				.withUpperBound(new TypeToken<ArrayList<?>>() {}.getType()).getResolver().getBounds());
		System.out.println();

		System.out.println(new TypeToken<Collection<? extends String>>() {}.getExtending(Wildcards.INFER)
				.withUpperBound(new TypeToken<ArrayList<?>>() {}.getType()).infer());
		System.out.println();

		System.out.println(new TypeToken<List<? super Number>>() {}.getExtending(Wildcards.INFER).getMethods().named("add")
				.resolveOverload(Integer.class).getReceiverType().infer());
		System.out.println();

		System.out.println(new TypeToken<@Infer List<@Infer ? extends Number>>() {}.getResolver().getBounds());
		System.out.println();

		System.out.println(new TypeToken<@Infer List<? extends Number>>() {});
		System.out.println(
				AnnotatedTypes.wrap(new TypeToken<@Infer List<? extends Number>>() {}.getClass().getAnnotatedSuperclass()));
		System.out.println(new @Infer TypeToken<List<@Infer ? extends Number>>() {});
		System.out.println(new @Infer TypeToken<List<@Infer ? extends Number>>() {}.getExtending(Wildcards.INFER));
		System.out.println(new @Infer TypeToken<List<? extends Number>>() {}.getExtending(Wildcards.INFER));
		System.out.println(
				new TypeToken<@Infer List<? extends Number>>() {}.getExtending(Wildcards.INFER).getResolver().getBounds());

		System.out.println(new TypeToken<@Infer Collection<? extends String>>() {}.getExtending(Wildcards.INFER)
				.withUpperBound(new TypeToken<ArrayList<? super String>>() {}.getType()).infer()
				.resolveSupertypeParameters(Iterable.class));
		System.out.println();

		System.out.println(new @Preserve TypeToken<List<? extends Number>>() {}.getExtending(Wildcards.INFER).getMethods()
				.named("add").resolveOverload(Integer.class).getReceiverType().infer());
		System.out.println();

		System.out.println(new TypeToken<List<? extends Number>>() {}.getExtending(Wildcards.INFER).getMethods()
				.named("add").resolveOverload(Integer.class).getReceiverType().getMethods().named("add")
				.resolveOverload(Double.class).getReceiverType().infer());
		System.out.println();

		System.out.println(new TypeToken<ArrayList<? super Integer>>() {}.getDeclaredConstructors().resolveOverload()
				.withTargetType(new TypeToken<Iterable<? extends Number>>() {}).infer());
		System.out.println();

		System.out.println(new TypeToken<HashMap<?, ?>>() {}.getExtending(Wildcards.INFER).getConstructors()
				.resolveOverload().withTargetType(new TypeToken<Map<? extends String, ? extends Number>>() {}).infer());
		System.out.println();

		System.out.println(new TypeToken<HashMap<String, Number>>() {}.getExtending(Wildcards.INFER).getConstructors()
				.resolveOverload().withTargetType(new TypeToken<@Infer Map<?, ?>>() {}).infer());
		System.out.println();

		System.out.println(new TypeToken<List<?>>() {}.getAnnotatedDeclaration());
		System.out.println(new TypeToken<Set<String>>() {}.getMethods().named("addAll")
				.resolveOverload(new TypeToken<List<@Infer ?>>() {}).inferParameterTypes());
		System.out.println();

		System.out.println(new TypeToken<Set<?>>() {}.getMethods().named("addAll")
				.resolveOverload(new TypeToken<List<Stream<?>>>() {}).inferParameterTypes());
		System.out.println();

		System.out.println(new TypeToken<ChildNode<?, ?>>() {}.getType() + " + + + "
				+ new TypeToken<ChildNode<?, ?>>() {}.isAssignableTo(new TypeToken<ChildNode<?, ?>>() {}));
		System.out.println();

		System.out.println(
				new TypeToken<ChildNode<?, ?>>() {}.getType() + " ~ = ~ " + new TypeToken<ChildNode<?, ?>>() {}.resolve());
		System.out.println();

		System.out.println(getIteratorType(new TypeToken<String>() {}));
		System.out.println();

		System.out.println(getIteratorType2(String.class));
		System.out.println();

		System.out.println(getIteratorType3(new TypeToken<String>() {}));
		System.out.println();

		System.out.println(getIteratorExtending(new TypeToken<@Infer List<? extends String>>() {}));
		System.out.println();

		System.out.println(getIteratorExtending(new TypeToken<@Infer List<? extends String>>() {}).infer());
		System.out.println();

		ExecutableToken<?, ?> blurner = new TypeToken<Blurn<? extends List<? extends Number>>>() {}.getMethods()
				.named("blurn").resolveOverload().withReceiverType(new TypeToken<Gurn<Integer>>() {});
		System.out.println(blurner);
		System.out.println();

		try {
			System.out.println(ExecutableToken.overMethod(Blurn.class.getMethod("blurn"), new TypeToken<Blurn<Long>>() {}));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		System.out.println();

		System.out.println(new TypeToken<SchemaNode<?, ?>>() {}.getMethods().named("children").resolveOverload()
				.withTargetType(getIteratorExtending(new TypeToken<ChildNode<?, ?>>() {})));
		System.out.println();

		System.out.println(new TypeToken<ChoiceNode>() {}.getMethods().named("getName").resolveOverload(new ArrayList<>()));
		System.out.println();

		System.out.println(new TypeToken<LinkedHashSet<?>>() {}.getMethods().named("add")
				.resolveOverload(new TypeToken<StringBuffer>() {}).infer());
		System.out.println();

		System.out.println(new TypeToken<Set<?>>() {}.getMethods().named("addAll")
				.resolveOverload(
						new TypeToken<@Infer ArrayList<? super Integer>>() {}.getConstructors().resolveOverload().getReturnType())
				.infer());
		System.out.println();

		System.out.println(new TypeToken<@Infer HashSet<? super Double>>() {}.getConstructors()
				.resolveOverload(
						new TypeToken<@Infer ArrayList<? super Integer>>() {}.getConstructors().resolveOverload().getReturnType())
				.getReturnType().infer());
		System.out.println();

		TypeToken<?> bball = new TypeToken<@Infer HashSet<? super Double>>() {}.getConstructors()
				.resolveOverload(
						new TypeToken<ArrayList<? super Integer>>() {}.getConstructors().resolveOverload().getReturnType())
				.getReturnType();
		System.out.println(bball.getResolver().getBounds());
		System.out.println(bball.deepCopy().getResolver().getBounds());
		System.out.println();

		TypeToken<?> eqselente = new TypeToken<SchemaNode<?, ?>>() {};
		System.out.println(eqselente);
		System.out.println(eqselente.getResolver().getBounds());

		TypeToken<?> dc = eqselente.deepCopy();
		System.out.println(dc);
		System.out.println(dc.getResolver().getBounds());

		eqselente = eqselente.withUpperBound(dc);
		System.out.println(eqselente.getResolver().getBounds());
		System.out.println(eqselente.infer());
		System.out.println();

		System.out.println(new TypeToken<List<? extends String>>() {});
		System.out.println();

		System.out.println(new TypeToken<List<? extends String>>() {});
		System.out.println();

		System.out.println(new TypeToken<List<? extends String>>() {});
		System.out.println();

		System.out.println(new TypeToken<List<? extends String>>() {});
		System.out.println();

		System.out.println(new TypeToken<List<? extends Long>>() {}.resolveSupertypeParameters(Iterable.class).getType());
		System.out.println();

		System.out.println(new TypeToken<List<? extends Long>>() {}.resolveSupertypeParameters(Iterable.class)
				.resubstituteCapturedWildcards().getType());
		System.out.println();

		System.out.println(new TypeToken<List<? extends @Capture Set<?>>>() {});
		System.out.println();

		System.out.println(new TypeToken<C1<C2<String>>>() {}.isAssignableFrom(new TypeToken<C2<String>>() {}));
		System.out.println();

		Imports imports = Imports.empty().withImports(Capture.class, Preserve.class, Test2.class, List.class, Type.class);
		String annotationString = AnnotatedTypes.toString(
				new TypeToken<@Test3(thisIsTest = "yeah!", wat = 2.5f) List<@Test2(
						idk = "helo",
						wat = 2) ? extends @Preserve Number> @Capture [] @Infer []>() {}.getAnnotatedDeclaration(),
				imports);
		System.out.println(annotationString);
		System.out.println();

		System.out.println(AnnotatedTypes.getParser().getRawType().append("-000").parse("java.lang.reflect.Type-000"));
		System.out.println();

		System.out.println(AnnotatedTypes.fromString("Type", imports));
		System.out.println();

		imports = imports.withImport(Test3.class);

		System.out.println(Annotations.getParser(imports).getProperty().parse("thisIsTest = \"yeah!\""));
		System.out.println();

		System.out.println(Annotations.getParser(imports).getPropertyMap().parse("wat = 2.5"));
		System.out.println();

		System.out.println(AnnotatedTypes.fromString("@Capture() java.lang.reflect.Type", imports));
		System.out.println();

		System.out.println(Annotations.getParser(imports).getPropertyMap().parse("thisIsTest = \"yeah!\", wat = 2.5"));
		System.out.println();

		System.out.println(Annotations.fromString("@Test3(thisIsTest = \"yeah!\", wat = 2.5f)", imports));
		System.out.println();

		System.out.println(AnnotatedTypes.fromString("java.util.ArrayList<java.lang.String>", imports));
		System.out.println();

		System.out.println(AnnotatedTypes.fromString(
				"@Test3(thisIsTest = \"yeah!\", wat = .2f) java.util.ArrayList<@Capture java.lang.String>", imports));
		System.out.println();

		System.out.println(Annotations.getParser(imports).getAnnotation().parse("@Test2(idk = \"helo\", wat = 2)"));
		System.out.println();

		System.out.println(AnnotatedTypes.fromString("@Capture java.util.ArrayList @Preserve [][]", imports));
		System.out.println();

		System.out
				.println(AnnotatedTypes.fromString("@Capture java.util.ArrayList<java.lang.String> [] @Preserve []", imports));
		System.out.println();

		System.out.println(AnnotatedTypes.fromString("java.util.ArrayList<@Preserve?>", imports));
		System.out.println();

		System.out.println(Annotations.fromString("@uk.co.strangeskies.reflection.TypeToken.Infer", imports));
		System.out.println();

		System.out.println(AnnotatedTypes
				.fromString("@uk.co.strangeskies.reflection.TypeToken.Infer List<? extends java.lang.String>", imports));
		System.out.println();

		System.out.println(AnnotatedTypes.fromString(
				"@Test3(thisIsTest = \"yeah!\", wat = 2.5f) List<@Test2(idk = \"helo\", wat = 2) ? extends @Preserve java.lang.String>",
				imports));
		System.out.println();

		System.out.println(AnnotatedTypes.fromString(
				"List<@Test2(idk = \"helo\", wat = 2) ? extends @Preserve java.lang.Number>@Capture []@Capture []", imports));
		System.out.println();

		System.out.println(AnnotatedTypes.fromString(
				"@Test3(thisIsTest = \"yeah!\", wat = 2.5f) List<@Test2(idk = \"helo\", wat = 2) ? extends @Preserve java.lang.Number> @Capture [] @uk.co.strangeskies.reflection.TypeToken.Infer []",
				imports));
		System.out.println();

		System.out.println(Annotations.fromString("@Test3(thisIsTest = \"yeah!\", wat = 2.5f)", imports));

		System.out.println(AnnotatedTypes.fromString(annotationString, imports));
		System.out.println();

		System.out.println(new TypeToken<TreeSet<? extends C2<?>>>() {});
		System.out.println();

		System.out.println(new TypeToken<SchemaNode<?, ?>>() {}.isAssignableTo(new TypeToken<SchemaNode<?, ?>>() {}));
		System.out.println();

		System.out
				.println(TypeToken.fromString("uk.co.strangeskies.reflection.test.SchemaNode<?, ?>").getAnnotatedDeclaration());
		System.out.println();

		System.out.println(TypeToken.fromString("java.util.Map<?, @uk.co.strangeskies.reflection.TypeToken.Infer ?>")
				.deepCopy().getAnnotatedDeclaration());
		System.out.println();

		System.out.println("annotationseq: " + AnnotatedWildcardTypes.unbounded()
				.equals(AnnotatedWildcardTypes.upperBounded(AnnotatedTypes.over(Object.class))));
		System.out.println();

		Imports imports2 = Imports.empty().withImports(Infer.class, Capture.class, Set.class, Map.class);

		System.out.println(TypeToken.fromString("@Infer Set<?>", imports2));
		System.out.println(TypeToken.fromString("@Infer ?", imports2));
		System.out.println(TypeToken.fromString("Map<?, @Capture ?>", imports2));
		System.out.println(TypeToken.fromString("Map<@Infer ?, @Capture ?>", imports2));
		System.out.println(TypeToken.fromString("@Capture Map<@Infer ?, ?>", imports2));
		System.out.println();

		System.out.println(new TypeToken<DataBindingType<Object>>() {}.getMethods().named("baseModel")
				.resolveOverload(new TypeToken<Model<?>>() {}).infer().getResolver().getBounds());
		System.out.println();

		/*- TODO Current open question on compiler-dev
		 * 
		 * creates an intersection type between C1<CAP#1 extends C2<String>> and C2<String> or
		 * something iirc. So I guess we need to realize that CAP#1 can only be C2<String>
		 * exactly, by lifting the resolved type for T = C2<String> from the bound on the capture
		 * 
		System.out.println(new TypeToken<C1<? extends C2<String>>>() {});
		System.out.println();
		
		System.out.println(new TypeToken<C1<? extends C2<?>>>() {});
		System.out.println();
		 */
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	@interface Test2 {
		String idk();

		int wat();
	};

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	@interface Test3 {
		String thisIsTest();

		float wat();
	};

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	@interface Tester {
		String thisIsTest();

		double[] wat();
	};

	@Tester(thisIsTest = "yes", wat = { 2, 3 })
	static class C1<T extends C1<T>> {}

	static class C2<U> extends C1<C2<U>> {}

	static class C22<U> extends C1<C22<U>> {}

	static class C3<T extends C3<?>> {}

	static class C4<V, W> extends C3<C4<V, W>> {}

	static class C5<X extends C3<X>> {}

	static class C6<Y, Z> extends C3<C6<Z, Y>> {}

	private static <U> TypeToken<Iterable<? extends U>> getIteratorExtending(TypeToken<U> type) {
		return new TypeToken<Iterable<? extends U>>() {}.withTypeArgument(new TypeParameter<U>() {}, type);
	}

	static <T> TypeToken<List<T>> listOf(Class<T> sub) {
		return new TypeToken<List<T>>() {}.withTypeArgument(new TypeParameter<T>() {}, sub);
	}

	static <T> TypeToken<List<T>> listOf(TypeToken<T> sub) {
		return new TypeToken<List<T>>() {}.withTypeArgument(new TypeParameter<T>() {}, sub);
	}

	static <U> TypeToken<Iterable<U>> getIteratorType(TypeToken<U> type) {
		return new TypeToken<Iterable<U>>() {}.withTypeArgument(new TypeParameter<U>() {}, type);
	}

	static <U> TypeToken<Iterable<U>> getIteratorType2(Class<U> type) {
		return new TypeToken<Iterable<U>>() {}.withTypeArgument(new TypeParameter<U>() {}, type);
	}

	static <U> TypeToken<Iterable<? extends U>> getIteratorType3(TypeToken<U> type) {
		return new TypeToken<Iterable<? extends U>>() {}.withTypeArgument(new TypeParameter<U>() {}, type);
	}
}

class TT<TTT> extends TypeToken<TTT> {}

class YY<YT> extends TT<Set<YT>> {}

class G extends YY<List<String>> {}

class Outer1<T> {
	public class Inner<N extends T, J extends Collection<? extends T>, P> {}

	public class Inner2<M extends Number & Comparable<?>> extends Outer1<Comparable<?>>.Inner<M, List<Integer>, T> {}
}

class Outer2<F, Z extends F> {
	public class Inner3<X extends Set<F>> extends Outer1<F>.Inner<Z, X, Set<Z>> {
		Inner3() {
			new Outer1<F>() {}.super();
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
	interface Effective<S extends SchemaNode<S, E>, E extends Effective<S, E>> extends SchemaNode<S, E> {}

	String getName();

	ChildNode<?, ?> child(String name);

	List<? extends ChildNode<?, ?>> children();
}

interface ChildNode<S extends ChildNode<S, E>, E extends ChildNode.Effective<S, E>> extends SchemaNode<S, E> {
	interface Effective<S extends ChildNode<S, E>, E extends Effective<S, E>>
			extends ChildNode<S, E>, SchemaNode.Effective<S, E> {}
}

interface ChoiceNode extends ChildNode<ChoiceNode, ChoiceNode.Effective> {
	interface Effective extends ChoiceNode, ChildNode.Effective<ChoiceNode, ChoiceNode.Effective> {}
}

interface SchemaNodeConfigurator<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<N, ?>> {
	public S name(String name);
}

interface ChildNodeConfigurator<S extends ChildNodeConfigurator<S, N>, N extends ChildNode<N, ?>>
		extends SchemaNodeConfigurator<S, N> {}

interface DataBindingType<T> extends BindingNode<T, DataBindingType<T>, DataBindingType.Effective<T>> {
	interface Effective<T> extends DataBindingType<T>, BindingNode.Effective<T, DataBindingType<T>, Effective<T>> {}

	List<? extends Model<? super T>> baseModel();

	<V extends T> DataBindingType<V> baseModel(Model<? super V> baseModel);
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
