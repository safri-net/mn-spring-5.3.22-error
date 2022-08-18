## Micronaut Spring `Multi element access not supported`

After using spring-framework 5.3.22 (change was introduced with 5.3.21) our application
stack (spring-boot with Micronaut context and Vaadin) ran into an error.

```
org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'routerFunctionMapping' defined in class path resource [org/springframework/boot/autoconfigure/web/servlet/WebMvcAutoConfiguration$EnableWebMvcConfiguration.class]: Invocation of init method failed; nested exception is java.lang.UnsupportedOperationException: Multi element access not supported
```

The reason is following code in the [`org.springframework.web.servlet.function.support.RouterFunctionMapping` class](https://github.com/spring-projects/spring-framework/blob/v5.3.22/spring-webmvc/src/main/java/org/springframework/web/servlet/function/support/RouterFunctionMapping.java#L165):

```java
parentContext.getBeanProvider(RouterFunction.class).stream().forEach(routerFunctions::remove);
```

The `stream()` call leads into an error as `ObjectProvider` in [`io.micronaut.spring.context.factory.MicronautBeanFactory`](https://github.com/micronaut-projects/micronaut-spring/blob/v4.2.1/spring-context/src/main/java/io/micronaut/spring/context/factory/MicronautBeanFactory.java#L278) doesn't override the `stream()` method of the [`ObjectProvider`](https://github.com/spring-projects/spring-framework/blob/v5.3.22/spring-beans/src/main/java/org/springframework/beans/factory/ObjectProvider.java) and the [default implementation](https://github.com/spring-projects/spring-framework/blob/v5.3.22/spring-beans/src/main/java/org/springframework/beans/factory/ObjectProvider.java#L160) throws an `UnsupportedOperationException("Multi element access not supported")`.

I fixed the problem with following workaround, I replaced the MicronautBeanFactory with an overridden version that overrides the `getBeanProvider` with a method that returns an `ObjectProvider`  with an implemented `stream()` method.

```groovy
@Override
Stream<T> stream() {
    return beanContext.getBeansOfType(requiredType).stream()
}
```

If this is an acceptable solution I will provide a PR.

To reproduce the error just start the application with `./gradlew run`. 

To see the application working with the fixed `ObjectProvider` run it with `MICRONAUT_ENVIRONMENTS=fixed ./gradlew run`   
