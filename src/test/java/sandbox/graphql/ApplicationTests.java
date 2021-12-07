package sandbox.graphql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTests {

	@LocalServerPort
	private int port;

	private WebGraphQlTester graphQlTester;


	@BeforeEach
	void setUp(@Autowired WebTestClient client) {
		this.graphQlTester = WebGraphQlTester.create(client.mutate()
				.baseUrl("http://localhost:" + this.port + "/graphql").build());
	}


	@Test
	void greeting() {
		String queryName = "greeting";
		this.graphQlTester.query("{" + queryName + "}").execute()
				.path(queryName).entity(String.class).isEqualTo("Hello 007");
	}

	@Test
	void greetingMono() {
		String queryName = "greetingMono";
		this.graphQlTester.query("{" + queryName + "}").execute()
				.path(queryName).entity(String.class).isEqualTo("Hello");
	}

}
