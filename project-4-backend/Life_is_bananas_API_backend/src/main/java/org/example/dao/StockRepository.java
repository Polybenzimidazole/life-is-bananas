package org.example.dao;
import org.example.formdata.ProductionData;

import org.example.domain.Stock;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface StockRepository extends CrudRepository<Stock, Integer> {

    Optional<Stock> findByRawMaterial_Id(int rawMaterialId);
}
