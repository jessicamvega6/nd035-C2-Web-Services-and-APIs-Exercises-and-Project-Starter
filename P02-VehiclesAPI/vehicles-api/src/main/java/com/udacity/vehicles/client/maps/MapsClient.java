package com.udacity.vehicles.client.maps;

import com.udacity.vehicles.domain.Location;
import java.util.Objects;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Implements a class to interface with the Maps Client for location data.
 */
@Component
public class MapsClient {

    private static final Logger log = LoggerFactory.getLogger(MapsClient.class);

    private final WebClient client;
    private final ModelMapper mapper;

    @Value("${maps.api.name}")
    private String serviceName;


    private RestTemplate restTemplate;


    public MapsClient(WebClient maps,
                      ModelMapper mapper,
                      RestTemplate restTemplate) {
        this.client = maps;
        this.mapper = mapper;
        this.restTemplate = restTemplate;
    }

    /**
     * Gets an address from the Maps client, given latitude and longitude.
     * @param location An object containing "lat" and "lon" of location
     * @return An updated location including street, city, state and zip,
     *   or an exception message noting the Maps service is down
     */
    public Location getAddress(Location location) {
        try {
            Address address = client
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/maps/")
                            .queryParam("lat", location.getLat())
                            .queryParam("lon", location.getLon())
                            .build()
                    )
                    .retrieve().bodyToMono(Address.class).block();

            mapper.map(Objects.requireNonNull(address), location);

            return location;
        } catch (Exception e) {
            log.warn("Map service is down");
            return location;
        }
    }


    public Location getAddressWithServiceName(Location location) {
        String url = "http://"+ serviceName +"/maps/?lat="+location.getLat()+"&lon="+location.getLon();

        try {
            Address address = restTemplate.getForObject(url, Address.class);
            mapper.map(Objects.requireNonNull(address), location);

        } catch (Exception e) {
            log.warn("Map service is down");
            return location;
        }

        return location;
    }

}
