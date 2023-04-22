package com.filmdoms.community.config;

import static org.mockito.BDDMockito.given;

import com.filmdoms.community.account.config.SecurityConfig;
import com.filmdoms.community.account.config.jwt.JwtTokenProvider;
import com.filmdoms.community.account.config.oauth.CustomOAuthSuccessHandler;
import com.filmdoms.community.account.data.constant.AccountRole;
import com.filmdoms.community.account.data.entity.Account;
import com.filmdoms.community.account.repository.AccountRepository;
import com.filmdoms.community.account.service.TokenAuthenticationService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import org.springframework.test.util.ReflectionTestUtils;

@Import({SecurityConfig.class, JwtTokenProvider.class, TokenAuthenticationService.class, CustomOAuthSuccessHandler.class})
@TestPropertySource(properties = {
        "JWT_KEY=aKeyThatIsVeryLongToBeUsedForJWTKEY"
})
public class TestSecurityConfig {

    @MockBean
    private AccountRepository accountRepository;

    @BeforeTestMethod
    public void securitySetUp() {
        Account mockAdminAccount = Account.builder()
                .email("testAdmin@filmdoms.com")
                .password("password")
                .role(AccountRole.ADMIN)
                .build();
        ReflectionTestUtils.setField(mockAdminAccount, Account.class, "id", 1L, Long.class);
        given(accountRepository.findByEmail("testAdmin@filmdoms.com")).willReturn(Optional.of(mockAdminAccount));

        Account mockUserAccount = Account.builder()
                .email("testUser@filmdoms.com")
                .password("password")
                .role(AccountRole.USER)
                .build();
        ReflectionTestUtils.setField(mockUserAccount, Account.class, "id", 2L, Long.class);
        given(accountRepository.findByEmail("testUser@filmdoms.com")).willReturn(Optional.of(mockUserAccount));
    }

}