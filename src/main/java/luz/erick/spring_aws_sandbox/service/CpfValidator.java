package luz.erick.spring_aws_sandbox.service;

import org.springframework.stereotype.Service;

@Service
public class CpfValidator {

    private static final int CPF_LENGTH = 11;

    public boolean isValid(String cpf) {
        if (cpf == null) {
            return false;
        }

        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() != CPF_LENGTH || hasAllDigitsEqual(digits)) {
            return false;
        }

        return calculateDigit(digits, 9) == Character.getNumericValue(digits.charAt(9))
                && calculateDigit(digits, 10) == Character.getNumericValue(digits.charAt(10));
    }

    private boolean hasAllDigitsEqual(String cpf) {
        return cpf.chars().distinct().count() == 1;
    }

    private int calculateDigit(String cpf, int length) {
        int sum = 0;

        for (int index = 0; index < length; index++) {
            sum += Character.getNumericValue(cpf.charAt(index)) * (length + 1 - index);
        }

        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
