package br.com.alr.api.shared.infrastructure.security;

import br.com.alr.api.shared.domain.PasswordHasher;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BcryptPasswordHasher implements PasswordHasher {

    @Override
    public String hash(String rawPassword) {
        return BcryptUtil.bcryptHash(rawPassword);
    }
}
