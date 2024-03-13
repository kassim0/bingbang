package com.stars.bigbang;

//import com.stars.bigbang.rest.RawgApi;
import com.stars.bigbang.rest.RawgApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
//import org.springframework.web.reactive.function.client.WebClient;

/*import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;*/

@SpringBootApplication
@EnableFeignClients(clients = RawgApi.class)
public class BigbangApplication {

	public static void main(String[] args) {
		SpringApplication.run(BigbangApplication.class, args);
/*
		String url = "https://api.rawg.io/api/games?key=d4df6345d7fb4a4e842849ef2bf16ba7";
		WebClient.Builder builder = WebClient.builder();

		String allGames = builder.build().get().uri(url).retrieve().bodyToMono(String.class).block();

		System.out.println("---------------------------------");
		System.out.println(allGames);
		System.out.println("---------------------------------");*/

		// appel à l'API//
/*		System.out.println("Début du protocole à la requête http: ");
		URL url = new URL("https://api.rawg.io/api/games?key=d4df6345d7fb4a4e842849ef2bf16ba7");
		System.out.println("URL crée");
		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			System.out.println("Connexion établi");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestMethod("GET");
			System.out.println("Requête envoyé !");
			System.out.println(con.getResponseCode());

		} catch (IOException e) {
			throw new RuntimeException(e);
		}*/
	}

}
