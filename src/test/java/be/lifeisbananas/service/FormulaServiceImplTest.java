package be.lifeisbananas.service;

import be.lifeisbananas.dao.FormulaRepository;
import be.lifeisbananas.dao.RawMaterialRepository;
import be.lifeisbananas.domain.BioEngineer;
import be.lifeisbananas.domain.Formula;
import be.lifeisbananas.domain.FruitRawMaterial;
import be.lifeisbananas.domain.LifecycleStatus;
import be.lifeisbananas.domain.RawMaterial;
import be.lifeisbananas.domain.UnitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testen van de servicelaag, met nagebootste repositories: er is dus geen
 * databank nodig om deze testen te draaien.
 */
@ExtendWith(MockitoExtension.class)
class FormulaServiceImplTest {

	@Mock
	private FormulaRepository formulaRepository;

	@Mock
	private RawMaterialRepository rawMaterialRepository;

	@InjectMocks
	private FormulaServiceImpl service;

	private Formula receptuur;
	private RawMaterial banaan;

	@BeforeEach
	void setUp() {
		BioEngineer bioEngineer = new BioEngineer("Dr. Sarah Johnson", "sarah.johnson@lifeisbananas.com",
				null, "Fruit Processing", "banaan123");
		receptuur = new Formula("Banana Chips Formula", "1.0", "1. Was de bananen.", bioEngineer);
		receptuur.setId(1L);
		banaan = new FruitRawMaterial("Banana", true, new BigDecimal("2.50"), "kg");
		banaan.setId(7L);
	}

	@Test
	void eenLegeZoektermGeeftAlleRecepturen() {
		when(formulaRepository.findAllByOrderByNameAsc()).thenReturn(List.of(receptuur));

		assertThat(service.search("  ")).containsExactly(receptuur);
		verify(formulaRepository, never()).findByNameContainingIgnoreCaseOrderByNameAsc(any());
	}

	@Test
	void erWordtGezochtOpEenStukVanDeNaam() {
		when(formulaRepository.findByNameContainingIgnoreCaseOrderByNameAsc("Banana"))
				.thenReturn(List.of(receptuur));

		assertThat(service.search("  Banana  ")).containsExactly(receptuur);
	}

	@Test
	void eenOnbekendeReceptuurGeeftNietGevonden() {
		when(formulaRepository.findByIdWithIngredients(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.findById(404L))
				.isInstanceOf(NietGevondenException.class)
				.hasMessageContaining("404");
	}

	@Test
	void eenIngredientWordtToegevoegdEnBewaard() {
		when(formulaRepository.findByIdWithIngredients(1L)).thenReturn(Optional.of(receptuur));
		when(rawMaterialRepository.findById(7L)).thenReturn(Optional.of(banaan));
		when(formulaRepository.save(receptuur)).thenReturn(receptuur);

		Formula resultaat = service.addIngredient(1L, 7L, new BigDecimal("300"), UnitType.GRAM);

		assertThat(resultaat.getIngredientCount()).isEqualTo(1);
		assertThat(resultaat.getIngredients().get(0).getRawMaterialName()).isEqualTo("Banana");
		verify(formulaRepository).save(receptuur);
	}

	@Test
	void eenOnbekendeGrondstofGeeftNietGevonden() {
		when(formulaRepository.findByIdWithIngredients(1L)).thenReturn(Optional.of(receptuur));
		when(rawMaterialRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.addIngredient(1L, 99L, new BigDecimal("300"), UnitType.GRAM))
				.isInstanceOf(NietGevondenException.class);
	}

	@Test
	void deStatusWordtEenStapOpgeschoven() {
		when(formulaRepository.findByIdWithIngredients(1L)).thenReturn(Optional.of(receptuur));
		when(formulaRepository.save(receptuur)).thenReturn(receptuur);

		Formula resultaat = service.changeStatus(1L, LifecycleStatus.TESTED);

		assertThat(resultaat.getStatus()).isEqualTo(LifecycleStatus.TESTED);
	}

	@Test
	void eenNietToegelatenOvergangWordtNietBewaard() {
		when(formulaRepository.findByIdWithIngredients(1L)).thenReturn(Optional.of(receptuur));

		assertThatThrownBy(() -> service.changeStatus(1L, LifecycleStatus.APPROVED))
				.isInstanceOf(IllegalStateException.class);

		verify(formulaRepository, never()).save(any());
	}
}
