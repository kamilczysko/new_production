package application;

import java.util.Calendar;

public class NextWorkingDay {
	public static Calendar getNextWorkingday(Calendar today, boolean workingSaturday) {

		Calendar theRet = null;
		boolean isHoliday = true;
		Calendar[] holidays = null;

		theRet = Calendar.getInstance();
		theRet.setTime(today.getTime());
		while (isHoliday) {
			theRet.add(Calendar.DAY_OF_YEAR, 1);
			if (theRet.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) // niedziela
				if (theRet.get(Calendar.MONTH) != Calendar.JANUARY || // Nowy
																		// Rok i
																		// Trzech
																		// Kroli
						(theRet.get(Calendar.DAY_OF_MONTH) != 1 && theRet.get(Calendar.DAY_OF_MONTH) != 6))
					if (theRet.get(Calendar.MONTH) != Calendar.DECEMBER || // Boze
																			// Narodzenie
							(theRet.get(Calendar.DAY_OF_MONTH) != 25 && theRet.get(Calendar.DAY_OF_MONTH) != 26))
						if (theRet.get(Calendar.MONTH) != Calendar.NOVEMBER || // swieta
																				// listopadowe
								(theRet.get(Calendar.DAY_OF_MONTH) != 1 && theRet.get(Calendar.DAY_OF_MONTH) != 11))
							if (theRet.get(Calendar.MONTH) != Calendar.AUGUST || // swieto
																					// Wojska
																					// Polskiego
									theRet.get(Calendar.DAY_OF_MONTH) != 15)
								if (theRet.get(Calendar.MONTH) != Calendar.MAY || // swieta
																					// majowe
										(theRet.get(Calendar.DAY_OF_MONTH) != 1
												&& theRet.get(Calendar.DAY_OF_MONTH) != 3))
									if (workingSaturday || theRet.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) { // robocza
																													// sobota
										if (holidays == null) {
											// obliczenie daty Lanego
											// Poniedzialku
											int e = today.get(Calendar.YEAR);
											int a = e % 19; // a = rok mod 19
											int b = e % 4; // b = rok mod 4
											int c = e % 7; // c = rok mod 7
											int d = (a * 19 + 24) % 30; // d =
																		// (a*19
																		// + A)
																		// mod
																		// 30,
																		// gdzie
																		// A =
																		// 24
																		// dla
																		// 1900-2199
											Calendar wielkanoc = Calendar.getInstance();
											wielkanoc.set(e, 2, 22); // 2-marzec,
																		// czyli
																		// 22
																		// marca
																		// danego
																		// roku
											if (e < 2100) // e = (2b + 4c + 6d +
															// B) mod 7,
												e = (2 * b + 4 * c + 6 * d + 5) % 7; // gdzie
																						// B
																						// =
																						// 5
																						// dla
																						// <2100,
											else
												e = (2 * b + 4 * c + 6 * d + 6) % 7; // a
																						// pozniej
																						// B
																						// =
																						// 6
											/*
											 * Zakres lat A B - 1582 15 6 1583 -
											 * 1699 22 2 1700 - 1799 23 3 1800 -
											 * 1999 23 4 1900 - 2099 24 5 2100 -
											 * 2199 24 6 2200 - 2299 25 0 2300 -
											 * 2399 26 1 2400 - 2499 25 1
											 */
											if (e == 6) // jeżeli e = 6
												// oraz d = 29 lub d = 28 to
												// Wielkanoc miałaby przypaść na
												// dzień 26 lub 25 kwietnia.
												if (d == 29 || d == 28)
													d -= 7; // Wtedy zawsze
															// obchodzi się ją
															// tydzień
															// wcześniej, tzn.
															// 19 lub 18
															// kwietnia
											// Suma liczb d + e okresla ilosc
											// dni po 22 marca, kiedy przypada
											// niedziela Wielkanocna
											wielkanoc.add(Calendar.DAY_OF_YEAR, d + e + 1); // data
																							// Lanego
																							// Poniedzialku
											Calendar bozeCialo = Calendar.getInstance(); // Boze
																							// Cialo
																							// jest
																							// swietem
																							// obchodzonym
																							// 60
																							// dni
																							// po
											bozeCialo.setTime(wielkanoc.getTime()); // Wielkanocy,
																					// czyli
											bozeCialo.add(Calendar.DAY_OF_YEAR, 59); // 59
																						// dni
																						// po
																						// Lanym
																						// Poniedzialku
											holidays = new Calendar[] { wielkanoc, bozeCialo };
										}
										isHoliday = false;
										for (int i = 0; i < holidays.length; i++)
											if (theRet.get(Calendar.YEAR) == holidays[i].get(Calendar.YEAR)
													&& theRet.get(Calendar.MONTH) == holidays[i].get(Calendar.MONTH)
													&& theRet.get(Calendar.DAY_OF_MONTH) == holidays[i]
															.get(Calendar.DAY_OF_MONTH)) {
												isHoliday = true;
												break;
											}
									}
		}

		return theRet;
	}
}
