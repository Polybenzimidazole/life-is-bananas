package be.lifeisbananas.service;

import be.lifeisbananas.dao.RawMaterialRepository;
import be.lifeisbananas.domain.RawMaterial;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RawMaterialServiceImpl implements RawMaterialService {

	private final RawMaterialRepository rawMaterialRepository;

	@Override
	public List<RawMaterial> findAll() {
		return rawMaterialRepository.findAllByOrderByNameAsc();
	}
}
