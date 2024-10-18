package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {
    @Autowired
    RestTemplate restTemplate;
    private ServiceInstance serviceInstanceMaps;

    private final CarRepository carRepository;
    private MapsClient mapsClient;
    private PriceClient priceClient;

    public CarService(CarRepository carRepository, MapsClient mapsClient, PriceClient priceClient) {
        this.carRepository = carRepository;
        this.mapsClient =  mapsClient;
        this.priceClient = priceClient;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return carRepository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        Car car = findCar(id);

        String priceOfCar = priceClient.getAddressWithServiceName(id);
        car.setPrice(priceOfCar);

        Location addressOfCar = mapsClient.getAddressWithServiceName(car.getLocation());
        car.setLocation(addressOfCar);

        return car;
    }


    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            return carRepository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        carToBeUpdated.setCondition(car.getCondition());
                        return carRepository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        return carRepository.save(car);
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        Car car = findCar(id);

        try {
            carRepository.delete(car);
        } catch (Exception e) {
            throw new CarNotFoundException("Car could not be deleted. CarId: " + id);
        }
    }

    private Car findCar(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException("Car not found with id: " + id));
        return car;
    }

}
