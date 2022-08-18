package com.example

import com.vaadin.flow.spring.annotation.EnableVaadin
import groovy.transform.CompileStatic
import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.NonNull
import io.micronaut.spring.context.MicronautApplicationContext
import io.micronaut.spring.context.aware.SpringAwareListener
import io.micronaut.spring.context.factory.MicronautBeanFactory
import io.micronaut.spring.context.factory.MicronautBeanFactoryConfiguration
import jakarta.inject.Singleton
import org.springframework.beans.BeansException
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationContext

import java.util.stream.Stream

@CompileStatic
@SpringBootApplication
@EnableVaadin
class Application {
    static void main(String[] args) {
        new SpringApplicationBuilder()
                .parent(new MicronautApplicationContext().tap { start() })
                .sources(Application)
                .build()
                .run()
    }
}

@Replaces(MicronautBeanFactory)
@Requires(env = "fixed")
@Singleton
class MyMicronautBeanFactory extends MicronautBeanFactory {

    MyMicronautBeanFactory(BeanContext beanContext, SpringAwareListener awareListener, MicronautBeanFactoryConfiguration configuration) {
        super(beanContext, awareListener, configuration)
    }

    @NonNull
    <T> ObjectProvider<T> getBeanProvider(@NonNull final Class<T> requiredType) {
        return new ObjectProvider<T>() {
            T getObject(Object... args) throws BeansException {
                return beanContext.createBean(requiredType, args)
            }

            T getIfAvailable() throws BeansException {
                return beanContext.containsBean(requiredType) ? beanContext.getBean(requiredType) : null
            }

            T getIfUnique() throws BeansException {
                Collection<T> beansOfType = beanContext.getBeansOfType(requiredType);
                return beansOfType.size() == 1 ? beansOfType.stream().findFirst().orElse((Object)null) : null
            }

            T getObject() throws BeansException {
                return beanContext.getBean(requiredType)
            }

            @Override
            Stream<T> stream() {
                return beanContext.getBeansOfType(requiredType).stream()
            }

        }
    }

}
