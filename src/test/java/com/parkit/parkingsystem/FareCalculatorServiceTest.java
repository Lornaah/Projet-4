package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;

@ExtendWith(MockitoExtension.class)

public class FareCalculatorServiceTest {

	private static FareCalculatorService fareCalculatorService;
	private Ticket ticket;

	@Mock
	TicketDAO ticketDAO;

	@BeforeAll
	private static void setUp() {
		fareCalculatorService = new FareCalculatorService();
	}

	@BeforeEach
	private void setUpPerTest() {
		ticket = new Ticket();
	}

	private void setUpTicket(Date inTime, Date outTime, ParkingSpot parkingSpot) {
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
	}

	@Test
	public void calculateFareCar() {
		// Arrange
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		// Act
		setUpTicket(inTime, outTime, parkingSpot);
		fareCalculatorService.calculateFare(ticket, ticketDAO);

		// Assert
		assertEquals(ticket.getPrice(), 1 * Fare.CAR_RATE_PER_HOUR);
	}

	@Test
	public void calculateFareBike() {
		// Arrange
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		// Act
		setUpTicket(inTime, outTime, parkingSpot);
		fareCalculatorService.calculateFare(ticket, ticketDAO);

		// Assert
		assertEquals(ticket.getPrice(), 1 * Fare.BIKE_RATE_PER_HOUR);
	}

	@Test
	public void calculateFareUnkownType() {
		// Arrange
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

		// Act
		setUpTicket(inTime, outTime, parkingSpot);

		// Assert
		assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket, ticketDAO));
	}

	@Test
	public void calculateFareBikeWithFutureInTime() {
		// Arrange
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		// Act
		setUpTicket(inTime, outTime, parkingSpot);

		// Assert
		assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket, ticketDAO));
	}

	@Test
	public void calculateFareBikeWithLessThanOneHourParkingTime() {
		// Arrange
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));// 45 minutes parking time should give 3/4th
																		// parking fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		// Act
		setUpTicket(inTime, outTime, parkingSpot);
		fareCalculatorService.calculateFare(ticket, ticketDAO);

		// Assert
		assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareCarWithLessThanOneHourParkingTime() {
		// Arrange
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));// 45 minutes parking time should give 3/4th
																		// parking fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		// Act
		setUpTicket(inTime, outTime, parkingSpot);

		fareCalculatorService.calculateFare(ticket, ticketDAO);

		// Assert
		assertEquals((0.75 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareCarWithMoreThanADayParkingTime() {
		// Arrange
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));// 24 hours parking time should give 24 *
																			// parking fare per hour
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		// Act
		setUpTicket(inTime, outTime, parkingSpot);
		fareCalculatorService.calculateFare(ticket, ticketDAO);

		// Assert
		assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void freeThirtyMinutes() {
		// Arrange
		Date inTime = new Date();
		// 30 minutes parking time or less should be free
		inTime.setTime(System.currentTimeMillis() - (30 * 60 * 1000));

		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		// Act
		setUpTicket(inTime, outTime, parkingSpot);
		fareCalculatorService.calculateFare(ticket, ticketDAO);

		// Assert
		assertEquals(0, ticket.getPrice());
	}

	@Test
	@DisplayName("A vehicle which has already visited the parking has to get 5% off discount")
	public void getDiscount() {

		// Arrange
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		// Mock ticketDAO's countVisit returning true
		when(ticketDAO.hasVisited(ticket)).thenReturn(true);

		// Act
		setUpTicket(inTime, outTime, parkingSpot);
		fareCalculatorService.calculateFare(ticket, ticketDAO);

		// Assert
		assertEquals(Fare.CAR_RATE_PER_HOUR * 0.95, ticket.getPrice());
	}
}