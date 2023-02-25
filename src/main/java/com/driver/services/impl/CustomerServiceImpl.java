package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer = customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);

	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> drivers = driverRepository2.findAll();
		int lowestID = Integer.MAX_VALUE;
		Driver freeDriver = null;

		for(Driver driver:drivers){
			if(driver.getCab().getAvailable() && driver.getDriverId() < lowestID){
				lowestID = driver.getDriverId();
				freeDriver = driver;
			}
		}

		if( lowestID != Integer.MAX_VALUE){
			Customer customer = customerRepository2.findById(customerId).get();

			TripBooking tripBooking = new TripBooking();
			int bill = freeDriver.getCab().getPerKmRate() * distanceInKm;

			tripBooking.setCustomer(customer);
			tripBooking.setDriver(freeDriver);
			tripBooking.setFromLocation(fromLocation);
			tripBooking.setToLocation(toLocation);
			tripBooking.setDistanceInKm(distanceInKm);
			tripBooking.setBill(bill);
			tripBooking.setTripStatus(TripStatus.CONFIRMED);

			freeDriver.getTripBookingList().add(tripBooking);
			freeDriver.getCab().setAvailable(false);

			customer.getTripBookings().add(tripBooking);

			// tripBookingRepository2.save(tripBooking);
			customerRepository2.save(customer);
			driverRepository2.save(freeDriver);

			return tripBooking;
		}
		else
			throw new Exception("No cab available!");

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setTripStatus(TripStatus.CANCELED);
		tripBooking.setBill(0);

		Driver driver = tripBooking.getDriver();
		driver.getCab().setAvailable(true);

		tripBookingRepository2.save(tripBooking);

	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		if(tripBookingRepository2.findById(tripId).isPresent()) {
			TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
			tripBooking.setTripStatus(TripStatus.COMPLETED);

			Driver driver = tripBooking.getDriver();
			driver.getCab().setAvailable(true);

			tripBookingRepository2.save(tripBooking);
		}

	}
}
