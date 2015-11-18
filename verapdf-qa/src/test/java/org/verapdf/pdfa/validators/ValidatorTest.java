/**
 * 
 */
package org.verapdf.pdfa.validators;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.verapdf.core.ValidationException;
import org.verapdf.integration.CorpusManager;
import org.verapdf.model.ModelParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.qa.CorpusSampler;
import org.verapdf.pdfa.qa.GitHubBackedProfileDirectory;
import org.verapdf.pdfa.qa.TestCorpus;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.results.ValidationResults;
import org.verapdf.pdfa.validation.ProfileDirectory;
import org.verapdf.pdfa.validation.ValidationProfile;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *
 */
@SuppressWarnings("static-method")
public class ValidatorTest {
    private static final ProfileDirectory PROFILES = GitHubBackedProfileDirectory.INTEGRATION;

    /**
     * Test method for
     * {@link org.verapdf.pdfa.validators.BaseValidator#getProfile()}.
     */
    @Test
    public final void testGetProfile() {
        for (ValidationProfile profile : PROFILES.getValidationProfiles()) {
            PDFAValidator validator = Validators
                    .createValidator(profile, false);
            assertTrue(profile.equals(validator.getProfile()));
        }
    }

    /**
     * Test method for
     * {@link org.verapdf.pdfa.validators.BaseValidator#validate(org.verapdf.pdfa.ValidationModelParser)}
     * .
     * 
     * @throws IOException
     * @throws ValidationException
     * @throws JAXBException
     */
    @Test
    public final void testValidateValidationConsistency() throws IOException,
            ValidationException, JAXBException {
        // Grab a random sample of 10 corpus files
        TestCorpus veraCorpus = CorpusManager.getVeraCorpus();
        Set<String> sample = CorpusSampler.randomSample(veraCorpus, 10);
        /// Cycle through sample
        for (String itemName : sample) {
            // Try all profiles
            for (ValidationProfile profile : PROFILES.getValidationProfiles()) {
                // Create a validator for profile
                PDFAValidator validator = Validators.createValidator(profile,
                        false);
                Set<ValidationResult> results = new HashSet<>();
                // Validate a fresh model instance and add the result to the set
                for (int index = 0; index < 5; index++) {
                    try (ModelParser parser = new ModelParser(
                            veraCorpus.getItemStream(itemName))) {
                        results.add(validator.validate(parser));
                    }
                }
                // Set should have only one result IF all results are identical
                assertTrue(results.size() == 1);
            }
        }

    }

    @SuppressWarnings("javadoc")
    @Test
    public void testValidatorConsistency() throws IOException,
            ValidationException, JAXBException {
        // Grab a random sample of 10 corpus files
        TestCorpus veraCorpus = CorpusManager.getVeraCorpus();
        Set<String> sample = CorpusSampler.randomSample(veraCorpus, 10);

        // Cycle through all available profile on GitHub
        for (ValidationProfile profile : PROFILES.getValidationProfiles()) {
            for (String itemName : sample) {
                // Create fresh validators for each sample item
                PDFAValidator validator = Validators.createValidator(profile,
                        false);
                PDFAValidator checkValidator = Validators.createValidator(
                        profile, false);
                // Create a new model parser instance
                try (ModelParser parser = new ModelParser(
                        veraCorpus.getItemStream(itemName))) {
                    // Validate model with fresh validator
                    ValidationResult firstResult = validator.validate(parser);
                    // Validate same model with second fresh validator instance
                    ValidationResult checkResult = checkValidator
                            .validate(parser);
                    // Validate model with first validator again
                    ValidationResult secondResult = validator.validate(parser);

                    // The results of the two separate validators should be the
                    // same (this works)
                    assertTrue(
                            "Failed checkResult:\n"
                                    + ValidationResults.resultToXml(
                                            checkResult, Boolean.TRUE)
                                    + " != SecondResult:\n"
                                    + ValidationResults.resultToXml(
                                            secondResult, Boolean.TRUE),
                            checkResult.equals(secondResult));
                    // The results of the same validator should be the same
                    // (this doesn't)
                    // The act of validation changes something in the
                    // model......
                    assertTrue(
                            "Failed firstResult:\n"
                                    + ValidationResults.resultToXml(
                                            firstResult, Boolean.TRUE)
                                    + " != SecondResult\n"
                                    + ValidationResults.resultToXml(
                                            secondResult, Boolean.TRUE),
                            firstResult.equals(secondResult));
                }
            }
        }
    }

}
