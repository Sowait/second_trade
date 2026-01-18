package com.secondtrade.seed;

import com.secondtrade.market.brand.Brand;
import com.secondtrade.market.brand.BrandRepository;
import com.secondtrade.market.category.Category;
import com.secondtrade.market.category.CategoryRepository;
import com.secondtrade.market.device.DeviceModel;
import com.secondtrade.market.device.DeviceModelRepository;
import com.secondtrade.user.User;
import com.secondtrade.user.UserRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SeedRunner implements CommandLineRunner {
  private final boolean seed;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final CategoryRepository categoryRepository;
  private final BrandRepository brandRepository;
  private final DeviceModelRepository deviceModelRepository;

  public SeedRunner(
    @Value("${app.seed:true}") boolean seed,
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    CategoryRepository categoryRepository,
    BrandRepository brandRepository,
    DeviceModelRepository deviceModelRepository
  ) {
    this.seed = seed;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.categoryRepository = categoryRepository;
    this.brandRepository = brandRepository;
    this.deviceModelRepository = deviceModelRepository;
  }

  @Override
  public void run(String... args) {
    if (!seed) return;

    if (userRepository.count() == 0) {
      User admin = new User();
      admin.setStudentId("admin");
      admin.setUsername("admin");
      admin.setNickname("管理员");
      admin.setRole("admin");
      admin.setSuperuser(true);
      admin.setStaff(true);
      admin.setDisabled(false);
      admin.setCreditScore(100);
      admin.setCreatedAt(LocalDateTime.now());
      admin.setPasswordHash(passwordEncoder.encode("admin123"));
      userRepository.save(admin);

      User u = new User();
      u.setStudentId("20240001");
      u.setUsername("mockuser");
      u.setNickname("Mock User");
      u.setRole("user");
      u.setSuperuser(false);
      u.setStaff(false);
      u.setDisabled(false);
      u.setCreditScore(90);
      u.setSchool("示例大学");
      u.setPhone("13800138000");
      u.setAddress("Mock Address");
      u.setCreatedAt(LocalDateTime.now());
      u.setPasswordHash(passwordEncoder.encode("123456"));
      userRepository.save(u);
    }

    if (categoryRepository.count() == 0) {
      Category c1 = new Category();
      c1.setName("Smartphones");
      c1.setCode("smartphones");
      categoryRepository.save(c1);

      Category c2 = new Category();
      c2.setName("Laptops");
      c2.setCode("laptops");
      categoryRepository.save(c2);

      Category c3 = new Category();
      c3.setName("Tablets");
      c3.setCode("tablets");
      categoryRepository.save(c3);

      Category c4 = new Category();
      c4.setName("Smartwatches");
      c4.setCode("smartwatches");
      categoryRepository.save(c4);

      Brand b1 = new Brand();
      b1.setName("Apple");
      b1.setCategory(c1);
      brandRepository.save(b1);

      Brand b2 = new Brand();
      b2.setName("Samsung");
      b2.setCategory(c1);
      brandRepository.save(b2);

      Brand b3 = new Brand();
      b3.setName("Xiaomi");
      b3.setCategory(c1);
      brandRepository.save(b3);

      Brand b4 = new Brand();
      b4.setName("Dell");
      b4.setCategory(c2);
      brandRepository.save(b4);

      Brand b5 = new Brand();
      b5.setName("Lenovo");
      b5.setCategory(c2);
      brandRepository.save(b5);

      DeviceModel m1 = new DeviceModel();
      m1.setName("iPhone 13");
      m1.setBrand(b1);
      m1.setCategory(c1);
      m1.setImageUrl("https://placehold.co/200x200?text=iPhone+13");
      m1.setMsrpPrice("5999");
      m1.setIndexType("phone_standard");
      deviceModelRepository.save(m1);

      DeviceModel m2 = new DeviceModel();
      m2.setName("Galaxy S21");
      m2.setBrand(b2);
      m2.setCategory(c1);
      m2.setImageUrl("https://placehold.co/200x200?text=Galaxy+S21");
      m2.setMsrpPrice("4999");
      m2.setIndexType("phone_standard");
      deviceModelRepository.save(m2);
    }
  }
}

