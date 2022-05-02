package me.practice.shop.shop;

import me.practice.shop.shop.database.authors.AuthorsRepository;
import me.practice.shop.shop.database.products.ProductsRepository;
import me.practice.shop.shop.database.products.types.CommonTypesRepository;
import me.practice.shop.shop.database.users.RolesRepository;
import me.practice.shop.shop.database.users.UsersRepository;
import me.practice.shop.shop.models.*;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

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
		List<Author> authors = new ArrayList<>(2000);
		for(int i=0; i<2000; i++) {
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
		List<CommonType> types = new ArrayList<>(1000);
		for(int i=0; i<1000; i++) {
			CommonType type = CommonType.builder()
					.name(UUID.randomUUID().toString())
					.build();
			types.add(type);
		}
		this.typesRepository.saveAll(types);
	}

	@Test
	void createExampleProducts(){
		List<BookProduct> books = new ArrayList<>(8000);
		for(int i=0; i<8000; i++){
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

	private final Random random = new Random();

	public Set<Author> randomAuthors(){
		return random.longs(random.nextInt(20) + 1, 1, 6001).boxed()
				.map(id->Author.builder().id(id).build()).collect(Collectors.toSet());
	}

	public Set<CommonType> randomTypes(){
		return random.longs(random.nextInt(20) + 1, 1, 1601).boxed()
				.map(id->CommonType.builder().id(id).build()).collect(Collectors.toSet());
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
	@Transactional
	public void initialIndexing() throws InterruptedException {
		SearchSession searchSession = Search.session(this.em);

		MassIndexer indexer = searchSession.massIndexer(BookProduct.class )
				.threadsToLoadObjects(8);

		indexer.startAndWait();
	}

	@Test
	@Transactional
	public void testSearch(){
		SearchSession searchSession = Search.session(this.em);
		SearchScope<BookProduct> scope = searchSession.scope(BookProduct.class);
		SearchResult<BookProduct> bookProduct = searchSession.search(scope).where(scope.predicate().match()
			.fields("description", "name").matching("brązu").toPredicate()).fetchAll();
		System.out.println(bookProduct.hits());
	}

	@Test
	@Transactional
	public void testSave(){
		this.productsRepository.save(BookProduct.builder().id(20L).name("test").price(10.0).build());
//		this.productsRepository.save();
//		System.out.println(this.productsRepository.findById(20L));
//		this.em.flush();
	}
}
