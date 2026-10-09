package be.lifeisbananas.service;

import be.lifeisbananas.domain.BioEngineer;

import java.util.Optional;

public interface BioEngineerService {

	/** Geeft de bio-ingenieur terug als e-mailadres en wachtwoord kloppen. */
	Optional<BioEngineer> authenticate(String email, String password);

	/** @throws NietGevondenException als er geen bio-ingenieur met dat e-mailadres is */
	BioEngineer findByEmail(String email);
}
