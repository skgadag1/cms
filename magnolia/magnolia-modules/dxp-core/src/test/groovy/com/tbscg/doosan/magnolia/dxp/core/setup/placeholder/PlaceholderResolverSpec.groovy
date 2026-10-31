package com.tbscg.doosan.magnolia.dxp.core.setup.placeholder

import com.tbscg.doosan.magnolia.dxp.core.setup.placeholder.PlaceholderResolver.PlaceholderResolverStrategy
import spock.lang.Specification

class PlaceholderResolverSpec extends Specification {

    private Placeholder placeholder = new Placeholder("/a", "some", "placeholder");
    private String SOME_MODULE = "someModule";

    def 'use both strategies and returns no value'() {
        given:
        PlaceholderResolverStrategy strategy = { a, b -> Optional.empty() }
        PlaceholderResolverStrategy strategy2 = { a, b -> Optional.empty() }


        when:
        Optional<String> optional = new PlaceholderResolver([strategy, strategy2]).resolve(placeholder, SOME_MODULE);

        then:
        !optional.isPresent()
    }

    def 'use both strategies and returns value from first strategy'() {
        given:
        PlaceholderResolverStrategy strategy = { a, b -> Optional.of("result") }
        PlaceholderResolverStrategy strategy2 = { a, b -> Optional.empty() }

        when:
        Optional<String> optional = new PlaceholderResolver([strategy, strategy2]).resolve(placeholder, SOME_MODULE);

        then:
        optional.get() == "result"
    }

    def 'use both strategies and returns value from second strategy'() {
        given:
        PlaceholderResolverStrategy strategy = { a, b -> Optional.empty() }
        PlaceholderResolverStrategy strategy2 = { a, b -> Optional.of("result") }

        when:
        Optional<String> optional = new PlaceholderResolver([strategy, strategy2]).resolve(placeholder, SOME_MODULE);

        then:
        optional.get() == "result"
    }

    def 'use both strategies and returns value from first strategy, even if second strategy is also successful'() {
        given:
        PlaceholderResolverStrategy strategy = { a, b -> Optional.of("result") }
        PlaceholderResolverStrategy strategy2 = { a, b -> Optional.of("result2") }

        when:
        Optional<String> optional = new PlaceholderResolver([strategy, strategy2]).resolve(placeholder, SOME_MODULE);

        then:
        optional.get() == "result"
    }
}
