package kh.com.csx.posapi.seed;

import kh.com.csx.posapi.entity.PermissionEntity;
import kh.com.csx.posapi.entity.RoleEntity;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (args.containsOption("seed")) {
            PermissionEntity createPermission = PermissionEntity.builder()
                    .name("CREATE")
                    .build();
            PermissionEntity updatePermission = PermissionEntity.builder()
                    .name("UPDATE")
                    .build();
            PermissionEntity readPermission = PermissionEntity.builder()
                    .name("READ")
                    .build();
            PermissionEntity deletePermission = PermissionEntity.builder()
                    .name("DELETE")
                    .build();
            RoleEntity adminRole = RoleEntity.builder()
                    .name("ADMIN")
                    .permissions(Set.of(readPermission,createPermission,updatePermission,deletePermission))
                    .build();
            UserEntity userEntity = UserEntity.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("123456"))
                    .roles(Set.of(adminRole))
                    .build();
            userRepository.save(userEntity);
        }
    }
}
