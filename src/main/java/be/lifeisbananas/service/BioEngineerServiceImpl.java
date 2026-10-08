package be.lifeisbananas.service;

import be.lifeisbananas.dao.BioEngineerRepository;
import be.lifeisbananas.domain.BioEngineer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BioEngineerServiceImpl implements BioEngineerService {

	private final BioEngineerRepository bioEngineerRepository;

	@Override
	public Optional<BioEngineer> authenticate(String email, String password) {
		if (email == null || password == null) {
			return Optional.empty();
		}
		return bioEngineerRepository.findByEmail(email.trim())
				.filter(bioEngineer -> password.equals(bioEngineer.getPassword()));
	}

	@Override
	public BioEngineer findByEmail(String email) {
		return bioEngineerRepository.findByEmail(email)
				.orElseThrow(() -> new NietGevondenException("Geen bio-ingenieur met e-mailadres " + email));
	}
}
