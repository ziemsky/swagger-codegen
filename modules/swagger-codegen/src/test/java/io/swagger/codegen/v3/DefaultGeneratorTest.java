package io.swagger.codegen.v3;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class DefaultGeneratorTest {

    private File tempDirectory;

    @BeforeMethod
    public void setUp() throws IOException {
        tempDirectory = newTempDirectory();
    }

    @AfterMethod
    public void tearDown() throws IOException {
        deleteDirectoryWithContentIfExists(tempDirectory);
    }

    @Test
    public void processPaths_propagatesAllPathParametersToAllOperations_whereOperationsHaveNoParameters_issueXXXX() {

        // given
        final String parameterNameA = "path-parameter-a";
        final String parameterNameB = "path-parameter-b";

        final PathItem pathItemWithParameters = new PathItem()
            .addParametersItem(parameterWithName(parameterNameA))
            .addParametersItem(parameterWithName(parameterNameB))

            .get(operationWithNoParameters())
            .head(operationWithNoParameters())
            .put(operationWithNoParameters())
            .post(operationWithNoParameters())
            .delete(operationWithNoParameters())
            .patch(operationWithNoParameters())
            .options(operationWithNoParameters());

        final Paths paths = new Paths().addPathItem("/path-item-with-parameters", pathItemWithParameters);

        final DefaultGenerator defaultGenerator = defaultGeneratorWithMinimalConfig();

        // when
        final Map<String, List<CodegenOperation>> actualProcessedPaths = defaultGenerator.processPaths(paths);

        // then
        final List<CodegenOperation> actualOperations = actualProcessedPaths.get("Default");
        // operations in order emitted by DefaultCodegen
        assertOperationHasParametersWithNames(actualOperations.get(0), parameterNameA, parameterNameB); // get
        assertOperationHasParametersWithNames(actualOperations.get(1), parameterNameA, parameterNameB); // head
        assertOperationHasParametersWithNames(actualOperations.get(2), parameterNameA, parameterNameB); // put
        assertOperationHasParametersWithNames(actualOperations.get(3), parameterNameA, parameterNameB); // post
        assertOperationHasParametersWithNames(actualOperations.get(4), parameterNameA, parameterNameB); // delete
        assertOperationHasParametersWithNames(actualOperations.get(5), parameterNameA, parameterNameB); // patch
        assertOperationHasParametersWithNames(actualOperations.get(6), parameterNameA, parameterNameB); // options
    }

    private Parameter parameterWithName(final String s) {
        return new Parameter().name(s);
    }

    private Operation operationWithNoParameters() {
        return new Operation();
    }

    private DefaultGenerator defaultGeneratorWithMinimalConfig() {
        final String irrelevantLang = "html2";
        final String irrelevantSpec = "irrelevant: spec";

        final ClientOptInput clientOptInput = new CodegenConfigurator()
            .setOutputDir(tempDirectory.getAbsolutePath())
            .setLang(irrelevantLang)
            .setInputSpec(irrelevantSpec)
            .toClientOptInput();

        final DefaultGenerator defaultGenerator = new DefaultGenerator();

        defaultGenerator.opts(clientOptInput);
        return defaultGenerator;
    }

    private void assertOperationHasParametersWithNames(final CodegenOperation codegenOperation, final String parameterNameA, final String parameterNameB) {
        assertThat(
            codegenOperation.getAllParams(),
            hasItems(
                codegenParameterWithBaseName(parameterNameA),
                codegenParameterWithBaseName(parameterNameB)
            )
        );
    }

    private File newTempDirectory() throws IOException {
        final File tempFolder = Files.createTempDirectory("codegentest-").toFile();
        tempFolder.deleteOnExit();

        return tempFolder;
    }

    private void deleteDirectoryWithContentIfExists(final File directory) throws IOException {
        if (directory != null) {
            FileUtils.deleteDirectory(directory);
        }
    }

    private Matcher<CodegenParameter> codegenParameterWithBaseName(final String expectedParameterName) {
        return new CustomTypeSafeMatcher<CodegenParameter>("parameter with name " + expectedParameterName) {
            @Override protected boolean matchesSafely(final CodegenParameter actualCodegenParameter) {
                return expectedParameterName.equals(actualCodegenParameter.baseName);
            }
        };
    }
}