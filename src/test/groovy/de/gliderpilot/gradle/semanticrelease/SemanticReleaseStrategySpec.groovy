package de.gliderpilot.gradle.semanticrelease

import com.github.zafarkhaja.semver.Version
import org.ajoberstar.gradle.git.release.base.ReleaseVersion
import org.ajoberstar.gradle.git.release.semver.NearestVersion
import org.ajoberstar.gradle.git.release.semver.SemVerStrategyState
import spock.lang.Specification
import spock.lang.Subject

/**
 * Created by tobias on 8/11/15.
 */
class SemanticReleaseStrategySpec extends Specification {

    SemVerStrategyState initialState = initialState('0.0.0')
    SemanticReleaseInitialStateService initialStateService = Mock() {
        initialState(_, _) >> { initialState }
    }
    SemanticReleaseNormalStrategy normalStrategy = Mock()

    @Subject
    SemanticReleaseStrategy strategy = new SemanticReleaseStrategy(initialStateService: initialStateService,
            normalStrategy: normalStrategy)

    def "selector closure is used"() {
        expect:
        !strategy.selector(null, null)

        when:
        strategy = strategy.copyWith(selector: { true })

        then:
        strategy.selector(null, null)
    }

    def "infers the normal version from the normal strategy"() {
        when:
        def releaseVersion = strategy.infer(null, null)

        then:
        1 * normalStrategy.infer(initialState) >> { initialState.copyWith(inferredNormal: '1.0.1') }

        releaseVersion == new ReleaseVersion('1.0.1', '0.0.0', false)
    }

    def "Uses initial version 1.0.0 when the normalStrategy does not change the state"() {
        when:
        def releaseVersion = strategy.infer(null, null)

        then:
        1 * normalStrategy.infer(initialState) >> { initialState }

        releaseVersion == new ReleaseVersion('1.0.0', '0.0.0', false)
    }

    def "increments the PATCH when the normalStrategy does not change the state"() {
        given:
        initialState = initialState('1.1.1')
        when:
        def releaseVersion = strategy.infer(null, null)

        then:
        1 * normalStrategy.infer(initialState) >> { initialState }

        releaseVersion == new ReleaseVersion('1.1.2', '1.1.1', false)
    }

    def initialState(String version) {
        new SemVerStrategyState(nearestVersion: new NearestVersion(normal: Version.valueOf(version)))
    }
}