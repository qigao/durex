package com.github.durex.bytebuddy;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.jupiter.api.Test;

class ByteBuddyUnitTest {

  @Test
  void givenObject_whenToString_thenReturnHelloWorldString() throws Exception {
    DynamicType.Unloaded<Object> unloadedType =
        new ByteBuddy()
            .subclass(Object.class)
            .method(ElementMatchers.isToString())
            .intercept(FixedValue.value("Hello World ByteBuddy!"))
            .make();

    Class<?> dynamicType = unloadedType.load(getClass().getClassLoader()).getLoaded();

    assertEquals(
        "Hello World ByteBuddy!", dynamicType.getDeclaredConstructor().newInstance().toString());
  }

  @Test
  void givenFoo_whenRedefined_thenReturnFooRedefined() throws Exception {
    ByteBuddyAgent.install();
    new ByteBuddy()
        .redefine(Foo.class)
        .method(named("sayHelloFoo"))
        .intercept(FixedValue.value("Hello Foo Redefined"))
        .make()
        .load(Foo.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
    Foo f = new Foo();
    assertEquals("Hello Foo Redefined", f.sayHelloFoo());
  }

  @Test
  void givenSayHelloFoo_whenMethodDelegation_thenSayHelloBar() throws Exception {
    String r =
        new ByteBuddy()
            .subclass(Foo.class)
            .method(named("sayHelloFoo").and(isDeclaredBy(Foo.class).and(returns(String.class))))
            .intercept(MethodDelegation.to(Bar.class))
            .make()
            .load(getClass().getClassLoader())
            .getLoaded()
            .getDeclaredConstructor()
            .newInstance()
            .sayHelloFoo();

    assertEquals(r, Bar.sayHelloBar());
  }

  @Test
  void givenMethodName_whenDefineMethod_thenCreateMethod() throws Exception {
    Class<?> type =
        new ByteBuddy()
            .subclass(Object.class)
            .name("MyClassName")
            .defineMethod("custom", String.class, Modifier.PUBLIC)
            .intercept(MethodDelegation.to(Bar.class))
            .defineField("x", String.class, Modifier.PUBLIC)
            .make()
            .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
            .getLoaded();

    Method m = type.getDeclaredMethod("custom");

    assertEquals(m.invoke(type.getDeclaredConstructor().newInstance()), Bar.sayHelloBar());
    assertNotNull(type.getDeclaredField("x"));
  }
}
