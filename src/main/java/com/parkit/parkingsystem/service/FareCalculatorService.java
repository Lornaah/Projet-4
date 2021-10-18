package com.parkit.parkingsystem.service;

import java.util.Date;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	public void calculateFare(Ticket ticket) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		Date inDate = ticket.getInTime();
		Date outDate = ticket.getOutTime();

		long inTime = inDate.getTime();
		long outTime = outDate.getTime();

		float duration = outTime - inTime;
		float durationInHour = duration / 1000 / 3600;

		if (durationInHour <= 0.5) {
			ticket.setPrice(0);
		}

		else {
			switch (ticket.getParkingSpot().getParkingType()) {
			case CAR: {
				ticket.setPrice((durationInHour - 0.5) * Fare.CAR_RATE_PER_HOUR);
				break;
			}

			case BIKE: {
				ticket.setPrice((durationInHour - 0.5) * Fare.BIKE_RATE_PER_HOUR);
				break;
			}

			default:
				throw new IllegalArgumentException("Unkown Parking Type");
			}

		}
	}
}