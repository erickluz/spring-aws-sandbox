package luz.erick.spring_aws_sandbox.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CpfValidatorTest {

    private final CpfValidator validator = new CpfValidator();

    @Test
    void shouldValidateCpfWithAndWithoutFormatting() {
        assertTrue(validator.isValid("52998224725"));
        assertTrue(validator.isValid("529.982.247-25"));
    }

    @Test
    void shouldRejectInvalidCpf() {
        assertFalse(validator.isValid(null));
        assertFalse(validator.isValid("111.111.111-11"));
        assertFalse(validator.isValid("529.982.247-24"));
        assertFalse(validator.isValid("123"));
    }
}
