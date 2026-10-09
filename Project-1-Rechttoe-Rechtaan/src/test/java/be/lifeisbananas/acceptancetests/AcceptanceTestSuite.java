package be.lifeisbananas.acceptancetests;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Draait alle features uit src/test/resources/features.
 * <p>
 * Deze testen hebben een draaiende toepassing en een browser nodig. Start dus
 * eerst de toepassing, en draai deze klasse daarna vanuit de IDE of met
 * {@code mvn verify -Pacceptance}.
 * <p>
 * De laatste regel laat Serenity het HTML-rapport maken; verwijder die regel
 * en je houdt gewone Cucumber over.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "be.lifeisbananas.acceptancetests.steps")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "io.cucumber.core.plugin.SerenityReporterParallel")
public class AcceptanceTestSuite {
}
