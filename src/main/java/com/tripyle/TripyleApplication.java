package com.tripyle;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"99.TripyleApplication"})
@EnableJpaAuditing
@SpringBootApplication
@RestController
public class TripyleApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripyleApplication.class, args);

		//메모리 사용량 출력
		long heapSize = Runtime.getRuntime().totalMemory();
		System.out.println("HEAP Size(M) : "+ heapSize / (1024*1024) + " MB");

	}

	@ApiOperation(value = "TripyleApplication")
	@GetMapping("/")
	@ResponseStatus(HttpStatus.OK)
	public String status() {
		return "TripyleApplication Server is On";
	}
}
