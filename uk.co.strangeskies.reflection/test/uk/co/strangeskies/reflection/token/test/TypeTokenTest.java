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

import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.CONTAINMENT;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.LOOSE_COMPATIBILILTY;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.SUBTYPE;
import static uk.co.strangeskies.reflection.token.ExecutableToken.staticMethods;

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
import uk.co.strangeskies.reflection.token.TypeToken.Retain;
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
				T t,
				U u) {}

		public <U, R> ExecutableToken<U, ? extends R> okay(
				Set<? extends ExecutableToken<U, ? extends R>> candidates,
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

		Assert.assertTrue(nullType.satisfiesConstraintTo(LOOSE_COMPATIBILILTY, String.class));
		Assert.assertTrue(nullType.satisfiesConstraintFrom(LOOSE_COMPATIBILILTY, String.class));

		Assert.assertTrue(nullType.satisfiesConstraintTo(LOOSE_COMPATIBILILTY, stringType));
		Assert.assertTrue(nullType.satisfiesConstraintFrom(LOOSE_COMPATIBILILTY, stringType));

		Assert.assertTrue(stringType.satisfiesConstraintTo(LOOSE_COMPATIBILILTY, nullType));
		Assert.assertTrue(stringType.satisfiesConstraintFrom(LOOSE_COMPATIBILILTY, nullType));
	}

	@Test
	public void makeTestsFromThese() {
		printlines(
				"#" + Types
						.leastUpperBound(Integer.class, Double.class, new TypeToken<Comparable<? extends Number>>() {}.getType()));

		printlines(
				"#" + Types
						.leastUpperBound(new TypeToken<Comparable<? extends Number>>() {}.getType(), Integer.class, Double.class));

		List<ParameterizedType> bestTypes = Arrays.asList(
				(ParameterizedType) new TypeToken<Comparable<? extends Number>>() {}.getType(),
				(ParameterizedType) new TypeToken<Comparable<Double>>() {}.getType(),
				(ParameterizedType) new TypeToken<Comparable<Integer>>() {}.getType());
		Type bestType = ((ParameterizedType) Types.best(Comparable.class, bestTypes)).getActualTypeArguments()[0];
		printlines("!  " + bestType);

		List<ParameterizedType> bestTypes2 = Arrays.asList(
				(ParameterizedType) new TypeToken<Comparable<Double>>() {}.getType(),
				(ParameterizedType) new TypeToken<Comparable<Integer>>() {}.getType(),
				(ParameterizedType) new TypeToken<Comparable<? extends Number>>() {}.getType());
		Type bestType2 = ((ParameterizedType) Types.best(Comparable.class, bestTypes2)).getActualTypeArguments()[0];
		printlines("!  " + bestType2);

		List<ParameterizedType> bestTypes3 = Arrays.asList(
				(ParameterizedType) new TypeToken<Comparable<Double>>() {}.getType(),
				(ParameterizedType) new TypeToken<Comparable<Integer>>() {}.getType());
		Type bestType3 = ((ParameterizedType) Types.best(Comparable.class, bestTypes3)).getActualTypeArguments()[0];
		printlines("!  " + bestType3);

		printlines();
		printlines(Types.leastContainingArgument(bestType, bestType3));
	}

	// @Test
	public void supertypeParameterTest() {
		Assert.assertEquals(
				"uk.co.strangeskies.reflection.test.SchemaNode<?, ?>",
				new TypeToken<SchemaNode.Effective<?, ?>>() {}.resolveSupertype(SchemaNode.class).toString());
	}

	@Test
	public void hugeTest1() {
		printlines(new TypeToken<HashSet<String>>() {}.resolveSupertype(Set.class));
		printlines();
		printlines();

		printlines("List with T = String: " + listOf(String.class));
		printlines();
		printlines();

		new TypeToken<Outer1<Serializable>.Inner<String, HashSet<Serializable>, Set<String>>>() {}.getBounds();
		printlines();
		printlines();

		printlines(
				new TypeToken<Outer2<Serializable, String>.Inner3<HashSet<Serializable>>>() {}
						.resolveSupertype(Outer1.Inner.class));
		printlines();
		printlines();

		printlines(new TypeToken<Outer1<String>.Inner2<Double>>() {}.resolveSupertype(Outer1.Inner.class));
		printlines();
		printlines();

		printlines("type test: " + new TypeToken<String>() {}.resolveSupertype(Comparable.class));
		printlines();
		printlines();

		class SM<YO> {}
		class NM<V extends Number> extends SM<V> {}
		printlines(new TypeToken<NM<?>>() {});
		printlines(new TypeToken<NM<?>>() {}.resolveSupertype(SM.class));
		printlines();
		printlines();

		printlines(TypeToken.overType(new TypeToken<Nest<?>>() {}.getType()));
		printlines();
		printlines();

		printlines(TypeToken.overType(new TypeToken<C2<?>>() {}.getType()));
		printlines();
		printlines();

		printlines(TypeToken.overType(new TypeToken<C1<?>>() {}.getType()));
		printlines();
		printlines();

		printlines(TypeToken.overType(new TypeToken<Base<LeftN, RightN>>() {}.getType()));
		printlines();
		printlines();

		printlines(TypeToken.overType(new TypeToken<RightN>() {}.resolveSupertype(Base.class).getType()));
		printlines();
		printlines();

		printlines("TYPELITTEST: " + new TT<String>() {});
		printlines("TYPELITTEST-2: " + new YY<String>() {});
		printlines("TYPELITTEST-3: " + new G() {});
		printlines("TYPELITTEST-4: " + new YY<Integer>() {}.resolveSupertype(Collection.class));
		printlines();
		printlines();

		printlines(new TypeToken<Self<?>>() {}.satisfiesConstraintFrom(LOOSE_COMPATIBILILTY, new TypeToken<Nest<?>>() {}));
		printlines();
		printlines();

		printlines(TypeToken.overType(new TypeToken<C1<? extends C1<?>>>() {}.getType()));
		printlines();
		printlines();

		printlines(new TypeToken<SchemaNode.Effective<?, ?>>() {}.resolveSupertype(SchemaNode.class));
		printlines();
		printlines();

		TypeToken<?> receiver = new TypeToken<BindingState>() {};
		printlines("RESOLVE 1:");
		printlines(receiver.methods().named("bindingNode").resolveOverload(int.class));
		printlines();
		printlines();

		receiver = new @Capture TypeToken<SchemaNodeConfigurator<?, ?>>() {};
		printlines("RESOLVE 2:");
		printlines(TypeToken.overType(receiver.getType()).methods().named("name").resolveOverload(String.class));
		printlines();
		printlines();

		receiver = new @Capture TypeToken<ChildNodeConfigurator<?, ?>>() {};
		printlines("RESOLVE 3:");
		printlines(TypeToken.overType(receiver.getType()).methods().named("name").resolveOverload(String.class));
		printlines();
		printlines();

		receiver = new @Capture TypeToken<DataBindingType.Effective<?>>() {};
		printlines("RESOLVE 4:");
		printlines(TypeToken.overType(receiver.getType()).methods().named("child").resolveOverload(String.class));
		printlines();
		printlines();

		printlines(
				new TypeToken<IncludeTarget>() {}.methods().named("includer").resolveOverload(Model.class, Collection.class));
		printlines();
		printlines();

		printlines(new TypeToken<Map<String, ?>>() {}.getThisType());
		printlines();
		printlines();
	}

	//@Test
	public <H extends C2<H>> void yugeTestTheBiggest() {
		for (int i = 0; i < 3500; i++) {
			hugeTest1();
			huge2Test();
		}
	}

	@Test
	public <H extends C2<H>> void huge2Test() {
		printlines("<T extends Number, U extends List<? super T>> U method4(Collection<? extends T> a, U b)");
		printlines("((B) null).method4((Collection<? extends Integer>) null, (List<? super Number>) null)");
		printlines(
				TypeToken
						.overType(B.class)
						.methods()
						.named("method4")
						.resolveOverload(
								new TypeToken<Collection<? extends Integer>>() {}.getType(),
								new TypeToken<List<? super Number>>() {}.getType())
						.infer());
		printlines();

		printlines(new TypeParameter<H>() {});
		printlines(new TypeToken<List<String>>() {});
		printlines();

		printlines(
				TypeToken
						.overType(B.class)
						.methods()
						.named("bothways")
						.resolveOverload(String.class, new TypeToken<List<String>>() {}.getType())
						.infer());
		printlines();

		printlines(TypeToken.overType(B.class).methods().named("moothod").resolveOverload(Integer.class, Number.class));
		printlines();

		printlines(TypeToken.overType(B.class).methods().named("moothod").resolveOverload(Number.class, Integer.class));
		printlines();

		printlines(TypeToken.overType(B.class).methods().named("moothod").resolveOverload(Number.class, Number.class));
		printlines();

		/*-
		systemOutPrintln(TypeToken.of(B.class).getMethods(
				"moothod", Integer.class, Integer.class));
		systemOutPrintln();
		 */

		printlines(new TypeToken<List<? extends Number>>() {}.getType());
		printlines(
				staticMethods(Arrays.class)
						.named("asList")
						.resolveOverload(int.class, double.class)
						.withTargetType(new TypeToken<List<? extends Number>>() {})
						.infer());
		printlines();

		printlines(
				staticMethods(Arrays.class).named("asList").resolveOverload(int.class, double.class).withTargetType(
						new TypeToken<List<? super Comparable<? extends Number>>>() {}));
		printlines();

		printlines(staticMethods(Arrays.class).named("asList").resolveOverload(int.class, double.class).getBounds());
		printlines();

		printlines(
				staticMethods(Arrays.class)
						.named("asList")
						.resolveOverload(int.class, double.class)
						.withTargetType(new TypeToken<List<? super Comparable<? extends Number>>>() {})
						.infer());
		printlines();

		printlines(
				TypeToken.overType(B.class).methods().named("method").resolveOverload(
						new TypeToken<List<Integer>>() {},
						new TypeToken<List<Number>>() {}));
		printlines();

		printlines(
				new TypeToken<B>() {}.methods().named("method2").resolveOverload(
						new TypeToken<List<Integer>>() {},
						new TypeToken<List<Comparable<Integer>>>() {}));
		printlines();

		printlines(
				TypeToken
						.overType(B.class)
						.methods()
						.named("method")
						.resolveOverload(new TypeToken<Collection<? super Integer>>() {})
						.infer());
		printlines();

		printlines(
				new TypeToken<B>() {}.methods().named("okay").resolveOverload(
						new TypeToken<Set<ExecutableToken<H, ?>>>() {},
						new TypeToken<List<? extends Type>>() {}));
		printlines();

		printlines(
				staticMethods(B.class)
						.named("testeroonie")
						.resolveOverload(new TypeToken<Class<?>>() {}.getType(), String.class)
						.infer());
		printlines();

		TypeToken<?> targetClass = new TypeToken<List<?>>() {};
		TypeToken<?> resultClass = new TypeToken<Iterable<String>>() {};
		printlines(resultClass.satisfiesConstraintTo(CONTAINMENT, targetClass.resolveSupertype(resultClass.getRawType())));
		printlines();

		printlines(new TypeToken<List<?>>() {}.getExtending(Wildcards.CAPTURE));
		printlines();

		printlines(new TypeToken<List<?>>() {}.getExtending(Wildcards.CAPTURE).resolveSupertype(Collection.class));
		printlines();

		printlines(new TypeToken<List<BigInteger>>() {}.getExtending(Wildcards.INFER).resolveSupertype(Collection.class));
		printlines();

		printlines(new TypeToken<HashSet<?>>() {}.getBounds());
		printlines();

		printlines(
				new TypeToken<Collection<? extends String>>() {}
						.getExtending(Wildcards.INFER)
						.withConstraintTo(SUBTYPE, new TypeToken<ArrayList<?>>() {}.getType())
						.getBounds());
		printlines();

		printlines(
				new TypeToken<Collection<? extends String>>() {}
						.getExtending(Wildcards.INFER)
						.withConstraintTo(SUBTYPE, new TypeToken<ArrayList<?>>() {}.getType())
						.infer());
		printlines();

		printlines(
				new TypeToken<List<? super Number>>() {}
						.getExtending(Wildcards.INFER)
						.methods()
						.named("add")
						.resolveOverload(Integer.class)
						.getReceiverType()
						.infer());
		printlines();

		printlines(new TypeToken<@Infer List<@Infer ? extends Number>>() {}.getBounds());
		printlines();

		printlines(new TypeToken<@Infer List<? extends Number>>() {});
		printlines(
				AnnotatedTypes.wrap(new TypeToken<@Infer List<? extends Number>>() {}.getClass().getAnnotatedSuperclass()));
		printlines(new @Infer TypeToken<List<@Infer ? extends Number>>() {});
		printlines(new @Infer TypeToken<List<@Infer ? extends Number>>() {}.getExtending(Wildcards.INFER));
		printlines(new @Infer TypeToken<List<? extends Number>>() {}.getExtending(Wildcards.INFER));
		printlines(new TypeToken<@Infer List<? extends Number>>() {}.getExtending(Wildcards.INFER).getBounds());

		printlines(
				new TypeToken<@Infer Collection<? extends String>>() {}
						.getExtending(Wildcards.INFER)
						.withConstraintTo(SUBTYPE, new TypeToken<ArrayList<? super String>>() {}.getType())
						.infer()
						.resolveSupertype(Iterable.class));
		printlines();

		printlines(
				new @Infer TypeToken<List<? extends Number>>() {}
						.getExtending(Wildcards.INFER)
						.methods()
						.named("add")
						.resolveOverload(Integer.class)
						.getReceiverType()
						.methods()
						.named("add")
						.resolveOverload(Double.class)
						.getReceiverType()
						.infer());
		printlines();

		printlines(new TypeToken<HashMap<?, ?>>() {}.getExtending(Wildcards.INFER).constructors().resolveOverload());
		printlines(
				new TypeToken<HashMap<?, ?>>() {}.getExtending(Wildcards.INFER).constructors().resolveOverload().getBounds());

		printlines(new TypeToken<HashMap<?, ?>>() {}.getExtending(Wildcards.INFER));
		printlines(new TypeToken<HashMap<?, ?>>() {}.getExtending(Wildcards.INFER).getBounds());

		printlines(
				new TypeToken<HashMap<?, ?>>() {}
						.getExtending(Wildcards.INFER)
						.constructors()
						.resolveOverload()
						.withTargetType(new TypeToken<Map<? extends String, ? extends Number>>() {})
						.infer());
		printlines();

		printlines(
				new TypeToken<HashMap<String, Number>>() {}
						.getExtending(Wildcards.INFER)
						.constructors()
						.resolveOverload()
						.withTargetType(new TypeToken<@Infer Map<?, ?>>() {})
						.infer());
		printlines();

		printlines(new TypeToken<List<?>>() {}.getAnnotatedDeclaration());
		printlines(
				new TypeToken<Set<String>>() {}
						.methods()
						.named("addAll")
						.resolveOverload(new @Infer TypeToken<List<?>>() {})
						.inferParameterTypes());
		printlines();

		printlines(
				new TypeToken<ChildNode<?, ?>>() {}.getType() + " + + + " + new TypeToken<ChildNode<?, ?>>() {}
						.satisfiesConstraintTo(LOOSE_COMPATIBILILTY, new TypeToken<ChildNode<?, ?>>() {}));
		printlines();

		printlines(
				new TypeToken<ChildNode<?, ?>>() {}.getType() + " ~ = ~ " + new TypeToken<ChildNode<?, ?>>() {}.resolve());
		printlines();

		printlines(getIteratorType(new TypeToken<String>() {}));
		printlines();

		printlines(getIteratorType2(String.class));
		printlines();

		printlines(getIteratorType3(new TypeToken<String>() {}));
		printlines();

		printlines(getIteratorExtending(new @Infer TypeToken<List<? extends String>>() {}));
		printlines();

		printlines(getIteratorExtending(new @Infer TypeToken<List<? extends String>>() {}).infer());
		printlines();

		ExecutableToken<?, ?> blurner = new @Infer TypeToken<Blurn<? extends List<? extends Number>>>() {}
				.methods()
				.named("blurn")
				.resolveOverload()
				.withReceiverType(new TypeToken<Gurn<Integer>>() {});
		printlines(blurner);
		printlines();

		try {
			printlines(ExecutableToken.overMethod(Blurn.class.getMethod("blurn"), new TypeToken<Blurn<Long>>() {}));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		printlines();

		printlines(
				new @Capture TypeToken<SchemaNode<?, ?>>() {}.methods().named("children").resolveOverload().withTargetType(
						getIteratorExtending(new TypeToken<ChildNode<?, ?>>() {})));
		printlines();

		printlines(new TypeToken<ChoiceNode>() {}.methods().named("getName").resolveOverload(new ArrayList<>()));
		printlines();

		printlines(
				new @Infer TypeToken<LinkedHashSet<?>>() {}
						.methods()
						.named("add")
						.resolveOverload(new TypeToken<StringBuffer>() {})
						.infer());
		printlines();

		printlines(
				new @Infer TypeToken<Set<?>>() {}
						.methods()
						.named("addAll")
						.resolveOverload(
								new TypeToken<@Infer ArrayList<? super Integer>>() {}.constructors().resolveOverload().getReturnType())
						.infer());
		printlines();

		printlines(
				new TypeToken<@Infer HashSet<? super Double>>() {}
						.constructors()
						.resolveOverload(
								new TypeToken<@Infer ArrayList<? super Integer>>() {}.constructors().resolveOverload().getReturnType())
						.getReturnType()
						.infer());
		printlines();

		TypeToken<?> bball = new TypeToken<@Infer HashSet<? super Double>>() {}
				.constructors()
				.resolveOverload(
						new @Capture TypeToken<ArrayList<? super Integer>>() {}.constructors().resolveOverload().getReturnType())
				.getReturnType();
		printlines(bball.getBounds());
		printlines(bball.deepCopy().getBounds());
		printlines();

		TypeToken<?> eqselente = new TypeToken<SchemaNode<?, ?>>() {};
		printlines(eqselente);
		printlines(eqselente.getBounds());

		TypeToken<?> dc = eqselente.deepCopy();
		printlines(dc);
		printlines(dc.getBounds());

		eqselente = eqselente.withConstraintTo(SUBTYPE, dc);
		printlines(eqselente.getBounds());
		printlines(eqselente.infer());
		printlines();

		printlines(new TypeToken<List<? extends String>>() {});
		printlines();

		printlines(new TypeToken<List<? extends String>>() {});
		printlines();

		printlines(new TypeToken<List<? extends String>>() {});
		printlines();

		printlines(new TypeToken<List<? extends String>>() {});
		printlines();

		printlines(new TypeToken<List<? extends Long>>() {}.resolveSupertype(Iterable.class).getType());
		printlines();

		printlines(new TypeToken<List<? extends Long>>() {}.resolveSupertype(Iterable.class).getType());
		printlines();

		printlines(new TypeToken<List<? extends @Capture Set<?>>>() {});
		printlines();

		printlines(
				new TypeToken<C1<C2<String>>>() {}
						.satisfiesConstraintFrom(LOOSE_COMPATIBILILTY, new TypeToken<C2<String>>() {}));
		printlines();

		Imports imports = Imports.empty().withImports(Capture.class, Retain.class, Test2.class, List.class, Type.class);
		String annotationString = AnnotatedTypes.toString(
				new TypeToken<@Test3(thisIsTest = "yeah!", wat = 2.5f) List<@Test2(
						idk = "helo",
						wat = 2) ? extends @Retain Number> @Capture [] @Infer []>() {}.getAnnotatedDeclaration(),
				imports);
		printlines(annotationString);
		printlines();

		printlines(AnnotatedTypes.getParser().getRawType().append("-000").parse("java.lang.reflect.Type-000"));
		printlines();

		printlines(AnnotatedTypes.fromString("Type", imports));
		printlines();

		imports = imports.withImport(Test3.class);

		printlines(Annotations.getParser(imports).getProperty().parse("thisIsTest = \"yeah!\""));
		printlines();

		printlines(Annotations.getParser(imports).getPropertyMap().parse("wat = 2.5"));
		printlines();

		printlines(AnnotatedTypes.fromString("@Capture() java.lang.reflect.Type", imports));
		printlines();

		printlines(Annotations.getParser(imports).getPropertyMap().parse("thisIsTest = \"yeah!\", wat = 2.5"));
		printlines();

		printlines(Annotations.fromString("@Test3(thisIsTest = \"yeah!\", wat = 2.5f)", imports));
		printlines();

		printlines(AnnotatedTypes.fromString("java.util.ArrayList<java.lang.String>", imports));
		printlines();

		printlines(
				AnnotatedTypes.fromString(
						"@Test3(thisIsTest = \"yeah!\", wat = .2f) java.util.ArrayList<@Capture java.lang.String>",
						imports));
		printlines();

		printlines(Annotations.getParser(imports).getAnnotation().parse("@Test2(idk = \"helo\", wat = 2)"));
		printlines();

		printlines(AnnotatedTypes.fromString("@Capture java.util.ArrayList @Retain [][]", imports));
		printlines();

		printlines(AnnotatedTypes.fromString("@Capture java.util.ArrayList<java.lang.String> [] @Retain []", imports));
		printlines();

		printlines(AnnotatedTypes.fromString("java.util.ArrayList<@Retain?>", imports));
		printlines();

		printlines(Annotations.fromString("@uk.co.strangeskies.reflection.token.TypeToken.Infer", imports));
		printlines();

		printlines(
				AnnotatedTypes.fromString(
						"@uk.co.strangeskies.reflection.token.TypeToken.Infer List<? extends java.lang.String>",
						imports));
		printlines();

		printlines(
				AnnotatedTypes.fromString(
						"@Test3(thisIsTest = \"yeah!\", wat = 2.5f) List<@Test2(idk = \"helo\", wat = 2) ? extends @Retain java.lang.String>",
						imports));
		printlines();

		printlines(
				AnnotatedTypes.fromString(
						"List<@Test2(idk = \"helo\", wat = 2) ? extends @Retain java.lang.Number>@Capture []@Capture []",
						imports));
		printlines();

		printlines(
				AnnotatedTypes.fromString(
						"@Test3(thisIsTest = \"yeah!\", wat = 2.5f) List<@Test2(idk = \"helo\", wat = 2) ? extends @Retain java.lang.Number> @Capture [] @uk.co.strangeskies.reflection.token.TypeToken.Infer []",
						imports));
		printlines();

		printlines(Annotations.fromString("@Test3(thisIsTest = \"yeah!\", wat = 2.5f)", imports));

		printlines(AnnotatedTypes.fromString(annotationString, imports));
		printlines();

		printlines(new TypeToken<TreeSet<? extends C2<?>>>() {});
		printlines();

		printlines(
				new TypeToken<SchemaNode<?, ?>>() {}
						.satisfiesConstraintTo(LOOSE_COMPATIBILILTY, new TypeToken<SchemaNode<?, ?>>() {}));
		printlines();

		printlines(
				TypeToken.fromString("uk.co.strangeskies.reflection.token.test.SchemaNode<?, ?>").getAnnotatedDeclaration());
		printlines();

		printlines(
				TypeToken
						.fromString("java.util.Map<?, @uk.co.strangeskies.reflection.token.TypeToken.Infer ?>")
						.deepCopy()
						.getAnnotatedDeclaration());
		printlines();

		printlines(
				"annotationseq: " + AnnotatedWildcardTypes.wildcard().equals(
						AnnotatedWildcardTypes.wildcardExtending(AnnotatedTypes.annotated(Object.class))));
		printlines();

		Imports imports2 = Imports.empty().withImports(Infer.class, Capture.class, Set.class, Map.class);

		printlines(TypeToken.fromString("@Infer Set<?>", imports2));
		printlines(TypeToken.fromString("@Infer ?", imports2));
		printlines(TypeToken.fromString("Map<?, @Capture ?>", imports2));
		printlines(TypeToken.fromString("Map<@Infer ?, @Capture ?>", imports2));
		printlines(TypeToken.fromString("@Capture Map<@Infer ?, ?>", imports2));
		printlines();

		printlines(
				new TypeToken<DataBindingType<Object>>() {}
						.methods()
						.named("baseModel")
						.resolveOverload(new TypeToken<Model<?>>() {})
						.infer()
						.getBounds());
		printlines();

		/*- TODO Current open question on compiler-dev
		 * 
		 * creates an intersection type between C1<CAP#1 extends C2<String>> and C2<String> or
		 * something iirc. So I guess we need to realize that CAP#1 can only be C2<String>
		 * exactly, by lifting the resolved type for T = C2<String> from the bound on the capture
		 * 
		systemOutPrintln(new TypeToken<C1<? extends C2<String>>>() {});
		systemOutPrintln();
		
		systemOutPrintln(new TypeToken<C1<? extends C2<?>>>() {});
		systemOutPrintln();
		 */
	}

	public void printlines(Object... lines) {
		for (Object line : lines) {
			// System.out.println(line);
		}
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
