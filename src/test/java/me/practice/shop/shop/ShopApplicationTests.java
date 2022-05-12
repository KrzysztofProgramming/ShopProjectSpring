package me.practice.shop.shop;

import me.practice.shop.shop.database.authors.AuthorsRepository;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.database.products.types.CommonTypesRepository;
import me.practice.shop.shop.database.users.RolesRepository;
import me.practice.shop.shop.database.users.UsersRepository;
import me.practice.shop.shop.models.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
class ShopApplicationTests {

	@Autowired
	private AuthorsRepository authorsRepository;

	@Autowired
	private ProductsRepository productsRepository;

	@Autowired
	private CommonTypesRepository typesRepository;

	@Autowired
	private RolesRepository rolesRepository;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	private UsersRepository usersRepository;

	@Autowired
	private EntityManager em;

	private final List<String> exampleNames = Arrays.asList("Adams", "Baker", "Clark", "Davis", "Evans", "Frank", "Ghosh",
			"Hills", "Irwin", "Jones", "Klein", "Lopez", "Mason", "Nalty", "Ochoa", "Patel", "Quinn", "Reily",
			"Smith", "Trott", "Usman", "Valdo", "White", "Xiang", "Yakub", "Zafar", "Zaheer");

	@Test
	void contextLoads() {
	}

	@Test
	void createExampleAuthors(){
		List<Author> authors = new ArrayList<>(4000);
		for(int i=0; i<4000; i++) {
			Author author = Author.builder()
					.name(this.exampleNames.get((int) (Math.random() * this.exampleNames.size())) + Math.random())
					.description(Math.random() > 0.5 ? "Przykładowy opis autora" : null)
					.build();
			authors.add(author);
		}
		this.authorsRepository.saveAll(authors);
	}

	@Test
	void createExampleTypes(){
		List<CommonType> types = new ArrayList<>(1400);
		for(int i=0; i<1400; i++) {
			CommonType type = CommonType.builder()
					.name(UUID.randomUUID().toString())
					.build();
			types.add(type);
		}
		this.typesRepository.saveAll(types);
	}

	@Test
	void createExampleProducts(){
		List<BookProduct> books = new ArrayList<>(20000);
		for(int i=0; i<20000; i++){
			BookProduct book = BookProduct.builder()
					.name(UUID.randomUUID().toString().replaceAll("-", ""))
					.price((Math.random() * 100 + 1))
					.description(UUID.randomUUID().toString().replaceAll("-", ""))
					.inStock(random.nextInt(20) + 1)
					.authors(randomAuthors())
					.types(randomTypes())
					.build();
			books.add(book);
		}
		this.productsRepository.saveAll(books);
	}

	@Test
	void createTypesWithRange(){
		List<CommonType> types = new ArrayList<>(300);
		for(int i = 1601; i<=1900; i++){
			if(i==1614 || i ==1675) continue;
			CommonType type = CommonType.builder()
					.id((long)i)
					.name(UUID.randomUUID().toString())
					.build();
			types.add(type);
		}
		this.typesRepository.saveAllAndFlush(types);
	}

	private final Random random = new Random();

	public Set<Author> randomAuthors(){
		return random.longs(random.nextInt(30) + 1, 1, 10001).boxed()
				.map(id->Author.builder().id(id).build()).collect(Collectors.toSet());
	}

	public Set<CommonType> randomTypes(){
		return Stream.concat(random.longs(random.nextInt(10) + 1, 1, 1605).boxed()
				.map(id->CommonType.builder().id(id).build()),
				random.longs(random.nextInt(20) + 1, 1901, 4190).boxed()
						.map(id->CommonType.builder().id(id).build())).collect(Collectors.toSet());
	}

	@Test
	public void createExampleData(){
		this.createExampleAuthors();
		this.createExampleTypes();
		this.createExampleProducts();
	}

	@Test
	public void createAll(){
		this.createExampleData();
		this.createAdminRole();
		this.createAdminUser();
	}

	@Test
	public void createAdminRole(){
		Role role = new Role("admin", Collections.emptySet(), 0.0);
		this.rolesRepository.save(role);
	}

	@Test
	public void createAdminUser(){
		ShopUser admin = ShopUser.builder()
				.email("admin@gmail.com")
				.username("admin")
				.password(encoder.encode("admin"))
				.roles(Set.of(Role.builder().name("admin").build()))
				.build();

//		System.out.println(admin);
		this.usersRepository.save(admin);
	}

	@Test
	public void testDJO(){
		System.out.println(this.typesRepository.getTypeResponses(PageRequest.of(0, 50)).toList());
	}


	@Test
	public void reflectiveTest(){
		Object obj = ShopUser.builder().build();
		Class<?> c = obj.getClass();
		try {
			Field field = c.getDeclaredField("username");
			field.setAccessible(true);
			field.set(obj, "elo");
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
		System.out.println(obj);
	}
}
