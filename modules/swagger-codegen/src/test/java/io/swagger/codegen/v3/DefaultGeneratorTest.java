package io.swagger.codegen.v3;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.List;

public class DefaultGeneratorTest {

    @Rule
    public TemporaryFolder temporaryFolder = TemporaryFolder.builder().assureDeletion().build();

    @Test
    public void processPaths_propagatesAllPathParametersToAllOperations_whereOperationsHaveNoParameters_issueXXXX() {

        // given
        final Parameter pathParameterA = parameterWithName("path-parameter-a");
        final Parameter pathParameterB = parameterWithName("path-parameter-b");

        final PathItem pathItemWithParameters = new PathItem()
            .addParametersItem(pathParameterA)
            .addParametersItem(pathParameterB)

            .get(operationWithNoParameters())
            .head(operationWithNoParameters())
            .put(operationWithNoParameters())
            .post(operationWithNoParameters())
            .delete(operationWithNoParameters())
            .patch(operationWithNoParameters())
            .options(operationWithNoParameters());

        final Paths paths = new Paths().addPathItem("/path-item-with-parameters", pathItemWithParameters);

        final List<CodegenParameter> expectedOperationParameters = codegenParametersFrom(
            pathParameterA,
            pathParameterB
        );

        final DefaultGenerator defaultGenerator = defaultGeneratorWithMinimalConfig();

        // when
        final List<CodegenOperation> actualOperations = defaultGenerator.processPaths(paths).get("Default");

        // then
        assertEquals(expectedOperationParameters, actualOperations.get(0).getAllParams());
        assertEquals(expectedOperationParameters, actualOperations.get(1).getAllParams());
        assertEquals(expectedOperationParameters, actualOperations.get(2).getAllParams());
        assertEquals(expectedOperationParameters, actualOperations.get(3).getAllParams());
        assertEquals(expectedOperationParameters, actualOperations.get(4).getAllParams());
        assertEquals(expectedOperationParameters, actualOperations.get(5).getAllParams());
        assertEquals(expectedOperationParameters, actualOperations.get(6).getAllParams());
    }

    private List<CodegenParameter> codegenParametersFrom(final Parameter pathParameterA, final Parameter pathParameterB) {
        return asList(
            firstCodegenParameterFrom(pathParameterA),
            lastCodegenParameterFrom(pathParameterB)
        );
    }

    private CodegenParameter lastCodegenParameterFrom(final Parameter pathParameterB) {
        final CodegenParameter expectedPathParameterB = new CodegenParameter();
        expectedPathParameterB.baseName = pathParameterB.getName();
        expectedPathParameterB.jsonSchema = "{\n  \"name\" : \"" + pathParameterB.getName() + "\"\n}";
        expectedPathParameterB.secondaryParam = true;
        expectedPathParameterB.vendorExtensions = ImmutableMap.of(
            "x-codegen-hasMoreOptional", false,
            "x-has-more", false
        );
        return expectedPathParameterB;
    }

    private CodegenParameter firstCodegenParameterFrom(final Parameter pathParameterA) {
        final CodegenParameter expectedPathParameterA = new CodegenParameter();
        expectedPathParameterA.baseName = pathParameterA.getName();
        expectedPathParameterA.jsonSchema = "{\n  \"name\" : \"" + pathParameterA.getName() + "\"\n}";
        expectedPathParameterA.secondaryParam = false;
        expectedPathParameterA.vendorExtensions = ImmutableMap.of(
            "x-codegen-hasMoreRequired", true,
            "x-codegen-hasMoreOptional", true,
            "x-has-more", true
        );
        return expectedPathParameterA;
    }

    private DefaultGenerator defaultGeneratorWithMinimalConfig() {
        final String irrelevantLang = "html2";
        final String irrelevantSpec = "irrelevant: spec";

        final ClientOptInput clientOptInput = new CodegenConfigurator()
            .setOutputDir(temporaryFolder.getRoot().getAbsolutePath())
            .setLang(irrelevantLang)
            .setInputSpec(irrelevantSpec)
            .toClientOptInput();

        final DefaultGenerator defaultGenerator = new DefaultGenerator();

        defaultGenerator.opts(clientOptInput);
        return defaultGenerator;
    }

    private Parameter parameterWithName(final String s) {
        return new Parameter().name(s);
    }

    private Operation operationWithNoParameters() {
        return new Operation();
    }
}